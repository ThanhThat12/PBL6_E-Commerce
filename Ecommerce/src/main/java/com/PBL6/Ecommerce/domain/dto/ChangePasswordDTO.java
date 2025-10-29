package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {
    
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    // Constructors
    public ChangePasswordDTO() {}
    
    public ChangePasswordDTO(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
