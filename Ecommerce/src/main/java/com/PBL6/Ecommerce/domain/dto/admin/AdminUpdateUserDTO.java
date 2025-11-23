package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO for admin to update buyer/admin user information
 * Fields that can be updated: username, email, phone, status (activated)
 */
public class AdminUpdateUserDTO {
    private String username;
    private String email;
    private String phone;
    private Boolean activated; // true = Active, false = Inactive
    
    // Constructors
    public AdminUpdateUserDTO() {}
    
    public AdminUpdateUserDTO(String username, String email, String phone, Boolean activated) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.activated = activated;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Boolean getActivated() {
        return activated;
    }
    
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }
}
