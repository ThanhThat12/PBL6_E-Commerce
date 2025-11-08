package com.PBL6.Ecommerce.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.dto.seller.CustomerDTO;
import com.PBL6.Ecommerce.dto.seller.CustomerDetailDTO;
import com.PBL6.Ecommerce.service.SellerCustomerService;

@RestController
@RequestMapping("/api/seller/customers")
@PreAuthorize("hasRole('SELLER')")
public class SellerCustomerController {

    @Autowired
    private SellerCustomerService customerService;

    /**
     * GET /api/seller/customers?page=0&size=10
     * Lấy danh sách khách hàng (có phân trang)
     */
    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getCustomers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDTO> customers = customerService.getCustomers(username, pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * GET /api/seller/customers/{id}
     * Lấy chi tiết khách hàng
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDetailDTO> getCustomerDetail(
            Authentication authentication,
            @PathVariable Long customerId
    ) {
        String username = authentication.getName();
        CustomerDetailDTO detail = customerService.getCustomerDetail(username, customerId);
        return ResponseEntity.ok(detail);
    }

    /**
     * GET /api/seller/customers/{id}/orders?page=0&size=10
     * Lấy lịch sử đơn hàng của khách hàng
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<Page<Order>> getCustomerOrders(
            Authentication authentication,
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = customerService.getCustomerOrders(username, customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/seller/customers/stats
     * Lấy thống kê tổng quan về khách hàng
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCustomerStats(
            Authentication authentication
    ) {
        String username = authentication.getName();
        Map<String, Object> stats = customerService.getCustomerStats(username);
        return ResponseEntity.ok(stats);
    }
}
