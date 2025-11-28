package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String mainImage;
    private String mainImagePublicId;
    private BigDecimal basePrice;
    private Boolean isActive;
    private String productCondition; // NEW, USED
    private BigDecimal rating; // Average rating (0-5)
    private Integer reviewCount; // Số lượng reviews
    private Integer soldCount; // Đã bán bao nhiêu
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    
    // Shipping dimensions
    private Integer weightGrams;
    private Integer lengthCm;
    private Integer widthCm;
    private Integer heightCm;
    
    // Relationships
    private CategoryDTO category;
    private Long shopId;
    private String shopName;
    private List<ProductVariantDTO> variants;
    private List<ProductImageDTO> images;

    // Constructor mặc định
    public ProductDTO() {}

    // Constructor with basic fields (backward compatibility)
    public ProductDTO(Long id, String name, String description, String image, BigDecimal price, 
                     String categoryName, String shopName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.mainImage = image;
        this.basePrice = price;
        this.shopName = shopName;
        this.category = new CategoryDTO();
        this.category.setName(categoryName);
        this.isActive = true;
        this.productCondition = "NEW";
        this.rating = BigDecimal.ZERO;
        this.reviewCount = 0;
        this.soldCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }

    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public String getMainImagePublicId() { return mainImagePublicId; }
    public void setMainImagePublicId(String mainImagePublicId) { this.mainImagePublicId = mainImagePublicId; }

    public String getProductCondition() { return productCondition; }
    public void setProductCondition(String productCondition) { this.productCondition = productCondition; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }

    public Integer getLengthCm() { return lengthCm; }
    public void setLengthCm(Integer lengthCm) { this.lengthCm = lengthCm; }

    public Integer getWidthCm() { return widthCm; }
    public void setWidthCm(Integer widthCm) { this.widthCm = widthCm; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    // Backward compatibility methods
    public String getImage() { return mainImage; }
    public void setImage(String image) { this.mainImage = image; }

    public BigDecimal getPrice() { return basePrice; }
    public void setPrice(BigDecimal price) { this.basePrice = price; }

    public String getCategoryName() { 
        return category != null ? category.getName() : null; 
    }
    public void setCategoryName(String categoryName) { 
        if (category == null) category = new CategoryDTO();
        category.setName(categoryName);
    }

    // Deprecated methods for backward compatibility - use variants or productCondition instead
    @Deprecated
    public Integer getStock() { 
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getStock();
        }
        return null; 
    }
    
    @Deprecated
    public void setStock(Integer stock) { /* Use variants instead */ }

    @Deprecated
    public String getCondition() { return productCondition; }
    
    @Deprecated
    public void setCondition(String condition) { this.productCondition = condition; }
}