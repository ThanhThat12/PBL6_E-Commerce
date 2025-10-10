package com.ecommerce.sportcommerce.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    
    private Long id;
    
    private String email;
    
    private String username;
    
    private String firstName;
    
    private String lastName;
    
    private String phone;
    
    private String role;
    
    private String status;
    
    private String provider;
    
    private Boolean emailVerified;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
