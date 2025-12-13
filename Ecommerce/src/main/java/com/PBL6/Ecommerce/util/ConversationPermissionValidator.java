package com.PBL6.Ecommerce.util;

import com.PBL6.Ecommerce.constant.ConversationType;
import com.PBL6.Ecommerce.domain.entity.chat.Conversation;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.exception.ConversationPermissionDeniedException;
import com.PBL6.Ecommerce.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class to validate user permissions for conversations.
 * 
 * IMPORTANT: This validator does NOT use role-based logic.
 * All permissions are determined by:
 * - user_id (the current user)
 * - shop_id and shop.owner (to determine seller)
 * - order.buyer_id (to determine buyer in ORDER conversations)
 */
@Component
@RequiredArgsConstructor
public class ConversationPermissionValidator {

    private final ConversationRepository conversationRepository;

    /**
     * Validate if a user has permission to access a conversation.
     * 
     * @param conversationId The ID of the conversation
     * @param currentUserId The ID of the current user
     * @throws ConversationPermissionDeniedException if user doesn't have permission
     */
    public void validateConversationAccess(Long conversationId, Long currentUserId) {
        boolean isMember = conversationRepository.isUserMember(conversationId, currentUserId);
        
        if (!isMember) {
            throw new ConversationPermissionDeniedException(
                "You do not have permission to access this conversation"
            );
        }
    }

    /**
     * Validate if a user can participate in an ORDER conversation.
     * Only the buyer and the seller (shop owner) can participate.
     * 
     * @param order The order entity
     * @param currentUserId The ID of the current user
     * @return true if user is buyer or seller
     */
    public boolean canParticipateInOrderConversation(Order order, Long currentUserId) {
        // Check if user is the buyer
        boolean isBuyer = order.getUser().getId().equals(currentUserId);
        
        // Check if user is the seller (shop owner)
        boolean isSeller = order.getShop().getOwner().getId().equals(currentUserId);
        
        return isBuyer || isSeller;
    }

    /**
     * Validate if a user can participate in a SHOP conversation.
     * Only the shop owner and the requesting user can participate.
     * 
     * @param shop The shop entity
     * @param requestingUserId The ID of the user requesting the conversation
     * @param currentUserId The ID of the current user
     * @return true if user is shop owner or the requesting user
     */
    public boolean canParticipateInShopConversation(Shop shop, Long requestingUserId, Long currentUserId) {
        // Check if user is the shop owner
        boolean isShopOwner = shop.getOwner().getId().equals(currentUserId);
        
        // Check if user is the requesting user
        boolean isRequester = requestingUserId.equals(currentUserId);
        
        return isShopOwner || isRequester;
    }

    /**
     * Validate if a user can participate in a SUPPORT conversation.
     * Only the requesting user and admins can participate.
     * 
     * @param conversation The conversation entity
     * @param currentUserId The ID of the current user
     * @param isAdmin Whether the current user is an admin
     * @return true if user is the creator or an admin
     */
    public boolean canParticipateInSupportConversation(Conversation conversation, Long currentUserId, boolean isAdmin) {
        // Check if user is the conversation creator
        boolean isCreator = conversation.getCreatedBy().getId().equals(currentUserId);
        
        return isCreator || isAdmin;
    }

    /**
     * Get the other participant in a two-party conversation.
     * Used for displaying conversation previews.
     * 
     * @param conversation The conversation entity
     * @param currentUserId The ID of the current user
     * @return The other user in the conversation, or null if not found
     */
    public User getOtherParticipant(Conversation conversation, Long currentUserId) {
        return conversation.getMembers().stream()
            .map(member -> member.getUser())
            .filter(user -> !user.getId().equals(currentUserId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Determine if a user is the seller in an ORDER conversation.
     * 
     * @param order The order entity
     * @param userId The ID of the user to check
     * @return true if user is the seller (shop owner)
     */
    public boolean isSellerInOrder(Order order, Long userId) {
        return order.getShop().getOwner().getId().equals(userId);
    }

    /**
     * Determine if a user is the buyer in an ORDER conversation.
     * 
     * @param order The order entity
     * @param userId The ID of the user to check
     * @return true if user is the buyer
     */
    public boolean isBuyerInOrder(Order order, Long userId) {
        return order.getUser().getId().equals(userId);
    }

    /**
     * Determine if a user is the shop owner.
     * 
     * @param shop The shop entity
     * @param userId The ID of the user to check
     * @return true if user is the shop owner
     */
    public boolean isShopOwner(Shop shop, Long userId) {
        return shop.getOwner().getId().equals(userId);
    }
}
