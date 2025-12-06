package com.pickupdelivery.model.AlgorithmModel;

import com.pickupdelivery.model.Segment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un trajet entre deux stops avec la liste des segments à parcourir
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trajet {
    private List<Segment> segments;
    private Stop stopDepart;
    private Stop stopArrivee;
    private double distance; // Distance totale en mètres
    
    /**
     * Durée du trajet en secondes (temps de déplacement uniquement, sans temps de service)
     * Calculé avec la formule : distance / vitesse_coursier
     */
    private double durationSec;
    
    /**
     * Retourne la durée en heures
     */
    public double getDurationHours() {
        return durationSec / 3600.0;
    }
    
    /**
     * Retourne la durée en minutes
     */
    public double getDurationMinutes() {
        return durationSec / 60.0;
    }
}
