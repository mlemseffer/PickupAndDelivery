package com.pickupdelivery.service;

import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.Node;
import com.pickupdelivery.model.Segment;
import com.pickupdelivery.xmlparser.MapXmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour MapService
 * Teste la logique métier de manière isolée
 */
class MapServiceTest {

    @Mock
    private MapXmlParser mapXmlParser;

    @InjectMocks
    private MapService mapService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parseMapFromXML_WithValidXML_ShouldParseSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            "test content".getBytes()
        );
        
        CityMap mockMap = new CityMap();
        mockMap.getNodes().add(new Node("1", 45.75, 4.85));
        mockMap.getNodes().add(new Node("2", 45.76, 4.86));
        mockMap.getSegments().add(new Segment("1", "2", 100.5, "Rue Test"));
        
        when(mapXmlParser.parseMapFromXML(any())).thenReturn(mockMap);

        // Act
        CityMap map = mapService.parseMapFromXML(file);

        // Assert
        assertNotNull(map);
        assertEquals(2, map.getNodes().size());
        assertEquals(1, map.getSegments().size());
        assertEquals("1", map.getNodes().get(0).getId());
        assertEquals(45.75, map.getNodes().get(0).getLatitude());
    }

    @Test
    void hasMap_WhenMapIsLoaded_ShouldReturnTrue() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            "test content".getBytes()
        );
        
        CityMap mockMap = new CityMap();
        mockMap.getNodes().add(new Node("1", 45.75, 4.85));
        
        when(mapXmlParser.parseMapFromXML(any())).thenReturn(mockMap);

        // Act
        mapService.parseMapFromXML(file);

        // Assert
        assertTrue(mapService.hasMap());
    }

    @Test
    void hasMap_WhenNoMapIsLoaded_ShouldReturnFalse() {
        // Assert
        assertFalse(mapService.hasMap());
    }

    @Test
    void clearMap_ShouldRemoveCurrentMap() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            "test content".getBytes()
        );
        
        CityMap mockMap = new CityMap();
        mockMap.getNodes().add(new Node("1", 45.75, 4.85));
        
        when(mapXmlParser.parseMapFromXML(any())).thenReturn(mockMap);
        
        mapService.parseMapFromXML(file);
        assertTrue(mapService.hasMap());

        // Act
        mapService.clearMap();

        // Assert
        assertFalse(mapService.hasMap());
        assertNull(mapService.getCurrentMap());
    }
}
