package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.*;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/webhook/ghn")
public class GhnWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(GhnWebhookController.class);
    
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GhnWebhookController(
            ShipmentRepository shipmentRepository, 
            OrderRepository orderRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Webhook nhận cập nhật trạng thái vận đơn từ GHN
     * POST /api/webhook/ghn/status
     * 
     * Payload mẫu từ GHN:
     * {
     *   "OrderCode": "GHN123456",
     *   "Status": "delivered",
     *   "StatusName": "Đã giao hàng",
     *   "Time": "2025-11-19 10:30:00"
     * }
     */
    @PostMapping("/status")
    @Transactional
    public Map<String,Object> updateStatus(@RequestBody Map<String,Object> payload) {
        try {
            String orderCode = (String) payload.get("OrderCode");
            String status = (String) payload.get("Status");
            
            logger.info("Received GHN webhook for order: {}, status: {}", orderCode, status);
            
            if (orderCode == null || status == null) {
                logger.warn("Missing OrderCode or Status in webhook payload");
                return Map.of("success", false, "error", "Missing OrderCode or Status");
            }
            
            // Tìm shipment theo mã vận đơn GHN
            Shipment shipment = shipmentRepository.findByGhnOrderCode(orderCode)
                .orElse(null);
            
            if (shipment == null) {
                logger.warn("Shipment not found for GHN order code: {}", orderCode);
                return Map.of("success", false, "error", "Shipment not found");
            }
            
            // Cập nhật trạng thái shipment
            String oldStatus = shipment.getStatus();
            shipment.setStatus(status);
            shipmentRepository.save(shipment);
            logger.info("Updated shipment {} status: {} → {}", shipment.getId(), oldStatus, status);
            
            // ✅ Cập nhật trạng thái đơn hàng tương ứng
            Order order = orderRepository.findById(shipment.getOrderId())
                .orElse(null);
            
            if (order != null) {
                Order.OrderStatus oldOrderStatus = order.getStatus();
                updateOrderStatusFromGhn(order, status);
                
                // Chỉ save nếu status thay đổi
                if (order.getStatus() != oldOrderStatus) {
                    orderRepository.save(order);
                    logger.info("✅ Updated order {} status: {} → {}", 
                        order.getId(), oldOrderStatus, order.getStatus());
                    
                    // ✅ Gửi notification cho buyer
                    sendOrderStatusNotification(order, status, payload);
                }
            } else {
                logger.warn("Order not found for shipment: {}", shipment.getId());
            }
            
            return Map.of("success", true, "message", "Status updated successfully");
        } catch (Exception e) {
            logger.error("Error processing GHN webhook: {}", e.getMessage(), e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    
    /**
     * Mapping trạng thái GHN sang trạng thái đơn hàng
     * Tài liệu GHN: https://api.ghn.vn/home/docs/detail?id=74
     */
    private void updateOrderStatusFromGhn(Order order, String ghnStatus) {
        switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick":
            case "picking":
                // Đơn đang được lấy hàng
                if (order.getStatus() == Order.OrderStatus.PROCESSING) {
                    order.setStatus(Order.OrderStatus.SHIPPING);
                }
                break;
                
            case "picked":
            case "storing":
            case "transporting":
            case "sorting":
            case "delivering":
                // Đơn đang trên đường giao
                order.setStatus(Order.OrderStatus.SHIPPING);
                break;
                
            case "delivered":
                // Đã giao thành công
                order.setStatus(Order.OrderStatus.COMPLETED);
                break;
                
            case "delivery_fail":
            case "waiting_to_return":
            case "return":
            case "return_transporting":
            case "return_sorting":
            case "returning":
                // Giao thất bại hoặc đang hoàn trả
                // Giữ nguyên SHIPPING, chờ xử lý thủ công
                break;
                
            case "returned":
                // Đã hoàn trả về shop - giữ nguyên status, cần xử lý thủ công
                logger.warn("Order {} returned to shop, status unchanged", order.getId());
                break;
                
            case "exception":
            case "damage":
            case "lost":
                // Bất thường: hư hỏng, thất lạc
                // Giữ nguyên, cần xử lý thủ công
                logger.warn("Order {} has exception status from GHN: {}", order.getId(), ghnStatus);
                break;
                
            case "cancel":
                // Đơn bị hủy
                order.setStatus(Order.OrderStatus.CANCELLED);
                break;
                
            default:
                logger.info("Unknown GHN status: {}, no order status update", ghnStatus);
                break;
        }
    }
    
    /**
     * Gửi notification cho buyer khi có cập nhật trạng thái
     */
    private void sendOrderStatusNotification(Order order, String ghnStatus, Map<String,Object> payload) {
        try {
            Long buyerId = order.getUser().getId();
            String statusName = (String) payload.getOrDefault("StatusName", ghnStatus);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ORDER_STATUS_UPDATE");
            notification.put("orderId", order.getId());
            notification.put("orderStatus", order.getStatus().name());
            notification.put("ghnStatus", ghnStatus);
            notification.put("ghnStatusName", statusName);
            notification.put("timestamp", System.currentTimeMillis());
            
            // Custom message dựa vào status
            String message;
            switch (ghnStatus.toLowerCase()) {
                case "delivered":
                    message = "🎉 Đơn hàng #" + order.getId() + " đã được giao thành công!";
                    break;
                case "delivering":
                    message = "🚚 Đơn hàng #" + order.getId() + " đang được giao đến bạn";
                    break;
                case "picked":
                    message = "📦 Đơn hàng #" + order.getId() + " đã được lấy hàng";
                    break;
                case "return":
                case "returned":
                    message = "🔄 Đơn hàng #" + order.getId() + " đang được hoàn trả";
                    break;
                case "delivery_fail":
                    message = "⚠️ Giao hàng thất bại cho đơn #" + order.getId();
                    break;
                default:
                    message = "📋 Đơn hàng #" + order.getId() + " có cập nhật: " + statusName;
                    break;
            }
            
            notification.put("message", message);
            
            String destination = "/topic/orderws/" + buyerId;
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("✅ Sent notification to buyer {} for order {}: {}", 
                buyerId, order.getId(), message);
            
        } catch (Exception e) {
            logger.error("❌ Error sending notification: {}", e.getMessage(), e);
        }
    }
}