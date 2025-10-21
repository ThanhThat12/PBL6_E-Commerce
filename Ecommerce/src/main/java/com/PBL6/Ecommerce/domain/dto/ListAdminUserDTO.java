package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

public class ListAdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean activated;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public ListAdminUserDTO(Long id, String username, String email, String phoneNumber, 
                       String role, boolean activated, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.activated = activated;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}