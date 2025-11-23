package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;

public class AdminListProductDTO {
    private Long productId;
    private String productName;
    private String mainImage;
    private String categoryName;
    private BigDecimal basePrice;
    private Long totalStock;
    private String status;
    private Long sales;
    private Double rating;

    public AdminListProductDTO(Long productId, String productName, String mainImage, String categoryName,
                               BigDecimal basePrice, Long totalStock, Boolean isActive,
                               Long sales, Double rating) {
        this.productId = productId;
        this.productName = productName;
        this.mainImage = mainImage;
        this.categoryName = categoryName;
        this.basePrice = basePrice;
        this.totalStock = totalStock != null ? totalStock : 0L;
        this.status = (isActive != null && isActive) ? "Active" : "Pending";
        this.sales = sales != null ? sales : 0L;
        this.rating = rating != null ? rating : 0.0;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Long totalStock) {
        this.totalStock = totalStock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSales() {
        return sales;
    }

    public void setSales(Long sales) {
        this.sales = sales;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
