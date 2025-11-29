package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MapXmlParser
 */
class MapXmlParserTest {

    private final MapXmlParser parser = new MapXmlParser();

    @Test
    void testParseGrandPlan() throws Exception {
        // Arrange
        String filePath = "c:\\Users\\Vostro 15 3000\\Desktop\\4IF\\AGILE\\moi\\PickupAndDelivery\\fichiersXMLPickupDelivery\\grandPlan.xml";
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        MultipartFile file = new MockMultipartFile(
            "file",
            "grandPlan.xml",
            "text/xml",
            content
        );

        // Act
        CityMap map = parser.parseMapFromXML(file);

        // Assert
        assertNotNull(map);
        assertNotNull(map.getNodes());
        assertNotNull(map.getSegments());
        assertTrue(map.getNodes().size() > 0, "La carte doit contenir des nœuds");
        assertTrue(map.getSegments().size() > 0, "La carte doit contenir des segments");
        
        System.out.println("✅ Nombre de nœuds : " + map.getNodes().size());
        System.out.println("✅ Nombre de segments : " + map.getSegments().size());
    }

    @Test
    void testParsePetitPlan() throws Exception {
        // Arrange
        String filePath = "c:\\Users\\Vostro 15 3000\\Desktop\\4IF\\AGILE\\moi\\PickupAndDelivery\\fichiersXMLPickupDelivery\\petitPlan.xml";
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        MultipartFile file = new MockMultipartFile(
            "file",
            "petitPlan.xml",
            "text/xml",
            content
        );

        // Act
        CityMap map = parser.parseMapFromXML(file);

        // Assert
        assertNotNull(map);
        assertTrue(map.getNodes().size() > 0);
        assertTrue(map.getSegments().size() > 0);
    }
}
