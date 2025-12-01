package com.PBL6.Ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoMoRefundRequestDTO {
    private Long orderId;
    private Long transactionId;
    private BigDecimal amount;
    private String description;
}
