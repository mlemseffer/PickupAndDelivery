package com.pickupdelivery.controller;

import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.service.DeliveryService;
import com.pickupdelivery.service.MapService;
import com.pickupdelivery.service.ServiceAlgo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de test pour vérifier la construction du Graph
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class GraphTestController {

    @Autowired
    private ServiceAlgo serviceAlgo;

    @Autowired
    private MapService mapService;

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Endpoint pour tester la création du Graph
     * GET /api/test/graph
     */
    @GetMapping("/graph")
    public ResponseEntity<?> testGraph() {
        try {
            // Vérifier qu'une carte est chargée
            CityMap cityMap = mapService.getCurrentMap();
            if (cityMap == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Aucune carte chargée. Veuillez d'abord charger une carte XML."
                ));
            }

            // Vérifier qu'un ensemble de demandes est chargé
            DeliveryRequestSet deliveryRequestSet = deliveryService.getCurrentRequestSet();
            if (deliveryRequestSet == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Aucune demande chargée. Veuillez d'abord charger un fichier de demandes XML."
                ));
            }

            // Créer le StopSet
            StopSet stopSet = serviceAlgo.getStopSet(deliveryRequestSet);

            // Mesurer le temps de construction du Graph
            long startTime = System.currentTimeMillis();
            Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
            long endTime = System.currentTimeMillis();
            long constructionTime = endTime - startTime;

            // Analyser le graphe
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("constructionTimeMs", constructionTime);

            // Informations sur la carte
            Map<String, Object> mapInfo = new HashMap<>();
            mapInfo.put("nodesCount", cityMap.getNodes().size());
            mapInfo.put("segmentsCount", cityMap.getSegments().size());
            result.put("cityMap", mapInfo);

            // Informations sur les stops
            Map<String, Object> stopInfo = new HashMap<>();
            stopInfo.put("totalStops", stopSet.getStops().size());
            stopInfo.put("warehouseCount", stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE).count());
            stopInfo.put("pickupCount", stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP).count());
            stopInfo.put("deliveryCount", stopSet.getStops().stream()
                    .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY).count());
            result.put("stops", stopInfo);

            // Informations sur le graph
            Map<String, Object> graphInfo = new HashMap<>();
            graphInfo.put("warehouseNodeId", graph.getStopDepart().getIdNode());
            graphInfo.put("matrixSize", graph.getDistancesMatrix().size());
            
            // Statistiques des distances
            double minDistance = Double.MAX_VALUE;
            double maxDistance = 0;
            double totalDistance = 0;
            int trajetCount = 0;

            for (Stop source : graph.getDistancesMatrix().keySet()) {
                Map<Stop, Trajet> destinations = graph.getDistancesMatrix().get(source);
                for (Trajet trajet : destinations.values()) {
                    double dist = trajet.getDistance();
                    if (dist < minDistance) minDistance = dist;
                    if (dist > maxDistance) maxDistance = dist;
                    totalDistance += dist;
                    trajetCount++;
                }
            }

            graphInfo.put("minDistance", minDistance);
            graphInfo.put("maxDistance", maxDistance);
            graphInfo.put("averageDistance", totalDistance / trajetCount);
            graphInfo.put("totalTrajets", trajetCount);
            result.put("graph", graphInfo);

            // Liste détaillée des stops avec leurs trajets
            Map<String, Map<String, Object>> stopsDetails = new HashMap<>();
            for (Stop source : graph.getDistancesMatrix().keySet()) {
                Map<String, Object> stopDetail = new HashMap<>();
                stopDetail.put("nodeId", source.getIdNode());
                stopDetail.put("type", source.getTypeStop().toString());
                stopDetail.put("demandId", source.getIdDemande());
                stopDetail.put("trajetsCount", graph.getDistancesMatrix().get(source).size());
                
                // Liste des distances vers les autres stops
                Map<String, Double> distances = new HashMap<>();
                for (Stop dest : graph.getDistancesMatrix().get(source).keySet()) {
                    Trajet trajet = graph.getDistancesMatrix().get(source).get(dest);
                    String destKey = dest.getTypeStop() + "@" + dest.getIdNode();
                    distances.put(destKey, trajet.getDistance());
                }
                stopDetail.put("distances", distances);
                
                String sourceKey = source.getTypeStop() + "@" + source.getIdNode();
                stopsDetails.put(sourceKey, stopDetail);
            }
            result.put("stopsDetails", stopsDetails);

            // Vérifications
            boolean isValid = true;
            int expectedStops = stopSet.getStops().size();
            for (Stop source : graph.getDistancesMatrix().keySet()) {
                Map<Stop, Trajet> destinations = graph.getDistancesMatrix().get(source);
                if (destinations.size() != expectedStops - 1) {
                    isValid = false;
                    break;
                }
            }
            result.put("isValid", isValid);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la construction du graph",
                    "message", e.getMessage(),
                    "stackTrace", e.getStackTrace()
            ));
        }
    }

    /**
     * Endpoint pour avoir un résumé rapide
     * GET /api/test/graph/summary
     */
    @GetMapping("/graph/summary")
    public ResponseEntity<?> testGraphSummary() {
        try {
            CityMap cityMap = mapService.getCurrentMap();
            DeliveryRequestSet deliveryRequestSet = deliveryService.getCurrentRequestSet();

            if (cityMap == null || deliveryRequestSet == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Veuillez charger une carte et des demandes d'abord"
                ));
            }

            StopSet stopSet = serviceAlgo.getStopSet(deliveryRequestSet);
            Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);

            return ResponseEntity.ok(Map.of(
                    "nodesCount", cityMap.getNodes().size(),
                    "stopsCount", stopSet.getStops().size(),
                    "matrixSize", graph.getDistancesMatrix().size(),
                    "warehouse", graph.getStopDepart().getIdNode()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
