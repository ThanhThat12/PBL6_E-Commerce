package com.PBL6.Ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDTO;
import com.PBL6.Ecommerce.service.AdminOrderService;
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
    private final AdminOrderService adminOrderService;

    public AdminOrderController(OrderService orderService, AdminOrderService adminOrderService) {
        this.orderService = orderService;
        this.adminOrderService = adminOrderService;
    }

    /**
     * API lấy tất cả đơn hàng với phân trang (Admin only)
     * GET /api/admin/orders?page=0&size=10
     * Lấy tất cả orders trong hệ thống với phân trang
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminOrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminOrderDTO> orders = adminOrderService.getAllOrdersWithPagination(page, size);
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

}
