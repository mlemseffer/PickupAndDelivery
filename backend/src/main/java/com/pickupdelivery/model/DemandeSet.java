package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un ensemble de demandes de livraison avec l'entrepôt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeSet {
    private Warehouse warehouse;
    private List<Demand> demands = new ArrayList<>();
}
