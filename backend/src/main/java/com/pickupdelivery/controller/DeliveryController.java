package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrôleur REST pour gérer les demandes de livraison
 * Expose les endpoints API pour le frontend React
 */
@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Upload et parse un fichier XML contenant des demandes de livraison
     * POST /api/deliveries/upload
     * @param file Le fichier XML à uploader
     * @return La liste des demandes de livraison
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<DeliveryRequest>>> uploadDeliveryRequests(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier est vide"));
            }

            if (!file.getOriginalFilename().endsWith(".xml")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier doit être au format XML"));
            }

            List<DeliveryRequest> requests = deliveryService.parseDeliveryRequestsFromXML(file);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Demandes de livraison chargées avec succès", requests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du chargement des demandes: " + e.getMessage()));
        }
    }

    /**
     * Récupère toutes les demandes de livraison
     * GET /api/deliveries
     * @return La liste des demandes de livraison
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryRequest>>> getAllDeliveryRequests() {
        List<DeliveryRequest> requests = deliveryService.getCurrentRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Ajoute une nouvelle demande de livraison
     * POST /api/deliveries
     * @param request La demande de livraison à ajouter
     * @return La confirmation d'ajout
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryRequest>> addDeliveryRequest(
            @RequestBody DeliveryRequest request) {
        deliveryService.addDeliveryRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Demande de livraison ajoutée avec succès", request));
    }

    /**
     * Supprime toutes les demandes de livraison
     * DELETE /api/deliveries
     * @return La confirmation de suppression
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearDeliveryRequests() {
        deliveryService.clearRequests();
        return ResponseEntity.ok(ApiResponse.success("Demandes de livraison supprimées avec succès", null));
    }

    /**
     * Charge un ensemble de demandes de livraison depuis un fichier XML
     * POST /api/deliveries/load
     * @param file Le fichier XML contenant les demandes
     * @return L'ensemble des demandes avec l'entrepôt et les couleurs
     */
    @PostMapping("/load")
    public ResponseEntity<ApiResponse<DeliveryRequestSet>> loadDeliveryRequests(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier est vide"));
            }

            if (!file.getOriginalFilename().endsWith(".xml")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier doit être au format XML"));
            }

            DeliveryRequestSet requestSet = deliveryService.loadDeliveryRequests(file);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Demandes de livraison chargées avec succès", requestSet));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du chargement des demandes: " + e.getMessage()));
        }
    }

    /**
     * Récupère l'ensemble des demandes actuelles
     * GET /api/deliveries/current
     * @return L'ensemble des demandes avec l'entrepôt
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<DeliveryRequestSet>> getCurrentRequestSet() {
        DeliveryRequestSet requestSet = deliveryService.getCurrentRequestSet();
        if (requestSet == null) {
            return ResponseEntity.ok(ApiResponse.success("Aucune demande chargée", null));
        }
        return ResponseEntity.ok(ApiResponse.success(requestSet));
    }
}
