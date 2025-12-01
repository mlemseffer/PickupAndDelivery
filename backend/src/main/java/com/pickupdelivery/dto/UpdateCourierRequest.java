package com.pickupdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour mettre à jour le coursier d'une tournée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourierRequest {
    private String oldCourierId;           // ID du coursier actuel
    private String newCourierId;           // ID du nouveau coursier
    private int deliveryIndex;             // Index de la livraison à reassigner
}
