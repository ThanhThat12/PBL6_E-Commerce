package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Order.OrderStatus;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderStats;
import com.PBL6.Ecommerce.repository.OrderRepository;

@Service
public class AdminOrderService {
    
    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Get all orders with pagination for admin
     * @param page page number (0-indexed)
     * @param size page size (default 10)
     * @return Page of AdminOrderDTO
     */
    public Page<AdminOrderDTO> getAllOrdersWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAll(pageable);
        
        return orders.map(this::convertToDTO);
    }

    /**
     * Get all orders without pagination (for backward compatibility)
     * @return List of AdminOrderDTO
     */
    public List<AdminOrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Order entity to AdminOrderDTO
     */
    private AdminOrderDTO convertToDTO(Order order) {
        return new AdminOrderDTO(
            order.getId(),
            order.getReceiverName(),
            order.getReceiverPhone(),
            order.getCreatedAt(),
            order.getTotalAmount(),
            order.getPaymentStatus(),
            order.getStatus(),
            order.getReceiverAddress() // Chỉ lấy receiver_address
        );
    }

    /**
     * Get order statistics for admin dashboard
     * Tính toán:
     * - Tổng số đơn hàng
     * - Số đơn hàng PENDING
     * - Số đơn hàng COMPLETED
     * - Tổng doanh thu (không tính đơn CANCELLED)
     * @return AdminOrderStats
     */
    public AdminOrderStats getOrderStats() {
        // Tổng số đơn hàng
        Long totalOrders = orderRepository.count();
        
        // Số đơn hàng PENDING
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        
        // Số đơn hàng COMPLETED
        Long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        
        // Tổng doanh thu (không tính đơn CANCELLED)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        return new AdminOrderStats(totalOrders, pendingOrders, completedOrders, totalRevenue);
    }

    /**
     * Get orders by status with pagination for admin
     * @param status OrderStatus (PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED)
     * @param page page number (0-indexed)
     * @param size page size (default 10)
     * @return Page of AdminOrderDTO filtered by status
     */
    public Page<AdminOrderDTO> getOrdersByStatus(Order.OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        
        return orders.map(this::convertToDTO);
    }
}
