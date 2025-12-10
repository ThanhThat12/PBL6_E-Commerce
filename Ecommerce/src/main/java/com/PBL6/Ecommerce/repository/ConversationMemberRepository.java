package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ConversationMember entity.
 * Provides database operations for managing conversation members.
 */
@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    /**
     * Find all members of a conversation.
     *
     * @param conversationId The ID of the conversation
     * @return List of conversation members
     */
    List<ConversationMember> findByConversationId(Long conversationId);

    /**
     * Find a specific member in a conversation.
     *
     * @param conversationId The ID of the conversation
     * @param userId The ID of the user
     * @return Optional containing the member if found
     */
    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);

    /**
     * Check if a user is already a member of a conversation.
     *
     * @param conversationId The ID of the conversation
     * @param userId The ID of the user
     * @return true if the user is a member, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
           "FROM ConversationMember cm " +
           "WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId")
    boolean existsByConversationIdAndUserId(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId
    );

    /**
     * Delete all members of a conversation.
     *
     * @param conversationId The ID of the conversation
     */
    void deleteByConversationId(Long conversationId);
}
