package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateProfileDTO {
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;
    
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    
    // Constructors
    public UpdateProfileDTO() {}
    
    public UpdateProfileDTO(String email, String phoneNumber, String fullName) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
