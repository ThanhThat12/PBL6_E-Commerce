package com.PBL6.Ecommerce.domain.dto;

public class AddCartItemDTO {
    private Long productId;
    private int quantity;

    public AddCartItemDTO() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}