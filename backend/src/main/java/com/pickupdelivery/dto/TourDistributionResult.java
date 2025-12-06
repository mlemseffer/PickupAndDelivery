package com.pickupdelivery.dto;

import com.pickupdelivery.model.AlgorithmModel.Tour;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Résultat de la distribution FIFO multi-coursiers
 * Contient les tournées générées, les métriques et les warnings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourDistributionResult {
    
    /**
     * Liste des tournées générées (1 à N tournées selon le nombre de coursiers)
     */
    private List<Tour> tours = new ArrayList<>();
    
    /**
     * IDs des demandes qui n'ont pas pu être assignées
     * (tous les coursiers sont à leur maximum de 4h)
     */
    private List<String> unassignedDemandIds = new ArrayList<>();
    
    /**
     * Métriques détaillées par coursier
     * Clé = courierId (1, 2, 3, ...)
     * Valeur = TourMetrics
     */
    private Map<Integer, TourMetrics> metricsByCourier = new HashMap<>();
    
    /**
     * Warnings et alertes générés lors de la distribution
     */
    private DistributionWarnings warnings = new DistributionWarnings();
    
    /**
     * Retourne le nombre total de coursiers utilisés
     */
    public int getCourierCount() {
        return tours != null ? tours.size() : 0;
    }
    
    /**
     * Retourne la distance totale cumulée de toutes les tournées
     */
    public double getTotalDistance() {
        if (tours == null) return 0.0;
        return tours.stream()
            .mapToDouble(Tour::getTotalDistance)
            .sum();
    }
    
    /**
     * Retourne la durée maximale parmi toutes les tournées
     */
    public double getMaxDuration() {
        if (tours == null || tours.isEmpty()) return 0.0;
        return tours.stream()
            .mapToDouble(Tour::getTotalDurationSec)
            .max()
            .orElse(0.0);
    }
    
    /**
     * Retourne le nombre total de demandes assignées
     */
    public int getTotalAssignedRequests() {
        if (tours == null) return 0;
        return tours.stream()
            .mapToInt(Tour::getRequestCount)
            .sum();
    }
}
