package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a requested conversation is not found.
 */
public class ConversationNotFoundException extends RuntimeException {
    
    public ConversationNotFoundException(String message) {
        super(message);
    }
    
    public ConversationNotFoundException(Long conversationId) {
        super("Conversation not found with ID: " + conversationId);
    }
}
