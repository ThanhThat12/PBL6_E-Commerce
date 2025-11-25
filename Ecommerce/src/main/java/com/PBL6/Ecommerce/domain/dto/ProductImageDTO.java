package com.PBL6.Ecommerce.domain.dto;

public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private String variantValueName;

     // Constructors
    public ProductImageDTO() {}

    public ProductImageDTO(String imageUrl, String variantValueName) {
        this.imageUrl = imageUrl;
        this.variantValueName = variantValueName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getVariantValueName() { return variantValueName; }
    public void setVariantValueName(String variantValueName) { this.variantValueName = variantValueName; }
}