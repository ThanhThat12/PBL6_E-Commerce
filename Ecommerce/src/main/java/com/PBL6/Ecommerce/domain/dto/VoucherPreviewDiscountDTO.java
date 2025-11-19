package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

public class VoucherPreviewDiscountDTO {
    private BigDecimal cartTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;

    public VoucherPreviewDiscountDTO() {
    }

    public VoucherPreviewDiscountDTO(BigDecimal cartTotal, BigDecimal discountAmount, BigDecimal finalTotal) {
        this.cartTotal = cartTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
    }

    // Getters and Setters
    public BigDecimal getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }
}
