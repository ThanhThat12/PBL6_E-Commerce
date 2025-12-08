package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.constant.ConversationType;
import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.dto.*;
import com.PBL6.Ecommerce.exception.*;
import com.PBL6.Ecommerce.repository.*;
import com.PBL6.Ecommerce.util.ConversationPermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final ConversationPermissionValidator permissionValidator;

    /**
     * Create a new conversation or return existing one.
     * 
     * Logic:
     * - ORDER type: Auto-add buyer + seller (seller = shop.owner)
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
            case ORDER -> createOrderConversation(request, currentUser);
            case SHOP -> createShopConversation(request, currentUser);
            case SUPPORT -> createSupportConversation(request, currentUser);
        };

        // Save conversation
        conversation = conversationRepository.save(conversation);
        log.info("Created new conversation {} of type {}", conversation.getId(), conversation.getType());

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
            case ORDER -> {
                if (request.getOrderId() != null) {
                    yield conversationRepository.findByOrderIdAndType(
                        request.getOrderId(),
                        ConversationType.ORDER
                    ).orElse(null);
                }
                yield null;
            }
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
        };
    }

    /**
     * Create an ORDER conversation.
     * Automatically adds buyer and seller (shop owner) as members.
     * 
     * @param request The conversation creation request
     * @param currentUser The user creating the conversation
     * @return Created conversation
     */
    private Conversation createOrderConversation(CreateConversationRequest request, User currentUser) {
        if (request.getOrderId() == null) {
            throw new InvalidConversationDataException("Order ID is required for ORDER conversations");
        }

        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate current user is buyer or seller
        if (!permissionValidator.canParticipateInOrderConversation(order, currentUser.getId())) {
            throw new ConversationPermissionDeniedException(
                "You are not authorized to create a conversation for this order"
            );
        }

        // Build conversation
        Conversation conversation = Conversation.builder()
            .type(ConversationType.ORDER)
            .order(order)
            .shop(order.getShop())
            .createdBy(currentUser)
            .build();

        // Add buyer as member
        User buyer = order.getUser();
        ConversationMember buyerMember = ConversationMember.builder()
            .conversation(conversation)
            .user(buyer)
            .build();

        // Add seller (shop owner) as member
        User seller = order.getShop().getOwner();
        ConversationMember sellerMember = ConversationMember.builder()
            .conversation(conversation)
            .user(seller)
            .build();

        conversation.getMembers().add(buyerMember);
        // Only add seller if different from buyer
        if (!seller.getId().equals(buyer.getId())) {
            conversation.getMembers().add(sellerMember);
        }

        return conversation;
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

        // Build conversation
        Conversation conversation = Conversation.builder()
            .type(ConversationType.SHOP)
            .shop(shop)
            .createdBy(currentUser)
            .build();

        // Add current user as member
        ConversationMember userMember = ConversationMember.builder()
            .conversation(conversation)
            .user(currentUser)
            .build();

        // Add shop owner as member
        User shopOwner = shop.getOwner();
        ConversationMember ownerMember = ConversationMember.builder()
            .conversation(conversation)
            .user(shopOwner)
            .build();

        conversation.getMembers().add(userMember);
        // Only add shop owner if different from current user
        if (!shopOwner.getId().equals(currentUser.getId())) {
            conversation.getMembers().add(ownerMember);
        }

        return conversation;
    }

    /**
     * Create a SUPPORT conversation.
     * Adds the requesting user and an admin as members.
     * 
     * @param request The conversation creation request
     * @param currentUser The user creating the conversation
     * @return Created conversation
     */
    private Conversation createSupportConversation(CreateConversationRequest request, User currentUser) {
        // Build conversation
        Conversation conversation = Conversation.builder()
            .type(ConversationType.SUPPORT)
            .createdBy(currentUser)
            .build();

        // Add current user as member
        ConversationMember userMember = ConversationMember.builder()
            .conversation(conversation)
            .user(currentUser)
            .build();

        conversation.getMembers().add(userMember);

        // Add admin if specified, otherwise admin will be added later when they respond
        if (request.getTargetUserId() != null) {
            User admin = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

            ConversationMember adminMember = ConversationMember.builder()
                .conversation(conversation)
                .user(admin)
                .build();

            conversation.getMembers().add(adminMember);
        }

        return conversation;
    }

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
            .unreadCount(0L) // TODO: Implement unread count logic
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
