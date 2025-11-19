package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une demande de livraison
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private String pickupAddress;
    private String deliveryAddress;
    private int pickupDuration;
    private int deliveryDuration;
}
