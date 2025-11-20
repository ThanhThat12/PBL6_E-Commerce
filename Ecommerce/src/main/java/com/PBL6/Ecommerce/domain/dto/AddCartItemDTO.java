package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemDTO {
    // Support both productId (old) and variantId (new) for backward compatibility
    private Long productId;
    
    private Long variantId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private int quantity;

    public AddCartItemDTO() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Long getVariantId() { 
        // If variantId is not provided, use productId as fallback
        return variantId != null ? variantId : productId; 
    }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    // Validation method to ensure at least one ID is provided
    public boolean hasValidId() {
        return productId != null || variantId != null;
    }
}