package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

public class VoucherApplicationResultDTO {
    private VoucherDTO voucher;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    
    // Getters and Setters
    public VoucherDTO getVoucher() {
        return voucher;
    }
    
    public void setVoucher(VoucherDTO voucher) {
        this.voucher = voucher;
    }
    
    public BigDecimal getOriginalTotal() {
        return originalTotal;
    }
    
    public void setOriginalTotal(BigDecimal originalTotal) {
        this.originalTotal = originalTotal;
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
