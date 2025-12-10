package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.domain.Order.OrderStatus;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderItemDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminOrderStats;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
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

    /**
     * Get order detail by ID for admin
     * @param orderId Order ID
     * @return AdminOrderDetailDTO with complete order information
     */
    public AdminOrderDetailDTO getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return convertToDetailDTO(order);
    }

    /**
     * Convert Order entity to AdminOrderDetailDTO
     */
    private AdminOrderDetailDTO convertToDetailDTO(Order order) {
        AdminOrderDetailDTO dto = new AdminOrderDetailDTO();
        
        // Thông Tin Chung
        dto.setOrderId(order.getId());
        dto.setCreatedAt(order.getCreatedAt() != null ? 
            LocalDateTime.ofInstant(order.getCreatedAt().toInstant(), ZoneId.systemDefault()) : null);
        dto.setUpdatedAt(order.getUpdatedAt() != null ? 
            LocalDateTime.ofInstant(order.getUpdatedAt().toInstant(), ZoneId.systemDefault()) : null);
        dto.setOrderStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        dto.setPaidAt(order.getPaidAt() != null ? 
            LocalDateTime.ofInstant(order.getPaidAt().toInstant(), ZoneId.systemDefault()) : null);
        
        // Thông Tin Cửa Hàng
        if (order.getShop() != null) {
            dto.setShopId(order.getShop().getId());
            dto.setShopName(order.getShop().getName());
        }
        
        // Thông Tin Khách Hàng
        if (order.getUser() != null) {
            dto.setCustomerId(order.getUser().getId());
            dto.setCustomerName(order.getUser().getFullName());
            dto.setCustomerEmail(order.getUser().getEmail());
            dto.setCustomerPhone(order.getUser().getPhoneNumber());
        }
        
        // Thông Tin Người Nhận & Giao Hàng
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
        dto.setPaymentMethod(order.getMethod());
        
        // Chi Tiết Sản Phẩm
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<AdminOrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
            
            // Tính subtotal (tổng tiền hàng)
            BigDecimal subtotal = itemDTOs.stream()
                .map(AdminOrderItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setSubtotal(subtotal);
        } else {
            dto.setSubtotal(BigDecimal.ZERO);
        }
        
        // Thanh Toán & Giảm Giá
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        
        if (order.getVoucher() != null) {
            dto.setVoucherId(order.getVoucher().getId());
            dto.setVoucherCode(order.getVoucher().getCode());
            dto.setDiscount(order.getVoucher().getDiscountValue() != null ? 
                order.getVoucher().getDiscountValue() : BigDecimal.ZERO);
        } else {
            dto.setDiscount(BigDecimal.ZERO);
        }
        
        return dto;
    }

    /**
     * Convert OrderItem to AdminOrderItemDTO
     */
    private AdminOrderItemDTO convertToItemDTO(OrderItem item) {
        AdminOrderItemDTO dto = new AdminOrderItemDTO();
        
        dto.setItemId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
        dto.setVariantName(item.getVariantName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
        
        // Calculate subtotal
        if (item.getPrice() != null && item.getQuantity() != null) {
            dto.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            dto.setSubtotal(BigDecimal.ZERO);
        }
        
        // Add product details if variant is available
        if (item.getVariant() != null && item.getVariant().getProduct() != null) {
            dto.setProductName(item.getVariant().getProduct().getName());
            dto.setProductImage(item.getVariant().getProduct().getMainImage());
        }
        
        return dto;
    }
}
