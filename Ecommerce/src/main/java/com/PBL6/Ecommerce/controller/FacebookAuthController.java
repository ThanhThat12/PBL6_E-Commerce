package com.PBL6.Ecommerce.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.RefreshToken;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AuthTokenResponse;
import com.PBL6.Ecommerce.domain.dto.FacebookLoginDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.FacebookAuthService;
import com.PBL6.Ecommerce.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class FacebookAuthController {

    private final FacebookAuthService facebookAuthService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public FacebookAuthController(FacebookAuthService facebookAuthService,
                                  RefreshTokenService refreshTokenService,
                                  UserRepository userRepository) {
        this.facebookAuthService = facebookAuthService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }
    
    @PostMapping("authenticate/facebook")
    public ResponseEntity<ResponseDTO<AuthTokenResponse>> loginWithFacebook(
            @RequestBody FacebookLoginDTO dto,
            HttpServletRequest request) {
        
        // Authenticate with Facebook and get access token + user info
        Map<String, Object> authResult = facebookAuthService.loginWithFacebookAndGetUser(dto);
        String accessToken = (String) authResult.get("accessToken");
        User user = (User) authResult.get("user");
        
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
                "Login with Facebook successful",
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
