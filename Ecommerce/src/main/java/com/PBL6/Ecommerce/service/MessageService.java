package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Conversation;
import com.PBL6.Ecommerce.domain.Message;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.MessageResponse;
import com.PBL6.Ecommerce.dto.SendMessageRequest;
import com.PBL6.Ecommerce.exception.ConversationNotFoundException;
import com.PBL6.Ecommerce.exception.MessageNotAllowedException;
import com.PBL6.Ecommerce.repository.ConversationRepository;
import com.PBL6.Ecommerce.repository.MessageRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.util.ConversationPermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing messages in conversations.
 * Handles sending messages, retrieving message history, and permission validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationPermissionValidator permissionValidator;

    /**
     * Send a message in a conversation.
     * 
     * Validation:
     * - Sender must be a conversation member
     * - Conversation must exist
     * 
     * After sending:
     * - Update conversation's last activity time
     * 
     * @param request The message sending request
     * @param currentUserId The ID of the user sending the message
     * @return MessageResponse with the sent message details
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Long currentUserId) {
        log.info("User {} sending message to conversation {}", currentUserId, request.getConversationId());

        // Validate user is a member of the conversation
        permissionValidator.validateConversationAccess(request.getConversationId(), currentUserId);

        // Get conversation
        Conversation conversation = conversationRepository.findById(request.getConversationId())
            .orElseThrow(() -> new ConversationNotFoundException(request.getConversationId()));

        // Get sender
        User sender = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Create message
        Message message = Message.builder()
            .conversation(conversation)
            .sender(sender)
            .messageType(request.getMessageType())
            .content(request.getContent())
            .build();

        // Save message
        message = messageRepository.save(message);

        // Update conversation's last activity time
        conversation.updateLastActivity();
        conversationRepository.save(conversation);

        log.info("Message {} sent successfully in conversation {}", message.getId(), conversation.getId());

        return mapToMessageResponse(message);
    }

    /**
     * Get all messages in a conversation.
     * Returns messages ordered by creation time (oldest first).
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @return List of messages
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(Long conversationId, Long currentUserId) {
        log.info("Fetching messages for conversation {} by user {}", conversationId, currentUserId);

        // Validate user is a member of the conversation
        permissionValidator.validateConversationAccess(conversationId, currentUserId);

        // Verify conversation exists
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException(conversationId);
        }

        // Get messages
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        return messages.stream()
            .map(this::mapToMessageResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get messages in a conversation with pagination.
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @param pageable Pagination information
     * @return Page of messages
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessagesPaginated(
        Long conversationId, 
        Long currentUserId,
        Pageable pageable
    ) {
        log.info("Fetching paginated messages for conversation {} by user {}", conversationId, currentUserId);

        // Validate user is a member of the conversation
        permissionValidator.validateConversationAccess(conversationId, currentUserId);

        // Verify conversation exists
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException(conversationId);
        }

        // Get messages with pagination
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        return messages.map(this::mapToMessageResponse);
    }

    /**
     * Get the latest message in a conversation.
     * Used for conversation previews.
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @return MessageResponse or null if no messages exist
     */
    @Transactional(readOnly = true)
    public MessageResponse getLatestMessage(Long conversationId, Long currentUserId) {
        log.info("Fetching latest message for conversation {} by user {}", conversationId, currentUserId);

        // Validate user is a member of the conversation
        permissionValidator.validateConversationAccess(conversationId, currentUserId);

        // Get latest message
        Message message = messageRepository.findLatestMessageByConversationId(conversationId);

        return message != null ? mapToMessageResponse(message) : null;
    }

    /**
     * Get message count for a conversation.
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @return Number of messages in the conversation
     */
    @Transactional(readOnly = true)
    public long getMessageCount(Long conversationId, Long currentUserId) {
        log.info("Getting message count for conversation {} by user {}", conversationId, currentUserId);

        // Validate user is a member of the conversation
        permissionValidator.validateConversationAccess(conversationId, currentUserId);

        return messageRepository.countByConversationId(conversationId);
    }

    /**
     * Delete a message (soft delete or mark as deleted).
     * Note: This is a placeholder for future implementation.
     * 
     * @param messageId The ID of the message to delete
     * @param currentUserId The ID of the current user
     */
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        log.info("Deleting message {} by user {}", messageId, currentUserId);

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        // Validate user is the sender or has admin rights
        if (!message.getSender().getId().equals(currentUserId)) {
            throw new MessageNotAllowedException("You can only delete your own messages");
        }

        // For now, hard delete. In production, consider soft delete
        messageRepository.delete(message);

        log.info("Message {} deleted successfully", messageId);
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
