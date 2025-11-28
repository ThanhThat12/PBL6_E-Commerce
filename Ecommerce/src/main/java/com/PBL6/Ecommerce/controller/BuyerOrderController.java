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
import com.PBL6.Ecommerce.domain.Refund;
import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ItemReturnRequestDTO;
import com.PBL6.Ecommerce.domain.dto.MultiShopOrderResult;
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
            order.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(order.getCreatedAt().toInstant(), java.time.ZoneId.systemDefault()) : null,
            null // GHN info if needed
        );
        // Map address fields
        response.setShippingFee(order.getShippingFee());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setProvince(order.getProvince());
        response.setDistrict(order.getDistrict());
        response.setWard(order.getWard());
        return ResponseDTO.created(response, "Đặt hàng thành công");
    }

    /**
     * Create multiple orders when cart contains items from different shops
     */
    @PostMapping("/multi-shop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<MultiShopOrderResult>> createMultiShopOrders(
            @Valid @RequestBody CreateOrderRequestDTO dto,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        dto.setUserId(userId);
        
        MultiShopOrderResult result = orderService.createMultiShopOrders(dto);
        
        return ResponseDTO.created(result, "Đặt hàng từ nhiều shop thành công");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getMyOrders(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = userService.extractUserIdFromJwt(jwt);
    List<OrderDTO> orders = orderService.getBuyerOrdersByUserId(userId);
    return ResponseDTO.success(orders, "Lấy danh sách đơn hàng thành công");
    }

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

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Void>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) String reason,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        orderService.cancelOrderAndRefund(id, userId, reason);
        return ResponseDTO.success(null, "Đã hủy đơn hàng thành công");
    }

    /**
     * Buyer xác nhận đã nhận hàng → chuyển đơn sang COMPLETED
     */
    @PostMapping("/{id}/confirm-received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Order>> confirmReceived(
            @PathVariable Long id,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        Order order = orderService.confirmReceived(id, userId);
        return ResponseDTO.success(order, "Xác nhận đã nhận hàng thành công");
    }

    /**
     * Buyer yêu cầu trả hàng cho một sản phẩm cụ thể
     */
    @PostMapping("/items/return")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Refund>> requestItemReturn(
            @Valid @RequestBody ItemReturnRequestDTO dto,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        Refund refund = orderService.requestItemReturn(dto, userId);
        return ResponseDTO.success(refund, "Yêu cầu trả hàng đã được gửi thành công");
    }
}
