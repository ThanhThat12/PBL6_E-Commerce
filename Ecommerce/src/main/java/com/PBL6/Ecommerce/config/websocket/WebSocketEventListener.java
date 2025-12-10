package com.PBL6.Ecommerce.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * Event listener for WebSocket lifecycle events.
 * 
 * This component listens to:
 * - Connection events (when a client connects)
 * - Disconnection events (when a client disconnects)
 * - Subscription events (when a client subscribes to a topic)
 * - Unsubscription events (when a client unsubscribes)
 * 
 * These events are useful for:
 * - Logging and monitoring
 * - User presence tracking (online/offline status)
 * - Session management
 * - Analytics and debugging
 */
@Component
@Slf4j
public class WebSocketEventListener {

    /**
     * Handle WebSocket connection events.
     * Triggered when a client successfully establishes a WebSocket connection.
     * 
     * @param event The connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headerAccessor.getSessionId();
        Long userId = WebSocketAuthInterceptor.getUserId(headerAccessor);
        String username = WebSocketAuthInterceptor.getUsername(headerAccessor);

        if (userId != null) {
            log.info("WebSocket connected - Session: {}, User ID: {}, Username: {}", 
                    sessionId, userId, username);
        } else {
            log.info("WebSocket connected - Session: {} (unauthenticated)", sessionId);
        }

        // TODO: You can implement user presence tracking here
        // For example:
        // - Update user's online status in database
        // - Broadcast user online event to friends/contacts
        // - Track active sessions for analytics
    }

    /**
     * Handle WebSocket disconnection events.
     * Triggered when a client disconnects (either intentionally or due to network issues).
     * 
     * @param event The disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headerAccessor.getSessionId();
        Long userId = WebSocketAuthInterceptor.getUserId(headerAccessor);
        String username = WebSocketAuthInterceptor.getUsername(headerAccessor);

        if (userId != null) {
            log.info("WebSocket disconnected - Session: {}, User ID: {}, Username: {}", 
                    sessionId, userId, username);
        } else {
            log.info("WebSocket disconnected - Session: {}", sessionId);
        }

        // TODO: You can implement cleanup logic here
        // For example:
        // - Update user's offline status
        // - Broadcast user offline event
        // - Clean up any session-specific data
        // - Send "stopped typing" events for all conversations
    }

    /**
     * Handle WebSocket subscription events.
     * Triggered when a client subscribes to a topic or queue.
     * 
     * @param event The subscription event
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        Long userId = WebSocketAuthInterceptor.getUserId(headerAccessor);

        log.debug("User {} subscribed to {} (Session: {})", userId, destination, sessionId);

        // TODO: You can implement subscription tracking here
        // For example:
        // - Track which conversations a user is actively viewing
        // - Send unread message count when subscribing to a conversation
        // - Mark messages as delivered when user subscribes to conversation
    }

    /**
     * Handle WebSocket unsubscription events.
     * Triggered when a client unsubscribes from a topic or queue.
     * 
     * @param event The unsubscription event
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        Long userId = WebSocketAuthInterceptor.getUserId(headerAccessor);

        log.debug("User {} unsubscribed (Subscription: {}, Session: {})", 
                 userId, subscriptionId, sessionId);

        // TODO: You can implement unsubscription tracking here
        // For example:
        // - Update conversation view status
        // - Stop sending typing indicators for that conversation
        // - Update last seen timestamp
    }
}
