package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when required data for creating a conversation is invalid or missing.
 */
public class InvalidConversationDataException extends RuntimeException {
    
    public InvalidConversationDataException(String message) {
        super(message);
    }
}
