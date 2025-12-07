package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Métriques détaillées pour une tournée d'un coursier
 * Utilisé pour les statistiques et le monitoring de la distribution multi-coursiers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourMetrics {
    
    /**
     * ID du coursier (1 à 10)
     */
    private int courierId;
    
    /**
     * Distance totale de la tournée en mètres
     */
    private double totalDistance;
    
    /**
     * Durée totale de la tournée en secondes
     * Inclut : déplacements + temps de service (pickup + delivery)
     */
    private double totalDurationSec;
    
    /**
     * Nombre de demandes (paires pickup/delivery) dans cette tournée
     */
    private int requestCount;
    
    /**
     * Nombre total de stops (warehouse + pickups + deliveries)
     */
    private int stopCount;
    
    /**
     * Indique si cette tournée dépasse la limite de 4 heures
     */
    private boolean exceedsTimeLimit;
    
    /**
     * Retourne la durée en heures
     */
    public double getTotalDurationHours() {
        return totalDurationSec / 3600.0;
    }
    
    /**
     * Retourne la distance en kilomètres
     */
    public double getTotalDistanceKm() {
        return totalDistance / 1000.0;
    }
}
