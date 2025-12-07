package com.pickupdelivery.model.AlgorithmModel;

import com.pickupdelivery.model.Demand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente un graphe complet avec tous les trajets entre les stops
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Graph {
    private Stop stopDepart; // Le warehouse
    private double cout;
    // Map contenant pour chaque stop, la distance vers tous les autres stops
    private Map<Stop, Map<Stop, Trajet>> distancesMatrix;
    
    /**
     * Map des demandes par ID (utilisé pour calcul de temps)
     * Clé: ID de la demande, Valeur: objet Demand
     */
    private Map<String, Demand> demandMap;
}
