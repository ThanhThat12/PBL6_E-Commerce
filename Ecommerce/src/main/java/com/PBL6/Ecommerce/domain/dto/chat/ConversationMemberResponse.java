package com.PBL6.Ecommerce.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for conversation member details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatar;
}
