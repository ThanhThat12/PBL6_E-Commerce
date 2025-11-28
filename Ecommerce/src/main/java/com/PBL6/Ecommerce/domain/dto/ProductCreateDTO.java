package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Enhanced ProductCreateDTO for creating products with separated concerns:
 * - Product basic info (this DTO)
 * - Images handled by /api/products/{id}/images/* endpoints
 * - Reviews handled by /api/products/{id}/reviews endpoints
 * - Variants can be included or added later
 */
public class ProductCreateDTO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    // shopId will be automatically set from JWT authentication for sellers
    private Long shopId;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    private BigDecimal basePrice;
    
    // Product condition - NEW or USED
    private String productCondition = "NEW";
    
    private Boolean isActive = true;
    
    // Shipping dimensions (optional)
    private Integer weightGrams;
    private Integer lengthCm;
    private Integer widthCm;
    private Integer heightCm;
    
    // Variants can be added during creation or later via separate endpoint
    @Valid
    private List<ProductVariantDTO> variants;
    
    // Primary attribute ID for variant images (optional)
    private Long primaryAttributeId;
    
    // Default constructor
    public ProductCreateDTO() {}

    // Getters and Setters
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public String getProductCondition() { return productCondition; }
    public void setProductCondition(String productCondition) { this.productCondition = productCondition; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }
    
    public Integer getLengthCm() { return lengthCm; }
    public void setLengthCm(Integer lengthCm) { this.lengthCm = lengthCm; }
    
    public Integer getWidthCm() { return widthCm; }
    public void setWidthCm(Integer widthCm) { this.widthCm = widthCm; }
    
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    
    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }
    
    public Long getPrimaryAttributeId() { return primaryAttributeId; }
    public void setPrimaryAttributeId(Long primaryAttributeId) { this.primaryAttributeId = primaryAttributeId; }
}