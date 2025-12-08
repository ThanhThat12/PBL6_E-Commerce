package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.MessageResponse;
import com.PBL6.Ecommerce.dto.SendMessageRequest;
import com.PBL6.Ecommerce.service.MessageService;
import com.PBL6.Ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing messages in conversations.
 * Provides endpoints for sending and retrieving messages.
 * 
 * Base path: /api/messages
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    /**
     * Send a message in a conversation.
     * 
     * POST /api/messages
     * 
     * Request body:
     * {
     *   "conversationId": 123,
     *   "messageType": "TEXT" | "IMAGE",
     *   "content": "Message content or image URL"
     * }
     * 
     * Validation:
     * - User must be a member of the conversation
     * - Conversation must exist
     * 
     * @param jwt JWT token for authentication
     * @param request Message sending request
     * @return MessageResponse with the sent message
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<MessageResponse>> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SendMessageRequest request) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        MessageResponse response = messageService.sendMessage(request, currentUserId);
        
        return ResponseDTO.created(response, "Message sent successfully");
    }

    /**
     * Get all messages in a conversation.
     * Returns messages ordered by creation time (oldest first).
     * 
     * GET /api/messages/conversation/{conversationId}
     * 
     * @param jwt JWT token for authentication
     * @param conversationId The ID of the conversation
     * @return List of messages
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ResponseDTO<List<MessageResponse>>> getConversationMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("conversationId") Long conversationId) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        List<MessageResponse> messages = messageService.getConversationMessages(conversationId, currentUserId);
        
        return ResponseDTO.success(messages, "Messages retrieved successfully");
    }

    /**
     * Get messages in a conversation with pagination.
     * 
     * GET /api/messages/conversation/{conversationId}/paginated?page=0&size=20
     * 
     * Query parameters:
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort direction (asc or desc, default: asc)
     * 
     * @param jwt JWT token for authentication
     * @param conversationId The ID of the conversation
     * @param page Page number
     * @param size Page size
     * @param sortDirection Sort direction (asc or desc)
     * @return Page of messages
     */
    @GetMapping("/conversation/{conversationId}/paginated")
    public ResponseEntity<ResponseDTO<Page<MessageResponse>>> getConversationMessagesPaginated(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        
        // Create pageable with sort by createdAt
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        Page<MessageResponse> messages = messageService.getConversationMessagesPaginated(
            conversationId, 
            currentUserId, 
            pageable
        );
        
        return ResponseDTO.success(messages, "Paginated messages retrieved successfully");
    }

    /**
     * Get the latest message in a conversation.
     * 
     * GET /api/messages/conversation/{conversationId}/latest
     * 
     * @param jwt JWT token for authentication
     * @param conversationId The ID of the conversation
     * @return MessageResponse or null if no messages exist
     */
    @GetMapping("/conversation/{conversationId}/latest")
    public ResponseEntity<ResponseDTO<MessageResponse>> getLatestMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("conversationId") Long conversationId) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        MessageResponse message = messageService.getLatestMessage(conversationId, currentUserId);
        
        return ResponseDTO.success(message, "Latest message retrieved successfully");
    }

    /**
     * Get message count for a conversation.
     * 
     * GET /api/messages/conversation/{conversationId}/count
     * 
     * @param jwt JWT token for authentication
     * @param conversationId The ID of the conversation
     * @return Number of messages in the conversation
     */
    @GetMapping("/conversation/{conversationId}/count")
    public ResponseEntity<ResponseDTO<Long>> getMessageCount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("conversationId") Long conversationId) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        long count = messageService.getMessageCount(conversationId, currentUserId);
        
        return ResponseDTO.success(count, "Message count retrieved successfully");
    }

    /**
     * Delete a message.
     * User can only delete their own messages.
     * 
     * DELETE /api/messages/{messageId}
     * 
     * @param jwt JWT token for authentication
     * @param messageId The ID of the message to delete
     * @return Success response
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseDTO<Void>> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("messageId") Long messageId) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        messageService.deleteMessage(messageId, currentUserId);
        
        return ResponseDTO.success(null, "Message deleted successfully");
    }
}
