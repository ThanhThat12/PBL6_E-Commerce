package com.ecommerce.sportcommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resending OTP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Loại OTP không được để trống")
    private String otpType; // "REGISTRATION", "PASSWORD_RESET", etc.
}
