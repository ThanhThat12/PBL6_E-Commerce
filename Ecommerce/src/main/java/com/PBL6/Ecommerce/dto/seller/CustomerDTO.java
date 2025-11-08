package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho danh sách khách hàng của seller
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private Long totalOrders;        // Tổng số đơn hàng
    private BigDecimal totalSpent;   // Tổng tiền đã mua
    private LocalDateTime lastOrderDate; // Đơn hàng gần nhất
    private LocalDateTime joinedDate;    // Ngày đăng ký (firstOrderDate)
}
