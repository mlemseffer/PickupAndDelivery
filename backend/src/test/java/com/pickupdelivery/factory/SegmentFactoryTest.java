package com.pickupdelivery.factory;

import com.pickupdelivery.model.Segment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SegmentFactory
 */
class SegmentFactoryTest {

    @Test
    void testCreateSegment_Valid() {
        // Arrange
        String origin = "123";
        String destination = "456";
        double length = 150.5;
        String name = "Rue de la République";

        // Act
        Segment segment = SegmentFactory.createSegment(origin, destination, length, name);

        // Assert
        assertNotNull(segment);
        assertEquals(origin, segment.getOrigin());
        assertEquals(destination, segment.getDestination());
        assertEquals(length, segment.getLength());
        assertEquals(name, segment.getName());
    }

    @Test
    void testCreateSegment_NullOrigin_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment(null, "456", 150.0, "Rue A")
        );
        assertTrue(exception.getMessage().contains("origine"));
    }

    @Test
    void testCreateSegment_NullDestination_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", null, 150.0, "Rue A")
        );
        assertTrue(exception.getMessage().contains("destination"));
    }

    @Test
    void testCreateSegment_NegativeLength_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", "456", -10.0, "Rue A")
        );
        assertTrue(exception.getMessage().contains("strictement positive"));
    }

    @Test
    void testCreateSegment_ZeroLength_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", "456", 0.0, "Rue A")
        );
        assertTrue(exception.getMessage().contains("strictement positive"));
    }

    @Test
    void testCreateSegment_NullName_IsValid() {
        // Le nom de rue est optionnel
        // Act
        Segment segment = SegmentFactory.createSegment("123", "456", 150.0, null);
        
        // Assert
        assertNotNull(segment);
        assertNull(segment.getName());
    }

    @Test
    void testCreateSegment_EmptyName_IsValid() {
        // Le nom de rue est optionnel
        // Act
        Segment segment = SegmentFactory.createSegment("123", "456", 150.0, "  ");
        
        // Assert
        assertNotNull(segment);
        assertEquals("  ", segment.getName());
    }

    @Test
    void testCreateSegment_SameOriginAndDestination_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", "123", 150.0, "Rue A")
        );
        assertTrue(exception.getMessage().contains("identiques"));
    }

    @Test
    void testCreateSegment_NaNLength_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", "456", Double.NaN, "Rue A")
        );
        assertTrue(exception.getMessage().contains("nombre fini valide"));
    }

    @Test
    void testCreateSegment_InfiniteLength_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SegmentFactory.createSegment("123", "456", Double.POSITIVE_INFINITY, "Rue A")
        );
        assertTrue(exception.getMessage().contains("nombre fini valide"));
    }
}
