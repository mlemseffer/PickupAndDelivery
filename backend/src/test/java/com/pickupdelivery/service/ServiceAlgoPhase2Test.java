package com.pickupdelivery.service;

import com.pickupdelivery.model.*;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la Phase 2: Fonctions Utilitaires
 * Teste les méthodes de calcul de distance, validation de contraintes et 2-opt swap
 */
@SpringBootTest
class ServiceAlgoPhase2Test {

    @Autowired
    private ServiceAlgo serviceAlgo;

    private Graph testGraph;
    private Stop warehouse;
    private Stop pickupD1;
    private Stop deliveryD1;
    private Stop pickupD2;
    private Stop deliveryD2;

    @BeforeEach
    void setUp() {
        // Créer les stops
        warehouse = new Stop("N5", null, Stop.TypeStop.WAREHOUSE);
        pickupD1 = new Stop("N1", "D1", Stop.TypeStop.PICKUP);
        deliveryD1 = new Stop("N9", "D1", Stop.TypeStop.DELIVERY);
        pickupD2 = new Stop("N3", "D2", Stop.TypeStop.PICKUP);
        deliveryD2 = new Stop("N7", "D2", Stop.TypeStop.DELIVERY);

        // Créer le graph avec des distances spécifiques
        testGraph = createTestGraphWithDistances();
    }

    /**
     * Créer un Graph avec des distances connues pour faciliter les tests
     */
    private Graph createTestGraphWithDistances() {
        List<Stop> allStops = Arrays.asList(warehouse, pickupD1, deliveryD1, pickupD2, deliveryD2);

        // Matrice de distances (symétrique pour simplifier)
        Map<Stop, Map<Stop, Double>> distances = new HashMap<>();
        distances.put(warehouse, Map.of(
                pickupD1, 100.0,
                deliveryD1, 300.0,
                pickupD2, 80.0,
                deliveryD2, 250.0
        ));
        distances.put(pickupD1, Map.of(
                warehouse, 100.0,
                deliveryD1, 200.0,
                pickupD2, 150.0,
                deliveryD2, 280.0
        ));
        distances.put(deliveryD1, Map.of(
                warehouse, 300.0,
                pickupD1, 200.0,
                pickupD2, 220.0,
                deliveryD2, 120.0
        ));
        distances.put(pickupD2, Map.of(
                warehouse, 80.0,
                pickupD1, 150.0,
                deliveryD1, 220.0,
                deliveryD2, 140.0
        ));
        distances.put(deliveryD2, Map.of(
                warehouse, 250.0,
                pickupD1, 280.0,
                deliveryD1, 120.0,
                pickupD2, 140.0
        ));

        // Créer la matrice avec des Trajets
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
        for (Stop source : allStops) {
            Map<Stop, Trajet> destinations = new HashMap<>();
            for (Stop dest : allStops) {
                if (!source.equals(dest)) {
                    Trajet trajet = new Trajet();
                    trajet.setStopDepart(source);
                    trajet.setStopArrivee(dest);
                    trajet.setDistance(distances.get(source).get(dest));
                    trajet.setSegments(new ArrayList<>());
                    destinations.put(dest, trajet);
                }
            }
            distancesMatrix.put(source, destinations);
        }

        Graph graph = new Graph();
        graph.setStopDepart(warehouse);
        graph.setCout(0.0);
        graph.setDistancesMatrix(distancesMatrix);

        return graph;
    }

    // =========================================================================
    // TESTS POUR distance()
    // =========================================================================

    @Test
    void testDistance_Success() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("distance", Stop.class, Stop.class, Graph.class);
        method.setAccessible(true);

        // Test distance warehouse → pickupD1
        Double dist1 = (Double) method.invoke(serviceAlgo, warehouse, pickupD1, testGraph);
        assertEquals(100.0, dist1, 0.001, "Distance W→P1 doit être 100.0");

        // Test distance pickupD1 → deliveryD1
        Double dist2 = (Double) method.invoke(serviceAlgo, pickupD1, deliveryD1, testGraph);
        assertEquals(200.0, dist2, 0.001, "Distance P1→D1 doit être 200.0");

