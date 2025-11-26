package com.pickupdelivery.service;

import com.pickupdelivery.model.*;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la Phase 1: Préparation des données
 * Teste les méthodes privées d'extraction et d'organisation des stops
 */
@SpringBootTest
class ServiceAlgoPhase1Test {

    @Autowired
    private ServiceAlgo serviceAlgo;
    
    private CityMap cityMap;
    private Graph testGraph;

    @BeforeEach
    void setUp() {
        // Créer une carte de test simple
        cityMap = createTestCityMap();
        
        // Créer un Graph de test avec 1 warehouse + 2 demandes (4 stops pickup/delivery)
        testGraph = createTestGraph();
    }

    /**
     * Créer une carte simple avec 5 nœuds
     */
    private CityMap createTestCityMap() {
        List<Node> nodes = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        
        // Nœuds
        nodes.add(new Node("N1", 45.75, 4.85));  // Pickup D1
        nodes.add(new Node("N3", 45.76, 4.86));  // Pickup D2
        nodes.add(new Node("N5", 45.77, 4.87));  // Warehouse
        nodes.add(new Node("N7", 45.78, 4.88));  // Delivery D2
        nodes.add(new Node("N9", 45.79, 4.89));  // Delivery D1
        
        // Segments (connexions simples)
        segments.add(new Segment("N5", "N1", 100.0, "Rue A"));
        segments.add(new Segment("N1", "N5", 100.0, "Rue A"));
        segments.add(new Segment("N5", "N3", 80.0, "Rue B"));
        segments.add(new Segment("N3", "N5", 80.0, "Rue B"));
        segments.add(new Segment("N1", "N9", 150.0, "Rue C"));
        segments.add(new Segment("N9", "N1", 150.0, "Rue C"));
        segments.add(new Segment("N3", "N7", 120.0, "Rue D"));
        segments.add(new Segment("N7", "N3", 120.0, "Rue D"));
        
        CityMap map = new CityMap();
        map.setNodes(nodes);
        map.setSegments(segments);
        
        return map;
    }

    /**
     * Créer un Graph de test avec:
     * - 1 Warehouse (N5)
     * - 2 Demandes:
     *   - D1: Pickup N1 → Delivery N9
     *   - D2: Pickup N3 → Delivery N7
     */
    private Graph createTestGraph() {
        // Créer les stops
        Stop warehouse = new Stop("N5", null, Stop.TypeStop.WAREHOUSE);
        Stop pickupD1 = new Stop("N1", "D1", Stop.TypeStop.PICKUP);
        Stop deliveryD1 = new Stop("N9", "D1", Stop.TypeStop.DELIVERY);
        Stop pickupD2 = new Stop("N3", "D2", Stop.TypeStop.PICKUP);
        Stop deliveryD2 = new Stop("N7", "D2", Stop.TypeStop.DELIVERY);
        
        // Créer la matrice de distances (simplifiée pour les tests)
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
        
        // Initialiser les maps pour chaque stop
        List<Stop> allStops = Arrays.asList(warehouse, pickupD1, deliveryD1, pickupD2, deliveryD2);
        for (Stop source : allStops) {
            Map<Stop, Trajet> destinations = new HashMap<>();
            for (Stop dest : allStops) {
                if (!source.equals(dest)) {
                    Trajet trajet = new Trajet();
                    trajet.setStopDepart(source);
                    trajet.setStopArrivee(dest);
                    trajet.setDistance(100.0); // Distance fictive
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
    // TESTS POUR extractWarehouse()
    // =========================================================================

    @Test
    void testExtractWarehouse_Success() throws Exception {
        // Utiliser la réflexion pour accéder à la méthode privée
        Method method = ServiceAlgo.class.getDeclaredMethod("extractWarehouse", Graph.class);
        method.setAccessible(true);
        
        // Act
        Stop warehouse = (Stop) method.invoke(serviceAlgo, testGraph);
        
        // Assert
        assertNotNull(warehouse, "Le warehouse ne doit pas être null");
        assertEquals(Stop.TypeStop.WAREHOUSE, warehouse.getTypeStop(), "Le type doit être WAREHOUSE");
        assertEquals("N5", warehouse.getIdNode(), "L'ID du nœud doit être N5");
        assertNull(warehouse.getIdDemande(), "L'ID de demande doit être null pour le warehouse");
    }

    @Test
    void testExtractWarehouse_ThrowsException_WhenNoWarehouse() throws Exception {
        // Créer un graph sans warehouse
        Graph graphWithoutWarehouse = new Graph();
        Map<Stop, Map<Stop, Trajet>> matrix = new HashMap<>();
        
        Stop pickup = new Stop("N1", "D1", Stop.TypeStop.PICKUP);
        matrix.put(pickup, new HashMap<>());
        
        graphWithoutWarehouse.setDistancesMatrix(matrix);
        
        // Utiliser la réflexion
        Method method = ServiceAlgo.class.getDeclaredMethod("extractWarehouse", Graph.class);
        method.setAccessible(true);
        
        // Assert
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, graphWithoutWarehouse);
        });
        
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getCause().getMessage().contains("Aucun entrepôt"));
    }

