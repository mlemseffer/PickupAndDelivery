package com.pickupdelivery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un trajet entre deux stops avec la liste des segments à parcourir
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trajet {
    private List<Segment> segments = new ArrayList<>();
    private Stop stopDepart;
    private Stop stopArrivee;
    private double duree;
}
