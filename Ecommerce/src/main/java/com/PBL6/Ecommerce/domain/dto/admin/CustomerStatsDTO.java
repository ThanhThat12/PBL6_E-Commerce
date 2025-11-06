package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerStatsDTO {
    private long totalCustomers;        // Tổng số customers
    private long activeCustomers;       // Customers có activated = true
    private long newThisMonth;          // Customers tạo trong tháng này
    private double totalRevenue;        // Tổng doanh thu từ tất cả orders
}