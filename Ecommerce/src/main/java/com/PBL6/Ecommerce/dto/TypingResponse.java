package com.PBL6.Ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for typing indicator broadcasts.
 * Broadcast to all conversation members to show who is typing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingResponse {

    /**
     * The conversation ID.
     */
    private Long conversationId;

    /**
     * The ID of the user who is typing.
     */
    private Long userId;

    /**
     * The name of the user who is typing.
     */
    private String userName;

    /**
     * Whether the user is currently typing.
     */
    private boolean typing;
}
