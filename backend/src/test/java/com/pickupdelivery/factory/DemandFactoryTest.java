package com.pickupdelivery.factory;

import com.pickupdelivery.model.Demand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DemandFactory
 */
class DemandFactoryTest {

    @Test
    void testCreateDemand_Valid() {
        // Arrange
        String id = "demand-123";
        String pickupNodeId = "node-456";
        String deliveryNodeId = "node-789";
        int pickupDuration = 300;
        int deliveryDuration = 480;
        String courierId = "courier-1";
        String color = "#FF6B6B";

        // Act
        Demand demand = DemandFactory.createDemand(
            id, pickupNodeId, deliveryNodeId, 
            pickupDuration, deliveryDuration, 
            courierId, color
        );

        // Assert
        assertNotNull(demand);
        assertEquals(id, demand.getId());
        assertEquals(pickupNodeId, demand.getPickupNodeId());
        assertEquals(deliveryNodeId, demand.getDeliveryNodeId());
        assertEquals(pickupDuration, demand.getPickupDurationSec());
        assertEquals(deliveryDuration, demand.getDeliveryDurationSec());
        assertEquals(courierId, demand.getCourierId());
        assertEquals(color, demand.getColor());
    }

    @Test
    void testCreateDemand_WithoutColor_UsesDefault() {
        // Act
        Demand demand = DemandFactory.createDemand(
            "demand-1", "node-1", "node-2", 300, 480, null
        );

        // Assert
        assertNotNull(demand);
        assertEquals("#FF6B6B", demand.getColor());
    }

    @Test
    void testCreateDemand_NullId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand(null, "n1", "n2", 300, 480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("identifiant"));
    }

    @Test
    void testCreateDemand_NullPickupNodeId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", null, "n2", 300, 480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("pickup"));
    }

    @Test
    void testCreateDemand_NullDeliveryNodeId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", null, 300, 480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("delivery"));
    }

    @Test
    void testCreateDemand_SamePickupAndDelivery_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", "n1", 300, 480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("différents"));
    }

    @Test
    void testCreateDemand_NegativePickupDuration_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", "n2", -100, 480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("pickup"));
        assertTrue(exception.getMessage().contains("négative"));
    }

    @Test
    void testCreateDemand_ZeroPickupDuration_IsValid() {
        // La durée peut être 0 (pas d'attente)
        // Act
        Demand demand = DemandFactory.createDemand("d1", "n1", "n2", 0, 480, null, "#FF6B6B");
        
        // Assert
        assertNotNull(demand);
        assertEquals(0, demand.getPickupDurationSec());
    }

    @Test
    void testCreateDemand_NegativeDeliveryDuration_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", "n2", 300, -480, null, "#FF6B6B")
        );
        assertTrue(exception.getMessage().contains("delivery"));
        assertTrue(exception.getMessage().contains("négative"));
    }

    @Test
    void testCreateDemand_ZeroDeliveryDuration_IsValid() {
        // La durée peut être 0 (pas d'attente)
        // Act
        Demand demand = DemandFactory.createDemand("d1", "n1", "n2", 300, 0, null, "#FF6B6B");
        
        // Assert
        assertNotNull(demand);
        assertEquals(0, demand.getDeliveryDurationSec());
    }

    @Test
    void testCreateDemand_InvalidColorFormat_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", "n2", 300, 480, null, "FF6B6B")
        );
        assertTrue(exception.getMessage().contains("couleur"));
    }

    @Test
    void testCreateDemand_InvalidColorFormat_ShortHex_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DemandFactory.createDemand("d1", "n1", "n2", 300, 480, null, "#FFF")
        );
        assertTrue(exception.getMessage().contains("couleur"));
    }

    @Test
    void testCreateDemand_ValidColorFormats() {
        // Test various valid color formats
        assertDoesNotThrow(() -> 
            DemandFactory.createDemand("d1", "n1", "n2", 300, 480, null, "#FF6B6B")
        );
        assertDoesNotThrow(() -> 
            DemandFactory.createDemand("d2", "n1", "n2", 300, 480, null, "#000000")
        );
        assertDoesNotThrow(() -> 
            DemandFactory.createDemand("d3", "n1", "n2", 300, 480, null, "#FFFFFF")
        );
        assertDoesNotThrow(() -> 
            DemandFactory.createDemand("d4", "n1", "n2", 300, 480, null, "#AbCdEf")
        );
    }

    @Test
    void testCreateDemand_NullCourier_IsValid() {
        // Le courierId peut être null
        Demand demand = DemandFactory.createDemand(
            "d1", "n1", "n2", 300, 480, null, "#FF6B6B"
        );
        assertNull(demand.getCourierId());
    }
}
