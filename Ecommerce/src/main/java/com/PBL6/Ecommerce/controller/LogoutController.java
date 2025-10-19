package com.PBL6.Ecommerce.controller;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.RefreshTokenService;
import com.PBL6.Ecommerce.service.TokenBlacklistService;
import com.PBL6.Ecommerce.util.TokenProvider;

/**
 * Logout Controller for token revocation and logout functionality
 * Prompt 4: Token Revocation & Logout Endpoint
 */
@RestController
@RequestMapping("/api/auth")
public class LogoutController {
    
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    
    public LogoutController(RefreshTokenService refreshTokenService,
                           TokenBlacklistService tokenBlacklistService,
                           TokenProvider tokenProvider,
                           UserRepository userRepository) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }
    
    /**
     * Logout endpoint - revokes both access and refresh tokens
     * Requires valid JWT token in Authorization header
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith("Bearer ")) {
                ResponseDTO<Void> response = new ResponseDTO<>(401, "Unauthorized", "Authorization header missing or invalid", null);
                return ResponseEntity.status(401).body(response);
            }
            
            // Extract token from "Bearer <token>"
            String accessToken = authHeader.substring("Bearer ".length()).trim();
            
            try {
                // Extract JTI from token for blacklisting
                String jti = tokenProvider.getJtiFromJwt(accessToken);
                Date expirationDate = tokenProvider.getExpirationFromJwt(accessToken);
                long expirationTime = expirationDate.getTime();
                
                // Add access token to blacklist
                tokenBlacklistService.blacklistToken(jti, expirationTime);
            } catch (Exception e) {
                ResponseDTO<Void> response = new ResponseDTO<>(401, "Unauthorized", "Invalid or expired token: " + e.getMessage(), null);
                return ResponseEntity.status(401).body(response);
            }
            
            ResponseDTO<Void> response = new ResponseDTO<>(200, null, "Đăng xuất thành công", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ResponseDTO<Void> response = new ResponseDTO<>(400, e.getMessage(), "Đăng xuất thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Logout from all devices - revokes all refresh tokens for the user
     * Requires authentication
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ResponseDTO<Void>> logoutAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith("Bearer ")) {
                ResponseDTO<Void> response = new ResponseDTO<>(401, "Unauthorized", "Authorization header missing or invalid", null);
                return ResponseEntity.status(401).body(response);
            }
            
            // Extract token
            String accessToken = authHeader.substring("Bearer ".length()).trim();
            
            try {
                // Blacklist current access token
                String jti = tokenProvider.getJtiFromJwt(accessToken);
                Date expirationDate = tokenProvider.getExpirationFromJwt(accessToken);
                long expirationTime = expirationDate.getTime();
                tokenBlacklistService.blacklistToken(jti, expirationTime);
                
                // Get username from token and find user
                String username = tokenProvider.getUsernameFromJwt(accessToken);
                User user = userRepository.findOneByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                // Revoke all refresh tokens for this user
                refreshTokenService.revokeAllUserTokens(user.getId());
                
                // Mark all user tokens issued before now as invalid
                tokenBlacklistService.logoutAllDevices(user.getId());
                
            } catch (Exception e) {
                ResponseDTO<Void> response = new ResponseDTO<>(401, "Unauthorized", "Invalid or expired token: " + e.getMessage(), null);
                return ResponseEntity.status(401).body(response);
            }
            
            ResponseDTO<Void> response = new ResponseDTO<>(200, null, "Đã đăng xuất trên tất cả các thiết bị", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ResponseDTO<Void> response = new ResponseDTO<>(400, e.getMessage(), "Đăng xuất thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
