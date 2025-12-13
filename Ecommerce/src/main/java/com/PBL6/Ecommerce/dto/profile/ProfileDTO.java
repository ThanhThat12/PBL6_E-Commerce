package com.PBL6.Ecommerce.dto.profile;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile information
 * Used for GET /api/profile responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Boolean activated;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
