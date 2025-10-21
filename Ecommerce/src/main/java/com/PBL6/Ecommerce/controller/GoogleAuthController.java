package com.PBL6.Ecommerce.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.RefreshToken;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AuthTokenResponse;
import com.PBL6.Ecommerce.domain.dto.GoogleLoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.GoogleAuthService;
import com.PBL6.Ecommerce.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public GoogleAuthController(GoogleAuthService googleAuthService,
                                RefreshTokenService refreshTokenService,
                                UserRepository userRepository) {
        this.googleAuthService = googleAuthService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/authenticate/google")
    public ResponseEntity<ResponseDTO<AuthTokenResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginDTO dto,
            HttpServletRequest request) throws Exception {
        
        // Authenticate with Google and get access token
        String accessToken = googleAuthService.loginWithGoogle(dto);
        
        // Extract user from token (GoogleAuthService already created/found user)
        String email = googleAuthService.getEmailFromToken(dto.getIdToken());
        User user = userRepository.findOneByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after Google authentication"));
        
        // Create refresh token with IP and User-Agent tracking
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(),
                ipAddress,
                userAgent
        );
        
        // Build user info DTO
        UserInfoDTO userInfo = new UserInfoDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPhoneNumber(),
                user.getRole().name()
        );
        
        // Create complete auth response with both tokens
        long expiresIn = jwtExpirationMs / 1000; // Convert to seconds
        AuthTokenResponse tokenResponse = new AuthTokenResponse(
                accessToken,
                refreshToken.getToken(),
                expiresIn,
                userInfo
        );
        
        ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                200,
                null,
                "Google login successful",
                tokenResponse
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extract client IP address from request, handling proxies
     */
    private String extractIpAddress(HttpServletRequest request) {
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