    @Test
    void testExtractWarehouse_ThrowsException_WhenGraphNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("extractWarehouse", Graph.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, (Graph) null);
        });
        
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    // =========================================================================
    // TESTS POUR extractNonWarehouseStops()
    // =========================================================================

    @Test
    void testExtractNonWarehouseStops_Success() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("extractNonWarehouseStops", Graph.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        List<Stop> stops = (List<Stop>) method.invoke(serviceAlgo, testGraph);
        
        // Assert
        assertNotNull(stops, "La liste ne doit pas être null");
        assertEquals(4, stops.size(), "Doit contenir 4 stops (2 pickups + 2 deliveries)");
        
        // Vérifier qu'il n'y a pas de warehouse
        long warehouseCount = stops.stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE)
                .count();
        assertEquals(0, warehouseCount, "Ne doit pas contenir de warehouse");
        
        // Vérifier qu'il y a 2 pickups et 2 deliveries
        long pickupCount = stops.stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP)
                .count();
        long deliveryCount = stops.stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY)
                .count();
        
        assertEquals(2, pickupCount, "Doit contenir 2 pickups");
        assertEquals(2, deliveryCount, "Doit contenir 2 deliveries");
    }

    @Test
    void testExtractNonWarehouseStops_EmptyWhenOnlyWarehouse() throws Exception {
        // Créer un graph avec seulement le warehouse
        Graph graphOnlyWarehouse = new Graph();
        Map<Stop, Map<Stop, Trajet>> matrix = new HashMap<>();
        
        Stop warehouse = new Stop("N5", null, Stop.TypeStop.WAREHOUSE);
        matrix.put(warehouse, new HashMap<>());
        
        graphOnlyWarehouse.setDistancesMatrix(matrix);
        
        Method method = ServiceAlgo.class.getDeclaredMethod("extractNonWarehouseStops", Graph.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        List<Stop> stops = (List<Stop>) method.invoke(serviceAlgo, graphOnlyWarehouse);
        
        // Assert
        assertNotNull(stops);
        assertEquals(0, stops.size(), "Doit être vide car seulement le warehouse");
    }

    // =========================================================================
    // TESTS POUR buildPickupsByRequestId()
    // =========================================================================

    @Test
    void testBuildPickupsByRequestId_Success() throws Exception {
        // Créer une liste de stops
        List<Stop> stops = Arrays.asList(
                new Stop("N1", "D1", Stop.TypeStop.PICKUP),
                new Stop("N9", "D1", Stop.TypeStop.DELIVERY),
                new Stop("N3", "D2", Stop.TypeStop.PICKUP),
                new Stop("N7", "D2", Stop.TypeStop.DELIVERY)
        );
        
        Method method = ServiceAlgo.class.getDeclaredMethod("buildPickupsByRequestId", List.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, List<Stop>> pickupsByRequestId = (Map<String, List<Stop>>) method.invoke(serviceAlgo, stops);
        
        // Assert
        assertNotNull(pickupsByRequestId, "La map ne doit pas être null");
        assertEquals(2, pickupsByRequestId.size(), "Doit contenir 2 demandes (D1 et D2)");
        
        // Vérifier D1
        assertTrue(pickupsByRequestId.containsKey("D1"), "Doit contenir la clé D1");
        List<Stop> pickupsD1 = pickupsByRequestId.get("D1");
        assertEquals(1, pickupsD1.size(), "D1 doit avoir 1 pickup");
        assertEquals("N1", pickupsD1.get(0).getIdNode(), "Le pickup D1 doit être au nœud N1");
        assertEquals(Stop.TypeStop.PICKUP, pickupsD1.get(0).getTypeStop());
        
        // Vérifier D2
        assertTrue(pickupsByRequestId.containsKey("D2"), "Doit contenir la clé D2");
        List<Stop> pickupsD2 = pickupsByRequestId.get("D2");
        assertEquals(1, pickupsD2.size(), "D2 doit avoir 1 pickup");
        assertEquals("N3", pickupsD2.get(0).getIdNode(), "Le pickup D2 doit être au nœud N3");
        assertEquals(Stop.TypeStop.PICKUP, pickupsD2.get(0).getTypeStop());
    }

    @Test
    void testBuildPickupsByRequestId_EmptyWhenNoPickups() throws Exception {
        // Liste avec seulement des deliveries
        List<Stop> stops = Arrays.asList(
                new Stop("N9", "D1", Stop.TypeStop.DELIVERY),
                new Stop("N7", "D2", Stop.TypeStop.DELIVERY)
        );
        
        Method method = ServiceAlgo.class.getDeclaredMethod("buildPickupsByRequestId", List.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, List<Stop>> pickupsByRequestId = (Map<String, List<Stop>>) method.invoke(serviceAlgo, stops);
        
        // Assert
        assertNotNull(pickupsByRequestId);
        assertEquals(0, pickupsByRequestId.size(), "Doit être vide car pas de pickups");
    }

    @Test
    void testBuildPickupsByRequestId_ThrowsException_WhenNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("buildPickupsByRequestId", List.class);
        method.setAccessible(true);
        
        // Assert
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, (List<Stop>) null);
        });
        
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    // =========================================================================
    // TESTS POUR buildDeliveryByRequestId()
    // =========================================================================

    @Test
    void testBuildDeliveryByRequestId_Success() throws Exception {
        // Créer une liste de stops
        List<Stop> stops = Arrays.asList(
                new Stop("N1", "D1", Stop.TypeStop.PICKUP),
                new Stop("N9", "D1", Stop.TypeStop.DELIVERY),
                new Stop("N3", "D2", Stop.TypeStop.PICKUP),
                new Stop("N7", "D2", Stop.TypeStop.DELIVERY)
        );
        
        Method method = ServiceAlgo.class.getDeclaredMethod("buildDeliveryByRequestId", List.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, Stop> deliveryByRequestId = (Map<String, Stop>) method.invoke(serviceAlgo, stops);
        
        // Assert
        assertNotNull(deliveryByRequestId, "La map ne doit pas être null");
        assertEquals(2, deliveryByRequestId.size(), "Doit contenir 2 demandes (D1 et D2)");
        
        // Vérifier D1
        assertTrue(deliveryByRequestId.containsKey("D1"), "Doit contenir la clé D1");
        Stop deliveryD1 = deliveryByRequestId.get("D1");
        assertEquals("N9", deliveryD1.getIdNode(), "Le delivery D1 doit être au nœud N9");
        assertEquals(Stop.TypeStop.DELIVERY, deliveryD1.getTypeStop());
        assertEquals("D1", deliveryD1.getIdDemande());
        
        // Vérifier D2
        assertTrue(deliveryByRequestId.containsKey("D2"), "Doit contenir la clé D2");
        Stop deliveryD2 = deliveryByRequestId.get("D2");
        assertEquals("N7", deliveryD2.getIdNode(), "Le delivery D2 doit être au nœud N7");
        assertEquals(Stop.TypeStop.DELIVERY, deliveryD2.getTypeStop());
        assertEquals("D2", deliveryD2.getIdDemande());
    }

    @Test
    void testBuildDeliveryByRequestId_EmptyWhenNoDeliveries() throws Exception {
        // Liste avec seulement des pickups
        List<Stop> stops = Arrays.asList(
                new Stop("N1", "D1", Stop.TypeStop.PICKUP),
                new Stop("N3", "D2", Stop.TypeStop.PICKUP)
        );
        
        Method method = ServiceAlgo.class.getDeclaredMethod("buildDeliveryByRequestId", List.class);
        method.setAccessible(true);
        
        // Act
        @SuppressWarnings("unchecked")
        Map<String, Stop> deliveryByRequestId = (Map<String, Stop>) method.invoke(serviceAlgo, stops);
        
        // Assert
        assertNotNull(deliveryByRequestId);
        assertEquals(0, deliveryByRequestId.size(), "Doit être vide car pas de deliveries");
    }

    @Test
    void testBuildDeliveryByRequestId_ThrowsException_WhenNull() throws Exception {
        Method method = ServiceAlgo.class.getDeclaredMethod("buildDeliveryByRequestId", List.class);
        method.setAccessible(true);
        
        // Assert
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(serviceAlgo, (List<Stop>) null);
        });
        
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    // =========================================================================
    // TEST D'INTÉGRATION - Toute la Phase 1
    // =========================================================================

    @Test
    void testPhase1Integration_FullWorkflow() throws Exception {
        // Utiliser la réflexion pour accéder aux méthodes privées
        Method extractWarehouseMethod = ServiceAlgo.class.getDeclaredMethod("extractWarehouse", Graph.class);
        extractWarehouseMethod.setAccessible(true);
        
        Method extractStopsMethod = ServiceAlgo.class.getDeclaredMethod("extractNonWarehouseStops", Graph.class);
        extractStopsMethod.setAccessible(true);
        
        Method buildPickupsMethod = ServiceAlgo.class.getDeclaredMethod("buildPickupsByRequestId", List.class);
        buildPickupsMethod.setAccessible(true);
        
        Method buildDeliveriesMethod = ServiceAlgo.class.getDeclaredMethod("buildDeliveryByRequestId", List.class);
        buildDeliveriesMethod.setAccessible(true);
        
        // Act - Exécuter le workflow complet de la Phase 1
        Stop warehouse = (Stop) extractWarehouseMethod.invoke(serviceAlgo, testGraph);
        
        @SuppressWarnings("unchecked")
        List<Stop> stops = (List<Stop>) extractStopsMethod.invoke(serviceAlgo, testGraph);
        
        @SuppressWarnings("unchecked")
        Map<String, List<Stop>> pickupsByRequestId = (Map<String, List<Stop>>) buildPickupsMethod.invoke(serviceAlgo, stops);
        
        @SuppressWarnings("unchecked")
        Map<String, Stop> deliveryByRequestId = (Map<String, Stop>) buildDeliveriesMethod.invoke(serviceAlgo, stops);
        
        // Assert - Vérifications complètes
        
        // 1. Warehouse
        assertNotNull(warehouse);
        assertEquals(Stop.TypeStop.WAREHOUSE, warehouse.getTypeStop());
        assertEquals("N5", warehouse.getIdNode());
        
        // 2. Stops
        assertEquals(4, stops.size());
        
        // 3. Pickups
        assertEquals(2, pickupsByRequestId.size());
        assertTrue(pickupsByRequestId.containsKey("D1"));
        assertTrue(pickupsByRequestId.containsKey("D2"));
        
        // 4. Deliveries
        assertEquals(2, deliveryByRequestId.size());
        assertTrue(deliveryByRequestId.containsKey("D1"));
        assertTrue(deliveryByRequestId.containsKey("D2"));
        
        // 5. Cohérence des données
        for (String requestId : pickupsByRequestId.keySet()) {
            assertTrue(deliveryByRequestId.containsKey(requestId), 
                    "Chaque pickup doit avoir un delivery correspondant");
        }
        
        System.out.println("✅ Phase 1 Integration Test: SUCCESS");
        System.out.println("   - Warehouse: " + warehouse.getIdNode());
        System.out.println("   - Stops (non-warehouse): " + stops.size());
        System.out.println("   - Pickups par demande: " + pickupsByRequestId.size());
        System.out.println("   - Deliveries par demande: " + deliveryByRequestId.size());
    }
}
