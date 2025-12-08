package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.constant.ConversationType;
import com.PBL6.Ecommerce.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Conversation entity.
 * Provides database operations for managing conversations.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find all conversations where the user is a member.
     * Results are ordered by last activity (most recent first).
     *
     * @param userId The ID of the user
     * @return List of conversations ordered by last activity
     */
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN c.members m " +
           "WHERE m.user.id = :userId " +
           "ORDER BY c.lastActivityAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    /**
     * Find an ORDER conversation by order ID.
     * Used to check if a conversation already exists for an order.
     *
     * @param orderId The ID of the order
     * @param type The conversation type (ORDER)
     * @return Optional containing the conversation if found
     */
    Optional<Conversation> findByOrderIdAndType(Long orderId, ConversationType type);

    /**
     * Find a SHOP conversation between a specific user and shop.
     * Used to check if a conversation already exists.
     *
     * @param shopId The ID of the shop
     * @param userId The ID of the user
     * @param type The conversation type (SHOP)
     * @return Optional containing the conversation if found
     */
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.members m " +
           "WHERE c.shop.id = :shopId " +
           "AND c.type = :type " +
           "AND m.user.id = :userId")
    Optional<Conversation> findShopConversation(
        @Param("shopId") Long shopId,
        @Param("userId") Long userId,
        @Param("type") ConversationType type
    );

    /**
     * Find a SUPPORT conversation for a specific user.
     *
     * @param userId The ID of the user
     * @param type The conversation type (SUPPORT)
     * @return Optional containing the conversation if found
     */
    @Query("SELECT c FROM Conversation c " +
           "JOIN c.members m " +
           "WHERE c.type = :type " +
           "AND m.user.id = :userId")
    Optional<Conversation> findSupportConversation(
        @Param("userId") Long userId,
        @Param("type") ConversationType type
    );

    /**
     * Check if a user is a member of a conversation.
     *
     * @param conversationId The ID of the conversation
     * @param userId The ID of the user
     * @return true if the user is a member, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM Conversation c " +
           "JOIN c.members m " +
           "WHERE c.id = :conversationId AND m.user.id = :userId")
    boolean isUserMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
