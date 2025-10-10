package com.ecommerce.sportcommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SellerRequestOtpDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phone,
    
    @NotBlank(message = "Shop name is required")
    String shopName,
    
    @NotBlank(message = "Shop address is required")
    String shopAddress,
    
    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^[A-Z0-9]{6,10}$", message = "Tax ID must be 6-10 alphanumeric characters")
    String taxId,
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "SELLER", message = "Role must be SELLER")
    String role
) {}
