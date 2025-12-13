package com.PBL6.Ecommerce.domain.dto;

import lombok.Data;

@Data
public class ConfirmOrderRequestDTO {
    private Integer serviceId;      // GHN service ID
    private Integer serviceTypeId;  // GHN service type ID
    private String note;            // Ghi chú cho đơn hàng
}
