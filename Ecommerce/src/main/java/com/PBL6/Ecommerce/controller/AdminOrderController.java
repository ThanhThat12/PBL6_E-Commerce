package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * Controller for admin order management
 * Admin can: view all orders, view order details, update order status
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API lấy tất cả đơn hàng (Admin only)
     * GET /api/admin/orders
     * Lấy tất cả orders trong hệ thống
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseDTO.success(orders, "Lấy danh sách đơn hàng thành công");
    }

    /**
     * API lấy chi tiết đơn hàng (Admin only)
     * GET /api/admin/orders/{id}
     * Admin có thể xem mọi đơn hàng
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getOrderDetail(@PathVariable Long id) {
        OrderDetailDTO order = orderService.getAdminOrderDetail(id);
        return ResponseDTO.success(order, "Lấy chi tiết đơn hàng thành công");
    }

    /**
     * API cập nhật trạng thái đơn hàng (Admin only)
     * PATCH /api/admin/orders/{id}/status
     * Admin có thể cập nhật bất kỳ đơn hàng nào
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO statusDTO) {
        OrderDetailDTO updatedOrder = orderService.updateOrderStatusByAdmin(id, statusDTO.getStatus());
        return ResponseDTO.success(updatedOrder, "Cập nhật trạng thái đơn hàng thành công");
    }
}
