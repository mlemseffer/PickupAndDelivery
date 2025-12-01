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
 * Tests unitaires pour la Phase 3: Construction de la Tournée Initiale (Glouton)
 * Teste l'algorithme du plus proche voisin avec contraintes de précédence
 */
@SpringBootTest
class ServiceAlgoPhase3Test {

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

        // Créer le graph avec des distances favorisant un certain ordre
        testGraph = createTestGraph();
    }

    /**
     * Créer un Graph avec des distances spécifiques pour tester l'algorithme glouton
     * 
     * Distances optimisées pour favoriser l'ordre: W → P2 → D2 → P1 → D1 → W
     */
    private Graph createTestGraph() {
        List<Stop> allStops = Arrays.asList(warehouse, pickupD1, deliveryD1, pickupD2, deliveryD2);

        // Distances asymétriques pour forcer un ordre spécifique
        Map<Stop, Map<Stop, Double>> distances = new HashMap<>();
        
        // Depuis warehouse: P2 est le plus proche
        distances.put(warehouse, Map.of(
                pickupD1, 150.0,
                deliveryD1, 400.0,
                pickupD2, 80.0,   // ← Plus proche
                deliveryD2, 350.0
        ));
        
        // Depuis P1
        distances.put(pickupD1, Map.of(
                warehouse, 150.0,
                deliveryD1, 200.0,
                pickupD2, 180.0,
                deliveryD2, 300.0
        ));
        
        // Depuis D1
        distances.put(deliveryD1, Map.of(
                warehouse, 310.0,
                pickupD1, 200.0,
                pickupD2, 290.0,
                deliveryD2, 120.0
        ));
        
        // Depuis P2: D2 est le plus proche
        distances.put(pickupD2, Map.of(
                warehouse, 80.0,
                pickupD1, 180.0,
                deliveryD1, 290.0,
                deliveryD2, 155.0  // ← Plus proche après P2
        ));
        
        // Depuis D2: P1 est le plus proche
        distances.put(deliveryD2, Map.of(
                warehouse, 250.0,
                pickupD1, 310.0,
                deliveryD1, 120.0,
                pickupD2, 155.0
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
    // TESTS POUR buildInitialRoute()
    // =========================================================================

    @Test
    void testBuildInitialRoute_SingleRequest() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        // Une seule demande: P1 → D1
        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1);
        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) method.invoke(serviceAlgo, testGraph, warehouse, stops, pickupsById);

        // Vérifications
        assertNotNull(route, "La route ne doit pas être null");
        assertEquals(4, route.size(), "Route doit avoir 4 stops: W, P1, D1, W");
        
        // Vérifier l'ordre
        assertEquals(warehouse, route.get(0), "Doit commencer au warehouse");
        assertEquals(pickupD1, route.get(1), "P1 doit être visité en premier");
        assertEquals(deliveryD1, route.get(2), "D1 doit être visité après P1");
        assertEquals(warehouse, route.get(3), "Doit finir au warehouse");
        
        System.out.println("✅ Route avec 1 demande: " + formatRoute(route));
    }

    @Test
    void testBuildInitialRoute_TwoRequests() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        // Deux demandes: D1 et D2
        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1, pickupD2, deliveryD2);
        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) method.invoke(serviceAlgo, testGraph, warehouse, stops, pickupsById);

        // Vérifications de base
        assertNotNull(route, "La route ne doit pas être null");
        assertEquals(6, route.size(), "Route doit avoir 6 stops: W, P*, P*, D*, D*, W");
        
        // Vérifier le départ et l'arrivée
        assertEquals(warehouse, route.get(0), "Doit commencer au warehouse");
        assertEquals(warehouse, route.get(5), "Doit finir au warehouse");
        
        // Vérifier que tous les stops sont présents
        assertTrue(route.contains(pickupD1), "P1 doit être dans la route");
        assertTrue(route.contains(deliveryD1), "D1 doit être dans la route");
        assertTrue(route.contains(pickupD2), "P2 doit être dans la route");
        assertTrue(route.contains(deliveryD2), "D2 doit être dans la route");
        
        // Vérifier les contraintes de précédence
        int indexP1 = route.indexOf(pickupD1);
        int indexD1 = route.indexOf(deliveryD1);
        int indexP2 = route.indexOf(pickupD2);
        int indexD2 = route.indexOf(deliveryD2);
        
        assertTrue(indexP1 < indexD1, "P1 doit être avant D1");
        assertTrue(indexP2 < indexD2, "P2 doit être avant D2");
        
        System.out.println("✅ Route avec 2 demandes: " + formatRoute(route));
    }

    @Test
    void testBuildInitialRoute_GreedyChoosesClosest() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        // Deux demandes
        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1, pickupD2, deliveryD2);
        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) method.invoke(serviceAlgo, testGraph, warehouse, stops, pickupsById);

        // Avec nos distances, l'ordre glouton devrait être:
        // W → P2 (80.0, le plus proche de W)
        // P2 → D2 (155.0, le plus proche faisable de P2)
        // D2 → P1 (310.0, le seul restant faisable)
        // P1 → D1 (200.0, le seul restant)
        // D1 → W (310.0, retour)
        
        assertEquals(pickupD2, route.get(1), "Glouton doit choisir P2 d'abord (plus proche de W)");
        
        System.out.println("✅ Glouton choisit bien le plus proche: " + formatRoute(route));
    }

    @Test
    void testBuildInitialRoute_EmptyStops() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        // Aucun stop à visiter
        List<Stop> emptyStops = new ArrayList<>();
        Map<String, List<Stop>> pickupsById = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) method.invoke(serviceAlgo, testGraph, warehouse, emptyStops, pickupsById);

        // Vérifications
        assertNotNull(route);
        assertEquals(2, route.size(), "Route vide doit avoir 2 stops: W, W");
        assertEquals(warehouse, route.get(0));
        assertEquals(warehouse, route.get(1));
        
        System.out.println("✅ Route vide: " + formatRoute(route));
    }

    @Test
    void testBuildInitialRoute_ThrowsException_WhenParametersNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1);
        Map<String, List<Stop>> pickupsById = Map.of("D1", Arrays.asList(pickupD1));

        // Graph null
        Exception ex1 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, null, warehouse, stops, pickupsById);
        });
        assertTrue(ex1.getCause() instanceof IllegalArgumentException);

        // Warehouse null
        Exception ex2 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, testGraph, null, stops, pickupsById);
        });
        assertTrue(ex2.getCause() instanceof IllegalArgumentException);

        // Stops null
        Exception ex3 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, testGraph, warehouse, null, pickupsById);
        });
        assertTrue(ex3.getCause() instanceof IllegalArgumentException);

        // PickupsById null
        Exception ex4 = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, testGraph, warehouse, stops, null);
        });
        assertTrue(ex4.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testBuildInitialRoute_RespectsPrecedenceConstraints() throws Exception {
        // Préparer les méthodes nécessaires
        Method buildRouteMethod = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        buildRouteMethod.setAccessible(true);

        Method precedenceMethod = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        precedenceMethod.setAccessible(true);

        // Créer une route avec 2 demandes
        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1, pickupD2, deliveryD2);
        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );
        Map<String, Stop> deliveriesById = Map.of(
                "D1", deliveryD1,
                "D2", deliveryD2
        );

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) buildRouteMethod.invoke(
                serviceAlgo, testGraph, warehouse, stops, pickupsById);

        // Vérifier que la route respecte les contraintes
        Boolean valid = (Boolean) precedenceMethod.invoke(
                serviceAlgo, route, pickupsById, deliveriesById);

        assertTrue(valid, "La route construite par glouton DOIT respecter les contraintes de précédence");
        
        System.out.println("✅ Route glouton respecte les contraintes: " + formatRoute(route));
    }

    @Test
    void testBuildInitialRoute_CalculateDistance() throws Exception {
        // Préparer les méthodes
        Method buildRouteMethod = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        buildRouteMethod.setAccessible(true);

        Method distanceMethod = ServiceAlgo.class.getDeclaredMethod(
                "computeRouteDistance", List.class, Graph.class);
        distanceMethod.setAccessible(true);

        // Construire la route
        List<Stop> stops = Arrays.asList(pickupD1, deliveryD1, pickupD2, deliveryD2);
        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2)
        );

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) buildRouteMethod.invoke(
                serviceAlgo, testGraph, warehouse, stops, pickupsById);

        // Calculer la distance
        Double distance = (Double) distanceMethod.invoke(serviceAlgo, route, testGraph);

        assertNotNull(distance);
        assertTrue(distance > 0, "La distance doit être positive");
        
        // Avec notre configuration, l'ordre attendu est: W → P2 → D2 → P1 → D1 → W
        // Distance: 80.0 + 155.0 + 310.0 + 200.0 + 310.0 = 1055.0
        // (peut varier selon l'ordre exact choisi par l'algorithme)
        
        System.out.println("✅ Distance de la route glouton: " + distance + " m");
    }

    @Test
    void testBuildInitialRoute_ThreeRequests() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        method.setAccessible(true);

        // Créer une troisième demande
        Stop pickupD3 = new Stop("N2", "D3", Stop.TypeStop.PICKUP);
        Stop deliveryD3 = new Stop("N8", "D3", Stop.TypeStop.DELIVERY);

        // Ajouter D3 au graph
        Graph extendedGraph = createExtendedGraph(pickupD3, deliveryD3);

        // Trois demandes
        List<Stop> stops = Arrays.asList(
                pickupD1, deliveryD1, pickupD2, deliveryD2, pickupD3, deliveryD3);
        Map<String, List<Stop>> pickupsById = Map.of(
                "D1", Arrays.asList(pickupD1),
                "D2", Arrays.asList(pickupD2),
                "D3", Arrays.asList(pickupD3)
        );

        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) method.invoke(serviceAlgo, extendedGraph, warehouse, stops, pickupsById);

        // Vérifications
        assertEquals(8, route.size(), "Route doit avoir 8 stops: W, 3×(P+D), W");
        assertEquals(warehouse, route.get(0));
        assertEquals(warehouse, route.get(7));

        // Vérifier que tous les stops sont présents
        assertTrue(route.contains(pickupD1));
        assertTrue(route.contains(deliveryD1));
        assertTrue(route.contains(pickupD2));
        assertTrue(route.contains(deliveryD2));
        assertTrue(route.contains(pickupD3));
        assertTrue(route.contains(deliveryD3));

        // Vérifier contraintes
        assertTrue(route.indexOf(pickupD1) < route.indexOf(deliveryD1));
        assertTrue(route.indexOf(pickupD2) < route.indexOf(deliveryD2));
        assertTrue(route.indexOf(pickupD3) < route.indexOf(deliveryD3));
        
        System.out.println("✅ Route avec 3 demandes: " + formatRoute(route));
    }

    /**
     * Créer un graph étendu avec D3
     */
    private Graph createExtendedGraph(Stop pickupD3, Stop deliveryD3) {
        List<Stop> allStops = Arrays.asList(
                warehouse, pickupD1, deliveryD1, pickupD2, deliveryD2, pickupD3, deliveryD3);

        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
        
        // Pour simplifier, créer des distances génériques entre tous les stops
        for (Stop source : allStops) {
            Map<Stop, Trajet> destinations = new HashMap<>();
            for (Stop dest : allStops) {
                if (!source.equals(dest)) {
                    Trajet trajet = new Trajet();
                    trajet.setStopDepart(source);
                    trajet.setStopArrivee(dest);
                    trajet.setDistance(100.0 + Math.random() * 200); // Distance aléatoire 100-300
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
    // TEST D'INTÉGRATION - Toute la Phase 3
    // =========================================================================

    @Test
    void testPhase3Integration_CompleteWorkflow() throws Exception {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("    TEST D'INTÉGRATION PHASE 3 - GLOUTON");
        System.out.println("═══════════════════════════════════════════════════════════");

        // Préparer toutes les méthodes nécessaires
        Method extractWarehouseMethod = ServiceAlgo.class.getDeclaredMethod("extractWarehouse", Graph.class);
        extractWarehouseMethod.setAccessible(true);

        Method extractStopsMethod = ServiceAlgo.class.getDeclaredMethod("extractNonWarehouseStops", Graph.class);
        extractStopsMethod.setAccessible(true);

        Method buildPickupsMethod = ServiceAlgo.class.getDeclaredMethod("buildPickupsByRequestId", List.class);
        buildPickupsMethod.setAccessible(true);

        Method buildDeliveriesMethod = ServiceAlgo.class.getDeclaredMethod("buildDeliveryByRequestId", List.class);
        buildDeliveriesMethod.setAccessible(true);

        Method buildRouteMethod = ServiceAlgo.class.getDeclaredMethod(
                "buildInitialRoute", Graph.class, Stop.class, List.class, Map.class);
        buildRouteMethod.setAccessible(true);

        Method distanceMethod = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        distanceMethod.setAccessible(true);

        Method precedenceMethod = ServiceAlgo.class.getDeclaredMethod(
                "respectsPrecedence", List.class, Map.class, Map.class);
        precedenceMethod.setAccessible(true);

        // 1. PHASE 1: Préparation des données
        System.out.println("\n📊 Phase 1: Préparation des données");
        
        Stop wh = (Stop) extractWarehouseMethod.invoke(serviceAlgo, testGraph);
        @SuppressWarnings("unchecked")
        List<Stop> stops = (List<Stop>) extractStopsMethod.invoke(serviceAlgo, testGraph);
        @SuppressWarnings("unchecked")
        Map<String, List<Stop>> pickupsById = (Map<String, List<Stop>>) buildPickupsMethod.invoke(serviceAlgo, stops);
        @SuppressWarnings("unchecked")
        Map<String, Stop> deliveriesById = (Map<String, Stop>) buildDeliveriesMethod.invoke(serviceAlgo, stops);

        System.out.println("   ✓ Warehouse: " + wh.getIdNode());
        System.out.println("   ✓ Stops à visiter: " + stops.size());
        System.out.println("   ✓ Demandes: " + pickupsById.size());

        // 2. PHASE 3: Construction de la route
        System.out.println("\n🛣️  Phase 3: Construction de la route glouton");
        
        @SuppressWarnings("unchecked")
        List<Stop> route = (List<Stop>) buildRouteMethod.invoke(
                serviceAlgo, testGraph, wh, stops, pickupsById);

        System.out.println("   ✓ Route construite: " + formatRoute(route));
        System.out.println("   ✓ Nombre de stops: " + route.size());

        // 3. PHASE 2: Validation et calcul de distance
        System.out.println("\n✅ Phase 2: Validation de la route");
        
        Double distance = (Double) distanceMethod.invoke(serviceAlgo, route, testGraph);
        Boolean valid = (Boolean) precedenceMethod.invoke(serviceAlgo, route, pickupsById, deliveriesById);

        System.out.println("   ✓ Distance totale: " + distance + " m");
        System.out.println("   ✓ Contraintes respectées: " + valid);

        // Assertions finales
        assertNotNull(route);
        assertEquals(6, route.size());
        assertTrue(distance > 0);
        assertTrue(valid);

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("    ✅ TEST D'INTÉGRATION PHASE 3: SUCCESS");
        System.out.println("═══════════════════════════════════════════════════════════\n");
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================

    /**
     * Formate une route pour l'affichage
     */
    private String formatRoute(List<Stop> route) {
        if (route == null || route.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < route.size(); i++) {
            Stop stop = route.get(i);
            if (stop.getTypeStop() == Stop.TypeStop.WAREHOUSE) {
                sb.append("W");
            } else {
                sb.append(stop.getTypeStop() == Stop.TypeStop.PICKUP ? "P" : "D");
                sb.append(stop.getIdDemande().substring(1)); // "D1" → "1"
            }
            if (i < route.size() - 1) {
                sb.append(" → ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
