package com.pickupdelivery.service;

import com.pickupdelivery.model.*;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de performance pour ServiceAlgo
 * Valide l'impact du caching et de la parallélisation
 */
@SpringBootTest
class ServiceAlgoPerformanceTest {

    @Autowired
    private ServiceAlgo serviceAlgo;

    @Autowired
    private MapService mapService;

    private CityMap testCityMap;
    private StopSet testStopSet;

    @BeforeEach
    void setUp() throws Exception {
        // Charger la carte de test (petitPlan.xml)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("petitPlan.xml");
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "petitPlan.xml",
            "text/xml",
            inputStream
        );
        testCityMap = mapService.parseMapFromXML(mockFile);

        // Créer un StopSet avec plusieurs stops pour tester
        testStopSet = new StopSet();
        List<Stop> stops = new ArrayList<>();

        // Warehouse
        Stop warehouse = new Stop();
        warehouse.setIdNode("25175791");
        warehouse.setTypeStop(Stop.TypeStop.WAREHOUSE);
        stops.add(warehouse);

        // 3 demandes = 6 stops (3 pickups + 3 deliveries)
        String[][] demandNodes = {
            {"2129259178", "26086130"}, // Demande D1
            {"2129259176", "479185301"}, // Demande D2
            {"25611760", "25303831"}  // Demande D3
        };

        for (int i = 0; i < demandNodes.length; i++) {
            String demandId = "D" + (i + 1);
            
            // Pickup
            Stop pickup = new Stop();
            pickup.setIdNode(demandNodes[i][0]);
            pickup.setIdDemande(demandId);
            pickup.setTypeStop(Stop.TypeStop.PICKUP);
            stops.add(pickup);

            // Delivery
            Stop delivery = new Stop();
            delivery.setIdNode(demandNodes[i][1]);
            delivery.setIdDemande(demandId);
            delivery.setTypeStop(Stop.TypeStop.DELIVERY);
            stops.add(delivery);
        }

        testStopSet.setStops(stops);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 1: Mesurer l'impact du cache
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    void testCachePerformance_ShouldBeFasterOnSecondCall() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          TEST DE PERFORMANCE: Impact du Cache                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        // Vider le cache avant le test
        serviceAlgo.clearDijkstraCache();

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Premier appel: Cache vide (calcul complet)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n🔄 Premier appel (cache vide)...");
        long startTime1 = System.currentTimeMillis();
        Graph graph1 = serviceAlgo.buildGraph(testStopSet, testCityMap);
        long elapsedTime1 = System.currentTimeMillis() - startTime1;

        System.out.println("   Temps écoulé: " + elapsedTime1 + " ms");
        System.out.println("   " + serviceAlgo.getCacheStats());

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Deuxième appel: Cache plein (doit être plus rapide)
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        System.out.println("\n🚀 Deuxième appel (cache plein)...");
        long startTime2 = System.currentTimeMillis();
        Graph graph2 = serviceAlgo.buildGraph(testStopSet, testCityMap);
        long elapsedTime2 = System.currentTimeMillis() - startTime2;

        System.out.println("   Temps écoulé: " + elapsedTime2 + " ms");
        System.out.println("   " + serviceAlgo.getCacheStats());

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Validation: Le deuxième appel doit être BEAUCOUP plus rapide
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        double speedup = (double) elapsedTime1 / elapsedTime2;
        double improvement = ((elapsedTime1 - elapsedTime2) * 100.0) / elapsedTime1;

        System.out.println("\n📊 RÉSULTATS:");
        System.out.println("   • Premier appel:  " + elapsedTime1 + " ms");
        System.out.println("   • Deuxième appel: " + elapsedTime2 + " ms");
        System.out.println("   • Accélération:   " + String.format("%.2fx", speedup));
        System.out.println("   • Amélioration:   " + String.format("%.1f%%", improvement));

        // Le deuxième appel doit être au moins 2x plus rapide (cache hit à 100%)
        assertTrue(speedup >= 2.0, 
                   "Le cache devrait accélérer d'au moins 2x (obtenu: " + String.format("%.2fx", speedup) + ")");
        
        // Les graphes doivent être équivalents
        assertNotNull(graph1);
        assertNotNull(graph2);
        assertEquals(graph1.getDistancesMatrix().size(), graph2.getDistancesMatrix().size());

