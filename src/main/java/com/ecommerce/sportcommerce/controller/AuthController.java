package com.ecommerce.sportcommerce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.sportcommerce.dto.request.LoginRequest;
import com.ecommerce.sportcommerce.dto.request.LogoutRequest;
import com.ecommerce.sportcommerce.dto.request.RefreshTokenRequest;
import com.ecommerce.sportcommerce.dto.request.RegisterRequest;
import com.ecommerce.sportcommerce.dto.request.ResendOtpRequest;
import com.ecommerce.sportcommerce.dto.request.VerifyOtpRequest;
import com.ecommerce.sportcommerce.dto.response.ApiResponse;
import com.ecommerce.sportcommerce.dto.response.AuthResponse;
import com.ecommerce.sportcommerce.entity.User;
import com.ecommerce.sportcommerce.service.AuthService;
import com.ecommerce.sportcommerce.security.UserPrincipal;

import jakarta.validation.Valid;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Step 1: Register - Send OTP
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Mã OTP đã được gửi đến email của bạn", response));
    }
    
    /**
     * Step 2: Verify OTP and complete registration
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        logger.info("OTP verification request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.verifyOtpAndCreateUser(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", response));
    }
    
    /**
     * Resend OTP
     * POST /api/auth/resend-otp
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        logger.info("Resend OTP request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.resendOtp(request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Mã OTP mới đã được gửi", response));
    }
    
    /**
     * Login with email and password
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Đăng nhập thành công", response));
    }
    
    /**
     * Refresh access token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Refresh token request received");
        
        AuthResponse response = authService.refreshToken(request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Token đã được làm mới", response));
    }
    
    /**
     * Logout user
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody(required = false) LogoutRequest request) {
        
        User user = userPrincipal.getUser();
        logger.info("Logout request received for user: {}", user.getEmail());
        
        if (request == null) {
            request = new LogoutRequest();
        }
        
        authService.logout(user, request);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Đăng xuất thành công"));
    }
}
