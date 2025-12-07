package com.pickupdelivery.dto;

import com.pickupdelivery.model.AlgorithmModel.Tour;
import com.pickupdelivery.model.Demand;

import java.util.ArrayList;
import java.util.List;

/**
 * Réponse du calcul de tournée contenant :
 * - Les tournées calculées
 * - Les demandes non assignées (contrainte 4h dépassée)
 */
public class TourCalculationResponse {
    private List<Tour> tours;
    private List<Demand> unassignedDemands;
    private List<String> warnings;

    public TourCalculationResponse() {
        this.tours = new ArrayList<>();
        this.unassignedDemands = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public TourCalculationResponse(List<Tour> tours, List<Demand> unassignedDemands) {
        this.tours = tours != null ? tours : new ArrayList<>();
        this.unassignedDemands = unassignedDemands != null ? unassignedDemands : new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public TourCalculationResponse(List<Tour> tours, List<Demand> unassignedDemands, List<String> warnings) {
        this.tours = tours != null ? tours : new ArrayList<>();
        this.unassignedDemands = unassignedDemands != null ? unassignedDemands : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public List<Tour> getTours() {
        return tours;
    }

    public void setTours(List<Tour> tours) {
        this.tours = tours;
    }

    public List<Demand> getUnassignedDemands() {
        return unassignedDemands;
    }

    public void setUnassignedDemands(List<Demand> unassignedDemands) {
        this.unassignedDemands = unassignedDemands;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public boolean hasUnassignedDemands() {
        return !unassignedDemands.isEmpty();
    }

    public int getTourCount() {
        return tours.size();
    }

    public int getUnassignedCount() {
        return unassignedDemands.size();
    }
}
