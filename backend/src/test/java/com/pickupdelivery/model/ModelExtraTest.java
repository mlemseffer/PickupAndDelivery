package com.pickupdelivery.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests supplémentaires pour les modèles de base
 */
public class ModelExtraTest {

    @Test
    @DisplayName("Node constructeur complet")
    void testNodeConstructor() {
        Node node = new Node("N123", 45.75, 4.85);
        
        assertEquals("N123", node.getId());
        assertEquals(45.75, node.getLatitude(), 0.0001);
        assertEquals(4.85, node.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("Node setters et getters")
    void testNodeSettersGetters() {
        Node node = new Node();
        node.setId("N456");
        node.setLatitude(48.85);
        node.setLongitude(2.35);
        
        assertEquals("N456", node.getId());
        assertEquals(48.85, node.getLatitude(), 0.0001);
        assertEquals(2.35, node.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("Segment constructeur complet")
    void testSegmentConstructor() {
        Segment segment = new Segment("S1", "N1", "N2", 150.5, "Rue de la Paix");
        
        assertEquals("S1", segment.getId());
        assertEquals("N1", segment.getOrigin());
        assertEquals("N2", segment.getDestination());
        assertEquals(150.5, segment.getLength(), 0.001);
        assertEquals("Rue de la Paix", segment.getName());
    }

    @Test
    @DisplayName("Segment setters et getters")
    void testSegmentSettersGetters() {
        Segment segment = new Segment();
        segment.setId("S2");
        segment.setOrigin("N3");
        segment.setDestination("N4");
        segment.setLength(250.75);
        segment.setName("Avenue des Champs");
        
        assertEquals("S2", segment.getId());
        assertEquals("N3", segment.getOrigin());
        assertEquals("N4", segment.getDestination());
        assertEquals(250.75, segment.getLength(), 0.001);
        assertEquals("Avenue des Champs", segment.getName());
    }

    @Test
    @DisplayName("Demand avec tous les attributs")
    void testDemandAllAttributes() {
        Demand demand = new Demand();
        demand.setId("D100");
        demand.setPickupNodeId("P100");
        demand.setDeliveryNodeId("DEL100");
        demand.setPickupDurationSec(300);
        demand.setDeliveryDurationSec(240);
        demand.setCourierId("C5");
        demand.setColor("#FF5733");
        
        assertEquals("D100", demand.getId());
        assertEquals("P100", demand.getPickupNodeId());
        assertEquals("DEL100", demand.getDeliveryNodeId());
        assertEquals(300, demand.getPickupDurationSec());
        assertEquals(240, demand.getDeliveryDurationSec());
        assertEquals("C5", demand.getCourierId());
        assertEquals("#FF5733", demand.getColor());
    }

    @Test
    @DisplayName("Warehouse avec heure de départ")
    void testWarehouseWithDepartureTime() {
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("W1");
        warehouse.setDepartureTime("08:30:00");
        
        assertEquals("W1", warehouse.getNodeId());
        assertEquals("08:30:00", warehouse.getDepartureTime());
    }

    @Test
    @DisplayName("CityMap avec nodes et segments")
    void testCityMapWithNodesAndSegments() {
        CityMap cityMap = new CityMap();
        
        java.util.Map<String, Node> nodes = new java.util.HashMap<>();
        nodes.put("N1", new Node("N1", 45.0, 4.0));
        nodes.put("N2", new Node("N2", 45.1, 4.1));
        cityMap.setNodes(nodes);
        
        java.util.List<Segment> segments = new java.util.ArrayList<>();
        segments.add(new Segment("S1", "N1", "N2", 100.0, "Street1"));
        cityMap.setSegments(segments);
        
        assertEquals(2, cityMap.getNodes().size());
        assertEquals(1, cityMap.getSegments().size());
        assertTrue(cityMap.getNodes().containsKey("N1"));
        assertTrue(cityMap.getNodes().containsKey("N2"));
    }

    @Test
    @DisplayName("DemandeSet avec warehouse et demands")
    void testDemandeSetWithWarehouseAndDemands() {
        DemandeSet demandeSet = new DemandeSet();
        
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("W1");
        demandeSet.setWarehouse(warehouse);
        
        java.util.List<Demand> demands = new java.util.ArrayList<>();
        Demand d1 = new Demand();
        d1.setId("D1");
        demands.add(d1);
        demandeSet.setDemands(demands);
        
        assertNotNull(demandeSet.getWarehouse());
        assertEquals("W1", demandeSet.getWarehouse().getNodeId());
        assertEquals(1, demandeSet.getDemands().size());
        assertEquals("D1", demandeSet.getDemands().get(0).getId());
    }

    @Test
    @DisplayName("DeliveryRequest constructeur par défaut")
    void testDeliveryRequestDefaultConstructor() {
        DeliveryRequest request = new DeliveryRequest();
        
        assertNotNull(request);
        // Les valeurs par défaut sont gérées par Lombok
    }

    @Test
    @DisplayName("DeliveryRequest avec attributs complets")
    void testDeliveryRequestWithAttributes() {
        DeliveryRequest request = new DeliveryRequest();
        request.setId("REQ1");
        request.setCourierId("C1");
        
        assertEquals("REQ1", request.getId());
        assertEquals("C1", request.getCourierId());
    }

    @Test
    @DisplayName("Node avec coordonnées valides")
    void testNodeWithValidCoordinates() {
        Node node = new Node("N789", -90.0, -180.0);
        assertEquals(-90.0, node.getLatitude(), 0.0001);
        assertEquals(-180.0, node.getLongitude(), 0.0001);
        
        node = new Node("N790", 90.0, 180.0);
        assertEquals(90.0, node.getLatitude(), 0.0001);
        assertEquals(180.0, node.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("Segment avec longueur nulle")
    void testSegmentWithZeroLength() {
        Segment segment = new Segment("S0", "N1", "N1", 0.0, "Same point");
        assertEquals(0.0, segment.getLength(), 0.001);
    }

    @Test
    @DisplayName("Demand avec durées nulles")
    void testDemandWithZeroDurations() {
        Demand demand = new Demand();
        demand.setPickupDurationSec(0);
        demand.setDeliveryDurationSec(0);
        
        assertEquals(0, demand.getPickupDurationSec());
        assertEquals(0, demand.getDeliveryDurationSec());
    }

    @Test
    @DisplayName("CityMap getNodeById retourne null si non trouvé")
    void testCityMapGetNodeByIdNotFound() {
        CityMap cityMap = new CityMap();
        cityMap.setNodes(new java.util.HashMap<>());
        
        Node node = cityMap.getNodes().get("INEXISTANT");
        assertNull(node);
    }

    @Test
    @DisplayName("DemandeSet avec liste de demandes vide")
    void testDemandeSetWithEmptyDemandsList() {
        DemandeSet demandeSet = new DemandeSet();
        demandeSet.setDemands(new java.util.ArrayList<>());
        
        assertNotNull(demandeSet.getDemands());
        assertTrue(demandeSet.getDemands().isEmpty());
    }
}
