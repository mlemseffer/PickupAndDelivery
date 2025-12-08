package com.pickupdelivery.service;

import com.pickupdelivery.dto.ShortestPathResult;
import com.pickupdelivery.dto.TourDistributionResult;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.Node;
import com.pickupdelivery.model.Segment;
import com.pickupdelivery.model.Demand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests additionnels couvrant les portions non testées de ServiceAlgo :
 * - Dijkstra + cache LRU
 * - Calcul de durée complet (trajet + temps de service)
 * - Optimisation 2-opt
 * - Distribution FIFO (avertissements / non assignation)
 * - Formatage de route pour les logs
 */
class ServiceAlgoAdvancedTest {

    private ServiceAlgo serviceAlgo;

    @BeforeEach
    void setUp() {
        serviceAlgo = new ServiceAlgo();
        serviceAlgo.clearDijkstraCache();
    }

    @Test
    void dijkstra_shouldReturnShortestPath_andUseCache() throws Exception {
        // Graph: N1 --10-- N2 --10-- N3  (N1--50--N3 direct, must be ignored)
        Node n1 = new Node("N1", 0, 0);
        Node n2 = new Node("N2", 0, 1);
        Node n3 = new Node("N3", 0, 2);

        List<Node> nodes = List.of(n1, n2, n3);
        List<Segment> segments = List.of(
            new Segment("N1", "N2", 10.0, "N1-N2"),
            new Segment("N2", "N3", 10.0, "N2-N3"),
            new Segment("N1", "N3", 50.0, "N1-N3")
        );

        CityMap cityMap = new CityMap(new ArrayList<>(nodes), new ArrayList<>(segments));

        Field cacheField = ServiceAlgo.class.getDeclaredField("dijkstraCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ShortestPathResult> cache = (Map<String, ShortestPathResult>) cacheField.get(serviceAlgo);
        assertEquals(0, cache.size(), "Le cache doit être vide au démarrage");

        ShortestPathResult first = serviceAlgo.dijkstra(n1, n3, cityMap);
        assertEquals(20.0, first.getDistance(), 0.001, "Le chemin optimal doit passer par N2 (20m)");
        assertEquals(2, first.getSegments().size(), "Deux segments attendus dans le plus court chemin");
        assertEquals(1, cache.size(), "Le cache doit contenir exactement 1 entrée après le premier appel");

        ShortestPathResult second = serviceAlgo.dijkstra(n1, n3, cityMap);
        assertSame(first, second, "Un cache hit doit renvoyer la même instance");
        assertEquals(1, cache.size(), "Le cache ne doit pas croître sur un cache hit");
    }

    @Test
    void computeRouteDuration_shouldIncludeTravelAndServiceTime() throws Exception {
        Stop warehouse = new Stop("W", null, Stop.TypeStop.WAREHOUSE);
        Stop pickup = new Stop("P1", "D1", Stop.TypeStop.PICKUP);
        Stop delivery = new Stop("D1", "D1", Stop.TypeStop.DELIVERY);

        List<Stop> stops = List.of(warehouse, pickup, delivery);
        Map<Stop, Map<Stop, Trajet>> matrix = buildCompleteMatrix(stops, 500.0);
        addDistance(matrix, warehouse, pickup, 1000.0);
        addDistance(matrix, pickup, delivery, 2000.0);
        addDistance(matrix, delivery, warehouse, 3000.0);

        Graph graph = new Graph();
        graph.setStopDepart(warehouse);
        graph.setDistancesMatrix(matrix);

        Demand demand = new Demand("D1", "P1", "D1", 120, 180, null);
        Map<String, Demand> demandMap = new HashMap<>();
        demandMap.put("D1", demand);

        Method method = ServiceAlgo.class.getDeclaredMethod("computeRouteDuration", List.class, Graph.class, Map.class);
        method.setAccessible(true);

        List<Stop> route = List.of(warehouse, pickup, delivery, warehouse);
        double duration = (double) method.invoke(serviceAlgo, route, graph, demandMap);

        // Travel: 1000/4.166.. + 2000/4.166.. + 3000/4.166.. = 1440s
        // Service: 120s (pickup) + 180s (delivery) = 300s
        assertEquals(1740.0, duration, 1.0, "La durée doit inclure trajets et temps de service");
    }

    @Test
    void optimizeWith2Opt_shouldReduceDistance_andRespectPrecedence() throws Exception {
        Stop warehouse = new Stop("W", null, Stop.TypeStop.WAREHOUSE);
        Stop pickup1 = new Stop("P1", "D1", Stop.TypeStop.PICKUP);
        Stop delivery1 = new Stop("D1", "D1", Stop.TypeStop.DELIVERY);
        Stop pickup2 = new Stop("P2", "D2", Stop.TypeStop.PICKUP);
        Stop delivery2 = new Stop("D2", "D2", Stop.TypeStop.DELIVERY);

        List<Stop> stops = List.of(warehouse, pickup1, delivery1, pickup2, delivery2);
        Map<Stop, Map<Stop, Trajet>> matrix = buildCompleteMatrix(stops, 80.0);

        // Distances rendant la route [W, P1, P2, D1, D2, W] sous-optimale
        addDistance(matrix, pickup1, pickup2, 120.0);
        addDistance(matrix, delivery1, delivery2, 120.0);
        addDistance(matrix, pickup1, delivery1, 10.0);
        addDistance(matrix, pickup2, delivery2, 10.0);
        addDistance(matrix, pickup1, delivery2, 80.0);
        addDistance(matrix, pickup2, delivery1, 15.0);
        addDistance(matrix, warehouse, pickup1, 10.0);
        addDistance(matrix, warehouse, pickup2, 12.0);
        addDistance(matrix, warehouse, delivery1, 40.0);
        addDistance(matrix, warehouse, delivery2, 30.0);

        Graph graph = new Graph();
        graph.setStopDepart(warehouse);
        graph.setDistancesMatrix(matrix);

        Map<String, List<Stop>> pickupsById = Map.of(
            "D1", List.of(pickup1),
            "D2", List.of(pickup2)
        );
        Map<String, Stop> deliveriesById = Map.of(
            "D1", delivery1,
            "D2", delivery2
        );

        Method routeDistance = ServiceAlgo.class.getDeclaredMethod("computeRouteDistance", List.class, Graph.class);
        routeDistance.setAccessible(true);
        Method optimize = ServiceAlgo.class.getDeclaredMethod("optimizeWith2Opt", List.class, Graph.class, Map.class, Map.class);
        optimize.setAccessible(true);

        List<Stop> initialRoute = List.of(warehouse, pickup1, pickup2, delivery1, delivery2, warehouse);
        double initialDistance = (double) routeDistance.invoke(serviceAlgo, initialRoute, graph);

        @SuppressWarnings("unchecked")
        List<Stop> optimized = (List<Stop>) optimize.invoke(serviceAlgo, initialRoute, graph, pickupsById, deliveriesById);
        double optimizedDistance = (double) routeDistance.invoke(serviceAlgo, optimized, graph);

        assertTrue(optimizedDistance < initialDistance, "L'optimisation 2-opt doit réduire la distance totale");
        assertEquals(warehouse, optimized.get(0), "La tournée optimisée doit commencer au warehouse");
        assertEquals(warehouse, optimized.get(optimized.size() - 1), "La tournée optimisée doit finir au warehouse");
        assertTrue(optimized.indexOf(pickup1) < optimized.indexOf(delivery1), "Pickup D1 doit précéder Delivery D1");
        assertTrue(optimized.indexOf(pickup2) < optimized.indexOf(delivery2), "Pickup D2 doit précéder Delivery D2");
    }

    @Test
    void distributeFIFO_shouldMarkDemandAsUnassignedWhenExceedingTimeLimit() throws Exception {
        Stop warehouse = new Stop("W", null, Stop.TypeStop.WAREHOUSE);
        Stop fastPickup = new Stop("P_FAST", "DFAST", Stop.TypeStop.PICKUP);
        Stop fastDelivery = new Stop("D_FAST", "DFAST", Stop.TypeStop.DELIVERY);
        Stop longPickup = new Stop("P_LONG", "DLONG", Stop.TypeStop.PICKUP);
        Stop longDelivery = new Stop("D_LONG", "DLONG", Stop.TypeStop.DELIVERY);

        List<Stop> allStops = List.of(warehouse, fastPickup, fastDelivery, longPickup, longDelivery);
        Map<Stop, Map<Stop, Trajet>> matrix = buildCompleteMatrix(allStops, 1000.0);

        Graph graph = new Graph();
        graph.setStopDepart(warehouse);
        graph.setDistancesMatrix(matrix);

        Map<String, Demand> demandMap = new HashMap<>();
        demandMap.put("DFAST", new Demand("DFAST", fastPickup.getIdNode(), fastDelivery.getIdNode(), 60, 60, null));
        // > 4h de service pour forcer la non-assignation
        demandMap.put("DLONG", new Demand("DLONG", longPickup.getIdNode(), longDelivery.getIdNode(), 10000, 6000, null));

        Map<String, List<Stop>> pickupsById = Map.of(
            "DFAST", List.of(fastPickup),
            "DLONG", List.of(longPickup)
        );
        Map<String, Stop> deliveriesById = Map.of(
            "DFAST", fastDelivery,
            "DLONG", longDelivery
        );

        Method distribute = ServiceAlgo.class.getDeclaredMethod(
            "distributeFIFO",
            List.class, Graph.class, int.class, Map.class, Map.class, Map.class, Stop.class
        );
        distribute.setAccessible(true);

        List<Stop> globalRoute = List.of(warehouse, fastPickup, fastDelivery, longPickup, longDelivery, warehouse);
        TourDistributionResult result = (TourDistributionResult) distribute.invoke(
            serviceAlgo,
            globalRoute,
            graph,
            1,
            pickupsById,
            deliveriesById,
            demandMap,
            warehouse
        );

        assertEquals(List.of("DLONG"), result.getUnassignedDemandIds(), "La demande trop longue doit être non assignée");
        assertTrue(result.getWarnings().isHasUnassignedDemands(), "Un avertissement doit être émis");
        assertEquals(1, result.getTours().size(), "Une seule tournée doit être générée pour la demande assignable");

        Stop tourPickup = result.getTours().get(0).getStops().stream()
            .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP)
            .findFirst()
            .orElseThrow();
        assertEquals("DFAST", tourPickup.getIdDemande(), "Seule la demande faisable doit être présente dans la tournée");
    }

