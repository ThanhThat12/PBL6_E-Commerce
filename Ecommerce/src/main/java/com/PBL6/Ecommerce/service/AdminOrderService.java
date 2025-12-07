package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDTO;
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
}
