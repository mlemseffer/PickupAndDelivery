package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une demande de livraison (pickup + delivery)
 * Correspond à l'entité "Demande" du diagramme de classe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Demand {
    private String id;
    private String pickupNodeId;      // idPickup : FK→Noeud
    private String deliveryNodeId;    // idDelivery : FK→Noeud
    private int pickupDurationSec;    // dureePickupSec : int
    private int deliveryDurationSec;  // dureeDeliverySec : int
    private String courierId;         // idCourier : FK→Courier (nullable)
    // Note: Les couleurs sont gérées uniquement dans le frontend
}
