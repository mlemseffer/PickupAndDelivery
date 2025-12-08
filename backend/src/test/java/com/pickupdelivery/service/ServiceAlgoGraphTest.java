package com.pickupdelivery.service;

import com.pickupdelivery.model.*;
import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour vérifier la création du Graph avec tous les Stops et la matrice d'adjacence
 */
class ServiceAlgoGraphTest {

    private ServiceAlgo serviceAlgo;
    private CityMap cityMap;
    private DemandeSet DemandeSet;

    @BeforeEach
    void setUp() {
        serviceAlgo = new ServiceAlgo();
        
        // Créer une carte simple pour les tests
        cityMap = createSimpleCityMap();
        
        // Créer un ensemble de demandes
        DemandeSet = createSimpleDemandeSet();
    }

    /**
     * Créer une carte simple en forme de grille 3x3
     * 
     * N1 -- N2 -- N3
     * |     |     |
     * N4 -- N5 -- N6
     * |     |     |
     * N7 -- N8 -- N9
     */
    private CityMap createSimpleCityMap() {
        List<Node> nodes = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        
        // Créer 9 nœuds en grille
        for (int i = 1; i <= 9; i++) {
            int row = (i - 1) / 3;
            int col = (i - 1) % 3;
            nodes.add(new Node("N" + i, 45.0 + row * 0.01, 4.8 + col * 0.01));
        }
        
        // Créer les segments horizontaux
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int nodeNum = row * 3 + col + 1;
                String origin = "N" + nodeNum;
                String dest = "N" + (nodeNum + 1);
                segments.add(new Segment(origin, dest, 100.0, "Rue " + origin + "-" + dest));
                segments.add(new Segment(dest, origin, 100.0, "Rue " + dest + "-" + origin));
            }
        }
        
        // Créer les segments verticaux
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 2; row++) {
                int nodeNum = row * 3 + col + 1;
                String origin = "N" + nodeNum;
                String dest = "N" + (nodeNum + 3);
                segments.add(new Segment(origin, dest, 100.0, "Rue " + origin + "-" + dest));
                segments.add(new Segment(dest, origin, 100.0, "Rue " + dest + "-" + origin));
            }
        }
        
        return new CityMap(nodes, segments);
    }

    /**
     * Créer un ensemble simple de demandes avec un warehouse et 2 demandes
     */
    private DemandeSet createSimpleDemandeSet() {
        // Warehouse au centre (N5)
        Warehouse warehouse = new Warehouse("W1", "N5", "8:0:0");
        
        List<Demand> demands = new ArrayList<>();
        
        // Demande 1 : Pickup N1, Delivery N9
        Demand demand1 = new Demand();
        demand1.setId("D1");
        demand1.setPickupNodeId("N1");
        demand1.setDeliveryNodeId("N9");
        demand1.setPickupDurationSec(60);
        demand1.setDeliveryDurationSec(60);
        demands.add(demand1);
        
        // Demande 2 : Pickup N3, Delivery N7
        Demand demand2 = new Demand();
        demand2.setId("D2");
        demand2.setPickupNodeId("N3");
        demand2.setDeliveryNodeId("N7");
        demand2.setPickupDurationSec(60);
        demand2.setDeliveryDurationSec(60);
        demands.add(demand2);
        
        return new DemandeSet(warehouse, demands);
    }

    @Test
    void testGetStopSet_shouldCreateCorrectNumberOfStops() {
        // GIVEN : Un DemandeSet avec 1 warehouse et 2 demandes
        
        // WHEN : On crée le StopSet
        StopSet stopSet = serviceAlgo.getStopSet(DemandeSet);
        
        // THEN : On doit avoir 5 stops (1 warehouse + 2 pickups + 2 deliveries)
        assertNotNull(stopSet);
        assertEquals(5, stopSet.getStops().size());
        
        // Vérifier qu'on a 1 warehouse
        long warehouseCount = stopSet.getStops().stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE)
                .count();
        assertEquals(1, warehouseCount);
        
        // Vérifier qu'on a 2 pickups
        long pickupCount = stopSet.getStops().stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.PICKUP)
                .count();
        assertEquals(2, pickupCount);
        
        // Vérifier qu'on a 2 deliveries
        long deliveryCount = stopSet.getStops().stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.DELIVERY)
                .count();
        assertEquals(2, deliveryCount);
        
        System.out.println("✅ StopSet créé avec succès : " + stopSet.getStops().size() + " stops");
        stopSet.getStops().forEach(stop -> 
            System.out.println("  - " + stop.getTypeStop() + " @ " + stop.getIdNode() + 
                             (stop.getIdDemande() != null ? " (Demande: " + stop.getIdDemande() + ")" : ""))
        );
    }

    @Test
    void testBuildGraph_shouldCreateCompleteDistanceMatrix() {
        // GIVEN : Un StopSet et une CityMap
        StopSet stopSet = serviceAlgo.getStopSet(DemandeSet);
        
        // WHEN : On construit le Graph
        Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
        
        // THEN : Le graph doit être correctement construit
        assertNotNull(graph);
        assertNotNull(graph.getStopDepart());
        assertEquals(Stop.TypeStop.WAREHOUSE, graph.getStopDepart().getTypeStop());
        
        // Vérifier la matrice de distances
        Map<Stop, Map<Stop, Trajet>> matrix = graph.getDistancesMatrix();
        assertNotNull(matrix);
        
        // Chaque stop doit avoir des trajets vers tous les autres stops (sauf lui-même)
        int numStops = stopSet.getStops().size();
        assertEquals(numStops, matrix.size());
        
        System.out.println("\n✅ Graph créé avec succès !");
        System.out.println("📍 Stop de départ (Warehouse) : " + graph.getStopDepart().getIdNode());
        System.out.println("📊 Nombre de stops dans la matrice : " + matrix.size());
        
        // Afficher la matrice de distances
        System.out.println("\n📋 MATRICE DE DISTANCES :");
        for (Stop source : matrix.keySet()) {
            Map<Stop, Trajet> destinations = matrix.get(source);
            System.out.println("\n  De " + source.getTypeStop() + " @ " + source.getIdNode() + " :");
            
            for (Stop dest : destinations.keySet()) {
                Trajet trajet = destinations.get(dest);
                System.out.printf("    → vers %s @ %s : %.2f mètres (%d segments)%n",
                    dest.getTypeStop(),
                    dest.getIdNode(),
                    trajet.getDistance(),
                    trajet.getSegments().size()
                );
            }
        }
        
        // Vérifier qu'il n'y a pas de trajet vers soi-même
        for (Stop source : matrix.keySet()) {
            Map<Stop, Trajet> destinations = matrix.get(source);
            assertEquals(numStops - 1, destinations.size(), 
                "Chaque stop doit avoir " + (numStops - 1) + " destinations (tous sauf lui-même)");
        }
    }

    @Test
    void testBuildGraph_shouldComputeCorrectDistances() {
        // GIVEN : Un StopSet et une CityMap
        StopSet stopSet = serviceAlgo.getStopSet(DemandeSet);
        
        // WHEN : On construit le Graph
        Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
        
        // THEN : Vérifier quelques distances spécifiques
        Map<Stop, Map<Stop, Trajet>> matrix = graph.getDistancesMatrix();
        
        // Trouver le warehouse
        Stop warehouse = stopSet.getStops().stream()
                .filter(s -> s.getTypeStop() == Stop.TypeStop.WAREHOUSE)
                .findFirst()
                .orElseThrow();
        
        // Vérifier que tous les trajets depuis le warehouse sont valides
        Map<Stop, Trajet> trajetsFromWarehouse = matrix.get(warehouse);
        assertNotNull(trajetsFromWarehouse);
        
        for (Trajet trajet : trajetsFromWarehouse.values()) {
            assertTrue(trajet.getDistance() > 0, "La distance doit être positive");
            assertFalse(trajet.getSegments().isEmpty(), "Le trajet doit contenir des segments");
            assertEquals(warehouse, trajet.getStopDepart(), "Le stop de départ doit être le warehouse");
        }
        
        System.out.println("\n✅ Toutes les distances sont valides et positives !");
    }

    @Test
    void testBuildGraph_performanceTest() {
        // Test de performance : mesurer le temps de construction du graph
        StopSet stopSet = serviceAlgo.getStopSet(DemandeSet);
        
        long startTime = System.currentTimeMillis();
        Graph graph = serviceAlgo.buildGraph(stopSet, cityMap);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        
        System.out.println("\n⏱️  PERFORMANCE :");
        System.out.println("  Nombre de stops : " + stopSet.getStops().size());
        System.out.println("  Nombre de nœuds dans la carte : " + cityMap.getNodes().size());
        System.out.println("  Nombre de segments : " + cityMap.getSegments().size());
        System.out.println("  Temps de construction du Graph : " + duration + " ms");
        
        // Le temps devrait être raisonnable (< 5 secondes pour une petite carte)
        assertTrue(duration < 5000, "La construction du graph ne devrait pas prendre plus de 5 secondes");
    }
}
