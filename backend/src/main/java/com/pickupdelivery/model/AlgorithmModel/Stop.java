package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un stop (noeud de pickup, delivery ou warehouse)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    
    public enum TypeStop {
        PICKUP,
        DELIVERY,
        WAREHOUSE
    }
    
    private String idNode;
    //optionnel car pour les entrepots on n'a pas d'idDemande
    private java.util.Optional<String> idDemande = java.util.Optional.empty();
    private TypeStop typeStop;
}
