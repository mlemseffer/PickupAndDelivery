package com.pickupdelivery.controller;

import com.pickupdelivery.model.AlgorithmModel.*;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.DeliveryRequestSet;
import com.pickupdelivery.service.DeliveryService;
import com.pickupdelivery.service.MapService;
import com.pickupdelivery.service.ServiceAlgo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour TourController
 * Phase 6: Tests des endpoints REST pour le calcul de tournées
 */
class TourControllerTest {

    @Mock
    private ServiceAlgo serviceAlgo;

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private MapService mapService;

    @InjectMocks
    private TourController tourController;

    private CityMap mockCityMap;
    private DeliveryRequestSet mockDeliveryRequestSet;
    private StopSet mockStopSet;
    private Graph mockGraph;
    private Tour mockTour;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock objects
        mockCityMap = new CityMap();
        mockDeliveryRequestSet = new DeliveryRequestSet();
        mockStopSet = new StopSet(new ArrayList<>());
        
        // Setup mockGraph with non-null distancesMatrix
        mockGraph = new Graph();
        Map<Stop, Map<Stop, Trajet>> distancesMatrix = new HashMap<>();
        mockGraph.setDistancesMatrix(distancesMatrix);
        
        // PHASE 1: Constructeur Tour modifié - ajout de totalDurationSec
        mockTour = new Tour(new ArrayList<>(), new ArrayList<>(), 450.0, 0.0, 1);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 1: Status endpoint - Système prêt
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testGetStatus_WhenSystemReady_ShouldReturnReady() {
        // Given: Tous les éléments sont chargés
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        mockDeliveryRequestSet.getDemands().add(new com.pickupdelivery.model.Demand());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        
        // When
        var response = tourController.getStatus();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("READY", response.getBody().getData());
        
        System.out.println("✅ Test 1 réussi: Status endpoint retourne READY");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 2: Status endpoint - Système non prêt (carte manquante)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testGetStatus_WhenMapMissing_ShouldReturnNotReady() {
        // Given: Aucune carte chargée
        when(mapService.getCurrentMap()).thenReturn(null);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        
        // When
        var response = tourController.getStatus();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("NOT_READY", response.getBody().getData());
        
        System.out.println("✅ Test 2 réussi: Status retourne NOT_READY si carte manquante");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 3: Calculate tour - Carte manquante
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WhenMapMissing_ShouldReturnError() {
        // Given: Aucune carte chargée
        when(mapService.getCurrentMap()).thenReturn(null);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("carte"));
        
        System.out.println("✅ Test 3 réussi: Erreur si carte manquante");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 4: Calculate tour - Demandes manquantes
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WhenDeliveryRequestsMissing_ShouldReturnError() {
        // Given: Carte présente mais aucune demande
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(null);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("demande"));
        
        System.out.println("✅ Test 4 réussi: Erreur si demandes manquantes");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 5: Calculate tour - Entrepôt manquant
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WhenWarehouseMissing_ShouldReturnError() {
        // Given: Carte et demandes présentes mais pas d'entrepôt
        mockDeliveryRequestSet.setWarehouse(null);
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("entrepôt"));
        
        System.out.println("✅ Test 5 réussi: Erreur si entrepôt manquant");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 6: Calculate tour - Demandes vides
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WhenEmptyDemands_ShouldReturnError() {
        // Given: DeliveryRequestSet avec liste de demandes vide
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(Collections.emptyList());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Aucune demande de livraison"));
        
        System.out.println("✅ Test 6 réussi: Erreur si liste de demandes vide");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 7: Calculate tour - Succès complet
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WithValidData_ShouldReturnTour() {
        // Given: Système prêt avec données valides
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        mockDeliveryRequestSet.getDemands().add(new com.pickupdelivery.model.Demand());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        when(serviceAlgo.getStopSet(any(DeliveryRequestSet.class))).thenReturn(mockStopSet);
        when(serviceAlgo.buildGraph(any(StopSet.class), any(CityMap.class))).thenReturn(mockGraph);
        
        com.pickupdelivery.dto.TourDistributionResult distributionResult = new com.pickupdelivery.dto.TourDistributionResult();
        distributionResult.setTours(List.of(mockTour));
        when(serviceAlgo.calculateOptimalTours(any(Graph.class), eq(1))).thenReturn(distributionResult);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTours().size());
        assertEquals(450.0, response.getBody().getData().getTours().get(0).getTotalDistance());
        
        System.out.println("✅ Test 7 réussi: Calculate tour avec données valides");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 8: Calculate tour - Multi-livreurs supporté (Phase 2)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WithMultipleCouriers_ShouldReturnSuccess() {
        // Given: Multi-coursiers maintenant supporté
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        mockDeliveryRequestSet.getDemands().add(new com.pickupdelivery.model.Demand());
        
        // Créer 2 tours mockés avec tous les champs requis pour éviter NullPointerException
        List<com.pickupdelivery.model.AlgorithmModel.Tour> mockTours = new ArrayList<>();
        
        com.pickupdelivery.model.AlgorithmModel.Tour tour1 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour1.setCourierId(1);
        tour1.setStops(new ArrayList<>());  // Initialiser la liste vide
        tour1.setTrajets(new ArrayList<>()); // Initialiser la liste vide
        tour1.setTotalDistance(1000.0);
        tour1.setTotalDurationSec(600.0);
        
        com.pickupdelivery.model.AlgorithmModel.Tour tour2 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour2.setCourierId(2);
        tour2.setStops(new ArrayList<>());  // Initialiser la liste vide
        tour2.setTrajets(new ArrayList<>()); // Initialiser la liste vide
        tour2.setTotalDistance(1200.0);
        tour2.setTotalDurationSec(720.0);
        
        mockTours.add(tour1);
        mockTours.add(tour2);
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        when(serviceAlgo.getStopSet(any(DeliveryRequestSet.class))).thenReturn(mockStopSet);
        when(serviceAlgo.buildGraph(any(StopSet.class), any(CityMap.class))).thenReturn(mockGraph);
        
        com.pickupdelivery.dto.TourDistributionResult distributionResult = new com.pickupdelivery.dto.TourDistributionResult();
        distributionResult.setTours(mockTours);
        when(serviceAlgo.calculateOptimalTours(any(Graph.class), eq(2)))
                .thenReturn(distributionResult);
        
        // When
        var response = tourController.calculateTour(2);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().getTours().size());
        assertEquals(1, response.getBody().getData().getTours().get(0).getCourierId());
        assertEquals(2, response.getBody().getData().getTours().get(1).getCourierId());
        
        System.out.println("✅ Test 8 réussi: Multi-coursiers supporté");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 9: Calculate tour - Erreur inattendue
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_WhenUnexpectedError_ShouldReturnServerError() {
        // Given: Exception inattendue dans le calcul
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        mockDeliveryRequestSet.getDemands().add(new com.pickupdelivery.model.Demand());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        when(serviceAlgo.getStopSet(any(DeliveryRequestSet.class)))
                .thenThrow(new RuntimeException("Erreur de base de données"));
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Erreur"));
        
        System.out.println("✅ Test 9 réussi: Gestion erreur inattendue");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TEST 10: Calculate tour - Validation du message de succès
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Test
    void testCalculateTour_SuccessMessage_ShouldContainTiming() {
        // Given: Système prêt
        mockDeliveryRequestSet.setWarehouse(new com.pickupdelivery.model.Warehouse());
        mockDeliveryRequestSet.setDemands(new ArrayList<>());
        mockDeliveryRequestSet.getDemands().add(new com.pickupdelivery.model.Demand());
        
        when(mapService.getCurrentMap()).thenReturn(mockCityMap);
        when(deliveryService.getCurrentRequestSet()).thenReturn(mockDeliveryRequestSet);
        when(serviceAlgo.getStopSet(any(DeliveryRequestSet.class))).thenReturn(mockStopSet);
        when(serviceAlgo.buildGraph(any(StopSet.class), any(CityMap.class))).thenReturn(mockGraph);
        
        com.pickupdelivery.dto.TourDistributionResult distributionResult = new com.pickupdelivery.dto.TourDistributionResult();
        distributionResult.setTours(List.of(mockTour));
        when(serviceAlgo.calculateOptimalTours(any(Graph.class), eq(1))).thenReturn(distributionResult);
        
        // When
        var response = tourController.calculateTour(1);
        
        // Then
        assertTrue(response.getBody().getMessage().contains("succès"));
        assertTrue(response.getBody().getMessage().contains("ms"));
        
        System.out.println("✅ Test 10 réussi: Message de succès contient le timing");
    }
}
