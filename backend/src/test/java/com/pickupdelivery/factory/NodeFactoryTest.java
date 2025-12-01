package com.pickupdelivery.factory;

import com.pickupdelivery.model.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour NodeFactory
 */
class NodeFactoryTest {

    @Test
    void testCreateNode_Valid() {
        // Arrange
        String id = "123456";
        double latitude = 45.75;
        double longitude = 4.85;

        // Act
        Node node = NodeFactory.createNode(id, latitude, longitude);

        // Assert
        assertNotNull(node);
        assertEquals(id, node.getId());
        assertEquals(latitude, node.getLatitude());
        assertEquals(longitude, node.getLongitude());
    }

    @Test
    void testCreateNode_NullId_ThrowsException() {
        // Arrange
        double latitude = 45.75;
        double longitude = 4.85;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode(null, latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("null ou vide"));
    }

    @Test
    void testCreateNode_EmptyId_ThrowsException() {
        // Arrange
        double latitude = 45.75;
        double longitude = 4.85;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode("  ", latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("null ou vide"));
    }

    @Test
    void testCreateNode_LatitudeTooLow_ThrowsException() {
        // Arrange
        String id = "123456";
        double latitude = -91.0;
        double longitude = 4.85;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode(id, latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("Latitude invalide"));
    }

    @Test
    void testCreateNode_LatitudeTooHigh_ThrowsException() {
        // Arrange
        String id = "123456";
        double latitude = 91.0;
        double longitude = 4.85;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode(id, latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("Latitude invalide"));
    }

    @Test
    void testCreateNode_LongitudeTooLow_ThrowsException() {
        // Arrange
        String id = "123456";
        double latitude = 45.75;
        double longitude = -181.0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode(id, latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("Longitude invalide"));
    }

    @Test
    void testCreateNode_LongitudeTooHigh_ThrowsException() {
        // Arrange
        String id = "123456";
        double latitude = 45.75;
        double longitude = 181.0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NodeFactory.createNode(id, latitude, longitude)
        );
        assertTrue(exception.getMessage().contains("Longitude invalide"));
    }

    @Test
    void testCreateNode_BoundaryValues_Valid() {
        // Limites valides
        assertDoesNotThrow(() -> NodeFactory.createNode("1", -90.0, -180.0));
        assertDoesNotThrow(() -> NodeFactory.createNode("2", 90.0, 180.0));
        assertDoesNotThrow(() -> NodeFactory.createNode("3", 0.0, 0.0));
    }
}
