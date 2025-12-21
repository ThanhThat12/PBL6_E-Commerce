package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmOrderRequestDTO {
    @NotNull(message = "Service ID không được để trống")
    private Integer serviceId;      // GHN service ID
    
    private Integer serviceTypeId;  // GHN service type ID (optional)
    
    private String note;            // Ghi chú cho đơn hàng (optional)
}
