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
public class Segment {
    private String origin;
    private String destination;
    private double length;
    private String name;
}
