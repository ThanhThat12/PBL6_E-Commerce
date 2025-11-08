package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chi tiết khách hàng của seller (bao gồm lịch sử đơn hàng)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String address;
    
    // Statistics
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    
    // Timeline
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private LocalDateTime joinedDate;
    
    // Recent orders (top 5)
    private List<CustomerOrderSummary> recentOrders;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerOrderSummary {
        private Long orderId;
        private LocalDateTime orderDate;
        private String status;
        private BigDecimal totalAmount;
        private Integer itemCount;
    }
}
