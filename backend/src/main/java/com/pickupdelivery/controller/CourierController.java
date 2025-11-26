package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les livreurs
 */
@RestController
@RequestMapping("/api/couriers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CourierController {

    private int courierCount = 1; // Valeur par défaut

    /**
     * Définit le nombre de livreurs
     * @param count Le nombre de livreurs (doit être entre 1 et 10)
     * @return Réponse avec le nombre de livreurs défini
     */
    @PostMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> setCourierCount(@RequestParam int count) {
        // Validation : le nombre doit être entre 1 et 10
        if (count < 1 || count > 10) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse<>(
                            false,
                            "❌ Le nombre de livreurs doit être entre 1 et 10",
                            null
                    ));
        }

        // Sauvegarder le nombre de livreurs
        this.courierCount = count;

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de livreurs défini à " + count,
                count
        ));
    }

    /**
     * Récupère le nombre actuel de livreurs
     * @return Le nombre de livreurs
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCourierCount() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de livreurs actuel",
                this.courierCount
        ));
    }
}
