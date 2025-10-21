package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantDTO {
    private Long id;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private List<ProductVariantValueDTO> variantValues;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public List<ProductVariantValueDTO> getVariantValues() { return variantValues; }
    public void setVariantValues(List<ProductVariantValueDTO> variantValues) { this.variantValues = variantValues; }
}