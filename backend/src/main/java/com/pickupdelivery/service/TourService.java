package com.pickupdelivery.service;

import com.pickupdelivery.dto.AddDeliveryRequest;
import com.pickupdelivery.dto.RemoveDeliveryRequest;
import com.pickupdelivery.dto.TourModificationResponse;
import com.pickupdelivery.dto.UpdateCourierRequest;
import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.Tour;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour calculer et optimiser les tournées de livraison
 * Contient la logique métier pour l'optimisation des routes
 */
@Service
public class TourService {

    // Stockage en mémoire des tournées par coursier (en prod: base de données)
    private Map<String, Tour> toursByCourtier = new HashMap<>();

    /**
     * Calcule une tournée optimisée pour les demandes de livraison
     * @param warehouseAddress L'adresse de l'entrepôt
     * @return La tournée optimisée
     */
    public Tour calculateOptimalTour(String warehouseAddress) {
        // TODO: Implémenter l'algorithme d'optimisation
        // Pour l'instant, retourne une tournée vide
        Tour tour = new Tour();
        tour.setWarehouseAddress(warehouseAddress);
        return tour;
    }

    /**
     * Calcule la distance totale d'une tournée
     * @param tour La tournée
     * @return La distance totale en mètres
     */
    public double calculateTotalDistance(Tour tour) {
        // TODO: Implémenter le calcul de distance
        return 0.0;
    }

    /**
     * Calcule la durée totale d'une tournée
     * @param tour La tournée
     * @return La durée totale en secondes
     */
    public int calculateTotalDuration(Tour tour) {
        // TODO: Implémenter le calcul de durée
        return 0;
    }

    /**
     * Ajoute une livraison à la tournée d'un coursier
     * Recalcule la tournée optimale après ajout
     * @param request Contient courierId, pickupAddress, deliveryAddress, durées
     * @return TourModificationResponse avec le statut de l'opération
     */
    public TourModificationResponse addDeliveryToTour(AddDeliveryRequest request) {
        String courierId = request.getCourierId();
        
        // Récupérer ou créer la tournée du coursier
        Tour tour = toursByCourtier.getOrDefault(courierId, new Tour());
        tour.setCourierId(courierId);
        
        // Créer la nouvelle demande de livraison avec un nouvel UUID
        DeliveryRequest newDelivery = new DeliveryRequest(
            java.util.UUID.randomUUID().toString(),
            request.getPickupAddress(),
            request.getDeliveryAddress(),
            request.getPickupDuration(),
            request.getDeliveryDuration()
        );
        
        // Vérifier si l'ajout est faisable (contraintes de temps, distance, etc.)
        boolean isFeasible = validateDeliveryFeasibility(tour, newDelivery);
        
        if (!isFeasible) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setRequiresCourierChange(true);
            response.setErrorMessage(
                "Impossible d'ajouter cette livraison à la tournée du coursier " + courierId + 
                ". Sélectionnez un autre coursier."
            );
            return response;
        }
        
        // Ajouter la livraison
        tour.getDeliveryRequests().add(newDelivery);
        
        // Recalculer la tournée optimale
        Tour optimizedTour = recalculateOptimalTour(tour);
        
        // Sauvegarder la tournée mise à jour
        toursByCourtier.put(courierId, optimizedTour);
        
        TourModificationResponse response = new TourModificationResponse();
        response.setSuccess(true);
        response.setMessage("Livraison ajoutée avec succès et tournée réoptimisée");
        response.setUpdatedTour(optimizedTour);
        response.setRequiresCourierChange(false);
        
