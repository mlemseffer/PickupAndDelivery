package com.pickupdelivery.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires simples pour les DTOs
 */
public class SimpleDtoTest {

    // Tests pour ApiResponse
    @Test
    @DisplayName("ApiResponse.success avec data crée une réponse réussie")
    void testApiResponseSuccessWithData() {
        String data = "Test data";
        ApiResponse<String> response = ApiResponse.success(data);
        
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    @DisplayName("ApiResponse.success avec message et data")
    void testApiResponseSuccessWithMessageAndData() {
        String message = "Custom success message";
        Integer data = 42;
        ApiResponse<Integer> response = ApiResponse.success(message, data);
        
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    @DisplayName("ApiResponse.error crée une réponse d'erreur")
    void testApiResponseError() {
        String errorMessage = "Something went wrong";
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    // Tests pour MapUploadResponse
    @Test
    @DisplayName("MapUploadResponse constructeur par défaut")
    void testMapUploadResponseDefaultConstructor() {
        MapUploadResponse response = new MapUploadResponse();
        
        assertEquals(0, response.getNodeCount());
        assertEquals(0, response.getSegmentCount());
        assertNull(response.getMapName());
    }

    @Test
    @DisplayName("MapUploadResponse constructeur complet")
    void testMapUploadResponseAllArgsConstructor() {
        MapUploadResponse response = new MapUploadResponse(100, 200, "petitPlan.xml");
        
        assertEquals(100, response.getNodeCount());
        assertEquals(200, response.getSegmentCount());
        assertEquals("petitPlan.xml", response.getMapName());
    }

    // Tests pour TourMetrics
    @Test
    @DisplayName("TourMetrics getTotalDurationHours calcule correctement")
    void testTourMetricsGetTotalDurationHours() {
        TourMetrics metrics = new TourMetrics();
        metrics.setTotalDurationSec(7200.0); // 2 heures
        
        assertEquals(2.0, metrics.getTotalDurationHours(), 0.001);
    }

    @Test
    @DisplayName("TourMetrics avec dépassement du temps limite")
    void testTourMetricsExceedsTimeLimit() {
        TourMetrics metrics = new TourMetrics();
        metrics.setCourierId(3);
        metrics.setTotalDistance(15000.0);
        metrics.setTotalDurationSec(15000.0); // > 4 heures
        metrics.setExceedsTimeLimit(true);
        
        assertTrue(metrics.isExceedsTimeLimit());
        assertTrue(metrics.getTotalDurationHours() > 4.0);
    }

    // Tests pour DistributionWarnings
    @Test
    @DisplayName("DistributionWarnings addMessage ajoute un message")
    void testDistributionWarningsAddMessage() {
        DistributionWarnings warnings = new DistributionWarnings();
        warnings.addMessage("Test warning");
        
        assertEquals(1, warnings.getMessages().size());
        assertTrue(warnings.getMessages().contains("Test warning"));
    }

    @Test
    @DisplayName("DistributionWarnings hasWarnings détecte les avertissements")
    void testDistributionWarningsHasWarnings() {
        DistributionWarnings warnings = new DistributionWarnings();
        assertFalse(warnings.hasWarnings());
        
        warnings.setHasUnassignedDemands(true);
        assertTrue(warnings.hasWarnings());
        
        warnings = new DistributionWarnings();
        warnings.setHasTimeLimitExceeded(true);
        assertTrue(warnings.hasWarnings());
        
        warnings = new DistributionWarnings();
        warnings.addMessage("Warning message");
        assertTrue(warnings.hasWarnings());
    }

    @Test
    @DisplayName("DistributionWarnings avec plusieurs messages")
    void testDistributionWarningsMultipleMessages() {
        DistributionWarnings warnings = new DistributionWarnings();
        warnings.addMessage("Warning 1");
        warnings.addMessage("Warning 2");
        warnings.addMessage("Warning 3");
        
        assertEquals(3, warnings.getMessages().size());
        assertTrue(warnings.hasWarnings());
    }

    // Tests pour AddDeliveryRequest
    @Test
    @DisplayName("AddDeliveryRequest constructeur complet")
    void testAddDeliveryRequest() {
        AddDeliveryRequest request = new AddDeliveryRequest("C1", "Address1", "Address2", 300, 240);
        
        assertEquals("C1", request.getCourierId());
        assertEquals("Address1", request.getPickupAddress());
        assertEquals("Address2", request.getDeliveryAddress());
        assertEquals(300, request.getPickupDuration());
        assertEquals(240, request.getDeliveryDuration());
    }

    @Test
    @DisplayName("AddDeliveryRequest setters")
    void testAddDeliveryRequestSetters() {
        AddDeliveryRequest request = new AddDeliveryRequest();
        request.setCourierId("C2");
        request.setPickupAddress("Pickup Addr");
        request.setDeliveryAddress("Delivery Addr");
        request.setPickupDuration(600);
        request.setDeliveryDuration(480);
        
        assertEquals("C2", request.getCourierId());
        assertEquals("Pickup Addr", request.getPickupAddress());
        assertEquals("Delivery Addr", request.getDeliveryAddress());
        assertEquals(600, request.getPickupDuration());
        assertEquals(480, request.getDeliveryDuration());
    }

    // Tests pour TourCalculationResponse
    @Test
    @DisplayName("TourCalculationResponse constructeur par défaut")
    void testTourCalculationResponseDefaultConstructor() {
        TourCalculationResponse response = new TourCalculationResponse();
        
        assertNotNull(response.getTours());
        assertNotNull(response.getUnassignedDemands());
        assertNotNull(response.getWarnings());
        assertEquals(0, response.getTourCount());
        assertEquals(0, response.getUnassignedCount());
        assertFalse(response.hasUnassignedDemands());
    }

    @Test
    @DisplayName("TourCalculationResponse addWarning")
    void testTourCalculationResponseAddWarning() {
        TourCalculationResponse response = new TourCalculationResponse();
        response.addWarning("Test warning");
        
        assertEquals(1, response.getWarnings().size());
        assertTrue(response.getWarnings().contains("Test warning"));
    }

    @Test
    @DisplayName("TourCalculationResponse hasUnassignedDemands")
    void testTourCalculationResponseHasUnassignedDemands() {
        TourCalculationResponse response = new TourCalculationResponse();
        assertFalse(response.hasUnassignedDemands());
        
        response.getUnassignedDemands().add(new com.pickupdelivery.model.Demand());
        assertTrue(response.hasUnassignedDemands());
    }

    @Test
    @DisplayName("TourCalculationResponse getTourCount")
    void testTourCalculationResponseGetTourCount() {
        TourCalculationResponse response = new TourCalculationResponse();
        assertEquals(0, response.getTourCount());
        
        response.getTours().add(new com.pickupdelivery.model.AlgorithmModel.Tour());
        assertEquals(1, response.getTourCount());
    }

    // Tests pour TourDistributionResult
    @Test
    @DisplayName("TourDistributionResult getCourierCount")
    void testTourDistributionResultGetCourierCount() {
        TourDistributionResult result = new TourDistributionResult();
        assertEquals(0, result.getCourierCount());
        
        result.getTours().add(new com.pickupdelivery.model.AlgorithmModel.Tour());
        result.getTours().add(new com.pickupdelivery.model.AlgorithmModel.Tour());
        assertEquals(2, result.getCourierCount());
    }

    @Test
    @DisplayName("TourDistributionResult getTotalDistance")
    void testTourDistributionResultGetTotalDistance() {
        TourDistributionResult result = new TourDistributionResult();
        assertEquals(0.0, result.getTotalDistance(), 0.001);
    }

    @Test
    @DisplayName("TourDistributionResult getMaxDuration")
    void testTourDistributionResultGetMaxDuration() {
        TourDistributionResult result = new TourDistributionResult();
        assertEquals(0.0, result.getMaxDuration(), 0.001);
    }

    @Test
    @DisplayName("TourDistributionResult avec métriques")
    void testTourDistributionResultWithMetrics() {
        TourDistributionResult result = new TourDistributionResult();
        TourMetrics metrics1 = new TourMetrics(1, 1000.0, 1800.0, 2, 6, false);
        TourMetrics metrics2 = new TourMetrics(2, 1500.0, 2400.0, 3, 8, false);
        
        result.getMetricsByCourier().put(1, metrics1);
        result.getMetricsByCourier().put(2, metrics2);
        
        assertEquals(2, result.getMetricsByCourier().size());
        assertEquals(1000.0, result.getMetricsByCourier().get(1).getTotalDistance(), 0.001);
        assertEquals(3, result.getMetricsByCourier().get(2).getRequestCount());
    }

    @Test
    @DisplayName("TourDistributionResult avec demandes non assignées")
    void testTourDistributionResultWithUnassignedDemands() {
        TourDistributionResult result = new TourDistributionResult();
        result.getUnassignedDemandIds().add("D1");
        result.getUnassignedDemandIds().add("D2");
        
        assertEquals(2, result.getUnassignedDemandIds().size());
        assertTrue(result.getUnassignedDemandIds().contains("D1"));
    }

    @Test
    @DisplayName("TourDistributionResult avec warnings")
    void testTourDistributionResultWithWarnings() {
        TourDistributionResult result = new TourDistributionResult();
        DistributionWarnings warnings = new DistributionWarnings();
        warnings.setHasUnassignedDemands(true);
        warnings.addMessage("Some demands could not be assigned");
        result.setWarnings(warnings);
        
        assertTrue(result.getWarnings().isHasUnassignedDemands());
        assertEquals(1, result.getWarnings().getMessages().size());
    }
}
