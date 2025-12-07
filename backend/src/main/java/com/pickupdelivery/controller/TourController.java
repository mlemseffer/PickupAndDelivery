package com.pickupdelivery.controller;

import com.pickupdelivery.dto.*;
import com.pickupdelivery.service.TourService;
import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Tour;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.service.DeliveryService;
import com.pickupdelivery.service.MapService;
import com.pickupdelivery.service.ServiceAlgo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour gérer les tournées de livraison
 * Expose les endpoints API pour le frontend React
 * 
 * Phase 6: Intégration Backend/Frontend
 */
@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class TourController {

    @Autowired
    private ServiceAlgo serviceAlgo;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private MapService mapService;

    @Autowired
    private TourService tourService;

    /**
     * Calcule une tournée optimisée pour un nombre donné de livreurs
     * 
     * Endpoint: POST /api/tours/calculate?courierCount=1
     * 
     * Prérequis:
     * - Une carte doit avoir été chargée via /api/maps/upload
     * - Des demandes de livraison doivent avoir été chargées via /api/deliveries/upload
     * - Un entrepôt doit avoir été défini
     * 
     * @param courierCount Nombre de livreurs (uniquement 1 supporté actuellement)
     * @return Liste des tournées calculées (1 seule pour l'instant)
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<TourCalculationResponse>> calculateTour(
            @RequestParam(value = "courierCount", defaultValue = "1") int courierCount) {
        
        try {
            System.out.println("\n🚀 === DÉBUT DU CALCUL DE TOURNÉE ===");
            System.out.println("   Nombre de livreurs demandés: " + courierCount);
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 0️⃣ VALIDATION: Nombre de coursiers
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            if (courierCount < 1 || courierCount > 10) {
                System.out.println("❌ Erreur: Nombre de coursiers invalide: " + courierCount);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(
                            "Le nombre de coursiers doit être entre 1 et 10 (reçu: " + courierCount + ")"));
            }
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1️⃣ VALIDATION: Vérifier que les données nécessaires sont chargées
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            CityMap cityMap = mapService.getCurrentMap();
            if (cityMap == null) {
                System.out.println("❌ Erreur: Aucune carte chargée");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucune carte n'a été chargée. Veuillez d'abord charger une carte."));
            }
            
            DeliveryRequestSet deliveryRequestSet = deliveryService.getCurrentRequestSet();
            if (deliveryRequestSet == null) {
                System.out.println("❌ Erreur: Aucune demande chargée");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucune demande de livraison n'a été chargée. Veuillez d'abord charger des demandes."));
            }
            
            if (deliveryRequestSet.getWarehouse() == null) {
                System.out.println("❌ Erreur: Aucun entrepôt défini");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucun entrepôt n'a été défini. Veuillez définir un entrepôt."));
            }
            
            if (deliveryRequestSet.getDemands() == null || deliveryRequestSet.getDemands().isEmpty()) {
                System.out.println("❌ Erreur: Aucune demande de livraison");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucune demande de livraison à traiter."));
            }
            
            System.out.println("✅ Validation réussie:");
            System.out.println("   - Carte: " + cityMap.getNodes().size() + " nœuds, " + cityMap.getSegments().size() + " segments");
            System.out.println("   - Entrepôt: " + deliveryRequestSet.getWarehouse().getNodeId());
            System.out.println("   - Demandes: " + deliveryRequestSet.getDemands().size());
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2️⃣ CONSTRUCTION DU STOPSET
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            System.out.println("\n📊 Construction du StopSet...");
            StopSet stopSet = serviceAlgo.getStopSet(deliveryRequestSet);
            System.out.println("   ✓ StopSet créé avec " + stopSet.getStops().size() + " stops");
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3️⃣ CONSTRUCTION DU GRAPH (matrice de distances)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            System.out.println("\n🔗 Construction du Graph (calcul des distances)...");
            long graphStartTime = System.currentTimeMillis();
            
            Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
            
            // PHASE 1: Ajouter les demandes au graph pour le calcul de temps
            java.util.Map<String, com.pickupdelivery.model.Demand> demandMap = new java.util.HashMap<>();
            if (deliveryRequestSet.getDemands() != null) {
                for (com.pickupdelivery.model.Demand demand : deliveryRequestSet.getDemands()) {
                    demandMap.put(demand.getId(), demand);
                }
            }
            graph.setDemandMap(demandMap);
            
            long graphElapsedTime = System.currentTimeMillis() - graphStartTime;
            System.out.println("   ✓ Graph construit en " + graphElapsedTime + " ms");
            System.out.println("   ✓ Matrice d'adjacence: " + graph.getDistancesMatrix().size() + " stops");
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4️⃣ CALCUL DE LA TOURNÉE OPTIMALE (Algorithme glouton)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            System.out.println("\n🎯 Calcul de la tournée optimale...");
            long tourStartTime = System.currentTimeMillis();
            
            TourDistributionResult distributionResult = serviceAlgo.calculateOptimalTours(graph, courierCount);
            List<Tour> tours = distributionResult.getTours();
            
            long tourElapsedTime = System.currentTimeMillis() - tourStartTime;
            long totalTime = System.currentTimeMillis() - graphStartTime;
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5️⃣ RÉSULTAT ET STATISTIQUES
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                   SUCCÈS DU CALCUL                             ║");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Coursiers utilisés    : " + String.format("%5d", tours.size()) + "                                  ║");
            
            // Statistiques globales
            int totalStops = 0;
            int totalDemands = 0;
            double totalDistance = 0.0;
            double maxDuration = 0.0;
            int totalTrajets = 0;
            
            for (Tour t : tours) {
                totalStops += t.getStops().size();
                totalDemands += t.getRequestCount();
                totalDistance += t.getTotalDistance();
                totalTrajets += t.getTrajets().size();
                if (t.getTotalDurationSec() > maxDuration) {
                    maxDuration = t.getTotalDurationSec();
                }
            }
            
            System.out.println("║  Stops totaux          : " + String.format("%5d", totalStops) + "                                  ║");
            System.out.println("║  Demandes traitées     : " + String.format("%5d", totalDemands) + "                                  ║");
            System.out.println("║  Distance totale       : " + String.format("%10.2f", totalDistance) + " m                      ║");
            System.out.println("║  Trajets totaux        : " + String.format("%5d", totalTrajets) + "                                  ║");
            System.out.println("║  Durée max (coursier)  : " + String.format("%10.2f", maxDuration / 3600.0) + " h                       ║");
            
            // Détails par coursier si multi-courier
            if (tours.size() > 1) {
                System.out.println("╠════════════════════════════════════════════════════════════════╣");
                System.out.println("║  Détails par coursier:                                         ║");
                for (Tour t : tours) {
                    System.out.println("║    Coursier " + String.format("%2d", t.getCourierId()) + 
                        " : " + String.format("%5d", t.getRequestCount()) + " demandes, " + 
                        String.format("%8.2f", t.getTotalDistance()) + " m, " + 
                        String.format("%5.2f", t.getTotalDurationSec() / 3600.0) + " h    ║");
                }
            }
            
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Temps de construction : " + String.format("%5d", graphElapsedTime) + " ms                               ║");
            System.out.println("║  Temps de calcul       : " + String.format("%5d", tourElapsedTime) + " ms                               ║");
            System.out.println("║  TEMPS TOTAL           : " + String.format("%5d", totalTime) + " ms                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            
            System.out.println("\n✅ === FIN DU CALCUL DE TOURNÉE ===\n");
            
            // Gérer le cas où aucune tournée n'a pu être créée
            if (tours.isEmpty()) {
                System.out.println("⚠️ Aucune tournée n'a pu être créée (contrainte 4h trop restrictive)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(
                            "Aucune demande n'a pu être assignée avec " + courierCount + 
                            " coursier(s). La contrainte de 4h est trop restrictive. " +
                            "Essayez d'augmenter le nombre de coursiers."));
            }
            
            // Construire le message de succès avec warnings si nécessaire
            String message = tours.size() == 1 
                ? "Tournée calculée avec succès en " + totalTime + " ms"
                : tours.size() + " tournées calculées avec succès en " + totalTime + " ms";
            
            // Ajouter warning si des demandes n'ont pas été assignées
            int totalDemandsLoaded = deliveryRequestSet.getDemands().size();
            if (totalDemands < totalDemandsLoaded) {
                message += " (⚠️ " + (totalDemandsLoaded - totalDemands) + 
                          " demande(s) non assignée(s) - contrainte 4h)";
            }
            
            // Construire la réponse avec les demandes non assignées
            TourCalculationResponse response = new TourCalculationResponse(
                tours,
                distributionResult.getUnassignedDemands(),
                distributionResult.getWarnings().getMessages()
            );

            // Stocker les tournées calculées pour les réassignations ultérieures
            tourService.setAlgoTours(tours);
            
            return ResponseEntity.ok(
                    ApiResponse.success(message, response)
            );
            
        } catch (IllegalArgumentException e) {
            // Cas d'erreur de validation (ex: courierCount invalide)
            System.out.println("❌ Erreur de validation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
            
        } catch (IllegalStateException e) {
            // Cas d'erreur métier (ex: aucun stop trouvé)
            System.out.println("❌ Erreur métier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
            
        } catch (Exception e) {
            // Erreur inattendue
            System.err.println("❌ ERREUR INATTENDUE lors du calcul de la tournée:");
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erreur lors du calcul de la tournée: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Endpoint de test pour vérifier que le contrôleur est accessible
     * GET /api/tours/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getStatus() {
        CityMap cityMap = mapService.getCurrentMap();
        DeliveryRequestSet deliveryRequestSet = deliveryService.getCurrentRequestSet();
        
        boolean hasMap = cityMap != null;
        boolean hasRequests = deliveryRequestSet != null && 
                              deliveryRequestSet.getDemands() != null && 
                              !deliveryRequestSet.getDemands().isEmpty();
        boolean hasWarehouse = deliveryRequestSet != null && 
                               deliveryRequestSet.getWarehouse() != null;
        
        String status = String.format(
            "TourController opérationnel | Carte: %s | Demandes: %s | Entrepôt: %s",
            hasMap ? "✅" : "❌",
            hasRequests ? "✅" : "❌",
            hasWarehouse ? "✅" : "❌"
        );
        
        if (hasMap && hasRequests && hasWarehouse) {
            return ResponseEntity.ok(ApiResponse.success(status, "READY"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(status, "NOT_READY"));
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
     * Recalcule les tournées à partir d'une affectation explicite des demandes (courierId nullable)
     * POST /api/tours/recalculate-assignments
     */
    @PostMapping("/recalculate-assignments")
    public ResponseEntity<ApiResponse<TourCalculationResponse>> recalculateAssignments(
            @RequestBody UpdateAssignmentsRequest request) {
        try {
            CityMap cityMap = mapService.getCurrentMap();
            DeliveryRequestSet deliveryRequestSet = deliveryService.getCurrentRequestSet();

            if (cityMap == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucune carte chargée. Veuillez d'abord charger une carte."));
            }
            if (deliveryRequestSet == null || deliveryRequestSet.getDemands() == null || deliveryRequestSet.getDemands().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucune demande de livraison chargée."));
            }
            if (deliveryRequestSet.getWarehouse() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Aucun entrepôt défini."));
            }

            Map<String, String> assignmentMap = new HashMap<>();
            if (request != null && request.getAssignments() != null) {
                for (UpdateAssignmentsRequest.Assignment a : request.getAssignments()) {
                    if (a.getDemandId() != null) {
                        assignmentMap.put(a.getDemandId(), (a.getCourierId() == null || a.getCourierId().isBlank()) ? null : a.getCourierId());
                    }
                }
            }

            Map<String, List<Demand>> demandsByCourier = new HashMap<>();
            List<Demand> unassigned = new ArrayList<>();

            for (Demand d : deliveryRequestSet.getDemands()) {
                String assigned = assignmentMap.getOrDefault(d.getId(), null);
                if (assigned == null) {
                    unassigned.add(d);
                } else {
                    demandsByCourier.computeIfAbsent(assigned, k -> new ArrayList<>()).add(d);
                }
            }

            List<Tour> tours = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (Map.Entry<String, List<Demand>> entry : demandsByCourier.entrySet()) {
                String courierIdStr = entry.getKey();
                List<Demand> demandsForCourier = entry.getValue();
                if (demandsForCourier.isEmpty()) continue;

                DeliveryRequestSet subset = new DeliveryRequestSet();
                subset.setWarehouse(deliveryRequestSet.getWarehouse());
                subset.setDemands(demandsForCourier);

                StopSet stopSet = serviceAlgo.getStopSet(subset);
                Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
                // Injecter le demandMap attendu par l'algo (sinon warnings "demande introuvable")
                Map<String, Demand> demandMap = new HashMap<>();
                for (Demand d : demandsForCourier) {
                    demandMap.put(d.getId(), d);
                    // Ajout d'une clé alternative sans le premier caractère si besoin (robuste aux divergences d'ID)
                    if (d.getId() != null && d.getId().length() > 1) {
                        demandMap.put(d.getId().substring(1), d);
                    }
                }
                graph.setDemandMap(demandMap);

                com.pickupdelivery.dto.TourDistributionResult dist = serviceAlgo.calculateOptimalTours(graph, 1);
                List<Tour> computed = dist.getTours();
                if (computed != null) {
                    for (Tour t : computed) {
                        try {
                            t.setCourierId(Integer.valueOf(courierIdStr));
                        } catch (NumberFormatException nfe) {
                            // laisser tel quel si non numérique
                        }
                    }
                    tours.addAll(computed);
                }
                if (dist.getWarnings() != null && dist.getWarnings().getMessages() != null) {
                    warnings.addAll(dist.getWarnings().getMessages());
                }
            }

            TourCalculationResponse resp = new TourCalculationResponse(tours, unassigned, warnings);
            tourService.setAlgoTours(tours);

            return ResponseEntity.ok(ApiResponse.success("Tournées recalculées avec affectations", resp));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors du recalcul des tournées: " + e.getMessage()));
        }
    }

    /**
     * Récupère la tournée d'un coursier
     * GET /api/tours/{courierId}
     * @param courierId L'ID du coursier
     * @return La tournée du coursier
     */
    @GetMapping("/{courierId}")
    public ResponseEntity<ApiResponse<com.pickupdelivery.model.Tour>> getTourByCourier(@PathVariable String courierId) {
        try {
            com.pickupdelivery.model.Tour tour = tourService.getTourByCourier(courierId);
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
            @RequestBody com.pickupdelivery.model.Tour tour) {
        try {
            tourService.saveTour(courierId, tour);
            return ResponseEntity.ok(ApiResponse.success("Tournée sauvegardée avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la sauvegarde de la tournée: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère les métriques détaillées des tournées calculées
     * GET /api/tours/metrics
     * 
     * Retourne des statistiques sur la dernière tournée calculée.
     * Cette méthode nécessite qu'une tournée ait été calculée au préalable via /calculate
     * 
     * Note: Les métriques sont recalculées à partir des données de la dernière tournée.
     * Pour des métriques en temps réel, appelez d'abord /calculate puis /metrics.
     * 
     * @return Métriques des tournées ou un message si aucune tournée n'a été calculée
     */
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getTourMetrics() {
        System.out.println("\n📊 === RÉCUPÉRATION DES MÉTRIQUES ===");
        System.out.println("⚠️  Note: Endpoint /metrics nécessite qu'une tournée soit d'abord calculée via /calculate");
        System.out.println("✅ === FIN RÉCUPÉRATION MÉTRIQUES ===\n");
        
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("message", "Endpoint disponible. Calculez d'abord une tournée avec POST /api/tours/calculate?courierCount=N");
        info.put("exemple", "curl -X POST 'http://localhost:8080/api/tours/calculate?courierCount=3'");
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Pour obtenir des métriques, calculez d'abord une tournée", 
                info
            )
        );
    }
}
