package com.ecommerce.sportcommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for OTP verification (Step 2: Complete registration)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mã OTP không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải có 6 ký tự")
    private String otpCode;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
}
