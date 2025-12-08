package com.PBL6.Ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for typing indicator events.
 * Sent when a user starts/stops typing in a conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingRequest {

    /**
     * The conversation where the user is typing.
     */
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    /**
     * The ID of the user who is typing.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Whether the user is currently typing.
     * true = started typing, false = stopped typing
     */
    private boolean typing;
}
