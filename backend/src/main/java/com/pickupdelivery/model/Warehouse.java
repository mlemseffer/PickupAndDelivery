package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un entrepôt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    private String id;
    private String nodeId;  // ID du nœud où se trouve l'entrepôt
    private String departureTime; // Heure de départ par défaut (ex: "8:0:0")
}
