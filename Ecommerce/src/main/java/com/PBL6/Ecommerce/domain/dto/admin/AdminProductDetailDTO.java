package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đầy đủ cho chi tiết sản phẩm dành cho Admin
 * Bao gồm tất cả thông tin: product, variants, images, category, shop, stats, etc.
 */
public class AdminProductDetailDTO {
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private String description;
    private String mainImage;
    private AdminCategoryDetailDTO category;
    private AdminShopDetailDTO shop;
    private Boolean isActive;
    private Integer weightGrams;
    private AdminProductDimensionsDTO dimensions;
    
    // Thống kê
    private Long totalStock;       // Tổng stock từ tất cả variants
    private Long totalSold;        // Tổng đã bán (COMPLETED orders)
    private Double averageRating;  // Rating trung bình
    private Long reviewCount;      // Số lượng reviews
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Chi tiết
    private List<AdminProductImageDTO> images;      // Tất cả ảnh của product
    private List<AdminProductVariantDTO> variants;  // Tất cả variants

    public AdminProductDetailDTO() {
        this.images = new ArrayList<>();
        this.variants = new ArrayList<>();
    }

    // Constructor đầy đủ
    public AdminProductDetailDTO(Long id, String name, BigDecimal basePrice, String description,
                                String mainImage, AdminCategoryDetailDTO category, AdminShopDetailDTO shop,
                                Boolean isActive, Integer weightGrams, AdminProductDimensionsDTO dimensions,
                                Long totalStock, Long totalSold, Double averageRating, Long reviewCount,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.description = description;
        this.mainImage = mainImage;
        this.category = category;
        this.shop = shop;
        this.isActive = isActive;
        this.weightGrams = weightGrams;
        this.dimensions = dimensions;
        this.totalStock = totalStock != null ? totalStock : 0L;
        this.totalSold = totalSold != null ? totalSold : 0L;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.images = new ArrayList<>();
        this.variants = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public AdminCategoryDetailDTO getCategory() {
        return category;
    }

    public void setCategory(AdminCategoryDetailDTO category) {
        this.category = category;
    }

    public AdminShopDetailDTO getShop() {
        return shop;
    }

    public void setShop(AdminShopDetailDTO shop) {
        this.shop = shop;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(Integer weightGrams) {
        this.weightGrams = weightGrams;
    }

    public AdminProductDimensionsDTO getDimensions() {
        return dimensions;
    }

    public void setDimensions(AdminProductDimensionsDTO dimensions) {
        this.dimensions = dimensions;
    }

    public Long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Long totalStock) {
        this.totalStock = totalStock;
    }

    public Long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(Long totalSold) {
        this.totalSold = totalSold;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AdminProductImageDTO> getImages() {
        return images;
    }

    public void setImages(List<AdminProductImageDTO> images) {
        this.images = images;
    }

    public List<AdminProductVariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<AdminProductVariantDTO> variants) {
        this.variants = variants;
    }
}
