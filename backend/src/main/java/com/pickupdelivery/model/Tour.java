package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une tournée de livraison optimisée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    private String courierId;              // ID du coursier assigné à cette tournée
    private String warehouseAddress;
    private List<DeliveryRequest> deliveryRequests = new ArrayList<>();
    private List<String> route = new ArrayList<>();
    private double totalDistance;
    private int totalDuration;
}
