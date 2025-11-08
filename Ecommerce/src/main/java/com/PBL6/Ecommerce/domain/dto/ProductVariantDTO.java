package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantDTO {
    private Long id;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private List<ProductVariantValueDTO> variantValues;
    private String imageUrl; // ✅ Ảnh cho variant (theo màu)

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public List<ProductVariantValueDTO> getVariantValues() { return variantValues; }
    public void setVariantValues(List<ProductVariantValueDTO> variantValues) { this.variantValues = variantValues; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}