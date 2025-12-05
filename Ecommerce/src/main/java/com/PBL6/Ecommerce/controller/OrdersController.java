package com.PBL6.Ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateOrderStatusDTO;
import com.PBL6.Ecommerce.service.OrderService;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.repository.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seller")
public class OrdersController {
    
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GhnService ghnService;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public OrdersController(OrderService orderService, SimpMessagingTemplate messagingTemplate,
            GhnService ghnService, ShipmentRepository shipmentRepository,
            OrderRepository orderRepository, ShopRepository shopRepository,
            UserRepository userRepository, AddressRepository addressRepository) {
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
        this.ghnService = ghnService;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * NOTE: API lấy danh sách đơn hàng đã được chuyển sang SellerOrderController
     * Sử dụng endpoint: GET /api/seller/orders (có hỗ trợ filter theo status)
     */

    /**
     * API lấy chi tiết đơn hàng theo ID
     * GET /api/seller/orders/{id}
     * Lấy đầy đủ thông tin của 1 đơn hàng
     * Chỉ SELLER mới có quyền truy cập và chỉ xem được orders của shop mình
     */
    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getOrderDetail(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        OrderDetailDTO order = orderService.getOrderDetail(id, username);
        // Các trường shipping/receiver đã được map đầy đủ trong OrderDetailDTO
        return ResponseDTO.success(order, "Lấy chi tiết đơn hàng thành công");
    }

    /**
     * API cập nhật trạng thái đơn hàng
     * PATCH /api/seller/orders/{id}/status
     * Cập nhật trạng thái: PENDING, PROCESSING, COMPLETED, CANCELLED
     * Chỉ SELLER mới có quyền và chỉ cập nhật được orders của shop mình
     */
    @PatchMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO statusDTO,
            Authentication authentication) {
        String username = authentication.getName();
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, statusDTO.getStatus(), username);
        return ResponseDTO.success(updatedOrder, "Cập nhật trạng thái đơn hàng thành công");
    }

    /**
     * API thống kê số đơn hàng hoàn thành theo tháng (12 tháng gần nhất)
     * GET /api/seller/analytics/orders/completed-monthly
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/analytics/orders/completed-monthly")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<java.util.List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO>>> 
            getMonthlyCompletedOrderStats(Authentication authentication) {
        String username = authentication.getName();
        java.util.List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO> stats = 
            orderService.getMonthlyCompletedOrderStats(username);
        return ResponseDTO.success(stats, "Lấy thống kê đơn hàng hoàn thành thành công");
    }

    /**
     * API thống kê số đơn hàng bị hủy theo tháng (12 tháng gần nhất)
     * GET /api/seller/analytics/orders/cancelled-monthly
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/analytics/orders/cancelled-monthly")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<java.util.List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO>>> 
            getMonthlyCancelledOrderStats(Authentication authentication) {
        String username = authentication.getName();
        java.util.List<com.PBL6.Ecommerce.domain.dto.MonthlyOrderStatsDTO> stats = 
            orderService.getMonthlyCancelledOrderStats(username);
        return ResponseDTO.success(stats, "Lấy thống kê đơn hàng bị hủy thành công");
    }

    /**
     * API lấy top 5 sản phẩm bán chạy nhất của shop
     * GET /api/seller/analytics/products/top-selling
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/analytics/products/top-selling")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<java.util.List<com.PBL6.Ecommerce.domain.dto.TopProductDTO>>> 
            getTopSellingProducts(Authentication authentication) {
        String username = authentication.getName();
        java.util.List<com.PBL6.Ecommerce.domain.dto.TopProductDTO> topProducts = 
            orderService.getTopSellingProducts(username);
        return ResponseDTO.success(topProducts, "Lấy top sản phẩm bán chạy thành công");
        }
    //  * API đánh dấu đơn hàng sang SHIPPING (Đang giao hàng)
    //  * PATCH /api/seller/orders/{id}/mark-shipping
    //  * Seller xác nhận đã giao hàng cho đơn vị vận chuyển
    //  */
    @PatchMapping("/orders/{id}/mark-shipping")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> markAsShipping(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        
        // Verify seller owns this order's shop
        OrderDetailDTO currentOrder = orderService.getOrderDetail(id, username);
        
        // Update to SHIPPING
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, "SHIPPING", username);
        return ResponseDTO.success(updatedOrder, "Đã chuyển đơn hàng sang trạng thái đang giao hàng");
    }

    /**
     * API xác nhận đơn hàng (PENDING → PROCESSING)
     * POST /api/seller/orders/{id}/confirm
     * Seller xác nhận đơn hàng, sẵn sàng xử lý
     */
    @PatchMapping("/orders/{id}/confirm")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> confirmOrder(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, "PROCESSING", username);
        
        // ✅ Gửi WebSocket notification cho buyer
        sendOrderNotificationToBuyer(updatedOrder, "ORDER_CONFIRMED", 
            "✅ Đơn hàng #" + updatedOrder.getId() + " đã được xác nhận và đang chuẩn bị xử lý");
        
        return ResponseDTO.success(updatedOrder, "Đã xác nhận đơn hàng");
    }

    /**
     * API đánh dấu đã đóng gói/Giao cho ship (PROCESSING → SHIPPING)
     * POST /api/seller/orders/{id}/ship
     * Seller xác nhận đã đóng gói và giao cho đơn vị vận chuyển
     * Tự động tạo shipment GHN
     */
    @PatchMapping("/orders/{id}/ship")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> shipOrder(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, "SHIPPING", username);
        
        // ✅ Gửi WebSocket notification cho buyer
        sendOrderNotificationToBuyer(updatedOrder, "ORDER_SHIPPING", 
            "🚚 Đơn hàng #" + updatedOrder.getId() + " đã được giao cho đơn vị vận chuyển");
        
        // ✅ Tạo shipment GHN async (không block API response)
        try {
            createShipmentAsync(id, username);
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi tạo shipment async: " + e.getMessage());
            // Không throw exception để không block API response
        }
        
        return ResponseDTO.success(updatedOrder, "Đã giao đơn hàng cho đơn vị vận chuyển");
    }
    
    /**
     * Tạo shipment GHN async sau khi ship order
     */
    private void createShipmentAsync(Long orderId, String username) {
        // Đơn giản: check xem shipment đã tồn tại chưa
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            System.out.println("⚠️ Shipment đã tồn tại cho order " + orderId);
            return;
        }
        
        // Lấy order info
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            System.err.println("❌ Không tìm thấy order " + orderId);
            return;
        }
        
        // Lấy shop từ username
        User seller = userRepository.findByUsername(username).orElse(null);
        if (seller == null) {
            System.err.println("❌ Không tìm thấy seller " + username);
            return;
        }
        
        Shop shop = shopRepository.findByOwnerId(seller.getId()).orElse(null);
        if (shop == null) {
            System.err.println("❌ Không tìm thấy shop cho seller " + username);
            return;
        }
        
        // Tạo shipment record đơn giản
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setStatus("READY_TO_PICK");
        shipment.setGhnOrderCode("ORD-" + orderId); // Temporary code
        shipment.setGhnPayload("{}"); // Empty payload
        
        shipmentRepository.save(shipment);
        
        System.out.println("✅ Đã tạo shipment cơ bản cho order " + orderId);
        
        // TODO: Gọi GHN API tạo thực tế (async)
        // ghnService.createShippingOrderAsync(payload, shop.getId());
    }

    /**
     * API hủy đơn hàng (cho phép từ PENDING hoặc PROCESSING)
     * POST /api/seller/orders/{id}/cancel
     * Seller hủy đơn hàng với lý do
     */
    @PatchMapping("/orders/{id}/cancel")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        
        // Verify order exists and belongs to seller's shop
        OrderDetailDTO currentOrder = orderService.getOrderDetail(id, username);
        
        // Only allow cancel if order is PENDING
        if (!"PENDING".equals(currentOrder.getStatus())) {
            return ResponseDTO.badRequest("Chỉ có thể hủy đơn hàng ở trạng thái Chờ xác nhận");
        }
        
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, "CANCELLED", username);
        
        // ✅ Gửi WebSocket notification cho buyer
        sendOrderNotificationToBuyer(updatedOrder, "ORDER_CANCELLED", 
            "❌ Đơn hàng #" + updatedOrder.getId() + " đã bị hủy bởi người bán");
        
        return ResponseDTO.success(updatedOrder, "Đã hủy đơn hàng");
    }

    /**
     * Helper method: Gửi WebSocket notification cho buyer
     */
    private void sendOrderNotificationToBuyer(OrderDetailDTO order, String type, String message) {
        try {
            Long buyerId = order.getUserId();
            
            System.out.println("========== SENDING WEBSOCKET NOTIFICATION FROM OrdersController ==========");
            System.out.println("Buyer ID: " + buyerId);
            System.out.println("Order ID: " + order.getId());
            System.out.println("Type: " + type);
            System.out.println("Message: " + message);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("orderId", order.getId());
            notification.put("orderStatus", order.getStatus());
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());
            
            String destination = "/topic/orderws/" + buyerId;
            System.out.println("Destination: " + destination);
            System.out.println("Notification payload: " + notification);
            
            messagingTemplate.convertAndSend(destination, notification);
            
            System.out.println("✅ WebSocket notification sent successfully!");
            System.out.println("===================================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending WebSocket notification: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception, operation already successful
        }
    }
}

