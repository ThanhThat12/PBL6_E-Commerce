package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ApplyVoucherRequestDTO {
    @NotBlank
    private String voucherCode;
    
    @NotEmpty
    private List<Long> productIds;
    
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal cartTotal;
    
    // Getters and Setters
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public List<Long> getProductIds() {
        return productIds;
    }
    
    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
    
    public BigDecimal getCartTotal() {
        return cartTotal;
    }
    
    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }
}

