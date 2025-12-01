package com.pickupdelivery.dto;

import com.pickupdelivery.model.Tour;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse lors d'une modification de tournée
 * Contient la nouvelle tournée et des informations de succès/erreur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourModificationResponse {
    private boolean success;               // true si l'opération a réussi
    private String message;                // Message de statut
    private Tour updatedTour;              // La tournée mise à jour (si succès)
    private String errorMessage;           // Message d'erreur (si échoue)
    private boolean requiresCourierChange; // true si l'opérateur doit changer de coursier
}
