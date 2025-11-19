package com.pickupdelivery.controller;

import com.pickupdelivery.dto.ApiResponse;
import com.pickupdelivery.dto.MapUploadResponse;
import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MapController
 * Démontre la testabilité de l'architecture en couches
 */
class MapControllerTest {

    @Mock
    private MapService mapService;

    @InjectMocks
    private MapController mapController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadMap_WithValidXMLFile_ShouldReturnSuccess() throws Exception {
        // Arrange
        String xmlContent = "<?xml version=\"1.0\"?><reseau></reseau>";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.xml", 
            "text/xml", 
            xmlContent.getBytes()
        );
        
        CityMap mockMap = new CityMap();
        when(mapService.parseMapFromXML(any())).thenReturn(mockMap);

        // Act
        ResponseEntity<ApiResponse<MapUploadResponse>> response = mapController.uploadMap(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(mapService, times(1)).parseMapFromXML(any());
    }

    @Test
    void uploadMap_WithEmptyFile_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "test.xml", 
            "text/xml", 
            new byte[0]
        );

        // Act
        ResponseEntity<ApiResponse<MapUploadResponse>> response = mapController.uploadMap(emptyFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void getCurrentMap_WhenMapExists_ShouldReturnMap() {
        // Arrange
        CityMap mockMap = new CityMap();
        when(mapService.hasMap()).thenReturn(true);
        when(mapService.getCurrentMap()).thenReturn(mockMap);

        // Act
        ResponseEntity<ApiResponse<CityMap>> response = mapController.getCurrentMap();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void getCurrentMap_WhenNoMapExists_ShouldReturnNotFound() {
        // Arrange
        when(mapService.hasMap()).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<CityMap>> response = mapController.getCurrentMap();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void clearMap_ShouldReturnSuccess() {
        // Act
        ResponseEntity<ApiResponse<Void>> response = mapController.clearMap();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(mapService, times(1)).clearMap();
    }
}
