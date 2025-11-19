package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un nœud dans le graphe de la carte
 * Cette classe fait partie du modèle métier (Domain Model)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private String id;
    private double latitude;
    private double longitude;
}
