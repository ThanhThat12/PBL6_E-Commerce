package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a business rule is violated.
 * Used for general business logic errors that don't fit into more specific exception types.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
