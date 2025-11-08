package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Revenue Statistics
 * Used for revenue charts in dashboard
 */
public class RevenueDataDTO {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orderCount;
    
    public RevenueDataDTO() {}
    
    public RevenueDataDTO(LocalDate date, BigDecimal revenue, Long orderCount) {
        this.date = date;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
