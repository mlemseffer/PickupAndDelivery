package com.pickupdelivery.xmlparser;

import com.pickupdelivery.model.DemandeSet;
import com.pickupdelivery.model.Demand;
import com.pickupdelivery.model.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DeliveryRequestXmlParser
 * Teste le parsing et la validation des fichiers XML de demandes
 */
class DeliveryRequestXmlParserTest {

    private DeliveryRequestXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new DeliveryRequestXmlParser();
    }

    // ---------------------------------------------------------
    // Tests avec XML vide
    // ---------------------------------------------------------

    @Test
    void parseDeliveryRequestFromXML_WithEmptyXML_ShouldThrowException() {
        // Arrange - XML vide (pas de contenu)
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "".getBytes()
        );

        // Act & Assert
        assertThrows(Exception.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });
    }

    @Test
    void parseDeliveryRequestFromXML_WithEmptyDemandeDeLivraisons_ShouldThrowException() {
        // Arrange - XML avec balise racine vide
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "<demandeDeLivraisons></demandeDeLivraisons>".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("aucun élément <entrepot> trouvé"));
    }

    @Test
    void parseDeliveryRequestFromXML_WithNoEntrepot_ShouldThrowException() {
        // Arrange - XML sans entrepôt
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <livraison adresseEnlevement="1" adresseLivraison="2" dureeEnlevement="180" dureeLivraison="240"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("aucun élément <entrepot> trouvé"));
    }

    // ---------------------------------------------------------
    // Tests avec XML mal formaté
    // ---------------------------------------------------------

    @Test
    void parseDeliveryRequestFromXML_WithWrongRootElement_ShouldThrowException() {
        // Arrange - Mauvais élément racine (plan au lieu de demande)
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <reseau>
                <noeud id="1" latitude="45.75" longitude="4.85"/>
            </reseau>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("doit être une demande de livraison"));
        assertTrue(exception.getMessage().contains("<demandeDeLivraisons>"));
    }

    @Test
    void parseDeliveryRequestFromXML_WithInvalidXMLSyntax_ShouldThrowException() {
        // Arrange - XML avec erreur de syntaxe
        String xml = "<demandeDeLivraisons><entrepot</demandeDeLivraisons>";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        assertThrows(Exception.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });
    }

    @Test
    void parseDeliveryRequestFromXML_WithMissingEntrepotAttribute_ShouldThrowException() {
        // Arrange - Entrepôt sans attribut adresse
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot heureDepart="8:0:0"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("entrepôt") || 
                   exception.getMessage().contains("adresse"));
    }

    @Test
    void parseDeliveryRequestFromXML_WithMissingLivraisonAttributes_ShouldThrowException() {
        // Arrange - Livraison avec attributs manquants
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="8:0:0"/>
                <livraison adresseEnlevement="2" adresseLivraison="3"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("livraison") && 
                   exception.getMessage().contains("incomplète"));
    }

    @Test
    void parseDeliveryRequestFromXML_WithInvalidDurationFormat_ShouldThrowException() {
        // Arrange - Durée avec format invalide (non numérique)
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="8:0:0"/>
                <livraison adresseEnlevement="2" adresseLivraison="3" dureeEnlevement="abc" dureeLivraison="240"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("durée") || 
                   exception.getMessage().contains("format"));
    }

    // ---------------------------------------------------------
    // Tests avec fichier OK
    // ---------------------------------------------------------

    @Test
    void parseDeliveryRequestFromXML_WithValidXML_ShouldParseSuccessfully() throws Exception {
        // Arrange
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="342873658" heureDepart="8:0:0"/>
                <livraison adresseEnlevement="208769457" adresseLivraison="25336179" dureeEnlevement="180" dureeLivraison="240"/>
                <livraison adresseEnlevement="25336179" adresseLivraison="208769457" dureeEnlevement="300" dureeLivraison="180"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act
        DemandeSet result = parser.parseDeliveryRequestFromXML(file);

        // Assert
        assertNotNull(result);
        
        // Vérifier l'entrepôt
        Warehouse warehouse = result.getWarehouse();
        assertNotNull(warehouse);
        assertEquals("342873658", warehouse.getNodeId());
        assertEquals("8:0:0", warehouse.getDepartureTime());
        
        // Vérifier les livraisons
        assertNotNull(result.getDemands());
        assertEquals(2, result.getDemands().size());
        
        Demand demand1 = result.getDemands().get(0);
        assertEquals("208769457", demand1.getPickupNodeId());
        assertEquals("25336179", demand1.getDeliveryNodeId());
        assertEquals(180, demand1.getPickupDurationSec());
        assertEquals(240, demand1.getDeliveryDurationSec());
        
        Demand demand2 = result.getDemands().get(1);
        assertEquals("25336179", demand2.getPickupNodeId());
        assertEquals("208769457", demand2.getDeliveryNodeId());
        assertEquals(300, demand2.getPickupDurationSec());
        assertEquals(180, demand2.getDeliveryDurationSec());
    }

    @Test
    void parseDeliveryRequestFromXML_WithOneDemand_ShouldParseSuccessfully() throws Exception {
        // Arrange
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="8:0:0"/>
                <livraison adresseEnlevement="2" adresseLivraison="3" dureeEnlevement="180" dureeLivraison="240"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act
        DemandeSet result = parser.parseDeliveryRequestFromXML(file);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWarehouse());
        assertEquals("1", result.getWarehouse().getNodeId());
        assertEquals(1, result.getDemands().size());
    }

    @Test
    void parseDeliveryRequestFromXML_WithOnlyEntrepot_ShouldThrowException() {
        // Arrange - Seulement un entrepôt, sans livraisons (invalide)
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="342873658" heureDepart="8:0:0"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act & Assert - Devrait échouer car aucune livraison
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parser.parseDeliveryRequestFromXML(file);
        });

        assertTrue(exception.getMessage().contains("aucune demande de livraison trouvée") ||
                   exception.getMessage().contains("au moins un élément <livraison>"));
    }

    @Test
    void parseDeliveryRequestFromXML_WithMultipleDemands_ShouldParseAll() throws Exception {
        // Arrange - Plusieurs livraisons
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <demandeDeLivraisons>
                <entrepot adresse="1" heureDepart="8:0:0"/>
                <livraison adresseEnlevement="2" adresseLivraison="3" dureeEnlevement="180" dureeLivraison="240"/>
                <livraison adresseEnlevement="4" adresseLivraison="5" dureeEnlevement="300" dureeLivraison="180"/>
                <livraison adresseEnlevement="6" adresseLivraison="7" dureeEnlevement="120" dureeLivraison="200"/>
            </demandeDeLivraisons>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            xml.getBytes()
        );

        // Act
        DemandeSet result = parser.parseDeliveryRequestFromXML(file);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getDemands().size());
        
        // Vérifier chaque demande
        assertEquals("2", result.getDemands().get(0).getPickupNodeId());
        assertEquals("4", result.getDemands().get(1).getPickupNodeId());
        assertEquals("6", result.getDemands().get(2).getPickupNodeId());
    }
}
