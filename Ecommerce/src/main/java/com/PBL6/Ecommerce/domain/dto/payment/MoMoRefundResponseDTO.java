package com.PBL6.Ecommerce.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoMoRefundResponseDTO {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private BigDecimal amount;
    private Long transId;
    private Integer resultCode;
    private String message;
    private Long responseTime;
}
