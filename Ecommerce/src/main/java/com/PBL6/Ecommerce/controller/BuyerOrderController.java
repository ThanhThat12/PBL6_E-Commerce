package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.OrderResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.OrderService;
import com.PBL6.Ecommerce.service.UserService;

import jakarta.validation.Valid;

/**
 * Controller for buyer order operations
 * Buyer can: create orders, view their orders, view order details
 */
@RestController
@RequestMapping("/api/orders")
public class BuyerOrderController {
    
    private final OrderService orderService;
    private final UserService userService;

    public BuyerOrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * API tạo đơn hàng mới (Buyer/User)
     * POST /api/orders
     * 
     * Request body (userId được tự động lấy từ JWT token):
     * {
     *   "items": [
     *     {"variantId": 1, "quantity": 2}
     *   ],
     *   "toName": "Nguyễn Văn A",
     *   "toPhone": "0912345678",
     *   "toDistrictId": "1",
     *   "toWardCode": "1",
     *   "toAddress": "123 Đường ABC"
     * }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<OrderResponseDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO dto,
            Authentication authentication) {
    // Lấy userId từ JWT token (có thể là số hoặc username)
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = userService.extractUserIdFromJwt(jwt);
    dto.setUserId(userId);
        
        Order order = orderService.createOrder(dto);
        
        // Convert to response DTO
        OrderResponseDTO response = new OrderResponseDTO(
            order.getId(),
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getTotalAmount(),
            order.getCreatedAt(),
            null // GHN info if needed
        );
        
        return ResponseDTO.created(response, "Đặt hàng thành công");
    }

    /**
     * API lấy danh sách đơn hàng của buyer đang đăng nhập
     * GET /api/orders
     * Lấy tất cả orders của user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getMyOrders(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = userService.extractUserIdFromJwt(jwt);
    List<OrderDTO> orders = orderService.getBuyerOrdersByUserId(userId);
    return ResponseDTO.success(orders, "Lấy danh sách đơn hàng thành công");
    }

    /**
     * API lấy chi tiết đơn hàng (Buyer)
     * GET /api/orders/{id}
     * Chỉ xem được orders của chính mình
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getOrderDetail(
            @PathVariable Long id,
            Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = userService.extractUserIdFromJwt(jwt);
    OrderDetailDTO order = orderService.getBuyerOrderDetailByUserId(id, userId);
    return ResponseDTO.success(order, "Lấy chi tiết đơn hàng thành công");
    }

    /**
     * Update order status after successful wallet payment (SPORTYPAY)
     * POST /api/orders/{id}/update-after-payment
     * Được gọi từ frontend sau khi thanh toán wallet thành công
     */
    @PostMapping("/{id}/update-after-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Void>> updateOrderAfterWalletPayment(
            @PathVariable Long id,
            Authentication authentication) {
    System.out.println("🔄 [API] POST /api/orders/" + id + "/update-after-payment called");
    
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = userService.extractUserIdFromJwt(jwt);
    System.out.println("  - User ID: " + userId);
    
    // Verify order belongs to user
    Order order = orderService.getOrderById(id);
    System.out.println("  - Order owner ID: " + order.getUser().getId());
    
    if (!order.getUser().getId().equals(userId)) {
        System.out.println("❌ Authorization failed - user doesn't own this order");
        return ResponseDTO.badRequest("Bạn không có quyền cập nhật đơn hàng này");
    }
    
    System.out.println("✅ Authorization passed");
    orderService.updateOrderAfterWalletPayment(id);
    System.out.println("✅ Order status updated successfully");
    return ResponseDTO.success(null, "Cập nhật trạng thái đơn hàng thành công");
    }
}
