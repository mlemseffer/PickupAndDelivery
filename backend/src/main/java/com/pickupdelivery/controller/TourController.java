package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.model.Tour;
import com.pickupdelivery.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer les tournées de livraison
 * Expose les endpoints API pour le frontend React
 */
@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TourController {

    @Autowired
    private TourService tourService;

    /**
     * Calcule une tournée optimisée
     * POST /api/tours/calculate
     * @param warehouseAddress L'adresse de l'entrepôt
     * @return La tournée optimisée
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<Tour>> calculateTour(
            @RequestParam("warehouseAddress") String warehouseAddress) {
        try {
            Tour tour = tourService.calculateOptimalTour(warehouseAddress);
            return ResponseEntity.ok(ApiResponse.success("Tournée calculée avec succès", tour));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors du calcul de la tournée: " + e.getMessage()));
        }
    }
}
