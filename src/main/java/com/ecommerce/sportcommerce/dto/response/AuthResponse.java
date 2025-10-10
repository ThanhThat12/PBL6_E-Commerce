package com.ecommerce.sportcommerce.dto.response;

import com.ecommerce.sportcommerce.dto.response.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication operations
 * Used after successful registration/login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn; // in seconds
    
    private UserResponse user;
    
    private String email;
    
    private Integer expiresInMinutes; // For OTP expiration
}
