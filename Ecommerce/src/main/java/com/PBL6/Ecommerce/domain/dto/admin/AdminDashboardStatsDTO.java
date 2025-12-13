package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long activeCustomers;
    private Double conversionRate;
    private Double revenueGrowth;
    private Double ordersGrowth;
    private Double customersGrowth;
    private Double conversionGrowth;
}