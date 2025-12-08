package com.PBL6.Ecommerce.config.websocket;

import com.PBL6.Ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

/**
 * WebSocket authentication interceptor.
 * Intercepts WebSocket STOMP messages to validate JWT tokens.
 * 
 * This interceptor:
 * 1. Extracts JWT token from WebSocket connection headers
 * 2. Validates the token using Spring Security's JwtDecoder
 * 3. Extracts and stores user ID in the WebSocket session
 * 4. Rejects connections with invalid or missing tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    /**
     * Intercept messages before they are sent to the channel.
     * This is where we validate JWT tokens for CONNECT commands.
     * 
     * @param message The STOMP message
     * @param channel The message channel
     * @return The message if authentication succeeds, null to reject
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from headers
            // Clients should send token in "Authorization" header as "Bearer <token>"
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket connection rejected: Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            try {
                // Extract token (remove "Bearer " prefix)
                String token = authHeader.substring(7);

                // Decode and validate JWT token
                Jwt jwt = jwtDecoder.decode(token);

                // Extract user ID from JWT
                Long userId = userService.extractUserIdFromJwt(jwt);

                // Store user ID in WebSocket session attributes
                // This will be available in subsequent message handlers
                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("username", jwt.getSubject());

                log.info("WebSocket connection authenticated for user ID: {}", userId);

            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * Get authenticated user ID from WebSocket session.
     * 
     * @param accessor The STOMP header accessor
     * @return User ID if authenticated, null otherwise
     */
    public static Long getUserId(StompHeaderAccessor accessor) {
        if (accessor == null || accessor.getSessionAttributes() == null) {
            return null;
        }
        Object userId = accessor.getSessionAttributes().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    /**
     * Get authenticated username from WebSocket session.
     * 
     * @param accessor The STOMP header accessor
     * @return Username if authenticated, null otherwise
     */
    public static String getUsername(StompHeaderAccessor accessor) {
        if (accessor == null || accessor.getSessionAttributes() == null) {
            return null;
        }
        Object username = accessor.getSessionAttributes().get("username");
        return username instanceof String ? (String) username : null;
    }
}
