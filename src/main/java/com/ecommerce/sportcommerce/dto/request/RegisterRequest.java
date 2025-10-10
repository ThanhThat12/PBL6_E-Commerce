package com.ecommerce.sportcommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration (Step 1: Send OTP)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 số")
    private String password;
    
    @Size(min = 3, max = 100, message = "Username phải từ 3-100 ký tự")
    private String username;
    
    private String firstName;
    
    private String lastName;
    
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", 
             message = "Số điện thoại không hợp lệ")
    private String phone;
}
