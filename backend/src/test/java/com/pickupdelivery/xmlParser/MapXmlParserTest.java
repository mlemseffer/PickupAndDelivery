package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.Test;
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
        ClassLoader classLoader = getClass().getClassLoader();
        java.io.InputStream inputStream = classLoader.getResourceAsStream("grandPlan.xml");
        assertNotNull(inputStream, "Le fichier grandPlan.xml doit être présent dans src/test/resources");
        byte[] content = inputStream.readAllBytes();
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
        ClassLoader classLoader = getClass().getClassLoader();
        java.io.InputStream inputStream = classLoader.getResourceAsStream("petitPlan.xml");
        assertNotNull(inputStream, "Le fichier petitPlan.xml doit être présent dans src/test/resources");
        byte[] content = inputStream.readAllBytes();
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
