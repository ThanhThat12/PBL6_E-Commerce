package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.ConversationListResponse;
import com.PBL6.Ecommerce.dto.ConversationResponse;
import com.PBL6.Ecommerce.dto.CreateConversationRequest;
import com.PBL6.Ecommerce.service.ConversationService;
import com.PBL6.Ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing conversations.
 * Provides endpoints for creating and retrieving conversations.
 * 
 * Base path: /api/conversations
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    /**
     * Create a new conversation or return existing one.
     * 
     * POST /api/conversations
     * 
     * Request body:
     * {
     *   "type": "ORDER" | "SHOP" | "SUPPORT",
     *   "orderId": 123,        // required for ORDER type
     *   "shopId": 456,         // required for SHOP type, optional for ORDER
     *   "targetUserId": 789    // optional for SUPPORT type
     * }
     * 
     * @param jwt JWT token for authentication
     * @param request Conversation creation request
     * @return ConversationResponse with conversation details
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<ConversationResponse>> createConversation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateConversationRequest request) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        ConversationResponse response = conversationService.createConversation(request, currentUserId);
        
        return ResponseDTO.created(response, "Conversation created successfully");
    }

    /**
     * Get all conversations for the current user.
     * Returns conversations sorted by last activity (most recent first).
     * 
     * GET /api/conversations/my
     * 
     * @param jwt JWT token for authentication
     * @return List of conversations
     */
    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<List<ConversationListResponse>>> getMyConversations(
            @AuthenticationPrincipal Jwt jwt) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        List<ConversationListResponse> conversations = conversationService.getMyConversations(currentUserId);
        
        return ResponseDTO.success(conversations, "Conversations retrieved successfully");
    }

    /**
     * Get conversation details including members and metadata.
     * 
     * GET /api/conversations/{id}
     * 
     * @param jwt JWT token for authentication
     * @param conversationId The ID of the conversation
     * @return ConversationResponse with full details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ConversationResponse>> getConversationDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long conversationId) {
        
        Long currentUserId = userService.extractUserIdFromJwt(jwt);
        ConversationResponse response = conversationService.getConversationDetails(conversationId, currentUserId);
        
        return ResponseDTO.success(response, "Conversation details retrieved successfully");
    }
}
