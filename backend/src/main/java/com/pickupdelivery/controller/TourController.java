package com.pickupdelivery.controller;

import com.pickupdelivery.dto.*;
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
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
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

    /**
     * Ajoute une livraison à la tournée d'un coursier
     * POST /api/tours/add-delivery
     * @param request DTO contenant courierId, addresses et durées
     * @return La tournée modifiée et réoptimisée
     */
    @PostMapping("/add-delivery")
    public ResponseEntity<ApiResponse<TourModificationResponse>> addDeliveryToTour(
            @RequestBody AddDeliveryRequest request) {
        try {
            TourModificationResponse response = tourService.addDeliveryToTour(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(
                    ApiResponse.success(response.getMessage(), response)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.getErrorMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de l'ajout de la livraison: " + e.getMessage()));
        }
    }

    /**
     * Supprime une livraison d'une tournée
     * DELETE /api/tours/remove-delivery
     * @param request DTO contenant courierId et deliveryIndex
     * @return La tournée modifiée et réoptimisée
     */
    @PostMapping("/remove-delivery")
    public ResponseEntity<ApiResponse<TourModificationResponse>> removeDeliveryFromTour(
            @RequestBody RemoveDeliveryRequest request) {
        try {
            TourModificationResponse response = tourService.removeDeliveryFromTour(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(
                    ApiResponse.success(response.getMessage(), response)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.getErrorMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la suppression de la livraison: " + e.getMessage()));
        }
    }

    /**
     * Met à jour le coursier assigné à une livraison
     * PUT /api/tours/update-courier
     * @param request DTO contenant oldCourierId, newCourierId et deliveryIndex
     * @return La tournée modifiée du nouveau coursier
     */
    @PostMapping("/update-courier")
    public ResponseEntity<ApiResponse<TourModificationResponse>> updateCourierAssignment(
            @RequestBody UpdateCourierRequest request) {
        try {
            TourModificationResponse response = tourService.updateCourierAssignment(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(
                    ApiResponse.success(response.getMessage(), response)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response.getErrorMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la mise à jour du coursier: " + e.getMessage()));
        }
    }

    /**
     * Récupère la tournée d'un coursier
     * GET /api/tours/{courierId}
     * @param courierId L'ID du coursier
     * @return La tournée du coursier
     */
    @GetMapping("/{courierId}")
    public ResponseEntity<ApiResponse<Tour>> getTourByCourier(@PathVariable String courierId) {
        try {
            Tour tour = tourService.getTourByCourier(courierId);
            if (tour != null) {
                return ResponseEntity.ok(ApiResponse.success("Tournée récupérée", tour));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Aucune tournée trouvée pour le coursier: " + courierId));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la récupération de la tournée: " + e.getMessage()));
        }
    }

    /**
     * Sauvegarde une tournée pour un coursier
     * POST /api/tours/save
     * @param courierId L'ID du coursier
     * @param tour La tournée à sauvegarder
     * @return Confirmation de sauvegarde
     */
    @PostMapping("/save/{courierId}")
    public ResponseEntity<ApiResponse<Void>> saveTour(
            @PathVariable String courierId,
            @RequestBody Tour tour) {
        try {
            tourService.saveTour(courierId, tour);
            return ResponseEntity.ok(ApiResponse.success("Tournée sauvegardée avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la sauvegarde de la tournée: " + e.getMessage()));
        }
    }
}
