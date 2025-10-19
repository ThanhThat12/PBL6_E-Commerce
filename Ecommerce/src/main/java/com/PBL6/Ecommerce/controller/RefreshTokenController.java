package com.PBL6.Ecommerce.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.RefreshToken;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AuthTokenResponse;
import com.PBL6.Ecommerce.domain.dto.RefreshTokenRequest;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.service.RefreshTokenService;
import com.PBL6.Ecommerce.util.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Prompt 4: Token Revocation - Refresh Token Controller
 * Handles token refresh operations for obtaining new access tokens
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class RefreshTokenController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Refresh access token using refresh token
     * Endpoint: POST /api/auth/refresh
     * 
     * @param refreshTokenRequest contains the refresh token
     * @param request HTTP request to extract IP and User-Agent
     * @return new access token and optionally new refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<AuthTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        try {
            String refreshTokenString = refreshTokenRequest.getRefreshToken();
            
            // Trim token to remove any whitespace
            if (refreshTokenString != null) {
                refreshTokenString = refreshTokenString.trim();
            } else {
                ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(401, "Unauthorized", "Refresh token missing", null);
                return ResponseEntity.status(401).body(response);
            }

            // Validate refresh token exists and is not revoked
            boolean isValid = refreshTokenService.isTokenValid(refreshTokenString);
            if (!isValid) {
                ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                        401,
                        "Unauthorized",
                        "Refresh token không hợp lệ hoặc đã hết hạn",
                        null
                );
                return ResponseEntity.status(401).body(response);
            }

            // Get stored refresh token entity
            Optional<RefreshToken> storedTokenOpt = refreshTokenService.findValidToken(refreshTokenString);
            if (!storedTokenOpt.isPresent()) {
                ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                        401,
                        "Unauthorized",
                        "Refresh token không tìm thấy",
                        null
                );
                return ResponseEntity.status(401).body(response);
            }

            RefreshToken storedToken = storedTokenOpt.get();
            User user = storedToken.getUser();
            if (user == null) {
                ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                        401,
                        "Unauthorized",
                        "Người dùng không tìm thấy",
                        null
                );
                return ResponseEntity.status(401).body(response);
            }

            // Generate new access token with role information
            String newAccessToken = tokenProvider.createToken(user.getUsername(), user.getRole().name());

            // Optional: Rotate refresh token (revoke old, create new)
            String ipAddress = extractIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            RefreshToken newRefreshToken = refreshTokenService.rotateToken(
                    refreshTokenString,
                    ipAddress,
                    userAgent
            );

            // Create response with both tokens
            long expiresIn = jwtExpirationMs / 1000; // Convert to seconds
            
            // Create UserInfoDTO for response
            UserInfoDTO userInfo = new UserInfoDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPhoneNumber(),
                    user.getRole().name()
            );

            AuthTokenResponse tokenResponse = new AuthTokenResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    expiresIn,
                    userInfo
            );

            ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                    200,
                    null,
                    "Làm mới token thành công",
                    tokenResponse
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                    400,
                    e.getMessage(),
                    "Yêu cầu không hợp lệ",
                    null
            );
            return ResponseEntity.badRequest().body(response);

        } catch (RuntimeException e) {
            ResponseDTO<AuthTokenResponse> response = new ResponseDTO<>(
                    500,
                    e.getMessage(),
                    "Lỗi làm mới token",
                    null
            );
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Extract client IP address from request
     * Handles cases where client is behind proxy/load balancer
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        // Get first IP if multiple IPs are present (comma-separated)
        if (ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    /**
     * Get remaining validity of refresh token
     * Endpoint: GET /api/auth/refresh/remaining/{tokenId}
     * Useful for frontend to determine when to refresh
     */
    @GetMapping("/refresh/remaining/{tokenId}")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getTokenRemaining(@PathVariable Long tokenId) {
        try {
            Optional<RefreshToken> tokenOpt = refreshTokenService.getTokenById(tokenId);
            if (!tokenOpt.isPresent()) {
                ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(
                        404,
                        "Not Found",
                        "Token không tìm thấy",
                        null
                );
                return ResponseEntity.status(404).body(response);
            }

            RefreshToken token = tokenOpt.get();
            LocalDateTime expiryDateTime = token.getExpiryDate();
            LocalDateTime nowDateTime = LocalDateTime.now();
            
            long remainingMs = java.time.temporal.ChronoUnit.MILLIS.between(nowDateTime, expiryDateTime);
            long remainingSeconds = remainingMs / 1000;
            
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("remaining_seconds", remainingSeconds);
            data.put("expired", remainingMs <= 0);
            data.put("expiry_date", expiryDateTime.toString());

            ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(
                    200,
                    null,
                    "Lấy thông tin token thành công",
                    data
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ResponseDTO<Map<String, Object>> response = new ResponseDTO<>(
                    500,
                    e.getMessage(),
                    "Lỗi lấy thông tin token",
                    null
            );
            return ResponseEntity.status(500).body(response);
        }
    }
}
