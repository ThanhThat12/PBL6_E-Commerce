package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refresh token request
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token là bắt buộc")
    private String refreshToken;
    
    public RefreshTokenRequest() {
    }
    
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
