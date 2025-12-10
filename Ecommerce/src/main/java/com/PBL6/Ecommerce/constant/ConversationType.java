package com.PBL6.Ecommerce.constant;

/**
 * Enum representing the type of conversation in the chat system.
 * 
 * ORDER - Conversation between buyer and seller regarding a specific order
 * SHOP - General conversation between a user and a shop owner
 * SUPPORT - Conversation between a user and platform admin/support
 */
public enum ConversationType {
    ORDER,      // Conversation related to a specific order (buyer + seller)
    SHOP,       // General conversation with shop (user + shop owner)
    SUPPORT     // Customer support conversation (user + admin)
}
