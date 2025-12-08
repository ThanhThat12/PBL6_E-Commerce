package com.PBL6.Ecommerce.config;

import com.PBL6.Ecommerce.config.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat and notifications.
 * 
 * This configuration enables:
 * - STOMP over WebSocket for bi-directional communication
 * - Simple message broker for topic and queue destinations
 * - JWT authentication for WebSocket connections
 * - SockJS fallback for browsers that don't support WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Configure message broker options.
     * 
     * - /topic: Used for broadcast messages (e.g., all conversation members)
     * - /queue: Used for private messages (e.g., direct user notifications)
     * - /app: Prefix for messages routed to @MessageMapping methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topic and queue destinations
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     * 
     * Endpoint: /ws
     * - Primary WebSocket endpoint
     * - SockJS fallback enabled for browser compatibility
     * - Allows all origins (configure properly for production)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // TODO: Configure for production
                .withSockJS();
                
        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Configure client inbound channel with authentication interceptor.
     * This intercepts all incoming messages to validate JWT tokens.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