        return response;
    }

    /**
     * Supprime une livraison de la tournée d'un coursier
     * Recalcule la tournée optimale après suppression
     * @param request Contient courierId et deliveryIndex
     * @return TourModificationResponse avec le statut de l'opération
     */
    public TourModificationResponse removeDeliveryFromTour(RemoveDeliveryRequest request) {
        String courierId = request.getCourierId();
        int deliveryIndex = request.getDeliveryIndex();
        
        Tour tour = toursByCourtier.get(courierId);
        
        if (tour == null || tour.getDeliveryRequests().isEmpty()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("Aucune tournée trouvée pour le coursier " + courierId);
            return response;
        }
        
        if (deliveryIndex < 0 || deliveryIndex >= tour.getDeliveryRequests().size()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("Index de livraison invalide");
            return response;
        }
        
        // Supprimer la livraison
        tour.getDeliveryRequests().remove(deliveryIndex);
        
        // Recalculer la tournée optimale
        Tour optimizedTour = recalculateOptimalTour(tour);
        
        // Sauvegarder la tournée mise à jour
        toursByCourtier.put(courierId, optimizedTour);
        
        TourModificationResponse response = new TourModificationResponse();
        response.setSuccess(true);
        response.setMessage("Livraison supprimée avec succès et tournée réoptimisée");
        response.setUpdatedTour(optimizedTour);
        response.setRequiresCourierChange(false);
        
        return response;
    }

    /**
     * Met à jour l'assignation d'une livraison à un nouveau coursier
     * @param request Contient oldCourierId, newCourierId, deliveryIndex
     * @return TourModificationResponse avec le statut de l'opération
     */
    public TourModificationResponse updateCourierAssignment(UpdateCourierRequest request) {
        String oldCourierId = request.getOldCourierId();
        String newCourierId = request.getNewCourierId();
        int deliveryIndex = request.getDeliveryIndex();
        
        // Récupérer la tournée du coursier actuel
        Tour oldTour = toursByCourtier.get(oldCourierId);
        if (oldTour == null || deliveryIndex >= oldTour.getDeliveryRequests().size()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("Livraison non trouvée dans la tournée du coursier " + oldCourierId);
            return response;
        }
        
        // Extraire la livraison
        DeliveryRequest delivery = oldTour.getDeliveryRequests().remove(deliveryIndex);
        
        // Ajouter à la tournée du nouveau coursier
        Tour newTour = toursByCourtier.getOrDefault(newCourierId, new Tour());
        newTour.setCourierId(newCourierId);
        
        // Vérifier la faisabilité
        boolean isFeasible = validateDeliveryFeasibility(newTour, delivery);
        
        if (!isFeasible) {
            // Restaurer l'ancienne tournée
            oldTour.getDeliveryRequests().add(deliveryIndex, delivery);
            
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setRequiresCourierChange(true);
            response.setErrorMessage(
                "Impossible d'assigner cette livraison au coursier " + newCourierId + 
                ". Sélectionnez un autre coursier."
            );
            return response;
        }
        
        // Ajouter la livraison au nouveau coursier
        newTour.getDeliveryRequests().add(delivery);
        
        // Réoptimiser les deux tournées
        Tour optimizedOldTour = recalculateOptimalTour(oldTour);
        Tour optimizedNewTour = recalculateOptimalTour(newTour);
        
        // Sauvegarder
        toursByCourtier.put(oldCourierId, optimizedOldTour);
        toursByCourtier.put(newCourierId, optimizedNewTour);
        
        TourModificationResponse response = new TourModificationResponse();
        response.setSuccess(true);
        response.setMessage("Livraison réassignée au coursier " + newCourierId + " avec succès");
        response.setUpdatedTour(optimizedNewTour);
        response.setRequiresCourierChange(false);
        
        return response;
    }

    /**
     * Valide la faisabilité d'ajouter une livraison à une tournée
     * Vérifie les contraintes : capacité, temps, distance, etc.
     * @param tour La tournée
     * @param delivery La livraison à ajouter
     * @return true si l'ajout est faisable, false sinon
     */
    private boolean validateDeliveryFeasibility(Tour tour, DeliveryRequest delivery) {
        // TODO: Implémenter la validation complète
        // Pour maintenant, on accepte toujours (logique métier à affiner)
        
        // Exemple de validations possibles:
        // - Vérifier la durée totale < limite de journée (8h par exemple)
        // - Vérifier la distance totale < limite quotidienne
        // - Vérifier les fenêtres horaires
        // - Vérifier les compatibilités pick-up/delivery
        
        int totalDuration = calculateTotalDuration(tour) + 
                           delivery.getPickupDuration() + 
                           delivery.getDeliveryDuration();
        
        // Limite: 8 heures = 28800 secondes
        return totalDuration <= 28800;
    }

    /**
     * Recalcule la tournée optimale après modification
     * @param tour La tournée modifiée
     * @return La tournée réoptimisée
     */
    private Tour recalculateOptimalTour(Tour tour) {
        // TODO: Implémenter l'algorithme de réoptimisation
        // Pour maintenant, mettre à jour simplement les distances/durées
        tour.setTotalDistance(calculateTotalDistance(tour));
        tour.setTotalDuration(calculateTotalDuration(tour));
        return tour;
    }

    /**
     * Récupère la tournée d'un coursier
     * @param courierId L'ID du coursier
     * @return La tournée, ou null si non trouvée
     */
    public Tour getTourByCourier(String courierId) {
        return toursByCourtier.get(courierId);
    }

    /**
     * Sauvegarde une tournée
     * @param courierId L'ID du coursier
     * @param tour La tournée à sauvegarder
     */
    public void saveTour(String courierId, Tour tour) {
        tour.setCourierId(courierId);
        toursByCourtier.put(courierId, tour);
    }
}
