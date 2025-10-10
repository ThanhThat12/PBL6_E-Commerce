package com.ecommerce.sportcommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BuyerRequestOtpDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "BUYER", message = "Role must be BUYER")
    String role
) {}
