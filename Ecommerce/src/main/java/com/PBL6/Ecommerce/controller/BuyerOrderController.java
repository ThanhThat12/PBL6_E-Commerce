package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import jakarta.validation.Valid;

/**
 * Controller for buyer order operations
 * Buyer can: create orders, view their orders, view order details
 */
@RestController
@RequestMapping("/api/orders")
public class BuyerOrderController {
    
    private final OrderService orderService;

    public BuyerOrderController(OrderService orderService) {
        this.orderService = orderService;
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
        // Lấy userId từ JWT token (authentication.getName() trả về userId)
        Long userId = Long.parseLong(authentication.getName());
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
        Long userId = Long.parseLong(authentication.getName());
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
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getMyOrderDetail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        OrderDetailDTO order = orderService.getBuyerOrderDetailByUserId(id, userId);
        return ResponseDTO.success(order, "Lấy chi tiết đơn hàng thành công");
    }
}
