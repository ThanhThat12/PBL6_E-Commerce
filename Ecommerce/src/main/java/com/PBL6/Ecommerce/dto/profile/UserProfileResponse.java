package com.PBL6.Ecommerce.dto.profile;

import java.time.LocalDateTime;
import java.util.List;

import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.dto.AddressResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for user profile with addresses
 * Used for GET /api/profile and profile updates
 */
@Schema(description = "User profile response with personal info and addresses")
public class UserProfileResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "Phone number", example = "0901234567")
    private String phoneNumber;
    
    @Schema(description = "Avatar URL")
    private String avatarUrl;
    
    @Schema(description = "User role", example = "BUYER")
    private Role role;
    
    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "List of user addresses")
    private List<AddressResponseDTO> addresses;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<AddressResponseDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressResponseDTO> addresses) { this.addresses = addresses; }
}