package com.pickupdelivery.model.AlgorithmModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Représente une tournée de livraison pour un livreur
 * Contient l'ordre de visite des stops et les trajets détaillés entre chaque stop
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    
    /**
     * Liste ordonnée des stops à visiter (commence et finit au warehouse)
     */
    private List<Stop> stops;
    
    /**
     * Liste des trajets détaillés entre chaque paire de stops consécutifs
     * trajets[i] représente le trajet entre stops[i] et stops[i+1]
     */
    private List<Trajet> trajets;
    
    /**
     * Distance totale de la tournée en mètres
     */
    private double totalDistance;
    
    /**
     * ID du livreur assigné à cette tournée (optionnel pour l'instant)
     */
    private Integer courierId;
    
    /**
     * Retourne le nombre de stops dans la tournée (incluant le warehouse de départ et d'arrivée)
     */
    public int getStopCount() {
        return stops != null ? stops.size() : 0;
    }
    
    /**
     * Retourne le nombre de demandes de livraison dans la tournée
     * (nombre de deliveries = nombre de pickups)
     */
    public int getRequestCount() {
        if (stops == null) {
            return 0;
        }
        
        return (int) stops.stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY)
                .count();
    }
    
    /**
     * Vérifie si la tournée est valide (commence et finit au warehouse)
     */
    public boolean isValid() {
        if (stops == null || stops.isEmpty()) {
            return false;
        }
        
        Stop first = stops.get(0);
        Stop last = stops.get(stops.size() - 1);
        
        return first.getTypeStop() == Stop.TypeStop.WAREHOUSE &&
               last.getTypeStop() == Stop.TypeStop.WAREHOUSE;
    }
}
