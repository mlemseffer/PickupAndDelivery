package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour ajouter une livraison à une tournée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDeliveryRequest {
    private String courierId;              // ID du coursier
    private String pickupAddress;         // Adresse de pickup
    private String deliveryAddress;       // Adresse de livraison
    private int pickupDuration;           // Durée du pickup (secondes)
    private int deliveryDuration;         // Durée de la livraison (secondes)
}
