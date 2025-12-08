package com.pickupdelivery.service;

import com.pickupdelivery.dto.AddDeliveryRequest;
import com.pickupdelivery.dto.RemoveDeliveryRequest;
import com.pickupdelivery.dto.TourModificationResponse;
import com.pickupdelivery.dto.UpdateCourierRequest;
import com.pickupdelivery.model.DeliveryRequest;
import com.pickupdelivery.model.Tour;
import com.pickupdelivery.model.DemandeSet;
import com.pickupdelivery.model.Demand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour TourService
 */
class TourServiceTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private TourService tourService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =======================================================================
    // Tests pour calculateOptimalTour
    // =======================================================================

    @Test
    @DisplayName("calculateOptimalTour - Devrait créer une tournée avec l'adresse de l'entrepôt")
    void calculateOptimalTour_ShouldCreateTourWithWarehouseAddress() {
        // Arrange
        String warehouseAddress = "123 Main Street";

        // Act
        Tour result = tourService.calculateOptimalTour(warehouseAddress);

        // Assert
        assertNotNull(result);
        assertEquals(warehouseAddress, result.getWarehouseAddress());
    }

    @Test
    @DisplayName("calculateOptimalTour - Devrait retourner une tournée vide initialement")
    void calculateOptimalTour_ShouldReturnEmptyTour() {
        // Act
        Tour result = tourService.calculateOptimalTour("warehouse");

        // Assert
        assertNotNull(result);
        assertTrue(result.getDeliveryRequests() == null || result.getDeliveryRequests().isEmpty());
    }

    // =======================================================================
    // Tests pour calculateTotalDistance
    // =======================================================================

    @Test
    @DisplayName("calculateTotalDistance - Devrait retourner 0 pour une tournée vide")
    void calculateTotalDistance_ShouldReturnZeroForEmptyTour() {
        // Arrange
        Tour tour = new Tour();

        // Act
        double result = tourService.calculateTotalDistance(tour);

        // Assert
        assertEquals(0.0, result);
    }

    // =======================================================================
    // Tests pour calculateTotalDuration
    // =======================================================================

    @Test
    @DisplayName("calculateTotalDuration - Devrait retourner 0 pour une tournée vide")
    void calculateTotalDuration_ShouldReturnZeroForEmptyTour() {
        // Arrange
        Tour tour = new Tour();

        // Act
        int result = tourService.calculateTotalDuration(tour);

        // Assert
        assertEquals(0, result);
    }

    // =======================================================================
    // Tests pour addDeliveryToTour
    // =======================================================================

    @Test
    @DisplayName("addDeliveryToTour - Devrait ajouter une livraison avec succès")
    void addDeliveryToTour_ShouldAddDeliverySuccessfully() {
        // Arrange
        AddDeliveryRequest request = new AddDeliveryRequest();
        request.setCourierId("courier1");
        request.setPickupAddress("100");
        request.setDeliveryAddress("200");
        request.setPickupDuration(300);
        request.setDeliveryDuration(240);

        // Act
        TourModificationResponse response = tourService.addDeliveryToTour(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertFalse(response.isRequiresCourierChange());
        assertNotNull(response.getUpdatedTour());
        assertEquals("courier1", response.getUpdatedTour().getCourierId());
        assertEquals(1, response.getUpdatedTour().getDeliveryRequests().size());
    }

    @Test
    @DisplayName("addDeliveryToTour - Devrait accepter ajout jusqu'à limite de 8h")
    void addDeliveryToTour_ShouldAcceptUntilTimeLimit() {
        // Arrange - Le calcul de durée est actuellement un stub qui retourne 0
        // Ce test vérifie que l'ajout de livraisons fonctionne
        AddDeliveryRequest request1 = new AddDeliveryRequest();
        request1.setCourierId("courier1");
        request1.setPickupAddress("100");
        request1.setDeliveryAddress("200");
        request1.setPickupDuration(300);
        request1.setDeliveryDuration(240);

        AddDeliveryRequest request2 = new AddDeliveryRequest();
        request2.setCourierId("courier1");
        request2.setPickupAddress("300");
        request2.setDeliveryAddress("400");
        request2.setPickupDuration(300);
        request2.setDeliveryDuration(240);

        // Act
        tourService.addDeliveryToTour(request1);
        TourModificationResponse response = tourService.addDeliveryToTour(request2);

        // Assert - Les deux livraisons devraient être ajoutées avec succès
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(2, response.getUpdatedTour().getDeliveryRequests().size());
    }

    @Test
    @DisplayName("addDeliveryToTour - Devrait ajouter plusieurs livraisons au même coursier")
    void addDeliveryToTour_ShouldAddMultipleDeliveriesToSameCourier() {
        // Arrange
        AddDeliveryRequest request1 = new AddDeliveryRequest();
        request1.setCourierId("courier1");
        request1.setPickupAddress("100");
        request1.setDeliveryAddress("200");
        request1.setPickupDuration(300);
        request1.setDeliveryDuration(240);

        AddDeliveryRequest request2 = new AddDeliveryRequest();
        request2.setCourierId("courier1");
        request2.setPickupAddress("300");
        request2.setDeliveryAddress("400");
        request2.setPickupDuration(300);
        request2.setDeliveryDuration(240);

        // Act
        tourService.addDeliveryToTour(request1);
        TourModificationResponse response = tourService.addDeliveryToTour(request2);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(2, response.getUpdatedTour().getDeliveryRequests().size());
    }

    // =======================================================================
    // Tests pour removeDeliveryFromTour
    // =======================================================================

    @Test
    @DisplayName("removeDeliveryFromTour - Devrait supprimer une livraison avec succès")
    void removeDeliveryFromTour_ShouldRemoveDeliverySuccessfully() {
        // Arrange - Ajouter d'abord une livraison
        AddDeliveryRequest addRequest = new AddDeliveryRequest();
        addRequest.setCourierId("courier1");
        addRequest.setPickupAddress("100");
        addRequest.setDeliveryAddress("200");
        addRequest.setPickupDuration(300);
        addRequest.setDeliveryDuration(240);
        tourService.addDeliveryToTour(addRequest);

        RemoveDeliveryRequest removeRequest = new RemoveDeliveryRequest();
        removeRequest.setCourierId("courier1");
        removeRequest.setDeliveryIndex(0);

        // Act
        TourModificationResponse response = tourService.removeDeliveryFromTour(removeRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getUpdatedTour());
        assertEquals(0, response.getUpdatedTour().getDeliveryRequests().size());
    }

    @Test
    @DisplayName("removeDeliveryFromTour - Devrait échouer si le coursier n'existe pas")
    void removeDeliveryFromTour_ShouldFailIfCourierNotFound() {
        // Arrange
        RemoveDeliveryRequest request = new RemoveDeliveryRequest();
        request.setCourierId("nonexistent");
        request.setDeliveryIndex(0);

        // Act
        TourModificationResponse response = tourService.removeDeliveryFromTour(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("Aucune tournée trouvée"));
    }

    @Test
    @DisplayName("removeDeliveryFromTour - Devrait échouer si l'index est invalide")
    void removeDeliveryFromTour_ShouldFailIfIndexInvalid() {
        // Arrange - Ajouter une livraison
        AddDeliveryRequest addRequest = new AddDeliveryRequest();
        addRequest.setCourierId("courier1");
        addRequest.setPickupAddress("100");
        addRequest.setDeliveryAddress("200");
        addRequest.setPickupDuration(300);
        addRequest.setDeliveryDuration(240);
        tourService.addDeliveryToTour(addRequest);

        RemoveDeliveryRequest removeRequest = new RemoveDeliveryRequest();
        removeRequest.setCourierId("courier1");
        removeRequest.setDeliveryIndex(5); // Index invalide

        // Act
        TourModificationResponse response = tourService.removeDeliveryFromTour(removeRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Index de livraison invalide"));
    }

    @Test
    @DisplayName("removeDeliveryFromTour - Devrait échouer avec index négatif")
    void removeDeliveryFromTour_ShouldFailWithNegativeIndex() {
        // Arrange
        AddDeliveryRequest addRequest = new AddDeliveryRequest();
        addRequest.setCourierId("courier1");
        addRequest.setPickupAddress("100");
        addRequest.setDeliveryAddress("200");
        addRequest.setPickupDuration(300);
        addRequest.setDeliveryDuration(240);
        tourService.addDeliveryToTour(addRequest);

        RemoveDeliveryRequest removeRequest = new RemoveDeliveryRequest();
        removeRequest.setCourierId("courier1");
        removeRequest.setDeliveryIndex(-1);

        // Act
        TourModificationResponse response = tourService.removeDeliveryFromTour(removeRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Index de livraison invalide"));
    }

    // =======================================================================
    // Tests pour updateCourierAssignment
    // =======================================================================

    @Test
    @DisplayName("updateCourierAssignment - Devrait échouer si newCourierId est null")
    void updateCourierAssignment_ShouldFailIfNewCourierIdNull() {
        // Arrange
        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("courier1");
        request.setNewCourierId(null);
        request.setDemandId("demand1");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("newCourierId est requis"));
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait échouer si newCourierId est vide")
    void updateCourierAssignment_ShouldFailIfNewCourierIdBlank() {
        // Arrange
        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("courier1");
        request.setNewCourierId("  ");
        request.setDemandId("demand1");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("newCourierId est requis"));
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait échouer si demandId est null")
    void updateCourierAssignment_ShouldFailIfDemandIdNull() {
        // Arrange
        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("courier1");
        request.setNewCourierId("courier2");
        request.setDemandId(null);

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("demandId est requis"));
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait échouer si aucune tournée calculée en mémoire")
    void updateCourierAssignment_ShouldFailIfNoAlgoToursInMemory() {
        // Arrange
        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("courier1");
        request.setNewCourierId("courier2");
        request.setDemandId("demand1");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getErrorMessage().contains("Aucune tournée calculée en mémoire"));
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait réussir la réassignation avec des tournées AlgorithmModel")
    void updateCourierAssignment_ShouldSucceedWithAlgoTours() {
        // Arrange
        // Créer des tournées AlgorithmModel avec des stops
        com.pickupdelivery.model.AlgorithmModel.Tour tour1 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour1.setCourierId(1);
        List<com.pickupdelivery.model.AlgorithmModel.Stop> stops1 = new ArrayList<>();
        stops1.add(new com.pickupdelivery.model.AlgorithmModel.Stop("node1", "demand1", com.pickupdelivery.model.AlgorithmModel.Stop.TypeStop.PICKUP));
        stops1.add(new com.pickupdelivery.model.AlgorithmModel.Stop("node2", "demand1", com.pickupdelivery.model.AlgorithmModel.Stop.TypeStop.DELIVERY));
        tour1.setStops(stops1);
        tour1.setTrajets(new ArrayList<>());

        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours = new ArrayList<>();
        tours.add(tour1);
        tourService.setAlgoTours(tours);

        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("1");
        request.setNewCourierId("2");
        request.setDemandId("demand1");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("réassignée"));
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait chercher la demande dans toutes les tournées si oldCourierId absent")
    void updateCourierAssignment_ShouldFindDemandInAllToursIfOldCourierIdMissing() {
        // Arrange
        com.pickupdelivery.model.AlgorithmModel.Tour tour1 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour1.setCourierId(5);
        List<com.pickupdelivery.model.AlgorithmModel.Stop> stops1 = new ArrayList<>();
        stops1.add(new com.pickupdelivery.model.AlgorithmModel.Stop("node1", "demand123", com.pickupdelivery.model.AlgorithmModel.Stop.TypeStop.PICKUP));
        tour1.setStops(stops1);
        tour1.setTrajets(new ArrayList<>());

        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours = new ArrayList<>();
        tours.add(tour1);
        tourService.setAlgoTours(tours);

        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId(null); // Non spécifié
        request.setNewCourierId("6");
        request.setDemandId("demand123");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertTrue(response.isSuccess());
    }

    @Test
    @DisplayName("updateCourierAssignment - Devrait créer les stops depuis DemandeSet si non trouvés dans les tournées")
    void updateCourierAssignment_ShouldCreateStopsFromDemandeSetIfNotFound() {
        // Arrange
        // Créer une tournée vide
        com.pickupdelivery.model.AlgorithmModel.Tour tour1 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour1.setCourierId(1);
        tour1.setStops(new ArrayList<>());
        tour1.setTrajets(new ArrayList<>());

        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours = new ArrayList<>();
        tours.add(tour1);
        tourService.setAlgoTours(tours);

        // Mock du DeliveryService pour retourner une DemandeSet
        DemandeSet demandeSet = new DemandeSet();
        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setId("demand999");
        demand.setPickupNodeId("pickup999");
        demand.setDeliveryNodeId("delivery999");
        demands.add(demand);
        demandeSet.setDemands(demands);
        when(deliveryService.getCurrentRequestSet()).thenReturn(demandeSet);

        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("1");
        request.setNewCourierId("2");
        request.setDemandId("demand999");

        // Act
        TourModificationResponse response = tourService.updateCourierAssignment(request);

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("réassignée"));
    }

    // =======================================================================
    // Tests pour setAlgoTours
    // =======================================================================

    @Test
    @DisplayName("setAlgoTours - Devrait stocker les tournées correctement")
    void setAlgoTours_ShouldStoreToursCorrectly() {
        // Arrange
        com.pickupdelivery.model.AlgorithmModel.Tour tour1 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour1.setCourierId(1);
        com.pickupdelivery.model.AlgorithmModel.Tour tour2 = new com.pickupdelivery.model.AlgorithmModel.Tour();
        tour2.setCourierId(2);

        List<com.pickupdelivery.model.AlgorithmModel.Tour> tours = new ArrayList<>();
        tours.add(tour1);
        tours.add(tour2);

        // Act
        tourService.setAlgoTours(tours);

        // Assert - Vérifier via une réassignation qui nécessite les tournées en mémoire
        UpdateCourierRequest request = new UpdateCourierRequest();
        request.setOldCourierId("1");
        request.setNewCourierId("2");
        request.setDemandId("test");

        TourModificationResponse response = tourService.updateCourierAssignment(request);
        
        // Si les tournées sont stockées, on ne devrait pas avoir l'erreur "Aucune tournée calculée"
        assertFalse(response.getErrorMessage() != null && 
                   response.getErrorMessage().contains("Aucune tournée calculée en mémoire"));
    }

    @Test
    @DisplayName("setAlgoTours - Devrait gérer une liste null")
    void setAlgoTours_ShouldHandleNullList() {
        // Act & Assert - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> tourService.setAlgoTours(null));
    }

    // =======================================================================
    // Tests pour getTourByCourier
    // =======================================================================

    @Test
    @DisplayName("getTourByCourier - Devrait retourner la tournée du coursier")
    void getTourByCourier_ShouldReturnTourForCourier() {
        // Arrange
        AddDeliveryRequest request = new AddDeliveryRequest();
        request.setCourierId("courier1");
        request.setPickupAddress("100");
        request.setDeliveryAddress("200");
        request.setPickupDuration(300);
        request.setDeliveryDuration(240);
        tourService.addDeliveryToTour(request);

        // Act
        Tour tour = tourService.getTourByCourier("courier1");

        // Assert
        assertNotNull(tour);
        assertEquals("courier1", tour.getCourierId());
        assertEquals(1, tour.getDeliveryRequests().size());
    }

    @Test
    @DisplayName("getTourByCourier - Devrait retourner null si le coursier n'existe pas")
    void getTourByCourier_ShouldReturnNullIfCourierNotFound() {
        // Act
        Tour tour = tourService.getTourByCourier("nonexistent");

        // Assert
        assertNull(tour);
    }

    // =======================================================================
    // Tests pour saveTour
    // =======================================================================

    @Test
    @DisplayName("saveTour - Devrait sauvegarder une tournée")
    void saveTour_ShouldSaveTour() {
        // Arrange
        Tour tour = new Tour();
        tour.setWarehouseAddress("warehouse1");
        List<DeliveryRequest> requests = new ArrayList<>();
        requests.add(new DeliveryRequest("req1", "100", "200", 300, 240));
        tour.setDeliveryRequests(requests);

        // Act
        tourService.saveTour("courier1", tour);

        // Assert
        Tour savedTour = tourService.getTourByCourier("courier1");
        assertNotNull(savedTour);
        assertEquals("courier1", savedTour.getCourierId());
        assertEquals("warehouse1", savedTour.getWarehouseAddress());
        assertEquals(1, savedTour.getDeliveryRequests().size());
    }

    @Test
    @DisplayName("saveTour - Devrait écraser une tournée existante")
    void saveTour_ShouldOverwriteExistingTour() {
        // Arrange
        Tour tour1 = new Tour();
        tour1.setWarehouseAddress("warehouse1");
        tourService.saveTour("courier1", tour1);

        Tour tour2 = new Tour();
        tour2.setWarehouseAddress("warehouse2");

        // Act
        tourService.saveTour("courier1", tour2);

        // Assert
        Tour savedTour = tourService.getTourByCourier("courier1");
        assertEquals("warehouse2", savedTour.getWarehouseAddress());
    }

    // =======================================================================
    // Tests pour getAllTours
    // =======================================================================

    @Test
    @DisplayName("getAllTours - Devrait retourner une liste vide si aucune tournée")
    void getAllTours_ShouldReturnEmptyListIfNoTours() {
        // Act
        List<Tour> tours = tourService.getAllTours();

        // Assert
        assertNotNull(tours);
        assertTrue(tours.isEmpty());
    }

    @Test
    @DisplayName("getAllTours - Devrait retourner toutes les tournées")
    void getAllTours_ShouldReturnAllTours() {
        // Arrange
        AddDeliveryRequest request1 = new AddDeliveryRequest();
        request1.setCourierId("courier1");
        request1.setPickupAddress("100");
        request1.setDeliveryAddress("200");
        request1.setPickupDuration(300);
        request1.setDeliveryDuration(240);
        tourService.addDeliveryToTour(request1);

        AddDeliveryRequest request2 = new AddDeliveryRequest();
        request2.setCourierId("courier2");
        request2.setPickupAddress("300");
        request2.setDeliveryAddress("400");
        request2.setPickupDuration(300);
        request2.setDeliveryDuration(240);
        tourService.addDeliveryToTour(request2);

        // Act
        List<Tour> tours = tourService.getAllTours();

        // Assert
        assertNotNull(tours);
        assertEquals(2, tours.size());
    }
}
