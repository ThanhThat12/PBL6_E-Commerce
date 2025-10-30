package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private List<OrderItem> items;
    private Long shippingAddressId;
    private String paymentMethod; // "COD", "MOMO", "VNPAY", "BANK_TRANSFER"
    private String voucherCode;
    private String note;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long productId;
        private Long variantId;
        private Integer quantity;
    }
}
