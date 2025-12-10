package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a user is not authorized to perform
 * an action in a conversation.
 */
public class ConversationPermissionDeniedException extends RuntimeException {
    
    public ConversationPermissionDeniedException(String message) {
        super(message);
    }
}
