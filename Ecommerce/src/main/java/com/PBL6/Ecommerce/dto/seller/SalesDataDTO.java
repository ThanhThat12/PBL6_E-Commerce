package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Sales Data (used in statistical charts)
 */
public class SalesDataDTO {
    
    private LocalDate date;
    private Long quantity;
    private BigDecimal revenue;
    
    public SalesDataDTO() {}

    public SalesDataDTO(LocalDate date, Long quantity, BigDecimal revenue) {
        this.date = date;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
