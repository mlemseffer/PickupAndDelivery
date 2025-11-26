package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un ensemble de stops (tous les noeuds qui sont soit pickup, delivery ou warehouse)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopSet {
    private List<Stop> stops = new ArrayList<>();
}
