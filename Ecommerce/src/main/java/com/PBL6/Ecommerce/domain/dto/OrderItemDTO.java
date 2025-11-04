package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long id;
    private Long productId;
    private Long variantId;
    private String variantName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal; // price * quantity
    
    // Product details (optional, for display)
    private String productName;
    private String productImage;

    // Constructors
    public OrderItemDTO() {}

    public OrderItemDTO(Long id, Long productId, Long variantId, String variantName, 
                       BigDecimal price, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.variantName = variantName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        // Recalculate subtotal when price changes
        if (this.quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculate subtotal when quantity changes
        if (this.price != null) {
            this.subtotal = this.price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
}
