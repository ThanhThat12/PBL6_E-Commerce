package com.PBL6.Ecommerce.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile information
 * Used for PUT /api/profile
 * All fields are optional - only provided fields will be updated
 */
@Schema(description = "Profile update request with optional fields")
public class UpdateUserProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name", example = "John Doe", required = false)
    private String fullName;

    @Email(message = "Email is invalid")
    @Size(max = 100, message = "Email too long")
    @Schema(description = "Email address", example = "john@example.com", required = false)
    private String email;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    @Schema(description = "Phone number (Vietnamese format)", example = "0901234567", required = false)
    private String phoneNumber;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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
}