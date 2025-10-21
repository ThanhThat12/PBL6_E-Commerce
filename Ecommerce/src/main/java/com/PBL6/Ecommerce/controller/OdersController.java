package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateOrderStatusDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
public class OdersController {
    
    private final OrderService orderService;

    public OdersController(OrderService orderService) {
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
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getSellerOrders() {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Lấy danh sách đơn hàng của shop thuộc seller
            List<OrderDTO> orders = orderService.getSellerOrders(username);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy danh sách đơn hàng thành công", orders)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Lấy danh sách đơn hàng thất bại", null)
            );
        }
    }

    /**
     * API lấy chi tiết đơn hàng theo ID
     * GET /api/seller/orders/{id}
     * Lấy đầy đủ thông tin của 1 đơn hàng
     * Chỉ SELLER mới có quyền truy cập và chỉ xem được orders của shop mình
     */
    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<OrderDetailDTO>> getOrderDetail(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            OrderDetailDTO order = orderService.getOrderDetail(id, username);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy chi tiết đơn hàng thành công", order)
            );
        } catch (RuntimeException e) {
            // Phân biệt loại lỗi
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("Không tìm thấy đơn hàng")) {
                statusCode = 404;
            } else if (errorMessage.contains("không có quyền")) {
                statusCode = 403;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Lấy chi tiết đơn hàng thất bại", null)
            );
        }
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
            @RequestBody UpdateOrderStatusDTO statusDTO) {
        try {
            // Lấy thông tin user từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Cập nhật trạng thái đơn hàng
            OrderDetailDTO updatedOrder = orderService.updateOrderStatus(id, statusDTO.getStatus(), username);

            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Cập nhật trạng thái đơn hàng thành công", updatedOrder)
            );
        } catch (RuntimeException e) {
            // Phân biệt loại lỗi
            String errorMessage = e.getMessage();
            int statusCode;

            if (errorMessage.contains("Không tìm thấy đơn hàng")) {
                statusCode = 404;
            } else if (errorMessage.contains("không có quyền")) {
                statusCode = 403;
            } else if (errorMessage.contains("không hợp lệ")) {
                statusCode = 400;
            } else {
                statusCode = 400;
            }

            return ResponseEntity.status(statusCode).body(
                new ResponseDTO<>(statusCode, errorMessage, "Cập nhật trạng thái đơn hàng thất bại", null)
            );
        }
    }
}

