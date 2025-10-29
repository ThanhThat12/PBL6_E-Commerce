package com.PBL6.Ecommerce.domain.dto;

public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private String color;

     // Constructors
    public ProductImageDTO() {}

    public ProductImageDTO(String imageUrl, String color) {
        this.imageUrl = imageUrl;
        this.color = color;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}