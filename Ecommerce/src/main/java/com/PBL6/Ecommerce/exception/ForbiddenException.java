package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a user doesn't have permission to perform an action
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
