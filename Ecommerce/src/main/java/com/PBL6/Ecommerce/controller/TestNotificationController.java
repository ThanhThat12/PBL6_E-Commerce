package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestNotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping("/notification/health")
    public String health() {
        return notificationService.healthCheck();
    }
    
    @PostMapping("/notification/{userId}")
    public String testNotification(@PathVariable Long userId) {
        // Sử dụng method có sẵn
        notificationService.sendOrderConfirmationNotification(userId, 12345L);
        return "Notification sent to user: " + userId;
    }
    
    @PostMapping("/notification/{userId}/custom")
    public String testCustomNotification(
            @PathVariable Long userId,
            @RequestParam String message) {
        notificationService.sendOrderNotification(userId, "CUSTOM", message);
        return "Custom notification sent to user: " + userId;
    }
    
    // Test cho seller
    @PostMapping("/seller-notification/{sellerId}")
    public String testSellerNotification(@PathVariable Long sellerId) {
        notificationService.testSellerNotification(sellerId);
        return "Seller notification sent to: " + sellerId;
    }
    
    @PostMapping("/broadcast")
    public String testBroadcast(@RequestParam String message) {
        // Sử dụng method đúng tên
        notificationService.sendBroadcastNotification("BROADCAST", message);
        return "Broadcast sent: " + message;
    }
    
    // Test specific status
    @PostMapping("/order/{orderId}/status/{status}/user/{userId}")
    public String testOrderStatusNotification(
            @PathVariable Long orderId,
            @PathVariable String status,
            @PathVariable Long userId) {
        notificationService.sendOrderStatusUpdateNotification(userId, orderId, status);
        return String.format("Status notification sent - Order: %d, Status: %s, User: %d", 
                            orderId, status, userId);
    }
}