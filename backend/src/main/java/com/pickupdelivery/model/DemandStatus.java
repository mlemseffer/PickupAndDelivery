package com.pickupdelivery.model;

/**
 * Énumération des statuts possibles d'une demande
 * Correspond à "StatutDemande" du diagramme de classe
 */
public enum DemandStatus {
    NON_TRAITEE,    // Non traitée
    AFFECTEE,       // Affectée à un courier
    TRAITEE,        // Traitée/complétée
    REJETEE         // Rejetée
}
