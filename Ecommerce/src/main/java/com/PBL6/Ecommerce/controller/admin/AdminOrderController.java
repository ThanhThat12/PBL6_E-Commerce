package com.PBL6.Ecommerce.controller.admin;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderStats;
import com.PBL6.Ecommerce.service.AdminOrderService;
import com.PBL6.Ecommerce.service.OrderService;

import jakarta.validation.Valid;

/**
 * Controller for admin order management
 * Admin can: view all orders, view order details, update order status
 */
@Tag(name = "Admin Orders", description = "Admin order management and statistics")
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
     * Admin có thể xem mọi đơn hàng với thông tin đầy đủ
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDetailDTO>> getOrderDetail(@PathVariable Long id) {
        com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDetailDTO order = adminOrderService.getOrderDetail(id);
        return ResponseDTO.success(order, "Lấy chi tiết đơn hàng thành công");
    }

    /**
     * API lấy thống kê đơn hàng (Admin only)
     * GET /api/admin/orders/stats
     * Trả về: totalOrders, pendingOrders, completedOrders, totalRevenue
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminOrderStats>> getOrderStats() {
        AdminOrderStats stats = adminOrderService.getOrderStats();
        return ResponseDTO.success(stats, "Lấy thống kê đơn hàng thành công");
    }

    /**
     * API lấy đơn hàng theo status với phân trang (Admin only)
     * GET /api/admin/orders/status/{status}?page=0&size=10
     * @param status - OrderStatus: PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED
     * @param page - Số trang (0-indexed)
     * @param size - Số items mỗi trang
     * @return Page<AdminOrderDTO>
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminOrderDTO>>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Convert string to OrderStatus enum
        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseDTO.badRequest("Invalid status. Valid values: PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED");
        }
        
        Page<AdminOrderDTO> orders = adminOrderService.getOrdersByStatus(orderStatus, page, size);
        return ResponseDTO.success(orders, "Lấy danh sách đơn hàng theo trạng thái thành công");
    }

}
