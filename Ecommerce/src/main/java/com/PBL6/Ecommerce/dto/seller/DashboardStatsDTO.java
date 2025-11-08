package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;

/**
 * DTO for Dashboard Statistics
 * Used in Seller Dashboard overview
 */
public class DashboardStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalCustomers;
    
    // Revenue comparison
    private BigDecimal revenueChange; // % change compared to previous period
    private Long orderChange;
    
    public DashboardStatsDTO() {}
    
    public DashboardStatsDTO(BigDecimal totalRevenue, Long totalOrders, 
                            Long totalProducts, Long totalCustomers) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalProducts = totalProducts;
        this.totalCustomers = totalCustomers;
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(BigDecimal revenueChange) {
        this.revenueChange = revenueChange;
    }

    public Long getOrderChange() {
        return orderChange;
    }

    public void setOrderChange(Long orderChange) {
        this.orderChange = orderChange;
    }
}
