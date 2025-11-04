package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for admin login
 * Separated from regular LoginDTO for future mobile app integration
 */
public class AdminLoginDTO {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;

    public AdminLoginDTO() {
    }

    public AdminLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
