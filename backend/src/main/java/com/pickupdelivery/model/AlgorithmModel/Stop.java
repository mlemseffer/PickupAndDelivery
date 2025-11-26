package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un segment de route entre deux nœuds
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    private String idNode;
    private String idDemande;
    private enum TypeStop {
        PICKUP,
        DELIVERY,
        WAREHOUSE
    } typeStop;
}
