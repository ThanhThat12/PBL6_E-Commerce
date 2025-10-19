package com.PBL6.Ecommerce.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.AuthTokenResponse;
import com.PBL6.Ecommerce.domain.dto.LoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.service.AuthService;
import com.PBL6.Ecommerce.service.LoginAttemptService;
import com.PBL6.Ecommerce.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final com.PBL6.Ecommerce.repository.UserRepository userRepository;
    private final LoginAttemptService loginAttemptService; // Prompt 2: Rate limiting
    private final RefreshTokenService refreshTokenService; // Prompt 4: Token Revocation

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public AuthController(AuthService authService, 
                         com.PBL6.Ecommerce.repository.UserRepository userRepository,
                         LoginAttemptService loginAttemptService,
                         RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ResponseDTO<?>> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        try {
            // Prompt 2: Extract client IP and check global rate limiting
            String clientIp = getClientIp(request);
            
            if (!loginAttemptService.isGlobalRequestAllowed(clientIp)) {
                ResponseDTO<?> response = new ResponseDTO<>(429, "Too Many Requests", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.", null);
                return ResponseEntity.status(429).body(response);
            }
            loginAttemptService.recordGlobalAttempt(clientIp);
            
            String accessToken = authService.authenticate(loginDTO);
            
            // Lấy user info
            com.PBL6.Ecommerce.domain.User user = userRepository.findOneByUsername(loginDTO.getUsername().toLowerCase()).orElse(null);
            
            if (user == null) {
                ResponseDTO<?> response = new ResponseDTO<>(400, "User not found", "Người dùng không tìm thấy", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Prompt 4: Create refresh token
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            com.PBL6.Ecommerce.domain.RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user.getId(), 
                    ipAddress, 
                    userAgent
            );
            
            // Create user info DTO
            UserInfoDTO userInfo = new UserInfoDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPhoneNumber(),
                    user.getRole().name()
            );
            
            // Create response with both access and refresh tokens
            long expiresIn = jwtExpirationMs / 1000; // Convert to seconds
            AuthTokenResponse tokenResponse = new AuthTokenResponse(
                    accessToken,
                    refreshToken.getToken(),
                    expiresIn,
                    userInfo
            );
            
            ResponseDTO<?> response = new ResponseDTO<>(200, null, "Đăng nhập thành công", tokenResponse);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("IP bị khóa") || e.getMessage().contains("quá nhiều lần")) {
                ResponseDTO<?> response = new ResponseDTO<>(429, "Too Many Requests", e.getMessage(), null);
                return ResponseEntity.status(429).body(response);
            }
            ResponseDTO<?> response = new ResponseDTO<>(400, e.getMessage(), "Đăng nhập thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Extract client IP address from request, handling proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
