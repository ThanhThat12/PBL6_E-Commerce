package com.PBL6.Ecommerce.domain.dto.seller;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for cancelling order with reason - Phase 3
 * Used when seller cancels an order
 */
public class OrderCancelDTO {
    
    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
    
    public OrderCancelDTO() {}
    
    public OrderCancelDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
