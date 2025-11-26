package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Tour;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.service.DeliveryService;
import com.pickupdelivery.service.MapService;
import com.pickupdelivery.service.ServiceAlgo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les tournées de livraison
 * Expose les endpoints API pour le frontend React
 * 
 * Phase 6: Intégration Backend/Frontend
 */
@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TourController {

    @Autowired
    private ServiceAlgo serviceAlgo;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private MapService mapService;

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
    public ResponseEntity<ApiResponse<List<Tour>>> calculateTour(
            @RequestParam(value = "courierCount", defaultValue = "1") int courierCount) {
        
        try {
            System.out.println("\n🚀 === DÉBUT DU CALCUL DE TOURNÉE ===");
            System.out.println("   Nombre de livreurs demandés: " + courierCount);
            
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
            
            long graphElapsedTime = System.currentTimeMillis() - graphStartTime;
            System.out.println("   ✓ Graph construit en " + graphElapsedTime + " ms");
            System.out.println("   ✓ Matrice d'adjacence: " + graph.getDistancesMatrix().size() + " stops");
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4️⃣ CALCUL DE LA TOURNÉE OPTIMALE (Algorithme glouton)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            System.out.println("\n🎯 Calcul de la tournée optimale...");
            long tourStartTime = System.currentTimeMillis();
            
            List<Tour> tours = serviceAlgo.calculateOptimalTours(graph, courierCount);
            
            long tourElapsedTime = System.currentTimeMillis() - tourStartTime;
            long totalTime = System.currentTimeMillis() - graphStartTime;
            
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5️⃣ RÉSULTAT ET STATISTIQUES
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Tour tour = tours.get(0);
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                   SUCCÈS DU CALCUL                             ║");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Tours calculés        : " + String.format("%5d", tours.size()) + "                                  ║");
            System.out.println("║  Stops dans le tour    : " + String.format("%5d", tour.getStops().size()) + "                                  ║");
            System.out.println("║  Demandes traitées     : " + String.format("%5d", tour.getRequestCount()) + "                                  ║");
            System.out.println("║  Distance totale       : " + String.format("%10.2f", tour.getTotalDistance()) + " m                      ║");
            System.out.println("║  Trajets               : " + String.format("%5d", tour.getTrajets().size()) + "                                  ║");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.println("║  Temps de construction : " + String.format("%5d", graphElapsedTime) + " ms                               ║");
            System.out.println("║  Temps de calcul       : " + String.format("%5d", tourElapsedTime) + " ms                               ║");
            System.out.println("║  TEMPS TOTAL           : " + String.format("%5d", totalTime) + " ms                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            
            System.out.println("\n✅ === FIN DU CALCUL DE TOURNÉE ===\n");
            
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Tournée calculée avec succès en " + totalTime + " ms", 
                            tours
                    )
            );
            
        } catch (UnsupportedOperationException e) {
            // Cas spécifique: multi-livreurs pas encore supporté
            System.out.println("⚠️  Exception: " + e.getMessage());
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
}
