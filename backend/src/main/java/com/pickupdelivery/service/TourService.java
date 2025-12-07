package com.pickupdelivery.service;

import com.pickupdelivery.dto.AddDeliveryRequest;
import com.pickupdelivery.dto.RemoveDeliveryRequest;
import com.pickupdelivery.dto.TourModificationResponse;
import com.pickupdelivery.dto.UpdateCourierRequest;
import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.Tour;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service pour calculer et optimiser les tournées de livraison
 * Contient la logique métier pour l'optimisation des routes
 */
@Service
public class TourService {

    // Stockage en mémoire des tournées par coursier (en prod: base de données)
    private Map<String, Tour> toursByCourtier = new HashMap<>();
    // Stockage des tournées calculées (modèle AlgorithmModel) pour réassignation multi-coursiers
    private Map<String, com.pickupdelivery.model.AlgorithmModel.Tour> algoToursByCourier = new HashMap<>();

    @Autowired
    private DeliveryService deliveryService;

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
        String demandId = request.getDemandId();

        // Validation minimale
        if (newCourierId == null || newCourierId.isBlank()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("newCourierId est requis");
            return response;
        }
        if (demandId == null || demandId.isBlank()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("demandId est requis pour la réassignation");
            return response;
        }

        // S'assurer que les tournées calculées sont présentes
        if (algoToursByCourier == null || algoToursByCourier.isEmpty()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("Aucune tournée calculée en mémoire pour réaliser la réassignation");
            return response;
        }

        // Trouver la tournée source
        com.pickupdelivery.model.AlgorithmModel.Tour sourceTour = null;
        String resolvedOldCourierId = oldCourierId;

        if (resolvedOldCourierId != null && !resolvedOldCourierId.isBlank()) {
            sourceTour = algoToursByCourier.get(resolvedOldCourierId);
        }

        if (sourceTour == null) {
            // Chercher par demandId dans toutes les tournées
            for (Map.Entry<String, com.pickupdelivery.model.AlgorithmModel.Tour> entry : algoToursByCourier.entrySet()) {
                com.pickupdelivery.model.AlgorithmModel.Tour tour = entry.getValue();
                if (tour != null && tour.getStops() != null) {
                    boolean contains = tour.getStops().stream()
                            .anyMatch(s -> s != null && demandId.equals(s.getIdDemande()));
                    if (contains) {
                        sourceTour = tour;
                        resolvedOldCourierId = entry.getKey();
                        break;
                    }
                }
            }
        }

        if (sourceTour == null) {
            sourceTour = new com.pickupdelivery.model.AlgorithmModel.Tour();
            sourceTour.setStops(new ArrayList<>());
            sourceTour.setTrajets(new ArrayList<>());
        }

        // Identifier les stops pickup/delivery à déplacer (si présents)
        List<Stop> stops = new ArrayList<>(Optional.ofNullable(sourceTour.getStops()).orElse(new ArrayList<>()));
        List<Stop> toMove = new ArrayList<>();
        for (Stop s : stops) {
            if (s != null && demandId.equals(s.getIdDemande())) {
                toMove.add(s);
            }
        }

        if (!toMove.isEmpty()) {
            stops.removeAll(toMove);
            sourceTour.setStops(stops);
            sourceTour.setTrajets(new ArrayList<Trajet>()); // trajectoires invalidées
        }

        // Si non trouvé dans la tournée source, essayer de reconstruire les stops depuis le DeliveryRequestSet
        if (toMove.isEmpty()) {
            var requestSet = deliveryService.getCurrentRequestSet();
            if (requestSet != null && requestSet.getDemands() != null) {
                for (com.pickupdelivery.model.Demand demand : requestSet.getDemands()) {
                    if (demandId.equals(demand.getId())) {
                        // pickup
                        Stop pickup = new Stop(demand.getPickupNodeId(), demand.getId(), Stop.TypeStop.PICKUP);
                        // delivery
                        Stop delivery = new Stop(demand.getDeliveryNodeId(), demand.getId(), Stop.TypeStop.DELIVERY);
                        toMove.add(pickup);
                        toMove.add(delivery);
                        break;
                    }
                }
            }
        }

        if (toMove.isEmpty()) {
            TourModificationResponse response = new TourModificationResponse();
            response.setSuccess(false);
            response.setErrorMessage("Livraison non trouvée (id=" + demandId + ")");
            return response;
        }

        // Récupérer ou créer la tournée cible
        com.pickupdelivery.model.AlgorithmModel.Tour targetTour = algoToursByCourier.get(newCourierId);
        if (targetTour == null) {
            targetTour = new com.pickupdelivery.model.AlgorithmModel.Tour();
            targetTour.setCourierId(Integer.valueOf(newCourierId));
            targetTour.setStops(new ArrayList<>());
            targetTour.setTrajets(new ArrayList<>());
        }

        List<Stop> targetStops = new ArrayList<>(Optional.ofNullable(targetTour.getStops()).orElse(new ArrayList<>()));
        targetStops.addAll(toMove);
        targetTour.setStops(targetStops);
        targetTour.setTrajets(new ArrayList<Trajet>());

        // Mettre à jour les métriques (placeholder minimal)
        sourceTour.setTotalDistance(0);
        sourceTour.setTotalDurationSec(0);
        targetTour.setTotalDistance(0);
        targetTour.setTotalDurationSec(0);

        // Sauvegarder les tournées mises à jour
        algoToursByCourier.put(resolvedOldCourierId, sourceTour);
        algoToursByCourier.put(newCourierId, targetTour);

        TourModificationResponse response = new TourModificationResponse();
        response.setSuccess(true);
        response.setMessage("Livraison réassignée au coursier " + newCourierId + " avec succès");
        // Placeholder: pas de modèle AlgorithmModel dans TourModificationResponse
        response.setUpdatedTour(null);
        response.setRequiresCourierChange(false);

        return response;
    }

    /**
     * Met à disposition les tournées calculées (AlgorithmModel) pour les réassignations
     */
    public void setAlgoTours(java.util.List<com.pickupdelivery.model.AlgorithmModel.Tour> tours) {
        algoToursByCourier.clear();
        if (tours == null) return;
        for (com.pickupdelivery.model.AlgorithmModel.Tour t : tours) {
            if (t != null && t.getCourierId() != null) {
                algoToursByCourier.put(String.valueOf(t.getCourierId()), t);
            }
        }
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
    
    /**
     * Récupère toutes les tournées enregistrées
     * @return Liste de toutes les tournées
     */
    public java.util.List<Tour> getAllTours() {
        return new java.util.ArrayList<>(toursByCourtier.values());
    }
}
