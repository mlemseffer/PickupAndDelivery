package com.pickupdelivery.service;

import com.pickupdelivery.model.AlgorithmModel.Graph;
import com.pickupdelivery.model.AlgorithmModel.Stop;
import com.pickupdelivery.model.AlgorithmModel.StopSet;
import com.pickupdelivery.model.AlgorithmModel.Tour;
import com.pickupdelivery.model.AlgorithmModel.Trajet;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validation pour la distribution FIFO multi-coursiers (Phase 2)
 * 
 * Couvre:
 * - Compatibilité avec mono-coursier (courierCount = 1)
 * - Distribution multi-coursiers (courierCount > 1)
 * - Respect de la contrainte temporelle (4 heures max)
 * - Ordre FIFO strict
 * - Contrainte de précédence (pickup avant delivery)
 * - Paires indivisibles (pickup et delivery dans la même tournée)
 * - Cas limites et edge cases
 */
@SpringBootTest
public class ServiceAlgoMultiCourierTest {

    @Autowired
    private ServiceAlgo serviceAlgo;

    private Graph testGraph;
    private static final double TIME_LIMIT_SEC = 4 * 3600; // 4 heures en secondes

    @BeforeEach
    void setUp() {
        // Créer un graphe de test simple avec 3 demandes
        testGraph = createTestGraph();
    }

    /**
     * Crée un graphe de test avec 3 demandes (6 stops + 2 warehouses)
     */
    private Graph createTestGraph() {
        Graph graph = new Graph();
        
        // Créer stops
        Stop warehouse = new Stop("W", null, Stop.TypeStop.WAREHOUSE);
        Stop p1 = new Stop("P1", "D1", Stop.TypeStop.PICKUP);
        Stop d1 = new Stop("D1_delivery", "D1", Stop.TypeStop.DELIVERY);
        Stop p2 = new Stop("P2", "D2", Stop.TypeStop.PICKUP);
        Stop d2 = new Stop("D2_delivery", "D2", Stop.TypeStop.DELIVERY);
        Stop p3 = new Stop("P3", "D3", Stop.TypeStop.PICKUP);
        Stop d3 = new Stop("D3_delivery", "D3", Stop.TypeStop.DELIVERY);
        
        List<Stop> stops = Arrays.asList(warehouse, p1, d1, p2, d2, p3, d3);
        
        // Créer demandes avec le bon constructeur (6 paramètres)
        Map<String, Demand> demandMap = new HashMap<>();
        demandMap.put("D1", new Demand("D1", "P1", "D1_delivery", 300, 300, null));
        demandMap.put("D2", new Demand("D2", "P2", "D2_delivery", 300, 300, null));
        demandMap.put("D3", new Demand("D3", "P3", "D3_delivery", 300, 300, null));
        
        graph.setDemandMap(demandMap);
        
        // Créer matrice de distances (simplified)
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
        for (Stop from : stops) {
            Map<Stop, Trajet> row = new HashMap<>();
            for (Stop to : stops) {
                if (from != to) {
                    List<com.pickupdelivery.model.Segment> segments = new ArrayList<>();
                    // Créer un segment vide pour les tests
                    Trajet trajet = new Trajet(segments, from, to, 1000.0, 240.0); // 1 km, 4 min
                    row.put(to, trajet);
                }
            }
            distancesMatrix.put(from, row);
        }
        
        graph.setDistancesMatrix(distancesMatrix);
        
        return graph;
    }

    // =========================================================================
    // TESTS DE VALIDATION DES PARAMÈTRES
    // =========================================================================

