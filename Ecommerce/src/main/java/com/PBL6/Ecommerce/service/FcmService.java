package com.PBL6.Ecommerce.service;

import com.google.firebase.messaging.*;
import com.PBL6.Ecommerce.repository.UserFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending FCM push notifications
 * Uses Firebase Admin SDK to send notifications to mobile devices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    
    private final UserFcmTokenRepository tokenRepository;
    private final UserFcmTokenService tokenService;
    
    /**
     * Send FCM notification to a specific user (all their active devices)
     */
    public void sendNotificationToUser(Long userId, String title, String body, Map<String, String> data) {
        try {
            List<String> tokens = tokenService.getActiveTokens(userId);
            
            if (tokens.isEmpty()) {
                log.debug("No active FCM tokens found for user {}", userId);
                return;
            }
            
            // Build notification
            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
            
            // Add default data if null
            if (data == null) {
                data = new HashMap<>();
            }
            
            // Send to each token
            for (String token : tokens) {
                sendToToken(token, notification, data);
            }
            
            log.info("Sent FCM notification to {} device(s) for user {}", tokens.size(), userId);
            
        } catch (Exception e) {
            log.error("Error sending FCM notification to user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * Send FCM notification to multiple users
     */
    public void sendNotificationToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        for (Long userId : userIds) {
            sendNotificationToUser(userId, title, body, data);
        }
    }
    
    /**
     * Send to a single FCM token
     */
    private void sendToToken(String token, Notification notification, Map<String, String> data) {
        try {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .build())
                    .build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setSound("default")
                        .build())
                    .build())
                .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Successfully sent FCM message: {}", response);
            
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token {}: {}", 
                token.substring(0, Math.min(20, token.length())), e.getMessage());
            
            // If token is invalid, deactivate it
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deactivateInvalidToken(token);
            }
        } catch (Exception e) {
            log.error("Unexpected error sending FCM: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Deactivate invalid token
     */
    private void deactivateInvalidToken(String token) {
        try {
            tokenRepository.findByUser_IdAndDeviceId(null, token)
                .ifPresent(t -> {
                    t.setIsActive(false);
                    tokenRepository.save(t);
                    log.info("Deactivated invalid FCM token");
                });
        } catch (Exception e) {
            log.warn("Could not deactivate invalid token: {}", e.getMessage());
        }
    }
    
    /**
     * Send notification with orderId data
     */
    public void sendOrderNotification(Long userId, String title, String body, Long orderId, String notificationType) {
        Map<String, String> data = new HashMap<>();
        if (orderId != null) {
            data.put("orderId", orderId.toString());
        }
        data.put("type", notificationType);
        data.put("click_action", "OPEN_ORDER");
        
        sendNotificationToUser(userId, title, body, data);
    }
}
