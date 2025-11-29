package com.pickupdelivery.factory;

import com.pickupdelivery.model.Demand;

/**
 * Factory pour la création d'instances de Demand avec validation
 * 
 * Centralise la logique de création et de validation des demandes de livraison,
 * garantissant que seules des instances valides sont créées.
 */
public class DemandFactory {
    
    /**
     * Crée une nouvelle demande de livraison après validation des paramètres
     * 
     * @param id L'identifiant unique de la demande
     * @param pickupNodeId L'identifiant du nœud de pickup
     * @param deliveryNodeId L'identifiant du nœud de delivery
     * @param pickupDurationSec La durée du pickup en secondes (doit être positive)
     * @param deliveryDurationSec La durée de la delivery en secondes (doit être positive)
     * @param courierId L'identifiant du coursier (peut être null)
     * @return Une nouvelle instance de Demand valide
     * @throws IllegalArgumentException Si un paramètre est invalide
     */
    public static Demand createDemand(String id, String pickupNodeId, String deliveryNodeId,
                                     int pickupDurationSec, int deliveryDurationSec,
                                     String courierId) {
        validateId(id);
        validatePickupNodeId(pickupNodeId);
        validateDeliveryNodeId(deliveryNodeId);
        validateDifferentNodes(pickupNodeId, deliveryNodeId);
        validatePickupDuration(pickupDurationSec);
        validateDeliveryDuration(deliveryDurationSec);
        
        return new Demand(id, pickupNodeId, deliveryNodeId, 
                         pickupDurationSec, deliveryDurationSec, 
                         courierId);
    }
    
    /**
     * Valide l'identifiant de la demande
     * 
     * @param id L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID est null ou vide
     */
    private static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant de la demande ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide l'identifiant du nœud de pickup
     * 
     * @param pickupNodeId L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID est null ou vide
     */
    private static void validatePickupNodeId(String pickupNodeId) {
        if (pickupNodeId == null || pickupNodeId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud de pickup ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide l'identifiant du nœud de delivery
     * 
     * @param deliveryNodeId L'identifiant à valider
     * @throws IllegalArgumentException Si l'ID est null ou vide
     */
    private static void validateDeliveryNodeId(String deliveryNodeId) {
        if (deliveryNodeId == null || deliveryNodeId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "L'identifiant du nœud de delivery ne peut pas être null ou vide"
            );
        }
    }
    
    /**
     * Valide que les nœuds de pickup et delivery sont différents
     * 
     * @param pickupNodeId Le nœud de pickup
     * @param deliveryNodeId Le nœud de delivery
     * @throws IllegalArgumentException Si les deux nœuds sont identiques
     */
    private static void validateDifferentNodes(String pickupNodeId, String deliveryNodeId) {
        if (pickupNodeId.equals(deliveryNodeId)) {
            throw new IllegalArgumentException(
                String.format("Les nœuds de pickup et delivery doivent être différents (%s)", pickupNodeId)
            );
        }
    }
    
    /**
     * Valide la durée du pickup
     * 
     * @param pickupDurationSec La durée à valider
     * @throws IllegalArgumentException Si la durée est négative
     */
    private static void validatePickupDuration(int pickupDurationSec) {
        if (pickupDurationSec < 0) {
            throw new IllegalArgumentException(
                String.format("La durée de pickup ne peut pas être négative (reçu : %d secondes)", pickupDurationSec)
            );
        }
    }
    
    /**
     * Valide la durée de la delivery
     * 
     * @param deliveryDurationSec La durée à valider
     * @throws IllegalArgumentException Si la durée est négative
     */
    private static void validateDeliveryDuration(int deliveryDurationSec) {
        if (deliveryDurationSec < 0) {
            throw new IllegalArgumentException(
                String.format("La durée de delivery ne peut pas être négative (reçu : %d secondes)", deliveryDurationSec)
            );
        }
    }
}
