package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("")
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getAllOrders() {
        try {
            List<OrderDTO> orders = orderService.getAllOrders();
            ResponseDTO<List<OrderDTO>> response = new ResponseDTO<>(200, null, "Lấy danh sách đơn hàng thành công", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<List<OrderDTO>> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<OrderDTO>> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            if (order == null) {
                ResponseDTO<OrderDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy đơn hàng", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(200, null, "Lấy chi tiết đơn hàng thành công", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ResponseDTO<OrderDTO>> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            OrderDTO order = orderService.updateOrderStatus(id, status);
            if (order == null) {
                ResponseDTO<OrderDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy đơn hàng", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(200, null, "Cập nhật trạng thái đơn hàng thành công", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ResponseDTO<OrderDTO>> refundOrder(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.refundOrder(id);
            if (order == null) {
                ResponseDTO<OrderDTO> response = new ResponseDTO<>(404, "NOT_FOUND", "Không tìm thấy đơn hàng", null);
                return ResponseEntity.status(404).body(response);
            }
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(200, null, "Hoàn tiền đơn hàng thành công", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO<OrderDTO> response = new ResponseDTO<>(400, e.getMessage(), "Thất bại", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
