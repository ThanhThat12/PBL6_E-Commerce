package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.constant.ConversationType;
import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.domain.dto.chat.*;
import com.PBL6.Ecommerce.domain.entity.chat.Conversation;
import com.PBL6.Ecommerce.domain.entity.chat.ConversationMember;
import com.PBL6.Ecommerce.domain.entity.chat.Message;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.exception.*;
import com.PBL6.Ecommerce.repository.*;
import com.PBL6.Ecommerce.util.ConversationPermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing conversations.
 * Handles creation, retrieval, and permission validation for conversations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ConversationPermissionValidator permissionValidator;
    private final WebSocketMessageDispatcher messageDispatcher;

    /**
     * Create a new conversation or return existing one.
     * 
     * Logic:
     * - SHOP type: Add user + shop.owner
     * - SUPPORT type: Add user + admin
     * 
     * @param request The conversation creation request
     * @param currentUserId The ID of the user creating the conversation
     * @return ConversationResponse with conversation details
     */
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request, Long currentUserId) {
        log.info("Creating conversation of type {} for user {}", request.getType(), currentUserId);

        // Get the current user
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if conversation already exists and return it
        Conversation existingConversation = findExistingConversation(request, currentUserId);
        if (existingConversation != null) {
            log.info("Returning existing conversation {}", existingConversation.getId());
            return mapToConversationResponse(existingConversation, currentUserId);
        }

        // Create new conversation based on type
        Conversation conversation = switch (request.getType()) {
            case SHOP -> createShopConversation(request, currentUser);
            case SUPPORT -> createSupportConversation(request, currentUser);
            default -> throw new InvalidConversationDataException("Unsupported conversation type: " + request.getType());
        };

        // Save conversation
        conversation = conversationRepository.save(conversation);
        log.info("Created new conversation {} of type {}", conversation.getId(), conversation.getType());
        
        // Log all members after save
        log.info("Conversation {} has {} members:", conversation.getId(), conversation.getMembers().size());
        for (ConversationMember member : conversation.getMembers()) {
            log.info("  - Member: userId={}, userName={}, role={}", 
                    member.getUser().getId(), 
                    member.getUser().getFullName(),
                    member.getUser().getRole());
        }

        // If SUPPORT conversation, notify all admins
        if (request.getType() == ConversationType.SUPPORT) {
            notifyAdminsOfNewSupportConversation(conversation);
        }

        return mapToConversationResponse(conversation, currentUserId);
    }

    /**
     * Get all conversations for the current user.
     * Returns conversations sorted by last activity time (most recent first).
     * 
     * @param currentUserId The ID of the current user
     * @return List of conversations
     */
    @Transactional(readOnly = true)
    public List<ConversationListResponse> getMyConversations(Long currentUserId) {
        log.info("Fetching all conversations for user {}", currentUserId);

        List<Conversation> conversations = conversationRepository.findAllByUserId(currentUserId);
        log.info("Found {} conversations for user {}", conversations.size(), currentUserId);
        
        for (Conversation conv : conversations) {
            log.info("  - Conversation {}: type={}, members={}", 
                    conv.getId(), conv.getType(), conv.getMembers().size());
        }

        return conversations.stream()
            .map(conv -> mapToConversationListResponse(conv, currentUserId))
            .collect(Collectors.toList());
    }

    /**
     * Get conversation details including members and metadata.
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @return ConversationResponse with full details
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversationDetails(Long conversationId, Long currentUserId) {
        log.info("Fetching conversation {} details for user {}", conversationId, currentUserId);

        // Validate user has access to this conversation
        permissionValidator.validateConversationAccess(conversationId, currentUserId);

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        return mapToConversationResponse(conversation, currentUserId);
    }

    /**
     * Find existing conversation based on type and parameters.
     * 
     * @param request The conversation creation request
     * @param userId The ID of the user
     * @return Existing conversation or null
     */
    private Conversation findExistingConversation(CreateConversationRequest request, Long userId) {
        return switch (request.getType()) {
            case SHOP -> {
                if (request.getShopId() != null) {
                    yield conversationRepository.findShopConversation(
                        request.getShopId(),
                        userId,
                        ConversationType.SHOP
                    ).orElse(null);
                }
                yield null;
            }
            case SUPPORT -> conversationRepository.findSupportConversation(
                userId,
                ConversationType.SUPPORT
            ).orElse(null);
            default -> null;
        };
    }

    /**
     * Create a SHOP conversation.
     * Adds the requesting user and shop owner as members.
     * 
     * @param request The conversation creation request
     * @param currentUser The user creating the conversation
     * @return Created conversation
     */
    private Conversation createShopConversation(CreateConversationRequest request, User currentUser) {
        if (request.getShopId() == null) {
            throw new InvalidConversationDataException("Shop ID is required for SHOP conversations");
        }

        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Build conversation WITHOUT members
        Conversation conversation = Conversation.builder()
            .type(ConversationType.SHOP)
            .shop(shop)
            .createdBy(currentUser)
            .build();

        // Save first to get ID
        conversation = conversationRepository.save(conversation);

        // Now add members
        List<ConversationMember> members = new ArrayList<>();
        
        ConversationMember userMember = ConversationMember.builder()
            .conversation(conversation)
            .user(currentUser)
            .build();
        members.add(userMember);

        // Add shop owner as member
        User shopOwner = shop.getOwner();
        // Only add shop owner if different from current user
        if (!shopOwner.getId().equals(currentUser.getId())) {
            ConversationMember ownerMember = ConversationMember.builder()
                .conversation(conversation)
                .user(shopOwner)
                .build();
            members.add(ownerMember);
        }

        conversation.setMembers(members);
        conversation = conversationRepository.save(conversation);

        return conversation;
    }

    /**
     * Create a SUPPORT conversation.
     * Adds the requesting user and ALL admins as members.
     * 
     * @param request The conversation creation request
     * @param currentUser The user creating the conversation
     * @return Created conversation
     */
    private Conversation createSupportConversation(CreateConversationRequest request, User currentUser) {
        // Build conversation WITHOUT members first
        Conversation conversation = Conversation.builder()
            .type(ConversationType.SUPPORT)
            .createdBy(currentUser)
            .build();

        // Save conversation first to get ID
        conversation = conversationRepository.save(conversation);
        log.info("Created SUPPORT conversation {} (empty members)", conversation.getId());

        // Now add members after conversation has ID
        List<ConversationMember> members = new ArrayList<>();
        
        // Add current user as member
        ConversationMember userMember = ConversationMember.builder()
            .conversation(conversation)
            .user(currentUser)
            .build();
        members.add(userMember);

        // Add ALL admins to the conversation
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        log.info("Adding {} admin(s) to SUPPORT conversation", admins.size());
        
        for (User admin : admins) {
            // Skip if admin is the current user (already added)
            if (!admin.getId().equals(currentUser.getId())) {
                ConversationMember adminMember = ConversationMember.builder()
                    .conversation(conversation)
                    .user(admin)
                    .build();
                
                members.add(adminMember);
                log.info("Added admin {} to SUPPORT conversation", admin.getId());
            }
        }
        
        // Set members list and save again
        conversation.setMembers(members);
        conversation = conversationRepository.save(conversation);
        
        log.info("Total members in SUPPORT conversation: {}", conversation.getMembers().size());

        return conversation;
    }

    /**
     * Notify all admins about a new SUPPORT conversation via WebSocket.
     * Sends a notification to each admin's personal queue so they can
     * refresh their conversation list or auto-subscribe.
     * 
     * @param conversation The newly created SUPPORT conversation
     */
    private void notifyAdminsOfNewSupportConversation(Conversation conversation) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            
            for (User admin : admins) {
                // Skip if admin is the creator
                if (!admin.getId().equals(conversation.getCreatedBy().getId())) {
                    // Send notification to admin's personal queue
                    messageDispatcher.sendUserNotification(
                        admin.getId(),
                        new SupportConversationNotification(
                            conversation.getId(),
                            conversation.getCreatedBy().getFullName(),
                            "Có yêu cầu hỗ trợ mới từ " + conversation.getCreatedBy().getFullName()
                        )
                    );
                    log.info("Notified admin {} about new SUPPORT conversation {}", 
                            admin.getId(), conversation.getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to notify admins about new SUPPORT conversation: {}", e.getMessage(), e);
            // Don't throw - notification failure shouldn't break conversation creation
        }
    }

    // Inner class for support conversation notification
    private record SupportConversationNotification(
        Long conversationId,
        String requesterName,
        String message
    ) {}

    /**
     * Map Conversation entity to ConversationResponse DTO.
     * 
     * @param conversation The conversation entity
     * @param currentUserId The ID of the current user
     * @return ConversationResponse
     */
    private ConversationResponse mapToConversationResponse(Conversation conversation, Long currentUserId) {
        // Get members
        List<ConversationMemberResponse> members = conversation.getMembers().stream()
            .map(this::mapToMemberResponse)
            .collect(Collectors.toList());

        // Get last message
        Message lastMessage = messageRepository.findLatestMessageByConversationId(conversation.getId());
        MessageResponse lastMessageResponse = lastMessage != null ? mapToMessageResponse(lastMessage) : null;

        // Get message count
        long messageCount = messageRepository.countByConversationId(conversation.getId());

        return ConversationResponse.builder()
            .id(conversation.getId())
            .type(conversation.getType())
            .orderId(conversation.getOrder() != null ? conversation.getOrder().getId() : null)
            .shopId(conversation.getShop() != null ? conversation.getShop().getId() : null)
            .shopName(conversation.getShop() != null ? conversation.getShop().getName() : null)
            .createdById(conversation.getCreatedBy().getId())
            .createdByName(conversation.getCreatedBy().getFullName())
            .createdAt(conversation.getCreatedAt())
            .lastActivityAt(conversation.getLastActivityAt())
            .members(members)
            .lastMessage(lastMessageResponse)
            .messageCount(messageCount)
            .build();
    }

    /**
     * Map Conversation entity to ConversationListResponse DTO.
     * This is a lighter version for list views.
     * 
     * @param conversation The conversation entity
     * @param currentUserId The ID of the current user
     * @return ConversationListResponse
     */
    private ConversationListResponse mapToConversationListResponse(Conversation conversation, Long currentUserId) {
        // Get the other participant
        User otherParticipant = permissionValidator.getOtherParticipant(conversation, currentUserId);

        // Get last message
        Message lastMessage = messageRepository.findLatestMessageByConversationId(conversation.getId());
        MessageResponse lastMessageResponse = lastMessage != null ? mapToMessageResponse(lastMessage) : null;

        return ConversationListResponse.builder()
            .id(conversation.getId())
            .type(conversation.getType())
            .orderId(conversation.getOrder() != null ? conversation.getOrder().getId() : null)
            .shopId(conversation.getShop() != null ? conversation.getShop().getId() : null)
            .shopName(conversation.getShop() != null ? conversation.getShop().getName() : null)
            .otherParticipantName(otherParticipant != null ? otherParticipant.getFullName() : "Support")
            .otherParticipantAvatar(otherParticipant != null ? otherParticipant.getAvatarUrl() : null)
            .lastActivityAt(conversation.getLastActivityAt())
            .lastMessage(lastMessageResponse)
            .unreadCount(messageReadStatusRepository.countUnreadMessages(conversation.getId(), currentUserId))
            .build();
    }

    /**
     * Map ConversationMember entity to ConversationMemberResponse DTO.
     * 
     * @param member The conversation member entity
     * @return ConversationMemberResponse
     */
    private ConversationMemberResponse mapToMemberResponse(ConversationMember member) {
        User user = member.getUser();
        return ConversationMemberResponse.builder()
            .id(member.getId())
            .userId(user.getId())
            .userName(user.getFullName())
            .userEmail(user.getEmail())
            .userAvatar(user.getAvatarUrl())
            .build();
    }

    /**
     * Map Message entity to MessageResponse DTO.
     * 
     * @param message The message entity
     * @return MessageResponse
     */
    private MessageResponse mapToMessageResponse(Message message) {
        User sender = message.getSender();
        return MessageResponse.builder()
            .id(message.getId())
            .conversationId(message.getConversation().getId())
            .senderId(sender.getId())
            .senderName(sender.getFullName())
            .senderAvatar(sender.getAvatarUrl())
            .messageType(message.getMessageType())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
