package com.PBL6.Ecommerce.dto;

import com.PBL6.Ecommerce.constant.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for messages broadcast via WebSocket.
 * This is sent to all subscribers of a conversation topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageResponse {

    /**
     * The unique message ID.
     */
    private Long id;

    /**
     * The conversation ID this message belongs to.
     */
    private Long conversationId;

    /**
     * The ID of the user who sent the message.
     */
    private Long senderId;

    /**
     * The name of the sender (for display purposes).
     */
    private String senderName;

    /**
     * The avatar URL of the sender.
     */
    private String senderAvatar;

    /**
     * The type of message (TEXT or IMAGE).
     */
    private MessageType messageType;

    /**
     * The message content.
     */
    private String content;

    /**
     * When the message was created.
     */
    private LocalDateTime createdAt;

    /**
     * Message status (for client-side optimistic updates).
     * Can be: SENDING, SENT, DELIVERED, READ, FAILED
     */
    private String status;
}
