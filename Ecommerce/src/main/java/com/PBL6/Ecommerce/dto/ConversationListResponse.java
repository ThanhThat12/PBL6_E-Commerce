package com.PBL6.Ecommerce.dto;

import com.PBL6.Ecommerce.constant.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simplified conversation response for list view.
 * Contains less detail than ConversationResponse for better performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListResponse {

    private Long id;
    private ConversationType type;
    private Long orderId;
    private Long shopId;
    private String shopName;
    private String otherParticipantName;
    private String otherParticipantAvatar;
    private LocalDateTime lastActivityAt;
    private MessageResponse lastMessage;
    private Long unreadCount;
}
