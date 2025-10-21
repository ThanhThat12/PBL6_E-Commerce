package com.PBL6.Ecommerce.domain.dto;

public class UpdateOrderStatusDTO {
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
