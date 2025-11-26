package com.pickupdelivery.dto;

import com.pickupdelivery.model.Segment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO représentant le résultat d'un calcul de plus court chemin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortestPathResult {
    private double distance;
    private List<Segment> segments;
}
