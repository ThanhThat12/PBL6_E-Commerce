package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Message entity.
 * Provides database operations for managing messages.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a conversation ordered by creation time (oldest first).
     *
     * @param conversationId The ID of the conversation
     * @return List of messages ordered by creation time
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    /**
     * Find messages in a conversation with pagination.
     *
     * @param conversationId The ID of the conversation
     * @param pageable Pagination information
     * @return Page of messages
     */
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    /**
     * Find the latest message in a conversation.
     * Used for displaying conversation previews.
     *
     * @param conversationId The ID of the conversation
     * @return The latest message if exists
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT 1")
    Message findLatestMessageByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Count total messages in a conversation.
     *
     * @param conversationId The ID of the conversation
     * @return Number of messages
     */
    long countByConversationId(Long conversationId);

    /**
     * Delete all messages in a conversation.
     *
     * @param conversationId The ID of the conversation
     */
    void deleteByConversationId(Long conversationId);
}
