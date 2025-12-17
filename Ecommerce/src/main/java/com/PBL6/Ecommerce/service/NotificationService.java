package com.PBL6.Ecommerce.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.notification.Notification;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.repository.NotificationRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending real-time notifications via WebSocket and FCM
 * Persists notifications to database for history
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    
    /**
     * Gửi notification cho buyer (lưu DB + gửi WebSocket + FCM)
     */
    @Transactional
    public void sendOrderNotification(Long userId, String type, String message) {
        sendOrderNotification(userId, type, message, null);
    }
    
    /**
     * Gửi notification cho buyer với orderId (lưu DB + gửi WebSocket + FCM)
     */
    @Transactional
    public void sendOrderNotification(Long userId, String type, String message, Long orderId) {
        // 1. Lưu vào database
        Notification savedNotification = null;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setType(type);
                notification.setMessage(message);
                notification.setOrderId(orderId);
                notification.setIsRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                
                savedNotification = notificationRepository.save(notification);
                System.out.println("💾 Saved notification to DB for user: " + userId + " (ID: " + savedNotification.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to save notification to DB: " + e.getMessage());
            // Continue to send via WebSocket even if DB save fails
        }
        
        // 2. Gửi realtime qua WebSocket với notification object từ DB (có id)
        String destination = "/topic/orderws/" + userId;
        
        if (savedNotification != null) {
            // ✅ Gửi notification object từ DB (có id, createdAt, read status)
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", savedNotification.getId());
            notificationData.put("type", savedNotification.getType());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("orderId", savedNotification.getOrderId());
            notificationData.put("read", savedNotification.getIsRead());
            notificationData.put("createdAt", savedNotification.getCreatedAt());
            
            messagingTemplate.convertAndSend(destination, notificationData);
            System.out.println("📤 Sent BUYER notification to: " + destination + " (ID: " + savedNotification.getId() + ")");
        } else {
            // Fallback nếu không save được vào DB
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", type);
            notificationData.put("message", message);
            notificationData.put("timestamp", LocalDateTime.now());
            notificationData.put("userType", "BUYER");
            if (orderId != null) {
                notificationData.put("orderId", orderId);
            }
            messagingTemplate.convertAndSend(destination, notificationData);
        }
        System.out.println("📤 Message: " + message);
        
        // 3. Gửi FCM push notification (mobile)
        try {
            String title = "Thông báo đơn hàng";
            fcmService.sendOrderNotification(userId, title, message, orderId, type);
            System.out.println("📱 Sent FCM push notification to user: " + userId);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send FCM: " + e.getMessage());
            // Don't fail the whole notification if FCM fails
        }
    }
    
    /**
     * Gửi notification cho admin (lưu DB + gửi WebSocket + FCM)
     */
    @Transactional
    public void sendAdminNotification(String type, String message, Long orderId) {
        try {
            // Tìm admin user (chỉ có 1 admin trong hệ thống)
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            
            if (admins.isEmpty()) {
                System.out.println("⚠️ No admin user found");
                return;
            }
            
            User admin = admins.get(0); // Lấy admin đầu tiên
            System.out.println("📤 Sending notification to admin: " + admin.getId());
            
            // 1. Lưu vào database
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setType(type);
            notification.setMessage(message);
            notification.setOrderId(orderId);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            System.out.println("💾 Saved admin notification to DB (ID: " + savedNotification.getId() + ")");
            
            // 2. Gửi realtime qua WebSocket
            String destination = "/topic/admin/" + admin.getId();
            
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", savedNotification.getId());
            notificationData.put("type", savedNotification.getType());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("orderId", savedNotification.getOrderId());
            notificationData.put("read", savedNotification.getIsRead());
            notificationData.put("createdAt", savedNotification.getCreatedAt());
            
            messagingTemplate.convertAndSend(destination, notificationData);
            System.out.println("📤 Sent ADMIN notification to: " + destination);
            System.out.println("📤 Message: " + message);
            
            // 3. Gửi FCM push notification (mobile)
            try {
                String title = "Thông báo quản trị";
                fcmService.sendOrderNotification(admin.getId(), title, message, orderId, type);
                System.out.println("📱 Sent FCM push notification to admin: " + admin.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send FCM to admin: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send admin notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gửi notification cho seller (lưu DB + gửi WebSocket + FCM)
     */
    @Transactional
    public void sendSellerNotification(Long sellerId, String type, String message, Long orderId) {
        // 1. Lưu vào database
        Notification savedNotification = null;
        try {
            User seller = userRepository.findById(sellerId).orElse(null);
            if (seller != null) {
                Notification notification = new Notification();
                notification.setUser(seller);
                notification.setType(type);
                notification.setMessage(message);
                notification.setOrderId(orderId);
                notification.setIsRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                
                savedNotification = notificationRepository.save(notification);
                System.out.println("💾 Saved notification to DB for seller: " + sellerId + " (ID: " + savedNotification.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to save notification to DB: " + e.getMessage());
            // Continue to send via WebSocket even if DB save fails
        }
        
        // 2. Gửi realtime qua WebSocket với notification object từ DB (có id)
        String destination = "/topic/sellerws/" + sellerId;
        
        if (savedNotification != null) {
            // ✅ Gửi notification object từ DB (có id, createdAt, read status)
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", savedNotification.getId());
            notificationData.put("type", savedNotification.getType());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("orderId", savedNotification.getOrderId());
            notificationData.put("read", savedNotification.getIsRead());
            notificationData.put("createdAt", savedNotification.getCreatedAt());
            
            messagingTemplate.convertAndSend(destination, notificationData);
            System.out.println("📤 Sent SELLER notification to: " + destination + " (ID: " + savedNotification.getId() + ")");
        } else {
            // Fallback nếu không save được vào DB
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", type);
            notificationData.put("message", message);
            notificationData.put("timestamp", LocalDateTime.now());
            notificationData.put("userType", "SELLER");
            notificationData.put("orderId", orderId);
            messagingTemplate.convertAndSend(destination, notificationData);
        }
        System.out.println("📤 Message: " + message);
        
        // 3. Gửi FCM push notification (mobile)
        try {
            String title = "Thông báo người bán";
            fcmService.sendOrderNotification(sellerId, title, message, orderId, type);
            System.out.println("📱 Sent FCM push notification to seller: " + sellerId);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send FCM to seller: " + e.getMessage());
        }
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
}