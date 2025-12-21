package com.PBL6.Ecommerce.config;

import com.PBL6.Ecommerce.config.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

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
@Slf4j
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
     * - Extracts JWT token from query parameter during handshake
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Create handshake interceptor to extract token from query parameter
        HandshakeInterceptor handshakeInterceptor = new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                          WebSocketHandler wsHandler, Map<String, Object> attributes) {
                // Extract token from query parameter: /ws?token=xxx
                String query = request.getURI().getQuery();
                if (query != null && query.contains("token=")) {
                    String token = query.split("token=")[1].split("&")[0];
                    attributes.put("token", token);
                    log.debug("🔑 JWT token extracted from query parameter during handshake");
                } else {
                    log.warn("⚠️ No token found in query parameter during handshake");
                }
                return true; // Allow handshake to proceed
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler wsHandler, Exception exception) {
                // Nothing to do after handshake
            }
        };
        
        // Register STOMP endpoint with SockJS support and handshake interceptor
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // TODO: Configure for production
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
                
        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
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
