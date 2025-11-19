package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente la carte complète avec tous les nœuds et segments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityMap {
    private List<Node> nodes = new ArrayList<>();
    private List<Segment> segments = new ArrayList<>();
}
