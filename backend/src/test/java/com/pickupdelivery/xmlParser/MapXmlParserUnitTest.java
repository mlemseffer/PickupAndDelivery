package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.CityMap;
import com.pickupdelivery.model.Node;
import com.pickupdelivery.model.Segment;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class MapXmlParserUnitTest {

    private final MapXmlParser parser = new MapXmlParser();

    //TODO : 
    // ---------------------------------------------------------
    // 1. HAPPY PATH : XML minimal valide
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithMinimalValidXML_ShouldParseCorrectly() throws Exception {
        String xml = """
            <reseau>
                <noeud id="1" latitude="45.0" longitude="4.0"/>
                <troncon origine="1" destination="2" longueur="100" nomRue="Rue A"/>
            </reseau>
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xml", "text/xml", xml.getBytes()
        );

        CityMap map = parser.parseMapFromXML(file);

        assertNotNull(map);
        assertEquals(1, map.getNodes().size());

        Node n = map.getNodes().get(0);
        assertEquals("1", n.getId());
        assertEquals(45.0, n.getLatitude());
        assertEquals(4.0, n.getLongitude());

        assertEquals(1, map.getSegments().size());

        Segment s = map.getSegments().get(0);
        assertEquals("1", s.getOrigin());
        assertEquals("2", s.getDestination());
        assertEquals(100, s.getLength());
        assertEquals("Rue A", s.getName());
    }

    // ---------------------------------------------------------
    // 2. XML avec plusieurs valeurs
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithMultipleNodesAndSegments_ShouldParseAll() throws Exception {
        String xml = """
            <reseau>
                <noeud id="1" latitude="1" longitude="2"/>
                <noeud id="2" latitude="3" longitude="4"/>
                <troncon origine="1" destination="2" longueur="10" nomRue="Rue 1"/>
                <troncon origine="2" destination="1" longueur="20" nomRue="Rue 2"/>
            </reseau>
        """;

        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", xml.getBytes());

        CityMap map = parser.parseMapFromXML(file);

        assertEquals(2, map.getNodes().size());
        assertEquals(2, map.getSegments().size());
    }

    // ---------------------------------------------------------
    // 3. XML vide (sans nœuds) - doit lever une exception
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithEmptyXML_ShouldThrowException() {
        String xml = "<reseau></reseau>";

        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", xml.getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parser.parseMapFromXML(file));
        
        assertTrue(exception.getMessage().contains("aucun nœud trouvé"));
    }

    // ---------------------------------------------------------
    // 4. XML avec attributs manquants
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithMissingAttributes_ShouldThrowException() {
        String xml = """
            <reseau>
                <noeud id="1" latitude="45.0"/> <!-- longitude manquante -->
            </reseau>
        """;

        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", xml.getBytes());

        assertThrows(Exception.class, () -> parser.parseMapFromXML(file));
    }

    // ---------------------------------------------------------
    // 5. XML avec valeurs incorrectes
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithInvalidNumber_ShouldThrowException() {
        String xml = """
            <reseau>
                <noeud id="1" latitude="notANumber" longitude="4.0"/>
            </reseau>
        """;

        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", xml.getBytes());

        assertThrows(Exception.class, () -> parser.parseMapFromXML(file));
    }

    // ---------------------------------------------------------
    // 6. Fichier null --> ton parser doit lever une exception
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WhenFileIsNull_ShouldThrowException() {
        assertThrows(Exception.class, () -> parser.parseMapFromXML(null));
    }

    // ---------------------------------------------------------
    // 7. XML sans les balises attendues (aucun noeud/segment) - doit lever une exception
    // ---------------------------------------------------------
    @Test
    void parseMapFromXML_WithOtherTags_ShouldThrowException() {
        String xml = """
            <reseau>
                <foo id="1"/>
                <bar origine="2"/>
            </reseau>
        """;

        MockMultipartFile file = new MockMultipartFile("file", "test.xml", "text/xml", xml.getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parser.parseMapFromXML(file));
        
        assertTrue(exception.getMessage().contains("aucun nœud trouvé"));
    }
}
