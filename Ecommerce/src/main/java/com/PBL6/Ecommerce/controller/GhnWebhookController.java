package com.PBL6.Ecommerce.controller;

import org.springframework.web.bind.annotation.*;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ShipmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook/ghn")
public class GhnWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(GhnWebhookController.class);
    
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public GhnWebhookController(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
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
            shipment.setStatus(status);
            shipmentRepository.save(shipment);
            logger.info("Updated shipment {} status to: {}", shipment.getId(), status);
            
            // Cập nhật trạng thái đơn hàng tương ứng
            Order order = shipment.getOrder();
            if (order != null) {
                updateOrderStatusFromGhn(order, status);
                orderRepository.save(order);
                logger.info("Updated order {} status based on GHN status: {}", order.getId(), status);
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
}