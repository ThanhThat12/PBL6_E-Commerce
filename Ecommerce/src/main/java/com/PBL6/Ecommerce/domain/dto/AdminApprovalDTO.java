package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for Admin to approve a seller registration
 */
public class AdminApprovalDTO {
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    // Optional note from admin
    private String note;

    // Constructors
    public AdminApprovalDTO() {}

    public AdminApprovalDTO(Long shopId) {
        this.shopId = shopId;
    }

    // Getters and Setters
    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
