package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalculateResponse {
    private List<ItemDetail> items;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private VoucherDetail voucher;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDetail {
        private Long productId;
        private String productName;
        private Long variantId;
        private String variantName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Integer availableStock;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherDetail {
        private String code;
        private String name;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal appliedDiscount;
    }
}
