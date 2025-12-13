package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.UserFCMToken;
import com.PBL6.Ecommerce.domain.dto.UserFCMTokenDTO;
import com.PBL6.Ecommerce.repository.UserFCMTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/fcm")
@CrossOrigin(origins = "*")
public class UserFCMTokenController {

    @Autowired
    private UserFCMTokenRepository fcmTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestBody UserFCMTokenDTO dto) {
        try {
            // Check if token already exists
            Optional<UserFCMToken> existingToken = fcmTokenRepository.findByFcmToken(dto.getFcmToken());
            
            UserFCMToken token;
            if (existingToken.isPresent()) {
                // Update existing token
                token = existingToken.get();
                token.setUserId(dto.getUserId());
                token.setDeviceType(dto.getDeviceType());
                token.setDeviceId(dto.getDeviceId());
                token.setIsActive(true);
                token.setUpdatedAt(LocalDateTime.now());
            } else {
                // Create new token
                token = new UserFCMToken();
                token.setUserId(dto.getUserId());
                token.setFcmToken(dto.getFcmToken());
                token.setDeviceType(dto.getDeviceType());
                token.setDeviceId(dto.getDeviceId());
                token.setIsActive(true);
            }
            
            fcmTokenRepository.save(token);
            
            return ResponseEntity.ok().body("FCM token registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregisterToken(@RequestParam String fcmToken) {
        try {
            fcmTokenRepository.deleteByFcmToken(fcmToken);
            return ResponseEntity.ok().body("FCM token unregistered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> unregisterUserTokens(@PathVariable Long userId) {
        try {
            fcmTokenRepository.deleteByUserId(userId);
            return ResponseEntity.ok().body("All FCM tokens for user deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}