package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateOrderStatusDTO {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "PENDING|PROCESSING|COMPLETED|CANCELLED", 
             message = "Trạng thái phải là: PENDING, PROCESSING, COMPLETED hoặc CANCELLED")
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED

    public UpdateOrderStatusDTO() {}

    public UpdateOrderStatusDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
