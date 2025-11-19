package com.pickupdelivery.service;

import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MapService
 * Teste la logique métier de manière isolée
 */
class MapServiceTest {

    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapService = new MapService();
    }

    @Test
    void parseMapFromXML_WithValidXML_ShouldParseSuccessfully() throws Exception {
        // Arrange
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <reseau>
                <noeud id="1" latitude="45.75" longitude="4.85"/>
                <noeud id="2" latitude="45.76" longitude="4.86"/>
                <troncon origine="1" destination="2" longueur="100.5" nomRue="Rue Test"/>
            </reseau>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            xmlContent.getBytes()
        );

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
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <reseau>
                <noeud id="1" latitude="45.75" longitude="4.85"/>
            </reseau>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            xmlContent.getBytes()
        );

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
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <reseau>
                <noeud id="1" latitude="45.75" longitude="4.85"/>
            </reseau>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xml",
            "text/xml",
            xmlContent.getBytes()
        );
        
        mapService.parseMapFromXML(file);
        assertTrue(mapService.hasMap());

        // Act
        mapService.clearMap();

        // Assert
        assertFalse(mapService.hasMap());
        assertNull(mapService.getCurrentMap());
    }
}
