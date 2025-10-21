package com.PBL6.Ecommerce.domain.dto;

public class AddToCartRequest {
    private Long productVariantId;
    private int quantity;

    // Getters and Setters
    public Long getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(Long productVariantId) {
        this.productVariantId = productVariantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
