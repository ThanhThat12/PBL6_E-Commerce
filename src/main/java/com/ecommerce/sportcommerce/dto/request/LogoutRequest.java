package com.ecommerce.sportcommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user logout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequest {
    
    private String refreshToken; // Optional - If provided, revoke specific token, else revoke all user tokens
}
