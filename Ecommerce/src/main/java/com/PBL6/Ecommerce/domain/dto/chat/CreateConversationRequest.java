package com.PBL6.Ecommerce.domain.dto.chat;

import com.PBL6.Ecommerce.constant.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    @NotNull(message = "Conversation type is required")
    private ConversationType type;

    /**
     * Required for ORDER conversations.
     * The order ID that this conversation is related to.
     */
    private Long orderId;

    /**
     * Required for SHOP conversations, optional for ORDER conversations.
     * The shop ID that this conversation is related to.
     */
    private Long shopId;

    /**
     * Optional: Target user ID for SUPPORT conversations.
     * If not provided, system will assign to an available admin.
     */
    private Long targetUserId;
}
