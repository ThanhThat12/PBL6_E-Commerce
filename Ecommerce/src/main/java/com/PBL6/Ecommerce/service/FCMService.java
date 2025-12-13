package com.PBL6.Ecommerce.service;

import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FCMService {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);

    /**
     * Send push notification to specific device
     */
    public void sendPushNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setColor("#FF6347")
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("✅ Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            logger.error("❌ Error sending FCM notification", e);
        }
    }

    /**
     * Send notification to multiple devices
     */
    public void sendPushNotificationToMultipleDevices(
            java.util.List<String> fcmTokens, 
            String title, 
            String body, 
            Map<String, String> data
    ) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            logger.info("✅ Successfully sent {} notifications", response.getSuccessCount());
        } catch (Exception e) {
            logger.error("❌ Error sending multicast FCM notification", e);
        }
    }
}