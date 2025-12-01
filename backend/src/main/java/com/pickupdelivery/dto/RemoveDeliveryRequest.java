package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour supprimer une livraison d'une tournée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveDeliveryRequest {
    private String courierId;              // ID du coursier
    private int deliveryIndex;             // Index de la livraison dans la tournée
}
