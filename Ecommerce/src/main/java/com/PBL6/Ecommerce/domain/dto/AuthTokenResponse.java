package com.PBL6.Ecommerce.domain.dto;

/**
 * DTO for token responses (includes both access and refresh tokens)
 */
public class AuthTokenResponse {
    
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // Seconds until access token expires
    private String tokenType = "Bearer";
    
    // Optional: user info
    private UserInfoDTO user;
    
    public AuthTokenResponse() {
    }
    
    public AuthTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
    
    public AuthTokenResponse(String accessToken, String refreshToken, long expiresIn, UserInfoDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public UserInfoDTO getUser() {
        return user;
    }
    
    public void setUser(UserInfoDTO user) {
        this.user = user;
    }
}
