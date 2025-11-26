package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.CityMap;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class MapXmlParserIntegrationTest {

    // Test d'intégration, avec un fichier réel
    @Test
    void parseRealXml_ShouldParseAllNodesAndSegments() throws Exception {

        ClassPathResource resource = new ClassPathResource("petitPlan.xml");
        assertTrue(resource.exists(), "Le fichier n'existe pas !");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "petitPlan.xml",
                "text/xml",
                resource.getInputStream()
        );

        MapXmlParser parser = new MapXmlParser();
        CityMap map = parser.parseMapFromXML(file);

        assertNotNull(map);
        assertEquals(308, map.getNodes().size());
        assertEquals(616, map.getSegments().size());
    }
}
