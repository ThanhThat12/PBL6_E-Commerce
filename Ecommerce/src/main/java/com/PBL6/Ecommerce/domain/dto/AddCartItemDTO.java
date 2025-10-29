package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private int quantity;

    public AddCartItemDTO() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}