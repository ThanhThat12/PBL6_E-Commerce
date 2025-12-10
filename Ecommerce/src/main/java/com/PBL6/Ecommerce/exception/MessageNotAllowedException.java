package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a message operation fails or is not allowed.
 */
public class MessageNotAllowedException extends RuntimeException {
    
    public MessageNotAllowedException(String message) {
        super(message);
    }
}
