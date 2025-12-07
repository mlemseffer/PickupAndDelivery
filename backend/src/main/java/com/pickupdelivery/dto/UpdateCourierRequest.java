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
    /**
     * Coursier source. Optionnel : si null/vidé, on le déduira en cherchant la demande.
     */
    private String oldCourierId;

    /**
     * Coursier cible (obligatoire).
     */
    private String newCourierId;

    /**
     * Index de la livraison dans la tournée source (optionnel).
     * Si absent ou -1, on le recalculera à partir de demandId.
     */
    private Integer deliveryIndex;

    /**
     * Identifiant unique de la demande (recommandé).
     */
    private String demandId;
}
