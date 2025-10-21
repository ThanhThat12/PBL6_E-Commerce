package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng hợp thống kê shop
 * Bao gồm: tổng thu nhập, số đơn hàng, thu nhập theo tháng
 */
public class ShopAnalyticsDTO {
    private BigDecimal totalRevenue;           // Tổng thu nhập (chỉ COMPLETED)
    private Long totalCompletedOrders;         // Tổng số đơn hàng COMPLETED
    private List<MonthlyRevenueDTO> monthlyRevenue; // Thu nhập theo từng tháng

    public ShopAnalyticsDTO() {
    }

    public ShopAnalyticsDTO(BigDecimal totalRevenue, Long totalCompletedOrders, List<MonthlyRevenueDTO> monthlyRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalCompletedOrders = totalCompletedOrders != null ? totalCompletedOrders : 0L;
        this.monthlyRevenue = monthlyRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public void setTotalCompletedOrders(Long totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
    }

    public List<MonthlyRevenueDTO> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<MonthlyRevenueDTO> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
}
