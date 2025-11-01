package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a request is invalid or contains bad data
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
