package com.PBL6.Ecommerce.controller.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.order.Shipment;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.math.BigDecimal;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Seller Orders", description = "Seller order operations - view, update status, shipping")
@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {
    
    private final com.PBL6.Ecommerce.repository.OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;
    private final GhnService ghnService;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.PBL6.Ecommerce.service.OrderService orderService;

    public SellerOrderController(
            com.PBL6.Ecommerce.repository.OrderRepository orderRepository,
            ShopRepository shopRepository,
            AddressRepository addressRepository,
            GhnService ghnService,
            ShipmentRepository shipmentRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            com.PBL6.Ecommerce.service.OrderService orderService) {
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
        this.ghnService = ghnService;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.orderService = orderService;
    }

    /**
     * Seller xác nhận đơn hàng và tạo GHN shipment
     * POST /api/seller/orders/{orderId}/confirm-and-ship
     * 
     * Request body:
     * {
     *   "serviceId": 53320,
     *   "serviceTypeId": 2,
     *   "note": "Giao hàng cẩn thận"
     * }
     */
    @PostMapping("/{orderId}/confirm-and-ship")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Map<String,Object>>> confirmAndShip(
            @PathVariable Long orderId,
            @RequestBody com.PBL6.Ecommerce.domain.dto.ConfirmOrderRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            // Lấy email từ token
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }

            // Lấy User (seller)
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract parameters từ request
            Integer serviceId = request.getServiceId();
            Integer serviceTypeId = request.getServiceTypeId();
            String note = request.getNote();
            
            // GỌI SERVICE để xử lý toàn bộ logic
            Order confirmedOrder = orderService.confirmOrderAndCreateShipment(
                orderId, seller.getId(), serviceId, serviceTypeId, note
            );
            
            // Gửi WebSocket notification cho buyer
            sendOrderNotificationToBuyer(confirmedOrder, "ORDER_CONFIRMED", 
                "✅ Đơn hàng #" + confirmedOrder.getId() + " đã được xác nhận và đang chuẩn bị giao");
            
            // Lấy shipment info
            Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderId(confirmedOrder.getId());
            
            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", confirmedOrder.getId());
            response.put("status", confirmedOrder.getStatus().name());
            
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                response.put("shipmentId", shipment.getId());
                response.put("ghnOrderCode", shipment.getGhnOrderCode());
            }

            return ResponseDTO.ok(response, "Xác nhận đơn hàng và tạo vận đơn thành công");

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ ERROR in confirmAndShip");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi xác nhận đơn hàng", null));
        }
    }

    /**
     * Seller xác nhận bắt đầu giao hàng (PROCESSING → SHIPPING)
     * PUT /api/seller/orders/{orderId}/start-shipping
     */
    @PutMapping("/{orderId}/start-shipping")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<Map<String,Object>>> startShipping(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

            // Kiểm tra quyền
            if (!order.getShop().getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
            }

            // Kiểm tra trạng thái: phải là PROCESSING (đã xác nhận, chờ giao)
            if (order.getStatus() != Order.OrderStatus.PROCESSING) {
                throw new RuntimeException("Đơn hàng không ở trạng thái chờ giao hàng");
            }

            // Kiểm tra xem đã có shipment chưa
            Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderId(order.getId());
            if (shipmentOpt.isEmpty()) {
                throw new RuntimeException("Chưa có vận đơn cho đơn hàng này");
            }

            // Cập nhật trạng thái
            order.setStatus(Order.OrderStatus.SHIPPING);
            orderRepository.save(order);

            // Cập nhật shipment status nếu cần
            Shipment shipment = shipmentOpt.get();
            if ("READY_TO_PICK".equals(shipment.getStatus())) {
                shipment.setStatus("PICKING");
                shipmentRepository.save(shipment);
            }

            // ✅ Gửi WebSocket notification cho buyer
            sendOrderNotificationToBuyer(order, "ORDER_SHIPPING", 
                "🚚 Đơn hàng #" + order.getId() + " đang được giao đến bạn");

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus().name());
            response.put("message", "Đơn hàng đã chuyển sang trạng thái đang giao");

            return ResponseDTO.ok(response, "Bắt đầu giao hàng thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Seller hủy đơn hàng với lý do
     * PUT /api/seller/orders/{orderId}/cancel
     * 
     * Request body:
     * {
     *   "cancelReason": "Hết hàng"
     * }
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<Map<String,Object>>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String cancelReason = requestBody.get("cancelReason");
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập lý do hủy đơn");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

            // Kiểm tra quyền
            if (!order.getShop().getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
            }

            // Kiểm tra trạng thái: chỉ cho phép hủy khi PENDING hoặc PROCESSING
            if (order.getStatus() != Order.OrderStatus.PENDING && 
                order.getStatus() != Order.OrderStatus.PROCESSING) {
                throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái " + order.getStatus());
            }

            // Cập nhật trạng thái
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Hủy shipment nếu có
            Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderId(order.getId());
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                shipment.setStatus("CANCELLED");
                shipmentRepository.save(shipment);
                
                // TODO: Gọi GHN API để hủy vận đơn nếu cần
            }

            // Hoàn lại stock cho các variant
            for (var orderItem : order.getOrderItems()) {
                var variant = orderItem.getVariant();
                Integer currentStock = variant.getStock();
                if (currentStock != null) {
                    variant.setStock(currentStock + orderItem.getQuantity());
                } else {
                    variant.setStock(orderItem.getQuantity());
                }
            }

            // ✅ Gửi thông báo cho buyer qua WebSocket
            try {
                Long buyerId = order.getUser().getId();
                String notificationMessage = "Đơn hàng #" + order.getId() + 
                    " của bạn đã bị hủy. Lý do: " + cancelReason;
                
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "ORDER_CANCELLED");
                notification.put("orderId", order.getId());
                notification.put("message", notificationMessage);
                notification.put("cancelReason", cancelReason);
                notification.put("timestamp", System.currentTimeMillis());
                
                String destination = "/topic/orderws/" + buyerId;
                messagingTemplate.convertAndSend(destination, notification);
                
                System.out.println("✅ Notification sent to buyer (userId=" + buyerId + "): " + notificationMessage);
            } catch (Exception e) {
                System.err.println("❌ Error sending notification: " + e.getMessage());
                // Don't throw exception, order cancellation is already successful
            }

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus().name());
            response.put("cancelReason", cancelReason);
            response.put("message", "Đã hủy đơn hàng và gửi thông báo đến người mua");

            return ResponseDTO.ok(response, "Hủy đơn hàng thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Lấy danh sách đơn hàng của seller
     * GET /api/seller/orders?status=PENDING
     */
    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<Map<String,Object>>>> getSellerOrders(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy shop của seller
            Shop shop = shopRepository.findByOwnerId(seller.getId())
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Bạn chưa có shop"));

            // Lấy orders
            List<Order> orders;
            if (status != null && !status.isEmpty()) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByShopIdAndStatus(shop.getId(), orderStatus);
            } else {
                orders = orderRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());
            }

            // Convert to response
            List<Map<String,Object>> result = new ArrayList<>();
            for (Order order : orders) {
                Map<String,Object> map = new HashMap<>();
                map.put("id", order.getId());
                map.put("totalAmount", order.getTotalAmount());
                map.put("shippingFee", order.getShippingFee());
                map.put("status", order.getStatus().name());
                map.put("paymentStatus", order.getPaymentStatus().name());
                map.put("method", order.getMethod());
                map.put("createdAt", order.getCreatedAt());
                map.put("receiverName", order.getReceiverName());
                map.put("receiverPhone", order.getReceiverPhone());
                map.put("receiverAddress", order.getReceiverAddress());
                
                // Buyer info
                User buyer = order.getUser();
                map.put("buyerName", buyer.getFullName());
                map.put("buyerEmail", buyer.getEmail());
                
                // Items count
                map.put("itemsCount", order.getOrderItems().size());
                
                result.add(map);
            }

            return ResponseDTO.ok(result, "Lấy danh sách đơn hàng thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Seller xác nhận đã giao hàng thành công (SHIPPING → COMPLETED)
     * PUT /api/seller/orders/{orderId}/complete
     */
    @PutMapping("/{orderId}/complete")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public ResponseEntity<ResponseDTO<Map<String,Object>>> completeOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

            // Kiểm tra quyền
            if (!order.getShop().getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
            }

            // Kiểm tra trạng thái: phải là SHIPPING
            if (order.getStatus() != Order.OrderStatus.SHIPPING) {
                throw new RuntimeException("Đơn hàng không ở trạng thái đang giao");
            }

            // Cập nhật trạng thái
            order.setStatus(Order.OrderStatus.COMPLETED);
            orderRepository.save(order);

            // Cập nhật shipment status
            Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderId(order.getId());
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                shipment.setStatus("DELIVERED");
                shipmentRepository.save(shipment);
            }

            // ✅ Gửi WebSocket notification cho buyer
            sendOrderNotificationToBuyer(order, "ORDER_COMPLETED", 
                "🎉 Đơn hàng #" + order.getId() + " đã được giao thành công!");

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus().name());
            response.put("message", "Đơn hàng đã hoàn thành");

            return ResponseDTO.ok(response, "Đánh dấu giao hàng thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Helper method: Gửi WebSocket notification cho buyer
     */
    private void sendOrderNotificationToBuyer(Order order, String type, String message) {
        try {
            Long buyerId = order.getUser().getId();
            
            System.out.println("========== SENDING WEBSOCKET NOTIFICATION ==========");
            System.out.println("Buyer ID: " + buyerId);
            System.out.println("Order ID: " + order.getId());
            System.out.println("Type: " + type);
            System.out.println("Message: " + message);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("orderId", order.getId());
            notification.put("orderStatus", order.getStatus().name());
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
            // Don't throw exception, operation already successful
        }
    }
}
