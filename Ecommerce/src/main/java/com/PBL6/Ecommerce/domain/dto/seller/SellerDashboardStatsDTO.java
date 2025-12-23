package com.PBL6.Ecommerce.domain.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Seller Dashboard Statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardStatsDTO {
    
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalCustomers;
    
    // Trend indicators (optional)
    private String ordersTrend;
    private String productsTrend;
    private String customersTrend;
    
    // Growth percentages for UI
    private Double revenueGrowth;
    private Double ordersGrowth;
    private Double productsGrowth;
    private Double customersGrowth;
}
