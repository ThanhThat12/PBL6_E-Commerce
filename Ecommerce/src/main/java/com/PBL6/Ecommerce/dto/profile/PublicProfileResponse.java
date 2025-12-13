package com.PBL6.Ecommerce.dto.profile;

import com.PBL6.Ecommerce.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public profile response exposing safe subset of user data
 * Used for GET /api/profile/{username}
 * Does not expose email or phone for privacy
 */
@Schema(description = "Public profile information visible to all users")
public class PublicProfileResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Avatar URL")
    private String avatarUrl;
    
    @Schema(description = "User role", example = "SELLER")
    private Role role;
    
    @Schema(description = "Shop name (only if user is SELLER)", example = "John's Shop")
    private String shopName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
}