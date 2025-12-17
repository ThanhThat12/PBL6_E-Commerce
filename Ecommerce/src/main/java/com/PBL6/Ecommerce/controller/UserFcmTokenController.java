package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.UserFcmTokenDTO;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.UserFcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for FCM device token management
 */
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class UserFcmTokenController {
    
    private final UserFcmTokenService tokenService;
    private final UserRepository userRepository;
    
    /**
     * Register or update FCM token for current user
     * POST /api/fcm/token
     */
    @PostMapping("/token")
    public ResponseEntity<?> registerToken(
            @RequestBody UserFcmTokenDTO tokenDTO,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // Get user from JWT email claim
            String email = jwt.getClaimAsString("email");
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            tokenService.registerToken(
                user.getId(),
                tokenDTO.getFcmToken(),
                tokenDTO.getDeviceId(),
                tokenDTO.getDeviceType()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "FCM token registered successfully");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to register FCM token: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Unregister FCM token for current user
     * DELETE /api/fcm/token/{deviceId}
     */
    @DeleteMapping("/token/{deviceId}")
    public ResponseEntity<?> unregisterToken(
            @PathVariable String deviceId,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // Get user from JWT email claim
            String email = jwt.getClaimAsString("email");
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            tokenService.unregisterToken(user.getId(), deviceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FCM token unregistered successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to unregister FCM token: " + e.getMessage()
            ));
        }
    }
}
