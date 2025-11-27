package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductCreateDTO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private Long shopId; // Optional - will be set from authentication for sellers
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")

     @Min(value = 0, message = "Chiều dài phải >= 0")
    private Integer lengthCm;

    @Min(value = 0, message = "Chiều rộng phải >= 0")
    private Integer widthCm;

    @Min(value = 0, message = "Chiều cao phải >= 0")
    private Integer heightCm;

    // Cân nặng (gram) - bắt buộc
    @NotNull(message = "Cân nặng là bắt buộc")
    @Min(value = 0, message = "Cân nặng phải >= 0")
    private Integer weightGrams;

    private BigDecimal basePrice;
    
    private Boolean isActive = true;
    
    private String mainImage;
    private List<ProductVariantDTO> variants;
    private List<String> imageUrls; // Giữ để backward compatibility
    private List<ProductImageDTO> images; // 🔧 THÊM: Images với color
    
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
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    
    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    // 🔧 THÊM: Getter/Setter cho images với color
    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }

    public Integer getLengthCm() { return lengthCm; }
    public void setLengthCm(Integer lengthCm) { this.lengthCm = lengthCm; }

    public Integer getWidthCm() { return widthCm; }
    public void setWidthCm(Integer widthCm) { this.widthCm = widthCm; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }

}