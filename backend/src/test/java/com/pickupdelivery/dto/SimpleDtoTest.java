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
}
