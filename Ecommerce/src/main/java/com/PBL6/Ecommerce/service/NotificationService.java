package com.PBL6.Ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.UserFCMToken;
import com.PBL6.Ecommerce.repository.UserFCMTokenRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending real-time notifications via WebSocket
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private FCMService fcmService;

    @Autowired
    private UserFCMTokenRepository fcmTokenRepository;
    
    // Gửi cho buyer
    public void sendOrderNotification(Long userId, String type, String message) {
        String destination = "/topic/orderws/" + userId;
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("userType", "BUYER");
        
        messagingTemplate.convertAndSend(destination, notification);
        System.out.println("📤 Sent BUYER notification to: " + destination);
        System.out.println("📤 Message: " + message);
    }
    
    // Gửi cho seller
    public void sendSellerNotification(Long sellerId, String type, String message, Long orderId) {
        String destination = "/topic/sellerws/" + sellerId;
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("userType", "SELLER");
        notification.put("orderId", orderId);
        
        messagingTemplate.convertAndSend(destination, notification);
        System.out.println("📤 Sent SELLER notification to: " + destination);
        System.out.println("📤 Message: " + message);
    }
    
    // ===== LEGACY METHODS FOR BACKWARD COMPATIBILITY =====
    
    // OrderService đang gọi method này
    public void sendOrderConfirmationNotification(Long userId, Long orderId) {
        sendOrderNotification(userId, "ORDER_CONFIRMED", 
            "Đơn hàng #" + orderId + " đã được xác nhận");
    }
    
    // OrderService đang gọi method này  
    public void sendOrderStatusUpdateNotification(Long userId, Long orderId, String status) {
        String message = getMessageForStatus(orderId, status);
        String notificationType = getNotificationTypeForStatus(status);
        sendOrderNotification(userId, notificationType, message);
    }
    
    // OrderService đang gọi method này
    public void sendOrderShippingNotification(Long userId, Long orderId, String trackingCode) {
        String message = "Đơn hàng #" + orderId + " đang được giao. Mã vận đơn: " + trackingCode;
        sendOrderNotification(userId, "ORDER_SHIPPING", message);
    }
    
    // OrderService đang gọi method này
    public void sendOrderCompletedNotification(Long userId, Long orderId) {
        sendOrderNotification(userId, "ORDER_COMPLETED", 
            "Đơn hàng #" + orderId + " đã hoàn thành");
    }
    
    // OrderService đang gọi method này
    public void sendOrderCancelledNotification(Long userId, Long orderId, String reason) {
        String message = "Đơn hàng #" + orderId + " đã bị hủy";
        if (reason != null && !reason.trim().isEmpty()) {
            message += ". Lý do: " + reason;
        }
        sendOrderNotification(userId, "ORDER_CANCELLED", message);
    }
    
    // TestController đang gọi method này
    public void sendBroadcastNotification(String type, String message) {
        broadcastNotification(type, message);
    }
    
    // ===== NEW METHODS =====
    
    // Gửi cho cả buyer và seller - SỬ DỤNG FIELD NAMES ĐÚNG
    public void sendOrderNotificationToAll(Order order, String type, String buyerMessage, String sellerMessage) {
        // Gửi cho buyer - sử dụng field trực tiếp
        Long buyerId = order.getUser().getId(); // Đổi từ getUserId() thành getId()
        sendOrderNotification(buyerId, type, buyerMessage);
        
        // Gửi cho seller - handle safely với variant thay vì product
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                try {
                    // Sử dụng navigation an toàn qua variant
                    if (item.getVariant() != null && 
                        item.getVariant().getProduct() != null &&
                        item.getVariant().getProduct().getShop() != null && 
                        item.getVariant().getProduct().getShop().getOwner() != null) {
                        
                        Long sellerId = item.getVariant().getProduct().getShop().getOwner().getId();
                        sendSellerNotification(sellerId, type, sellerMessage, order.getId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error sending seller notification: " + e.getMessage());
                }
            });
        }
    }
    
    // Test cho seller
    public void testSellerNotification(Long sellerId) {
        sendSellerNotification(sellerId, "NEW_ORDER", "Bạn có đơn hàng mới #12345", 12345L);
    }
    
    // Broadcast
    public void broadcastNotification(String type, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("userType", "BROADCAST");
        
        messagingTemplate.convertAndSend("/topic/broadcast", notification);
        System.out.println("📢 Broadcast notification: " + message);
    }
    
    // Helper methods
    private String getMessageForStatus(Long orderId, String status) {
        switch (status.toUpperCase()) {
            case "CONFIRMED": return "Đơn hàng #" + orderId + " đã được xác nhận";
            case "SHIPPING": return "Đơn hàng #" + orderId + " đang được giao";
            case "COMPLETED": return "Đơn hàng #" + orderId + " đã hoàn thành";
            case "CANCELLED": return "Đơn hàng #" + orderId + " đã bị hủy";
            default: return "Đơn hàng #" + orderId + " đã được cập nhật";
        }
    }
    
    private String getNotificationTypeForStatus(String status) {
        switch (status.toUpperCase()) {
            case "CONFIRMED": return "ORDER_CONFIRMED";
            case "SHIPPING": return "ORDER_SHIPPING";
            case "COMPLETED": return "ORDER_COMPLETED";
            case "CANCELLED": return "ORDER_CANCELLED";
            default: return "ORDER_STATUS_UPDATE";
        }
    }
    
    // Health check
    public String healthCheck() {
        return "NotificationService is running at " + LocalDateTime.now();
    }

    public void sendOrderNotificationWithFCM(Long userId, String title, String message, Long orderId) {
        // Get user's FCM tokens
        List<UserFCMToken> tokens = fcmTokenRepository.findActiveTokensByUserId(userId);
        
        if (!tokens.isEmpty()) {
            Map<String, String> data = new HashMap<>();
            data.put("orderId", orderId.toString());
            data.put("type", "ORDER_UPDATE");
            
            for (UserFCMToken token : tokens) {
                fcmService.sendPushNotification(
                    token.getFcmToken(),
                    title,
                    message,
                    data
                );
            }
        }
    }
        
}