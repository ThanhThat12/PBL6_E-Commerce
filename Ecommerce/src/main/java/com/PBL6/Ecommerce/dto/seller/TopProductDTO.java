package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;

/**
 * DTO for Top Selling Products
 * Used in dashboard and statistical reports
 */
public class TopProductDTO {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long soldCount;
    private BigDecimal revenue;
    private BigDecimal price;
    
    public TopProductDTO() {}
    
    public TopProductDTO(Long productId, String productName, String imageUrl, 
                        Long soldCount, BigDecimal revenue, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.soldCount = soldCount;
        this.revenue = revenue;
        this.price = price;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Long soldCount) {
        this.soldCount = soldCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
