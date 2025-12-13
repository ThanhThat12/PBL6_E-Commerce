package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.chat.WebSocketMessageResponse;
import com.PBL6.Ecommerce.domain.dto.chat.TypingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for dispatching WebSocket messages to clients.
 * This is a helper service that encapsulates the logic for sending
 * messages through STOMP to various destinations.
 * 
 * Benefits of this service:
 * - Centralized message sending logic
 * - Reusable across multiple controllers
 * - Easy to test and mock
 * - Clean separation of concerns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageDispatcher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast a message to all members of a conversation.
     * All clients subscribed to /topic/conversations/{conversationId} will receive this.
     * 
     * @param conversationId The conversation ID
     * @param message The message to broadcast
     */
    public void broadcastMessage(Long conversationId, WebSocketMessageResponse message) {
        String destination = "/topic/conversations/" + conversationId;
        log.info("Broadcasting message {} to conversation {} (destination: {})", 
                message.getId(), conversationId, destination);
        messagingTemplate.convertAndSend(destination, message);
        log.info("Message {} broadcast completed", message.getId());
    }

    /**
     * Send a message to a specific user's private queue.
     * The user must be subscribed to /user/queue/messages to receive this.
     * 
     * @param userId The target user ID
     * @param message The message to send
     */
    public void sendPrivateMessage(Long userId, WebSocketMessageResponse message) {
        String destination = "/user/" + userId + "/queue/messages";
        log.info("Sending private message {} to user {}", message.getId(), userId);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast typing indicator to all conversation members.
     * 
     * @param conversationId The conversation ID
     * @param typingIndicator The typing indicator data
     */
    public void broadcastTypingIndicator(Long conversationId, TypingResponse typingIndicator) {
        String destination = "/topic/conversations/" + conversationId + "/typing";
        log.debug("Broadcasting typing indicator for user {} in conversation {}", 
                  typingIndicator.getUserId(), conversationId);
        messagingTemplate.convertAndSend(destination, typingIndicator);
    }

    /**
     * Send a notification to a specific user.
     * Can be used for delivery confirmations, read receipts, etc.
     * 
     * @param userId The target user ID
     * @param notification The notification object
     */
    public void sendUserNotification(Long userId, Object notification) {
        String destination = "/user/" + userId + "/queue/notifications";
        log.info("Sending notification to user {}", userId);
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Broadcast a generic event to a conversation.
     * Can be used for member joined, member left, conversation updated, etc.
     * 
     * @param conversationId The conversation ID
     * @param event The event object
     */
    public void broadcastConversationEvent(Long conversationId, Object event) {
        String destination = "/topic/conversations/" + conversationId + "/events";
        log.info("Broadcasting event to conversation {}", conversationId);
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send message delivery confirmation to the sender.
     * 
     * @param userId The sender's user ID
     * @param messageId The message ID that was delivered
     */
    public void sendDeliveryConfirmation(Long userId, Long messageId) {
        String destination = "/user/" + userId + "/queue/confirmations";
        log.debug("Sending delivery confirmation for message {} to user {}", messageId, userId);
        messagingTemplate.convertAndSend(destination, 
            new MessageConfirmation(messageId, "DELIVERED"));
    }

    /**
     * Simple DTO for message confirmations.
     */
    public record MessageConfirmation(Long messageId, String status) {}
}
