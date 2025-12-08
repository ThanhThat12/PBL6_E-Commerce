package com.PBL6.Ecommerce.controller.websocket;

import com.PBL6.Ecommerce.config.websocket.WebSocketAuthInterceptor;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.*;
import com.PBL6.Ecommerce.exception.MessageNotAllowedException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.MessageService;
import com.PBL6.Ecommerce.service.WebSocketMessageDispatcher;
import com.PBL6.Ecommerce.util.ConversationPermissionValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time chat functionality.
 * 
 * This controller handles:
 * - Real-time message sending and broadcasting
 * - Typing indicators
 * - Message delivery confirmations
 * 
 * Client subscription pattern:
 * - Subscribe to: /topic/conversations/{conversationId}
 * - Send messages to: /app/chat/send
 * - Send typing events to: /app/chat/typing
 * 
 * Authentication:
 * - All WebSocket connections must include a valid JWT token
 * - Token is validated by WebSocketAuthInterceptor
 * - User ID is extracted and stored in the WebSocket session
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;
    private final WebSocketMessageDispatcher messageDispatcher;
    private final ConversationPermissionValidator permissionValidator;
    private final UserRepository userRepository;

    /**
     * Handle incoming chat messages via WebSocket.
     * 
     * Client sends message to: /app/chat/send
     * 
     * Flow:
     * 1. Extract authenticated user ID from WebSocket session
     * 2. Validate sender ID matches authenticated user
     * 3. Validate sender is a member of the conversation
     * 4. Save message to database via MessageService
     * 5. Broadcast message to all conversation members
     * 6. Send delivery confirmation to sender
     * 
     * @param request The message request from the client
     * @param headerAccessor STOMP header accessor containing session info
     */
    @MessageMapping("/chat/send")
    public void handleMessage(
            @Payload @Valid WebSocketMessageRequest request,
            StompHeaderAccessor headerAccessor) {

        try {
            // Extract authenticated user ID from WebSocket session
            Long authenticatedUserId = WebSocketAuthInterceptor.getUserId(headerAccessor);
            
            if (authenticatedUserId == null) {
                log.error("No authenticated user found in WebSocket session");
                throw new MessageNotAllowedException("Authentication required");
            }

            // Validate sender ID matches authenticated user
            if (!authenticatedUserId.equals(request.getSenderId())) {
                log.error("Sender ID {} does not match authenticated user ID {}", 
                         request.getSenderId(), authenticatedUserId);
                throw new MessageNotAllowedException(
                    "Cannot send message on behalf of another user");
            }

            // Validate sender is a member of the conversation
            permissionValidator.validateConversationAccess(
                request.getConversationId(), 
                authenticatedUserId
            );

            log.info("Received WebSocket message from user {} for conversation {}", 
                    authenticatedUserId, request.getConversationId());

            // Create SendMessageRequest for the MessageService
            SendMessageRequest messageRequest = SendMessageRequest.builder()
                .conversationId(request.getConversationId())
                .messageType(request.getMessageType())
                .content(request.getContent())
                .build();

            // Save message to database
            MessageResponse savedMessage = messageService.sendMessage(
                messageRequest, 
                authenticatedUserId
            );

            // Convert to WebSocket response
            WebSocketMessageResponse wsResponse = WebSocketMessageResponse.builder()
                .id(savedMessage.getId())
                .conversationId(savedMessage.getConversationId())
                .senderId(savedMessage.getSenderId())
                .senderName(savedMessage.getSenderName())
                .senderAvatar(savedMessage.getSenderAvatar())
                .messageType(savedMessage.getMessageType())
                .content(savedMessage.getContent())
                .createdAt(savedMessage.getCreatedAt())
                .status("SENT")
                .build();

            // Broadcast message to all conversation members
            messageDispatcher.broadcastMessage(
                request.getConversationId(), 
                wsResponse
            );

            // Send delivery confirmation to sender
            messageDispatcher.sendDeliveryConfirmation(
                authenticatedUserId, 
                savedMessage.getId()
            );

            log.info("Message {} broadcast successfully to conversation {}", 
                    savedMessage.getId(), request.getConversationId());

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
            
            // Send error notification to sender
            // In production, you might want to send structured error messages
            if (WebSocketAuthInterceptor.getUserId(headerAccessor) != null) {
                messageDispatcher.sendUserNotification(
                    WebSocketAuthInterceptor.getUserId(headerAccessor),
                    new ErrorNotification("MESSAGE_SEND_FAILED", e.getMessage())
                );
            }
        }
    }

    /**
     * Handle typing indicator events.
     * 
     * Client sends to: /app/chat/typing
     * 
     * Flow:
     * 1. Extract authenticated user ID
     * 2. Validate user ID matches authenticated user
     * 3. Validate user is a member of the conversation
     * 4. Broadcast typing indicator to conversation members
     * 
     * @param request The typing indicator request
     * @param headerAccessor STOMP header accessor
     */
    @MessageMapping("/chat/typing")
    public void handleTypingIndicator(
            @Payload @Valid TypingRequest request,
            StompHeaderAccessor headerAccessor) {

        try {
            // Extract authenticated user ID
            Long authenticatedUserId = WebSocketAuthInterceptor.getUserId(headerAccessor);
            
            if (authenticatedUserId == null) {
                log.error("No authenticated user found for typing indicator");
                return;
            }

            // Validate user ID matches authenticated user
            if (!authenticatedUserId.equals(request.getUserId())) {
                log.error("User ID {} does not match authenticated user ID {}", 
                         request.getUserId(), authenticatedUserId);
                return;
            }

            // Validate user is a member of the conversation
            permissionValidator.validateConversationAccess(
                request.getConversationId(), 
                authenticatedUserId
            );

            // Get user info for the response
            User user = userRepository.findById(authenticatedUserId)
                .orElse(null);

            // Create typing response
            TypingResponse response = TypingResponse.builder()
                .conversationId(request.getConversationId())
                .userId(authenticatedUserId)
                .userName(user != null ? user.getFullName() : "Unknown")
                .typing(request.isTyping())
                .build();

            // Broadcast typing indicator to conversation
            messageDispatcher.broadcastTypingIndicator(
                request.getConversationId(), 
                response
            );

            log.debug("User {} typing indicator: {} in conversation {}", 
                     authenticatedUserId, request.isTyping(), request.getConversationId());

        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage());
            // Typing indicators are not critical, so we just log the error
        }
    }

    /**
     * Simple error notification DTO.
     */
    private record ErrorNotification(String errorCode, String message) {}
}
