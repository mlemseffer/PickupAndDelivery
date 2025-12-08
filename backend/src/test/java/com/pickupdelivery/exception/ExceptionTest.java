package com.pickupdelivery.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour les classes d'exception
 */
public class ExceptionTest {

    @Test
    @DisplayName("ValidationException avec message")
    void testValidationExceptionWithMessage() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("ValidationException avec message et cause")
    void testValidationExceptionWithMessageAndCause() {
        String message = "Validation failed";
        Throwable cause = new IllegalArgumentException("Invalid argument");
        ValidationException exception = new ValidationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("AlgorithmException avec message")
    void testAlgorithmExceptionWithMessage() {
        String message = "Algorithm failed";
        AlgorithmException exception = new AlgorithmException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("AlgorithmException avec message et cause")
    void testAlgorithmExceptionWithMessageAndCause() {
        String message = "Algorithm failed";
        Throwable cause = new RuntimeException("Internal error");
        AlgorithmException exception = new AlgorithmException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Les exceptions peuvent être lancées et attrapées")
    void testExceptionsCanBeThrownAndCaught() {
        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Test");
        });
        
        assertThrows(AlgorithmException.class, () -> {
            throw new AlgorithmException("Test");
        });
    }

    @Test
    @DisplayName("ValidationException hérite de RuntimeException")
    void testValidationExceptionIsRuntimeException() {
        ValidationException exception = new ValidationException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("AlgorithmException hérite de RuntimeException")
    void testAlgorithmExceptionIsRuntimeException() {
        AlgorithmException exception = new AlgorithmException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Les exceptions peuvent contenir des stack traces")
    void testExceptionsHaveStackTraces() {
        ValidationException validationEx = new ValidationException("Validation error");
        AlgorithmException algorithmEx = new AlgorithmException("Algorithm error");
        
        assertNotNull(validationEx.getStackTrace());
        assertNotNull(algorithmEx.getStackTrace());
        assertTrue(validationEx.getStackTrace().length > 0);
        assertTrue(algorithmEx.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Les exceptions peuvent être utilisées dans un contexte try-catch")
    void testExceptionsInTryCatch() {
        try {
            throw new ValidationException("Test validation");
        } catch (ValidationException e) {
            assertEquals("Test validation", e.getMessage());
        }
        
        try {
            throw new AlgorithmException("Test algorithm");
        } catch (AlgorithmException e) {
            assertEquals("Test algorithm", e.getMessage());
        }
    }

    @Test
    @DisplayName("ValidationException peut encapsuler une autre exception")
    void testValidationExceptionWrapsOtherException() {
        NullPointerException npe = new NullPointerException("Null value");
        ValidationException exception = new ValidationException("Validation failed due to null", npe);
        
        assertEquals("Validation failed due to null", exception.getMessage());
        assertTrue(exception.getCause() instanceof NullPointerException);
        assertEquals("Null value", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("AlgorithmException peut encapsuler une autre exception")
    void testAlgorithmExceptionWrapsOtherException() {
        ArithmeticException ae = new ArithmeticException("Division by zero");
        AlgorithmException exception = new AlgorithmException("Algorithm calculation failed", ae);
        
        assertEquals("Algorithm calculation failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof ArithmeticException);
        assertEquals("Division by zero", exception.getCause().getMessage());
    }
}