        // Test distance pickupD2 → deliveryD2
        Double dist3 = (Double) method.invoke(serviceAlgo, pickupD2, deliveryD2, testGraph);
        assertEquals(140.0, dist3, 0.001, "Distance P2→D2 doit être 140.0");
    }

    @Test
    void testDistance_ThrowsException_WhenStopsNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("distance", Stop.class, Stop.class, Graph.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, null, pickupD1, testGraph);
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testDistance_ThrowsException_WhenGraphNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("distance", Stop.class, Stop.class, Graph.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, warehouse, pickupD1, null);
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    // =========================================================================
    // TESTS POUR computeRouteDistance()
    // =========================================================================

    @Test
    void testComputeRouteDistance_SimpleRoute() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        method.setAccessible(true);

        // Route: W → P1 → D1 → W
        List<Stop> route = Arrays.asList(warehouse, pickupD1, deliveryD1, warehouse);

        Double totalDistance = (Double) method.invoke(serviceAlgo, route, testGraph);

        // Distance attendue: 100.0 (W→P1) + 200.0 (P1→D1) + 300.0 (D1→W) = 600.0
        assertEquals(600.0, totalDistance, 0.001, "Distance totale doit être 600.0");
    }

    @Test
    void testComputeRouteDistance_ComplexRoute() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        method.setAccessible(true);

        // Route: W → P2 → D2 → P1 → D1 → W
        List<Stop> route = Arrays.asList(warehouse, pickupD2, deliveryD2, pickupD1, deliveryD1, warehouse);

        Double totalDistance = (Double) method.invoke(serviceAlgo, route, testGraph);

        // Distance attendue:
        // W→P2: 80.0
        // P2→D2: 140.0
        // D2→P1: 280.0
        // P1→D1: 200.0
        // D1→W: 300.0
        // Total: 1000.0
        assertEquals(1000.0, totalDistance, 0.001, "Distance totale doit être 1000.0");
    }

    @Test
    void testComputeRouteDistance_EmptyRoute() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        method.setAccessible(true);

        List<Stop> emptyRoute = new ArrayList<>();
        Double distance = (Double) method.invoke(serviceAlgo, emptyRoute, testGraph);

        assertEquals(0.0, distance, 0.001, "Route vide doit avoir distance 0");
    }

    @Test
    void testComputeRouteDistance_SingleStop() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        method.setAccessible(true);

        List<Stop> singleStop = Arrays.asList(warehouse);
        Double distance = (Double) method.invoke(serviceAlgo, singleStop, testGraph);

        assertEquals(0.0, distance, 0.001, "Route avec 1 stop doit avoir distance 0");
    }

    // =========================================================================
    // TESTS POUR isStopFeasible()
    // =========================================================================

    @Test
    void testIsStopFeasible_Warehouse_AlwaysFeasible() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "isStopFeasible", Stop.class, Set.class, Map.class);
        method.setAccessible(true);

        Set<Stop> visited = new HashSet<>();
        Map<String, List<Stop>> pickupsById = new HashMap<>();

        Boolean feasible = (Boolean) method.invoke(serviceAlgo, warehouse, visited, pickupsById);

        assertTrue(feasible, "Warehouse doit toujours être faisable");
    }

    @Test
    void testIsStopFeasible_Pickup_AlwaysFeasible() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "isStopFeasible", Stop.class, Set.class, Map.class);
        method.setAccessible(true);

        Set<Stop> visited = new HashSet<>();
        Map<String, List<Stop>> pickupsById = new HashMap<>();

        Boolean feasible = (Boolean) method.invoke(serviceAlgo, pickupD1, visited, pickupsById);

        assertTrue(feasible, "Pickup doit toujours être faisable");
    }

    @Test
    void testIsStopFeasible_Delivery_FeasibleWhenPickupVisited() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "isStopFeasible", Stop.class, Set.class, Map.class);
        method.setAccessible(true);

        Set<Stop> visited = new HashSet<>(Arrays.asList(warehouse, pickupD1));
        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));

        Boolean feasible = (Boolean) method.invoke(serviceAlgo, deliveryD1, visited, pickupsById);

        assertTrue(feasible, "Delivery D1 doit être faisable car P1 est visité");
    }

    @Test
    void testIsStopFeasible_Delivery_NotFeasibleWhenPickupNotVisited() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "isStopFeasible", Stop.class, Set.class, Map.class);
        method.setAccessible(true);

        Set<Stop> visited = new HashSet<>(Arrays.asList(warehouse)); // P1 pas visité
        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));

        Boolean feasible = (Boolean) method.invoke(serviceAlgo, deliveryD1, visited, pickupsById);

        assertFalse(feasible, "Delivery D1 ne doit PAS être faisable car P1 n'est pas visité");
    }

    // =========================================================================
    // TESTS POUR respectsPrecedence()
    // =========================================================================

    @Test
    void testRespectsPrecedence_ValidRoute() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        method.setAccessible(true);

        // Route valide: W → P1 → D1 → W
        List<Stop> validRoute = Arrays.asList(warehouse, pickupD1, deliveryD1, warehouse);

        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));
        Map<String, Stop> deliveriesById = Map.of("D1", deliveryD1);

        Boolean valid = (Boolean) method.invoke(serviceAlgo, validRoute, pickupsById, deliveriesById);

        assertTrue(valid, "Route avec P1 avant D1 doit être valide");
    }

    @Test
    void testRespectsPrecedence_InvalidRoute_DeliveryBeforePickup() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        method.setAccessible(true);

        // Route invalide: W → D1 → P1 → W (D1 avant P1)
        List<Stop> invalidRoute = Arrays.asList(warehouse, deliveryD1, pickupD1, warehouse);

        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));
        Map<String, Stop> deliveriesById = Map.of("D1", deliveryD1);

        Boolean valid = (Boolean) method.invoke(serviceAlgo, invalidRoute, pickupsById, deliveriesById);

        assertFalse(valid, "Route avec D1 avant P1 doit être invalide");
    }

    @Test
    void testRespectsPrecedence_ValidRoute_MultipleRequests() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        method.setAccessible(true);

        // Route valide: W → P1 → P2 → D1 → D2 → W
        List<Stop> validRoute = Arrays.asList(
                warehouse, pickupD1, pickupD2, deliveryD1, deliveryD2, warehouse);

        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );
        Map<String, Stop> deliveriesById = Map.of(
                "D1", deliveryD1,
                "D2", deliveryD2
        );

        Boolean valid = (Boolean) method.invoke(serviceAlgo, validRoute, pickupsById, deliveriesById);

        assertTrue(valid, "Route avec tous les pickups avant leurs deliveries doit être valide");
    }

    @Test
    void testRespectsPrecedence_InvalidRoute_OneDeliveryBeforePickup() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        method.setAccessible(true);

        // Route invalide: W → P1 → D2 → P2 → D1 → W (D2 avant P2)
        List<Stop> invalidRoute = Arrays.asList(
                warehouse, pickupD1, deliveryD2, pickupD2, deliveryD1, warehouse);

        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );
        Map<String, Stop> deliveriesById = Map.of(
                "D1", deliveryD1,
                "D2", deliveryD2
        );

        Boolean valid = (Boolean) method.invoke(serviceAlgo, invalidRoute, pickupsById, deliveriesById);

        assertFalse(valid, "Route avec D2 avant P2 doit être invalide");
    }

    // =========================================================================
    // TESTS POUR twoOptSwap()
    // =========================================================================

    @Test
    void testTwoOptSwap_BasicSwap() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        method.setAccessible(true);

        // Route originale: [W, P1, P2, D1, D2, W]
        //                      0   1   2   3   4  5
        List<Stop> originalRoute = Arrays.asList(
                warehouse, pickupD1, pickupD2, deliveryD1, deliveryD2, warehouse);

        // Swap indices 1 à 3 (inverse P1, P2, D1)
        @SuppressWarnings("unchecked")
        List<Stop> swappedRoute = (List<Stop>) method.invoke(serviceAlgo, originalRoute, 1, 3);

        // Route attendue: [W, D1, P2, P1, D2, W]
        assertEquals(6, swappedRoute.size(), "La route doit avoir la même taille");
        assertEquals(warehouse, swappedRoute.get(0), "Position 0 doit être W");
        assertEquals(deliveryD1, swappedRoute.get(1), "Position 1 doit être D1 (inversé)");
        assertEquals(pickupD2, swappedRoute.get(2), "Position 2 doit être P2 (inversé)");
        assertEquals(pickupD1, swappedRoute.get(3), "Position 3 doit être P1 (inversé)");
        assertEquals(deliveryD2, swappedRoute.get(4), "Position 4 doit être D2");
        assertEquals(warehouse, swappedRoute.get(5), "Position 5 doit être W");
    }

    @Test
    void testTwoOptSwap_SwapTwoElements() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        method.setAccessible(true);

        // Route: [W, P1, P2, W]
        List<Stop> route = Arrays.asList(warehouse, pickupD1, pickupD2, warehouse);

        // Swap indices 1 à 2 (inverse P1 et P2)
        @SuppressWarnings("unchecked")
        List<Stop> swapped = (List<Stop>) method.invoke(serviceAlgo, route, 1, 2);

        // Attendu: [W, P2, P1, W]
        assertEquals(warehouse, swapped.get(0));
        assertEquals(pickupD2, swapped.get(1), "P1 et P2 doivent être inversés");
        assertEquals(pickupD1, swapped.get(2), "P1 et P2 doivent être inversés");
        assertEquals(warehouse, swapped.get(3));
    }

    @Test
    void testTwoOptSwap_SwapAtBeginning() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        method.setAccessible(true);

        // Route: [W, P1, P2, D1, W]
        List<Stop> route = Arrays.asList(warehouse, pickupD1, pickupD2, deliveryD1, warehouse);

        // Swap indices 0 à 2 (inverse W, P1, P2)
        @SuppressWarnings("unchecked")
        List<Stop> swapped = (List<Stop>) method.invoke(serviceAlgo, route, 0, 2);

        // Attendu: [P2, P1, W, D1, W]
        assertEquals(pickupD2, swapped.get(0));
        assertEquals(pickupD1, swapped.get(1));
        assertEquals(warehouse, swapped.get(2));
        assertEquals(deliveryD1, swapped.get(3));
        assertEquals(warehouse, swapped.get(4));
    }

    @Test
    void testTwoOptSwap_SwapAtEnd() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        method.setAccessible(true);

        // Route: [W, P1, P2, D1, D2]
        List<Stop> route = Arrays.asList(warehouse, pickupD1, pickupD2, deliveryD1, deliveryD2);

        // Swap indices 2 à 4 (inverse P2, D1, D2)
        @SuppressWarnings("unchecked")
        List<Stop> swapped = (List<Stop>) method.invoke(serviceAlgo, route, 2, 4);

        // Attendu: [W, P1, D2, D1, P2]
        assertEquals(warehouse, swapped.get(0));
        assertEquals(pickupD1, swapped.get(1));
        assertEquals(deliveryD2, swapped.get(2));
        assertEquals(deliveryD1, swapped.get(3));
        assertEquals(pickupD2, swapped.get(4));
    }

    @Test
    void testTwoOptSwap_ThrowsException_InvalidIndices() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        method.setAccessible(true);

        List<Stop> route = Arrays.asList(warehouse, pickupD1, pickupD2, warehouse);

        // i >= k (invalide)
        Exception exception1 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, route, 2, 1);
        });
        assertTrue(exception1.getCause() instanceof IllegalArgumentException);

        // i < 0 (invalide)
        Exception exception2 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, route, -1, 2);
        });
        assertTrue(exception2.getCause() instanceof IllegalArgumentException);

        // k >= size (invalide)
        Exception exception3 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, route, 1, 10);
        });
        assertTrue(exception3.getCause() instanceof IllegalArgumentException);
    }

    // =========================================================================
    // TEST D'INTÉGRATION - Toute la Phase 2
    // =========================================================================

    @Test
    void testPhase2Integration_FullWorkflow() throws Exception {
        // Préparer les méthodes
        Method distanceMethod = ServiceAlgo.class.getDeclaredMethod("distance", Stop.class, Stop.class, Graph.class);
        distanceMethod.setAccessible(true);

        Method routeDistanceMethod = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        routeDistanceMethod.setAccessible(true);

        Method precedenceMethod = ServiceAlgo.class.getDeclaredMethod("respectsPrecedence", List.class, Map.class, Map.class);
        precedenceMethod.setAccessible(true);

        Method swapMethod = ServiceAlgo.class.getDeclaredMethod("twoOptSwap", List.class, int.class, int.class);
        swapMethod.setAccessible(true);

        // Créer une route valide
        List<Stop> route = Arrays.asList(warehouse, pickupD1, deliveryD1, warehouse);

        // 1. Calculer distance
        Double distance = (Double) routeDistanceMethod.invoke(serviceAlgo, route, testGraph);
        assertEquals(600.0, distance, 0.001);

        // 2. Vérifier précédence
        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));
        Map<String, Stop> deliveriesById = Map.of("D1", deliveryD1);

        Boolean valid = (Boolean) precedenceMethod.invoke(serviceAlgo, route, pickupsById, deliveriesById);
        assertTrue(valid);

        // 3. Faire un swap invalide
        @SuppressWarnings("unchecked")
        List<Stop> swappedRoute = (List<Stop>) swapMethod.invoke(serviceAlgo, route, 1, 2);
        // Route devient: [W, D1, P1, W] (invalide)

        Boolean swappedValid = (Boolean) precedenceMethod.invoke(serviceAlgo, swappedRoute, pickupsById, deliveriesById);
        assertFalse(swappedValid, "Le swap doit créer une route invalide (D1 avant P1)");

        System.out.println("✅ Phase 2 Integration Test: SUCCESS");
        System.out.println("   - Distance route originale: " + distance + "m");
        System.out.println("   - Route originale valide: " + valid);
        System.out.println("   - Route swappée invalide: " + !swappedValid);
    }
}
