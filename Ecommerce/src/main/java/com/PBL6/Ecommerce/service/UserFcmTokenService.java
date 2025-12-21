package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.entity.notification.UserFcmToken;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.repository.UserFcmTokenRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing FCM device tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserFcmTokenService {
    
    private final UserFcmTokenRepository tokenRepository;
    private final UserRepository userRepository;
    
    /**
     * Register or update FCM token for a user
     */
    @Transactional
    public UserFcmToken registerToken(Long userId, String fcmToken, String deviceId, String deviceType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Check if token already exists for this user and device
        UserFcmToken token = tokenRepository.findByUser_IdAndDeviceId(userId, deviceId)
            .orElse(new UserFcmToken());
        
        token.setUser(user);
        token.setFcmToken(fcmToken);
        token.setDeviceId(deviceId);
        token.setDeviceType(deviceType);
        token.setIsActive(true);
        
        UserFcmToken saved = tokenRepository.save(token);
        log.info("Registered/updated FCM token for user {} on device {}", userId, deviceId);
        
        return saved;
    }
    
    /**
     * Unregister a device token
     */
    @Transactional
    public void unregisterToken(Long userId, String deviceId) {
        int updated = tokenRepository.deactivateToken(userId, deviceId);
        log.info("Deactivated {} FCM token(s) for user {} device {}", updated, userId, deviceId);
    }
    
    /**
     * Get all active FCM tokens for a user
     */
    public List<String> getActiveTokens(Long userId) {
        return tokenRepository.findByUser_IdAndIsActiveTrue(userId)
            .stream()
            .map(UserFcmToken::getFcmToken)
            .collect(Collectors.toList());
    }
    
    /**
     * Cleanup inactive tokens older than 30 days
     */
    @Transactional
    public int cleanupInactiveTokens() {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(30);
        int deleted = tokenRepository.deleteInactiveTokens(cutoff);
        log.info("Cleaned up {} inactive FCM tokens", deleted);
        return deleted;
    }
}
