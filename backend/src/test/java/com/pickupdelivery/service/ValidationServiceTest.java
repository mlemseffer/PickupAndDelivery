package com.pickupdelivery.service;

import com.pickupdelivery.exception.ValidationException;
import com.pickupdelivery.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ValidationService
 */
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // ---------------------------------------------------------
    // Tests validateDeliveryRequests - Cas valides
    // ---------------------------------------------------------
    @Test
    @DisplayName("Validation réussie avec des nœuds existants")
    void validateDeliveryRequests_WithValidNodes_ShouldPass() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        DemandeSet requestSet = createRequestSet("1", "2", "3");

        // Act & Assert - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> validationService.validateDeliveryRequests(requestSet, cityMap));
    }

    @Test
    @DisplayName("Validation réussie avec une seule demande")
    void validateDeliveryRequests_WithSingleDemand_ShouldPass() {
        // Arrange
        CityMap cityMap = createCityMap("100", "200", "300");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("100");
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setId("d1");
        demand.setPickupNodeId("200");
        demand.setDeliveryNodeId("300");
        demands.add(demand);
        requestSet.setDemands(demands);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateDeliveryRequests(requestSet, cityMap));
    }

    // ---------------------------------------------------------
    // Tests validateDeliveryRequests - Cas d'erreurs
    // ---------------------------------------------------------
    @Test
    @DisplayName("Validation échoue si la carte est null")
    void validateDeliveryRequests_WithNullMap_ShouldThrowException() {
        // Arrange
        DemandeSet requestSet = createRequestSet("1", "2", "3");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, null);
        });

        assertTrue(exception.getMessage().contains("Aucune carte n'est chargée"));
    }

    @Test
    @DisplayName("Validation échoue si le requestSet est null")
    void validateDeliveryRequests_WithNullRequestSet_ShouldThrowException() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(null, cityMap);
        });

        assertTrue(exception.getMessage().contains("L'ensemble de demandes est null"));
    }

    @Test
    @DisplayName("Validation échoue si la carte est vide")
    void validateDeliveryRequests_WithEmptyMap_ShouldThrowException() {
        // Arrange
        CityMap emptyMap = new CityMap();
        emptyMap.setNodes(new ArrayList<>());
        DemandeSet requestSet = createRequestSet("1", "2", "3");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, emptyMap);
        });

        assertTrue(exception.getMessage().contains("Aucune carte n'est chargée"));
    }

    @Test
    @DisplayName("Validation échoue si le nœud de l'entrepôt n'existe pas")
    void validateDeliveryRequests_WithInvalidWarehouseNode_ShouldThrowException() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        DemandeSet requestSet = createRequestSet("999", "2", "3"); // Entrepôt invalide

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, cityMap);
        });

        assertTrue(exception.getMessage().contains("n'existent pas dans le plan chargé"));
        assertTrue(exception.getMessage().contains("Entrepôt (nœud: 999)"));
    }

    @Test
    @DisplayName("Validation échoue si un nœud de pickup n'existe pas")
    void validateDeliveryRequests_WithInvalidPickupNode_ShouldThrowException() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setId("d1");
        demand.setPickupNodeId("999"); // Pickup invalide
        demand.setDeliveryNodeId("3");
        demands.add(demand);
        requestSet.setDemands(demands);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, cityMap);
        });

        assertTrue(exception.getMessage().contains("n'existent pas dans le plan chargé"));
        assertTrue(exception.getMessage().contains("Demande #1 - Pickup (nœud: 999)"));
    }

    @Test
    @DisplayName("Validation échoue si un nœud de delivery n'existe pas")
    void validateDeliveryRequests_WithInvalidDeliveryNode_ShouldThrowException() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setId("d1");
        demand.setPickupNodeId("2");
        demand.setDeliveryNodeId("888"); // Delivery invalide
        demands.add(demand);
        requestSet.setDemands(demands);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, cityMap);
        });

        assertTrue(exception.getMessage().contains("n'existent pas dans le plan chargé"));
        assertTrue(exception.getMessage().contains("Demande #1 - Delivery (nœud: 888)"));
    }

    @Test
    @DisplayName("Validation échoue si plusieurs nœuds n'existent pas")
    void validateDeliveryRequests_WithMultipleInvalidNodes_ShouldThrowException() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("999"); // Invalide
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("888"); // Invalide
        demand1.setDeliveryNodeId("777"); // Invalide
        demands.add(demand1);
        requestSet.setDemands(demands);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateDeliveryRequests(requestSet, cityMap);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("3 nœud(s) n'existent pas"));
        assertTrue(message.contains("Entrepôt"));
        assertTrue(message.contains("Pickup"));
        assertTrue(message.contains("Delivery"));
    }

    // ---------------------------------------------------------
    // Tests countValidDemands
    // ---------------------------------------------------------
    @Test
    @DisplayName("Compte toutes les demandes valides")
    void countValidDemands_WithAllValidDemands_ShouldReturnCount() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3", "4");
        DemandeSet requestSet = createRequestSetWithMultipleDemands(cityMap);

        // Act
        int count = validationService.countValidDemands(requestSet, cityMap);

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Compte 0 si toutes les demandes sont invalides")
    void countValidDemands_WithInvalidDemands_ShouldReturnZero() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2");
        
        DemandeSet requestSet = new DemandeSet();
        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setPickupNodeId("999");
        demand.setDeliveryNodeId("888");
        demands.add(demand);
        requestSet.setDemands(demands);

        // Act
        int count = validationService.countValidDemands(requestSet, cityMap);

        // Assert
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Compte 0 si requestSet est null")
    void countValidDemands_WithNullRequestSet_ShouldReturnZero() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2");

        // Act
        int count = validationService.countValidDemands(null, cityMap);

        // Assert
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Compte 0 si cityMap est null")
    void countValidDemands_WithNullMap_ShouldReturnZero() {
        // Arrange
        DemandeSet requestSet = createRequestSet("1", "2", "3");

        // Act
        int count = validationService.countValidDemands(requestSet, null);

        // Assert
        assertEquals(0, count);
    }

    // ---------------------------------------------------------
    // Tests filterValidDemands
    // ---------------------------------------------------------
    @Test
    @DisplayName("Filtre correctement les demandes valides")
    void filterValidDemands_ShouldKeepOnlyValidDemands() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        
        Demand validDemand = new Demand();
        validDemand.setId("valid");
        validDemand.setPickupNodeId("2");
        validDemand.setDeliveryNodeId("3");
        demands.add(validDemand);
        
        Demand invalidDemand = new Demand();
        invalidDemand.setId("invalid");
        invalidDemand.setPickupNodeId("999");
        invalidDemand.setDeliveryNodeId("888");
        demands.add(invalidDemand);
        
        requestSet.setDemands(demands);

        // Act
        DemandeSet filtered = validationService.filterValidDemands(requestSet, cityMap);

        // Assert
        assertNotNull(filtered);
        assertEquals(1, filtered.getDemands().size());
        assertEquals("valid", filtered.getDemands().get(0).getId());
    }

    @Test
    @DisplayName("Filtre supprime l'entrepôt invalide")
    void filterValidDemands_WithInvalidWarehouse_ShouldRemoveWarehouse() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2", "3");
        
        DemandeSet requestSet = new DemandeSet();
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("999"); // Invalide
        requestSet.setWarehouse(warehouse);
        requestSet.setDemands(new ArrayList<>());

        // Act
        DemandeSet filtered = validationService.filterValidDemands(requestSet, cityMap);

        // Assert
        assertNotNull(filtered);
        assertNull(filtered.getWarehouse());
    }

    @Test
    @DisplayName("Filtre retourne le requestSet si null")
    void filterValidDemands_WithNullRequestSet_ShouldReturnNull() {
        // Arrange
        CityMap cityMap = createCityMap("1", "2");

        // Act
        DemandeSet filtered = validationService.filterValidDemands(null, cityMap);

        // Assert
        assertNull(filtered);
    }

    @Test
    @DisplayName("Filtre retourne le requestSet si cityMap est null")
    void filterValidDemands_WithNullMap_ShouldReturnOriginal() {
        // Arrange
        DemandeSet requestSet = createRequestSet("1", "2", "3");

        // Act
        DemandeSet filtered = validationService.filterValidDemands(requestSet, null);

        // Assert
        assertEquals(requestSet, filtered);
    }

    // ---------------------------------------------------------
    // Méthodes utilitaires
    // ---------------------------------------------------------
    private CityMap createCityMap(String... nodeIds) {
        CityMap cityMap = new CityMap();
        List<Node> nodes = new ArrayList<>();
        for (String id : nodeIds) {
            nodes.add(new Node(id, 45.75, 4.85));
        }
        cityMap.setNodes(nodes);
        return cityMap;
    }

    private DemandeSet createRequestSet(String warehouseNodeId, String pickupNodeId, String deliveryNodeId) {
        DemandeSet requestSet = new DemandeSet();
        
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId(warehouseNodeId);
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        Demand demand = new Demand();
        demand.setId("d1");
        demand.setPickupNodeId(pickupNodeId);
        demand.setDeliveryNodeId(deliveryNodeId);
        demands.add(demand);
        requestSet.setDemands(demands);

        return requestSet;
    }

    private DemandeSet createRequestSetWithMultipleDemands(CityMap cityMap) {
        DemandeSet requestSet = new DemandeSet();
        
        Warehouse warehouse = new Warehouse();
        warehouse.setNodeId("1");
        requestSet.setWarehouse(warehouse);

        List<Demand> demands = new ArrayList<>();
        
        Demand demand1 = new Demand();
        demand1.setId("d1");
        demand1.setPickupNodeId("2");
        demand1.setDeliveryNodeId("3");
        demands.add(demand1);
        
        Demand demand2 = new Demand();
        demand2.setId("d2");
        demand2.setPickupNodeId("3");
        demand2.setDeliveryNodeId("4");
        demands.add(demand2);
        
        requestSet.setDemands(demands);
        return requestSet;
    }
}
