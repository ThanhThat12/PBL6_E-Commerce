package com.PBL6.Ecommerce.domain.dto.profile;

import java.time.LocalDateTime;
import java.util.List;

import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete profile DTO including user info and addresses
 * Used for comprehensive profile response
 * Shop info is available via ShopController endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileDTO {
    // User info
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Addresses
    private List<AddressResponseDTO> addresses;
}
