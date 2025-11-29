package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MapXmlParser
 */
class MapXmlParserTest {

    private final MapXmlParser parser = new MapXmlParser();

    @Test
    void testParseGrandPlan() throws Exception {
        // Arrange
        ClassPathResource resource = new ClassPathResource("fichiersXMLPickupDelivery/grandPlan.xml");
        byte[] content = resource.getInputStream().readAllBytes();
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
        ClassPathResource resource = new ClassPathResource("fichiersXMLPickupDelivery/petitPlan.xml");
        byte[] content = resource.getInputStream().readAllBytes();
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
