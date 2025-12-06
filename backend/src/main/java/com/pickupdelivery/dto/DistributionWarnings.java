package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Warnings et alertes générés lors de la distribution FIFO multi-coursiers
 * Permet d'informer l'utilisateur des situations nécessitant attention
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionWarnings {
    
    /**
     * Indique si certaines demandes n'ont pas pu être assignées
     * (plus de coursiers disponibles et contrainte 4h dépassée)
     */
    private boolean hasUnassignedDemands;
    
    /**
     * Indique si au moins une tournée dépasse la limite de 4 heures
     */
    private boolean hasTimeLimitExceeded;
    
    /**
     * Liste des messages d'avertissement pour l'utilisateur
     */
    private List<String> messages = new ArrayList<>();
    
    /**
     * Ajoute un message d'avertissement
     */
    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }
    
    /**
     * Vérifie s'il y a des avertissements
     */
    public boolean hasWarnings() {
        return hasUnassignedDemands || hasTimeLimitExceeded || 
               (messages != null && !messages.isEmpty());
    }
}
