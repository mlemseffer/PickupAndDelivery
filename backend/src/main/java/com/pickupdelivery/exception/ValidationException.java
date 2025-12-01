package com.pickupdelivery.exception;

/**
 * Exception levée lors d'une erreur de validation métier
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