    @Test
    @DisplayName("Validation: courierCount = 0 doit lever une exception")
    void testCalculateOptimalTours_InvalidCourierCount_Zero() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> serviceAlgo.calculateOptimalTours(testGraph, 0)
        );
        
        assertTrue(exception.getMessage().contains("entre 1 et 10"),
            "Le message d'erreur doit mentionner la plage valide");
    }

    @Test
    @DisplayName("Validation: courierCount = 11 doit lever une exception")
    void testCalculateOptimalTours_InvalidCourierCount_Eleven() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> serviceAlgo.calculateOptimalTours(testGraph, 11)
        );
        
        assertTrue(exception.getMessage().contains("entre 1 et 10"),
            "Le message d'erreur doit mentionner la plage valide");
    }

    @Test
    @DisplayName("Validation: courierCount négatif doit lever une exception")
    void testCalculateOptimalTours_InvalidCourierCount_Negative() {
        // When & Then
        assertThrows(
            IllegalArgumentException.class,
            () -> serviceAlgo.calculateOptimalTours(testGraph, -1)
        );
    }

    // =========================================================================
    // TESTS DE COMPATIBILITÉ MONO-COURSIER
    // =========================================================================

    @Test
    @DisplayName("1 coursier: doit retourner exactement 1 tournée")
    void testCalculateOptimalTours_OneCourier_ReturnsOneTour() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 1);
        List<Tour> tours = result.getTours();
        
        // Then
        assertNotNull(tours);
        assertEquals(1, tours.size(), "Doit retourner exactement 1 tournée");
        
        Tour tour = tours.get(0);
        assertNotNull(tour);
        assertEquals(1, tour.getCourierId(), "Doit avoir courierId = 1");
    }

    @Test
    @DisplayName("1 coursier: vérifier structure de base de la tournée")
    void testCalculateOptimalTours_OneCourier_BasicStructure() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 1);
        List<Tour> tours = result.getTours();
        
        // Then
        Tour tour = tours.get(0);
        assertTrue(tour.getStops().size() >= 3, "Doit avoir au moins warehouse + 1 stop + warehouse");
        assertTrue(tour.getTotalDistance() > 0, "Distance doit être positive");
        assertTrue(tour.getTotalDurationSec() > 0, "Durée doit être positive");
    }

    // =========================================================================
    // TESTS MULTI-COURSIERS
    // =========================================================================

    @Test
    @DisplayName("2 coursiers: doit créer 1 à 2 tournées")
    void testCalculateOptimalTours_TwoCouriers_ValidRange() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 2);
        List<Tour> tours = result.getTours();
        
        // Then
        assertNotNull(tours);
        assertTrue(tours.size() >= 1, "Doit créer au moins 1 tournée");
        assertTrue(tours.size() <= 2, "Ne doit pas dépasser 2 tournées");
    }

    @Test
    @DisplayName("Multi-coursiers: courierIds doivent être uniques")
    void testCalculateOptimalTours_MultiCourier_UniqueCourierIds() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        Set<Integer> courierIds = tours.stream()
            .map(Tour::getCourierId)
            .collect(Collectors.toSet());
        assertEquals(tours.size(), courierIds.size(), 
            "Chaque tournée doit avoir un courierId unique");
    }

    @Test
    @DisplayName("Multi-coursiers: courierIds doivent être séquentiels (FIFO)")
    void testCalculateOptimalTours_MultiCourier_SequentialIds() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 5);
        List<Tour> tours = result.getTours();
        
        // Then
        for (int i = 0; i < tours.size(); i++) {
            assertEquals(i + 1, tours.get(i).getCourierId(),
                "Les courierIds doivent être 1, 2, 3, ... (ordre FIFO)");
        }
    }

    // =========================================================================
    // TESTS DE CONTRAINTE TEMPORELLE
    // =========================================================================

    @Test
    @DisplayName("Multi-coursiers: aucune tournée ne doit dépasser 4 heures")
    void testCalculateOptimalTours_MultiCourier_TimeLimitRespected() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        for (Tour tour : tours) {
            assertFalse(tour.exceedsTimeLimit(), 
                "La tournée du coursier " + tour.getCourierId() + 
                " dépasse la limite de 4h: " + tour.getTotalDurationHours() + "h");
            assertTrue(tour.getTotalDurationSec() <= TIME_LIMIT_SEC,
                "Durée doit être <= 14400 secondes (4h)");
        }
    }

    // =========================================================================
    // TESTS DE PRÉCÉDENCE ET PAIRES
    // =========================================================================

    @Test
    @DisplayName("Précédence: pickup avant delivery dans toutes les tournées")
    void testCalculateOptimalTours_PrecedenceRespected() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        for (Tour tour : tours) {
            assertPrecedenceRespected(tour);
        }
    }

    @Test
    @DisplayName("Paires: pickup et delivery dans la même tournée")
    void testCalculateOptimalTours_PairsNotSplit() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        Map<String, Demand> demandMap = testGraph.getDemandMap();
        
        for (String demandId : demandMap.keySet()) {
            int tourWithPickup = -1;
            int tourWithDelivery = -1;
            
            for (int i = 0; i < tours.size(); i++) {
                Tour tour = tours.get(i);
                for (Stop stop : tour.getStops()) {
                    if (demandId.equals(stop.getIdDemande())) {
                        if (stop.getTypeStop() == Stop.TypeStop.PICKUP) {
                            tourWithPickup = i;
                        } else if (stop.getTypeStop() == Stop.TypeStop.DELIVERY) {
                            tourWithDelivery = i;
                        }
                    }
                }
            }
            
            if (tourWithPickup >= 0 && tourWithDelivery >= 0) {
                assertEquals(tourWithPickup, tourWithDelivery,
                    "Le pickup et delivery de " + demandId + " doivent être dans la même tournée");
            }
        }
    }

    // =========================================================================
    // TESTS DE COHÉRENCE
    // =========================================================================

    @Test
    @DisplayName("Cohérence: chaque tournée commence et finit au warehouse")
    void testCalculateOptimalTours_ToursStartEndAtWarehouse() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        for (Tour tour : tours) {
            List<Stop> stops = tour.getStops();
            assertTrue(stops.size() >= 2, "Tournée doit avoir au moins 2 stops");
            
            Stop firstStop = stops.get(0);
            Stop lastStop = stops.get(stops.size() - 1);
            
            assertEquals(Stop.TypeStop.WAREHOUSE, firstStop.getTypeStop(),
                "Premier stop doit être le warehouse");
            assertEquals(Stop.TypeStop.WAREHOUSE, lastStop.getTypeStop(),
                "Dernier stop doit être le warehouse");
        }
    }

    @Test
    @DisplayName("Métriques: distance totale positive")
    void testCalculateOptimalTours_TotalDistancePositive() {
        // When
        com.pickupdelivery.dto.TourDistributionResult result = serviceAlgo.calculateOptimalTours(testGraph, 3);
        List<Tour> tours = result.getTours();
        
        // Then
        double totalDistance = tours.stream()
            .mapToDouble(Tour::getTotalDistance)
            .sum();
        
        assertTrue(totalDistance > 0, "Distance totale cumulée doit être positive");
    }

    // =========================================================================
    // MÉTHODES AUXILIAIRES
    // =========================================================================

    /**
     * Vérifie que les contraintes de précédence sont respectées dans une tournée
     * (pickup avant delivery pour chaque demande)
     */
    private void assertPrecedenceRespected(Tour tour) {
        Map<String, Integer> pickupIndices = new HashMap<>();
        Map<String, Integer> deliveryIndices = new HashMap<>();
        
        List<Stop> stops = tour.getStops();
        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            String demandId = stop.getIdDemande();
            
            if (demandId != null) {
                if (stop.getTypeStop() == Stop.TypeStop.PICKUP) {
                    pickupIndices.put(demandId, i);
                } else if (stop.getTypeStop() == Stop.TypeStop.DELIVERY) {
                    deliveryIndices.put(demandId, i);
                }
            }
        }
        
        // Vérifier que chaque delivery a son pickup avant
        for (String demandId : deliveryIndices.keySet()) {
            assertTrue(pickupIndices.containsKey(demandId),
                "Delivery " + demandId + " doit avoir un pickup correspondant");
            
            int pickupIndex = pickupIndices.get(demandId);
            int deliveryIndex = deliveryIndices.get(demandId);
            
            assertTrue(pickupIndex < deliveryIndex,
                "Pickup " + demandId + " doit être avant delivery");
        }
    }
}
