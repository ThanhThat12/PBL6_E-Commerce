package com.PBL6.Ecommerce.domain.dto.chat;

import com.PBL6.Ecommerce.constant.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending messages via WebSocket.
 * This is received when a client sends a message through STOMP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageRequest {

    /**
     * The conversation ID where the message is being sent.
     */
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    /**
     * The ID of the user sending the message.
     * This will be validated against the authenticated WebSocket session.
     */
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    /**
     * The type of message (TEXT or IMAGE).
     */
    @NotNull(message = "Message type is required")
    private MessageType messageType;

    /**
     * The content of the message.
     * For TEXT: the actual text message
     * For IMAGE: the URL or path to the image
     */
    @NotBlank(message = "Message content is required")
    private String content;
}
