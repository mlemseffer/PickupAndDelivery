package com.pickupdelivery.service;

import com.pickupdelivery.exception.ValidationException;
import com.pickupdelivery.model.*;
import com.pickupdelivery.xmlparser.DeliveryRequestXmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour DeliveryService
 * Teste la logique métier de manière isolée
 */
class DeliveryServiceTest {

    @Mock
    private DeliveryRequestXmlParser deliveryRequestXmlParser;

    @Mock
    private ValidationService validationService;

    @Mock
    private MapService mapService;

    @InjectMocks
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------------------------------------
    // 1. Tests de chargement de demandes - XML vide
    // ---------------------------------------------------------
    @Test
    void loadDeliveryRequests_WithEmptyXML_ShouldThrowException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "<demandeDeLivraisons></demandeDeLivraisons>".getBytes()
        );

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any()))
            .thenThrow(new IllegalArgumentException("❌ Format XML incorrect : aucun élément <entrepot> trouvé."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("aucun élément <entrepot> trouvé"));
    }

    // ---------------------------------------------------------
    // 2. Tests de chargement de demandes - XML mal formaté
    // ---------------------------------------------------------
    @Test
    void loadDeliveryRequests_WithInvalidXML_ShouldThrowException() throws Exception {
        // Arrange - XML avec mauvais élément racine
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "<reseau></reseau>".getBytes()
        );

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any()))
            .thenThrow(new IllegalArgumentException("❌ Format XML incorrect : le fichier doit être une demande de livraison."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("doit être une demande de livraison"));
    }

    @Test
    void loadDeliveryRequests_WithMissingAttributes_ShouldThrowException() throws Exception {
        // Arrange - Livraison avec attributs manquants
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "invalid xml".getBytes()
        );

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any()))
            .thenThrow(new IllegalArgumentException("❌ Format XML incorrect : la livraison #1 est incomplète."));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("est incomplète"));
    }

    // ---------------------------------------------------------
    // 3. Tests de chargement de demandes - Fichier OK
    // ---------------------------------------------------------
    @Test
    void loadDeliveryRequests_WithValidXML_ShouldLoadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "valid xml content".getBytes()
        );

        // Créer une carte valide
        CityMap cityMap = new CityMap();
        cityMap.getNodes().add(new Node("1", 45.75, 4.85));
        cityMap.getNodes().add(new Node("2", 45.76, 4.86));
        cityMap.getNodes().add(new Node("3", 45.77, 4.87));

        // Créer un ensemble de demandes valide
        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setId("w1");
        warehouse.setNodeId("1");
        warehouse.setDepartureTime("8:0:0");
        mockRequestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("2");
        demand1.setDeliveryNodeId("3");
        demand1.setPickupDurationSec(300);
        demand1.setDeliveryDurationSec(240);
        demands.add(demand1);
        mockRequestSet.setDemands(demands);

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(cityMap);

        // Act
        DemandeSet result = deliveryService.loadDeliveryRequests(file);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWarehouse());
        assertEquals("1", result.getWarehouse().getNodeId());
        assertEquals(1, result.getDemands().size());
        assertEquals("2", result.getDemands().get(0).getPickupNodeId());
        assertEquals("3", result.getDemands().get(0).getDeliveryNodeId());
    }

    // ---------------------------------------------------------
    // 4. Tests de validation - Demandes incompatibles avec la carte
    // ---------------------------------------------------------
    @Test
    void loadDeliveryRequests_WithNodesNotInMap_ShouldThrowValidationException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demandeGrand7.xml",
            "text/xml",
            "xml content".getBytes()
        );

        // Petite carte avec seulement 2 nœuds
        CityMap smallMap = new CityMap();
        smallMap.getNodes().add(new Node("1", 45.75, 4.85));
        smallMap.getNodes().add(new Node("2", 45.76, 4.86));

        // Demandes avec des nœuds qui n'existent pas dans la carte
        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setId("w1");
        warehouse.setNodeId("999999"); // Nœud inexistant
        warehouse.setDepartureTime("8:0:0");
        mockRequestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("888888"); // Nœud inexistant
        demand1.setDeliveryNodeId("777777"); // Nœud inexistant
        demand1.setPickupDurationSec(300);
        demand1.setDeliveryDurationSec(240);
        demands.add(demand1);
        mockRequestSet.setDemands(demands);

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(smallMap);
        
        // Simuler la validation qui échoue (void method)
        org.mockito.Mockito.doThrow(new ValidationException(
            "❌ Impossible de charger les demandes : 3 nœud(s) n'existent pas dans le plan chargé.\n\n" +
            "Nœuds manquants :\n" +
            "Entrepôt (nœud: 999999)\n" +
            "Demande #1 - Pickup (nœud: 888888)\n" +
            "Demande #1 - Delivery (nœud: 777777)\n\n" +
            "💡 Solution : Chargez un plan plus grand (ex: moyenPlan.xml ou grandPlan.xml) qui contient ces nœuds."
        )).when(validationService).validateDeliveryRequests(any(), any());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("n'existent pas dans le plan chargé"));
        assertTrue(exception.getMessage().contains("Nœuds manquants"));
        assertTrue(exception.getMessage().contains("999999"));
    }

    @Test
    void loadDeliveryRequests_WithSomeNodesNotInMap_ShouldThrowValidationException() throws Exception {
        // Arrange - Cas où seuls certains nœuds existent
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        CityMap map = new CityMap();
        map.getNodes().add(new Node("1", 45.75, 4.85));
        map.getNodes().add(new Node("2", 45.76, 4.86));
        // Nœud "3" n'existe pas

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setId("w1");
        warehouse.setNodeId("1"); // OK
        warehouse.setDepartureTime("8:0:0");
        mockRequestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("2"); // OK
        demand1.setDeliveryNodeId("3"); // N'existe pas !
        demand1.setPickupDurationSec(300);
        demand1.setDeliveryDurationSec(240);
        demands.add(demand1);
        mockRequestSet.setDemands(demands);

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(map);
        
        // Simuler la validation qui échoue (void method)
        org.mockito.Mockito.doThrow(new ValidationException(
            "❌ Impossible de charger les demandes : 1 nœud(s) n'existent pas dans le plan chargé."
        )).when(validationService).validateDeliveryRequests(any(), any());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("n'existent pas dans le plan chargé"));
    }

    // ---------------------------------------------------------
    // 5. Tests de validation - Aucune carte chargée
    // ---------------------------------------------------------
    @Test
    void loadDeliveryRequests_WithNoMapLoaded_ShouldThrowValidationException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setId("w1");
        warehouse.setNodeId("1");
        mockRequestSet.setWarehouse(warehouse);
        mockRequestSet.setDemands(new ArrayList<>());

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(null);
        
        // Simuler la validation qui échoue (void method)
        org.mockito.Mockito.doThrow(new ValidationException(
            "Aucune carte n'est chargée. Veuillez d'abord charger un plan."
        )).when(validationService).validateDeliveryRequests(any(), any());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            deliveryService.loadDeliveryRequests(file);
        });

        assertTrue(exception.getMessage().contains("Aucune carte n'est chargée"));
    }

    // ---------------------------------------------------------
    // 6. Tests getCurrentRequestSet
    // ---------------------------------------------------------
    @Test
    void getCurrentRequestSet_WhenNoRequestsLoaded_ShouldReturnNull() {
        // Act
        DemandeSet result = deliveryService.getCurrentRequestSet();

        // Assert
        assertNull(result);
    }

    @Test
    void getCurrentRequestSet_WhenRequestsLoaded_ShouldReturnRequestSet() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        CityMap cityMap = new CityMap();
        cityMap.getNodes().add(new Node("1", 45.75, 4.85));

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        mockRequestSet.setWarehouse(warehouse);
        mockRequestSet.setDemands(new ArrayList<>());

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(cityMap);

        // Act
        deliveryService.loadDeliveryRequests(file);
        DemandeSet result = deliveryService.getCurrentRequestSet();

        // Assert
        assertNotNull(result);
        assertEquals(mockRequestSet, result);
    }

    // ---------------------------------------------------------
    // 7. Tests addDeliveryRequest
    // ---------------------------------------------------------
    @Test
    void addDeliveryRequest_ShouldAddRequestToCurrentList() {
        // Arrange
        DeliveryRequest request = new DeliveryRequest();
        request.setId("req1");
        request.setPickupAddress("100");
        request.setDeliveryAddress("200");
        request.setPickupDuration(300);
        request.setDeliveryDuration(240);

        // Act
        deliveryService.addDeliveryRequest(request);

        // Assert
        DemandeSet result = deliveryService.getCurrentRequestSet();
        assertNotNull(result);
        assertEquals(1, result.getDemands().size());
        assertEquals("100", result.getDemands().get(0).getPickupNodeId());
        assertEquals("200", result.getDemands().get(0).getDeliveryNodeId());
    }

    @Test
    void addDeliveryRequest_WithoutId_ShouldGenerateUUID() {
        // Arrange
        DeliveryRequest request = new DeliveryRequest();
        request.setPickupAddress("100");
        request.setDeliveryAddress("200");
        request.setPickupDuration(300);
        request.setDeliveryDuration(240);

        // Act
        deliveryService.addDeliveryRequest(request);

        // Assert
        DemandeSet result = deliveryService.getCurrentRequestSet();
        assertNotNull(result);
        assertEquals(1, result.getDemands().size());
        assertNotNull(result.getDemands().get(0).getId());
        assertFalse(result.getDemands().get(0).getId().isEmpty());
    }

    @Test
    void addDeliveryRequest_MultipleRequests_ShouldAddAll() {
        // Arrange
        DeliveryRequest request1 = new DeliveryRequest();
        request1.setId("req1");
        request1.setPickupAddress("100");
        request1.setDeliveryAddress("200");

        DeliveryRequest request2 = new DeliveryRequest();
        request2.setId("req2");
        request2.setPickupAddress("300");
        request2.setDeliveryAddress("400");

        // Act
        deliveryService.addDeliveryRequest(request1);
        deliveryService.addDeliveryRequest(request2);

        // Assert
        DemandeSet result = deliveryService.getCurrentRequestSet();
        assertNotNull(result);
        assertEquals(2, result.getDemands().size());
    }

    // ---------------------------------------------------------
    // 8. Tests setWarehouse
    // ---------------------------------------------------------
    @Test
    void setWarehouse_WithValidNodeId_ShouldSetWarehouse() {
        // Act
        deliveryService.setWarehouse("warehouse1", "08:00");

        // Assert
        DemandeSet result = deliveryService.getCurrentRequestSet();
        assertNotNull(result);
        assertNotNull(result.getWarehouse());
        assertEquals("warehouse1", result.getWarehouse().getNodeId());
        assertEquals("08:00", result.getWarehouse().getDepartureTime());
    }

    @Test
    void setWarehouse_WithoutDepartureTime_ShouldUseDefaultTime() {
        // Act
        deliveryService.setWarehouse("warehouse1", null);

        // Assert
        DemandeSet result = deliveryService.getCurrentRequestSet();
        assertNotNull(result);
        assertEquals("08:00", result.getWarehouse().getDepartureTime());
    }

    @Test
    void setWarehouse_WithNullNodeId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryService.setWarehouse(null, "08:00");
        });
    }

    @Test
    void setWarehouse_WithEmptyNodeId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            deliveryService.setWarehouse("", "08:00");
        });
    }

    // ---------------------------------------------------------
    // 9. Tests removeDemand
    // ---------------------------------------------------------
    @Test
    void removeDemand_WithValidId_ShouldRemoveDemand() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        CityMap cityMap = new CityMap();
        cityMap.getNodes().add(new Node("1", 45.75, 4.85));
        cityMap.getNodes().add(new Node("2", 45.76, 4.86));

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        mockRequestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("1");
        demand1.setDeliveryNodeId("2");
        demands.add(demand1);
        mockRequestSet.setDemands(demands);

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(cityMap);

        deliveryService.loadDeliveryRequests(file);

        // Act
        DemandeSet result = deliveryService.removeDemand("d1");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getDemands().size());
    }

    @Test
    void removeDemand_WithInvalidId_ShouldThrowException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        CityMap cityMap = new CityMap();
        cityMap.getNodes().add(new Node("1", 45.75, 4.85));

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        mockRequestSet.setWarehouse(warehouse);
        mockRequestSet.setDemands(new ArrayList<>());

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(cityMap);

        deliveryService.loadDeliveryRequests(file);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            deliveryService.removeDemand("nonexistent");
        });
    }

    @Test
    void removeDemand_WithNullRequestSet_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            deliveryService.removeDemand("anyId");
        });
    }

    // ---------------------------------------------------------
    // 10. Tests clearRequests
    // ---------------------------------------------------------
    @Test
    void clearRequests_ShouldResetAllData() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        CityMap cityMap = new CityMap();
        cityMap.getNodes().add(new Node("1", 45.75, 4.85));

        DemandeSet mockRequestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        mockRequestSet.setWarehouse(warehouse);
        mockRequestSet.setDemands(new ArrayList<>());

        when(deliveryRequestXmlParser.parseDeliveryRequestFromXML(any())).thenReturn(mockRequestSet);
        when(mapService.getCurrentMap()).thenReturn(cityMap);

        deliveryService.loadDeliveryRequests(file);

        // Act
        deliveryService.clearRequests();

        // Assert
        assertNull(deliveryService.getCurrentRequestSet());
    }

    // ---------------------------------------------------------
    // 11. Tests parseDeliveryRequestsFromXML (méthode legacy)
    // ---------------------------------------------------------
    @Test
    void parseDeliveryRequestsFromXML_ShouldReturnListOfRequests() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        List<DeliveryRequest> mockRequests = new ArrayList<>();
        DeliveryRequest req1 = new DeliveryRequest();
        req1.setId("req1");
        mockRequests.add(req1);

        when(deliveryRequestXmlParser.parseDeliveryRequestsFromXML(any())).thenReturn(mockRequests);

        // Act
        List<DeliveryRequest> result = deliveryService.parseDeliveryRequestsFromXML(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("req1", result.get(0).getId());
    }

    @Test
    void getCurrentRequests_AfterParsing_ShouldReturnSameList() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demande.xml",
            "text/xml",
            "xml content".getBytes()
        );

        List<DeliveryRequest> mockRequests = new ArrayList<>();
        DeliveryRequest req1 = new DeliveryRequest();
        req1.setId("req1");
        mockRequests.add(req1);

        when(deliveryRequestXmlParser.parseDeliveryRequestsFromXML(any())).thenReturn(mockRequests);

        // Act
        deliveryService.parseDeliveryRequestsFromXML(file);
        List<DeliveryRequest> result = deliveryService.getCurrentRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockRequests, result);
    }
}
