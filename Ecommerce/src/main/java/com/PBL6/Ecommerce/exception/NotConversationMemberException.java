package com.PBL6.Ecommerce.exception;

/**
 * Exception thrown when a user attempts an action on a conversation
 * they are not a member of.
 */
public class NotConversationMemberException extends RuntimeException {
    
    public NotConversationMemberException(String message) {
        super(message);
    }
    
    public NotConversationMemberException(Long userId, Long conversationId) {
        super("User " + userId + " is not a member of conversation " + conversationId);
    }
}
