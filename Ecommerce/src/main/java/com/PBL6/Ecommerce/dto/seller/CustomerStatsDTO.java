package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;

/**
 * DTO for Customer Statistics
 */
public class CustomerStatsDTO {
    
    private Long totalCustomers;
    private Long newCustomers;
    private Long returningCustomers;
    private BigDecimal averageOrderValue;
    
    public CustomerStatsDTO() {}

    public CustomerStatsDTO(Long totalCustomers, Long newCustomers, 
                           Long returningCustomers, BigDecimal averageOrderValue) {
        this.totalCustomers = totalCustomers;
        this.newCustomers = newCustomers;
        this.returningCustomers = returningCustomers;
        this.averageOrderValue = averageOrderValue;
    }

    // Getters and Setters
    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Long getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(Long newCustomers) {
        this.newCustomers = newCustomers;
    }

    public Long getReturningCustomers() {
        return returningCustomers;
    }

    public void setReturningCustomers(Long returningCustomers) {
        this.returningCustomers = returningCustomers;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
}
