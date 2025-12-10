package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    /**
     * Check if a message has been read by a specific user
     */
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Find read status for a message and user
     */
    Optional<MessageReadStatus> findByMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Get all read statuses for messages in a conversation by a user
     */
    @Query("SELECT mrs FROM MessageReadStatus mrs WHERE mrs.message.conversation.id = :conversationId AND mrs.user.id = :userId")
    List<MessageReadStatus> findByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Count unread messages in a conversation for a user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId " +
           "AND NOT EXISTS (SELECT 1 FROM MessageReadStatus mrs WHERE mrs.message.id = m.id AND mrs.user.id = :userId)")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * Count total unread messages for a user across all conversations
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "JOIN c.members cm " +
           "WHERE cm.user.id = :userId " +
           "AND m.sender.id != :userId " +
           "AND NOT EXISTS (SELECT 1 FROM MessageReadStatus mrs WHERE mrs.message.id = m.id AND mrs.user.id = :userId)")
    Long countUserUnreadMessages(@Param("userId") Long userId);

    /**
     * Delete all read statuses for messages in a conversation
     */
    void deleteByMessageConversationId(Long conversationId);
}