        System.out.println("\n✅ Test réussi: Le cache améliore les performances de " + String.format("%.1f%%", improvement));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 2: Vérifier que la parallélisation ne casse pas les résultats
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    void testParallelization_ShouldProduceCorrectResults() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     TEST DE PERFORMANCE: Cohérence de la Parallélisation      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        serviceAlgo.clearDijkstraCache();

        // Exécuter 5 fois et vérifier que les résultats sont cohérents
        Graph firstGraph = null;
        
        for (int i = 1; i <= 5; i++) {
            System.out.println("\n🔄 Itération " + i + "...");
            serviceAlgo.clearDijkstraCache();
            
            long startTime = System.currentTimeMillis();
            Graph graph = serviceAlgo.buildGraph(testStopSet, testCityMap);
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            System.out.println("   Temps: " + elapsedTime + " ms");
            
            assertNotNull(graph);
            assertNotNull(graph.getDistancesMatrix());
            assertEquals(7, graph.getDistancesMatrix().size()); // 7 stops au total

            if (firstGraph == null) {
                firstGraph = graph;
            } else {
                // Vérifier que les résultats sont identiques
                assertEquals(firstGraph.getDistancesMatrix().size(), 
                           graph.getDistancesMatrix().size(),
                           "Le nombre de stops doit être constant");
                
                // Vérifier quelques distances pour la cohérence
                Stop warehouse = testStopSet.getStops().get(0);
                Stop firstStop = testStopSet.getStops().get(1);
                
                double distance1 = firstGraph.getDistancesMatrix().get(warehouse).get(firstStop).getDistance();
                double distance2 = graph.getDistancesMatrix().get(warehouse).get(firstStop).getDistance();
                
                assertEquals(distance1, distance2, 0.01,
                           "Les distances doivent être identiques entre les exécutions");
            }
        }

        System.out.println("\n✅ Test réussi: La parallélisation produit des résultats cohérents");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 3: Benchmark avec plusieurs tailles de StopSet
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Test
    void testScalability_WithDifferentStopSetSizes() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          TEST DE PERFORMANCE: Scalabilité                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        int[] stopCounts = {3, 5, 7}; // Différentes tailles (warehouse + N stops)

        for (int stopCount : stopCounts) {
            serviceAlgo.clearDijkstraCache();
            
            // Créer un StopSet avec le nombre de stops souhaité
            StopSet scalabilityStopSet = new StopSet();
            List<Stop> stops = new ArrayList<>();
            
            // Warehouse
            Stop warehouse = new Stop();
            warehouse.setIdNode("25175791");
            warehouse.setTypeStop(Stop.TypeStop.WAREHOUSE);
            stops.add(warehouse);
            
            // Ajouter des stops
            String[] nodeIds = {"2129259178", "26086130", "2129259176", "479185301", "25611760", "25303831"};
            for (int i = 0; i < Math.min(stopCount - 1, nodeIds.length); i++) {
                Stop stop = new Stop();
                stop.setIdNode(nodeIds[i]);
                stop.setIdDemande("D" + (i + 1));
                stop.setTypeStop(i % 2 == 0 ? Stop.TypeStop.PICKUP : Stop.TypeStop.DELIVERY);
                stops.add(stop);
            }
            
            scalabilityStopSet.setStops(stops);
            
            // Mesurer le temps
            System.out.println("\n📊 Stops: " + stops.size());
            long startTime = System.currentTimeMillis();
            Graph graph = serviceAlgo.buildGraph(scalabilityStopSet, testCityMap);
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            int totalPaths = stops.size() * (stops.size() - 1);
            double msPerPath = (double) elapsedTime / totalPaths;
            
            System.out.println("   • Temps total: " + elapsedTime + " ms");
            System.out.println("   • Chemins calculés: " + totalPaths);
            System.out.println("   • Temps/chemin: " + String.format("%.2f ms", msPerPath));
            System.out.println("   • " + serviceAlgo.getCacheStats());
            
            assertNotNull(graph);
            assertTrue(elapsedTime < 5000, "Le calcul ne devrait pas prendre plus de 5 secondes");
        }

        System.out.println("\n✅ Test réussi: Bonne scalabilité avec différentes tailles");
    }
}
