package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

/**
 * DTO để hiển thị doanh thu theo tháng
 * Dùng cho biểu đồ cột tại Frontend
 */
public class MonthlyRevenueDTO {
    private int year;           // Năm (2025)
    private int month;          // Tháng (1-12)
    private String monthName;   // Tên tháng (Tháng 1, Tháng 2, ...)
    private BigDecimal revenue; // Tổng doanh thu
    private Long orderCount;    // Số lượng đơn hàng COMPLETED

    public MonthlyRevenueDTO() {
    }

    public MonthlyRevenueDTO(int year, int month, BigDecimal revenue, Long orderCount) {
        this.year = year;
        this.month = month;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.orderCount = orderCount != null ? orderCount : 0L;
        this.monthName = "Tháng " + month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
        this.monthName = "Tháng " + month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
