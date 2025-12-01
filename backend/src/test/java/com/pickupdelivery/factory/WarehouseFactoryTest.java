package com.pickupdelivery.factory;

import com.pickupdelivery.model.Warehouse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour WarehouseFactory
 */
class WarehouseFactoryTest {

    @Test
    void testCreateWarehouse_Valid() {
        // Arrange
        String id = "warehouse-1";
        String nodeId = "node-123";
        String departureTime = "8:0:0";

        // Act
        Warehouse warehouse = WarehouseFactory.createWarehouse(id, nodeId, departureTime);

        // Assert
        assertNotNull(warehouse);
        assertEquals(id, warehouse.getId());
        assertEquals(nodeId, warehouse.getNodeId());
        assertEquals(departureTime, warehouse.getDepartureTime());
    }

    @Test
    void testCreateWarehouse_ValidTimeFormat_WithSeconds() {
        // Act
        Warehouse warehouse = WarehouseFactory.createWarehouse(
            "w1", "n1", "14:30:45"
        );

        // Assert
        assertNotNull(warehouse);
        assertEquals("14:30:45", warehouse.getDepartureTime());
    }

    @Test
    void testCreateWarehouse_ValidTimeFormat_WithoutLeadingZeros() {
        // Act
        Warehouse warehouse = WarehouseFactory.createWarehouse(
            "w1", "n1", "9:5:3"
        );

        // Assert
        assertNotNull(warehouse);
        assertEquals("9:5:3", warehouse.getDepartureTime());
    }

    @Test
    void testCreateWarehouse_ValidTimeFormat_Midnight() {
        // Act
        Warehouse warehouse = WarehouseFactory.createWarehouse(
            "w1", "n1", "0:0:0"
        );

        // Assert
        assertNotNull(warehouse);
        assertEquals("0:0:0", warehouse.getDepartureTime());
    }

    @Test
    void testCreateWarehouse_ValidTimeFormat_EndOfDay() {
        // Act
        Warehouse warehouse = WarehouseFactory.createWarehouse(
            "w1", "n1", "23:59:59"
        );

        // Assert
        assertNotNull(warehouse);
        assertEquals("23:59:59", warehouse.getDepartureTime());
    }

    @Test
    void testCreateWarehouse_NullId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse(null, "n1", "8:0:0")
        );
        assertTrue(exception.getMessage().contains("identifiant"));
    }

    @Test
    void testCreateWarehouse_EmptyId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("", "n1", "8:0:0")
        );
        assertTrue(exception.getMessage().contains("identifiant"));
    }

    @Test
    void testCreateWarehouse_NullNodeId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", null, "8:0:0")
        );
        assertTrue(exception.getMessage().contains("nœud") || 
                   exception.getMessage().contains("noeud"));
    }

    @Test
    void testCreateWarehouse_EmptyNodeId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "", "8:0:0")
        );
        assertTrue(exception.getMessage().contains("nœud") || 
                   exception.getMessage().contains("noeud"));
    }

    @Test
    void testCreateWarehouse_NullDepartureTime_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", null)
        );
        assertTrue(exception.getMessage().contains("heure"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_NoColons_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "080000")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_TooManyParts_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "8:0:0:0")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_InvalidHour_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "25:0:0")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_InvalidMinute_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "8:60:0")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_InvalidSecond_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "8:0:60")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_NegativeHour_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "-1:0:0")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_NonNumeric_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "abc:def:ghi")
        );
        assertTrue(exception.getMessage().contains("format"));
    }

    @Test
    void testCreateWarehouse_InvalidTimeFormat_EmptyString_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> WarehouseFactory.createWarehouse("w1", "n1", "")
        );
        assertTrue(exception.getMessage().contains("heure"));
    }
}
