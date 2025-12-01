package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.dto.MapUploadResponse;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrôleur REST pour gérer les opérations liées aux cartes
 * Expose les endpoints API pour le frontend React
 */
@RestController
@RequestMapping("/api/maps")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class MapController {

    @Autowired
    private MapService mapService;

    /**
     * Upload et parse un fichier XML contenant une carte
     * POST /api/maps/upload
     * @param file Le fichier XML à uploader
     * @return La réponse avec les informations de la carte
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MapUploadResponse>> uploadMap(
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

            CityMap map = mapService.parseMapFromXML(file);
            
            MapUploadResponse response = new MapUploadResponse(
                    map.getNodes().size(),
                    map.getSegments().size(),
                    file.getOriginalFilename()
            );

            return ResponseEntity.ok(ApiResponse.success("Carte chargée avec succès", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors du chargement de la carte: " + e.getMessage()));
        }
    }

    /**
     * Récupère la carte actuellement chargée
     * GET /api/maps/current
     * @return La carte courante
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CityMap>> getCurrentMap() {
        if (!mapService.hasMap()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Aucune carte n'est chargée"));
        }

        return ResponseEntity.ok(ApiResponse.success(mapService.getCurrentMap()));
    }

    /**
     * Vérifie si une carte est chargée
     * GET /api/maps/status
     * @return Le statut de la carte
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getMapStatus() {
        return ResponseEntity.ok(ApiResponse.success(mapService.hasMap()));
    }

    /**
     * Supprime la carte courante
     * DELETE /api/maps/current
     * @return La confirmation de suppression
     */
    @DeleteMapping("/current")
    public ResponseEntity<ApiResponse<Void>> clearMap() {
        mapService.clearMap();
        return ResponseEntity.ok(ApiResponse.success("Carte supprimée avec succès", null));
    }
}
