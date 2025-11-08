package com.PBL6.Ecommerce.dto.seller;

/**
 * DTO for Order Status Distribution
 */
public class OrderStatusDistributionDTO {
    
    private String status;
    private Long count;
    private Double percentage;
    
    public OrderStatusDistributionDTO() {}

    public OrderStatusDistributionDTO(String status, Long count, Double percentage) {
        this.status = status;
        this.count = count;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
