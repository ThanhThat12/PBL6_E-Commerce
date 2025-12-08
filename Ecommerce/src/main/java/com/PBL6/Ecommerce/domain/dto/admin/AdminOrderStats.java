package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;

/**
 * DTO for admin order statistics
 * Contains aggregated data for dashboard display
 */
public class AdminOrderStats {
    private Long totalOrders;          // Tổng số đơn hàng
    private Long pendingOrders;        // Số đơn hàng đang chờ xử lý (PENDING)
    private Long completedOrders;      // Số đơn hàng đã hoàn thành (COMPLETED)
    private BigDecimal totalRevenue;   // Tổng doanh thu (không tính đơn CANCELLED)

    // Constructors
    public AdminOrderStats() {
    }

    public AdminOrderStats(Long totalOrders, Long pendingOrders, Long completedOrders, BigDecimal totalRevenue) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
