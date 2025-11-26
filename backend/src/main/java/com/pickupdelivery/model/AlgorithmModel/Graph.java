package com.pickupdelivery.model.AlgorithmModel;

import com.pickupdelivery.model.Stop;
import com.pickupdelivery.model.Trajet;
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
    private Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
}
