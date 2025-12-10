package com.PBL6.Ecommerce.dto;

import com.PBL6.Ecommerce.constant.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for conversation details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long id;
    private ConversationType type;
    private Long orderId;
    private Long shopId;
    private String shopName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private List<ConversationMemberResponse> members;
    private MessageResponse lastMessage;
    private Long messageCount;
}
