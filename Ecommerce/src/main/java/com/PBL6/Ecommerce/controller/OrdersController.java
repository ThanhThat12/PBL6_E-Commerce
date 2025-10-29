package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateOrderStatusDTO;
import com.PBL6.Ecommerce.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seller")
public class OrdersController {
    
    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API lấy danh sách đơn hàng của seller
     * GET /api/seller/orders
     * Lấy tất cả orders của shop thuộc seller đang đăng nhập
     * Chỉ SELLER mới có quyền truy cập
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getSellerOrders(Authentication authentication) {
        String username = authentication.getName();
        List<OrderDTO> orders = orderService.getSellerOrders(username);
        return ResponseDTO.success(orders, "Lấy danh sách đơn hàng thành công");
    }

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
}

