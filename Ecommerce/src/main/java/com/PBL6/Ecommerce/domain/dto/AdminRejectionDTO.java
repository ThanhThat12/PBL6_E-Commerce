package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for Admin to reject a seller registration
 */
public class AdminRejectionDTO {
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(min = 10, max = 1000, message = "Lý do từ chối phải từ 10-1000 ký tự")
    private String rejectionReason;

    // Constructors
    public AdminRejectionDTO() {}

    public AdminRejectionDTO(Long shopId, String rejectionReason) {
        this.shopId = shopId;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