    @Test
    void formatRouteForLog_shouldProduceReadableRepresentation() throws Exception {
        Stop warehouse = new Stop("W", null, Stop.TypeStop.WAREHOUSE);
        Stop pickup1 = new Stop("N1", "D1", Stop.TypeStop.PICKUP);
        Stop pickup2 = new Stop("N2", "D2", Stop.TypeStop.PICKUP);
        Stop delivery1 = new Stop("N3", "D1", Stop.TypeStop.DELIVERY);
        Stop delivery2 = new Stop("N4", "D2", Stop.TypeStop.DELIVERY);

        List<Stop> route = List.of(warehouse, pickup1, pickup2, delivery1, delivery2, warehouse);

        Method formatter = ServiceAlgo.class.getDeclaredMethod("formatRouteForLog", List.class);
        formatter.setAccessible(true);

        String formatted = (String) formatter.invoke(serviceAlgo, route);
        assertEquals("W → P1 → P2 → D1 → D2 → W", formatted);
    }

    // Helpers -----------------------------------------------------------------

    private Map<Stop, Map<Stop, Trajet>> buildCompleteMatrix(List<Stop> stops, double defaultDistance) {
        Map<Stop, Map<Stop, Trajet>> matrix = new HashMap<>();
        for (Stop from : stops) {
            Map<Stop, Trajet> row = new HashMap<>();
            for (Stop to : stops) {
                if (!from.equals(to)) {
                    row.put(to, new Trajet(new ArrayList<>(), from, to, defaultDistance, 0.0));
                }
            }
            matrix.put(from, row);
        }
        return matrix;
    }

    private void addDistance(Map<Stop, Map<Stop, Trajet>> matrix, Stop from, Stop to, double distance) {
        Map<Stop, Trajet> row = matrix.get(from);
        if (row == null) {
            row = new HashMap<>();
            matrix.put(from, row);
        }
        row.put(to, new Trajet(new ArrayList<>(), from, to, distance, 0.0));
    }
}

