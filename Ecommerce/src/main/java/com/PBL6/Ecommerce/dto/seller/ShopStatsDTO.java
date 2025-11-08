package com.PBL6.Ecommerce.dto.seller;

import java.math.BigDecimal;

/**
 * DTO for Shop Statistics
 * Used in GET /api/seller/shop/stats
 */
public class ShopStatsDTO {
    
    private BigDecimal rating;           // Average rating from reviews
    private Long followersCount;         // Future feature (default 0)
    private Long productsCount;          // Total products
    private Long ordersCount;            // Total orders
    private Long activeProductsCount;    // Active products only
    
    public ShopStatsDTO() {}

    public ShopStatsDTO(BigDecimal rating, Long followersCount, Long productsCount, 
                       Long ordersCount, Long activeProductsCount) {
        this.rating = rating;
        this.followersCount = followersCount;
        this.productsCount = productsCount;
        this.ordersCount = ordersCount;
        this.activeProductsCount = activeProductsCount;
    }

    // Getters and Setters
    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(Long productsCount) {
        this.productsCount = productsCount;
    }

    public Long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(Long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public Long getActiveProductsCount() {
        return activeProductsCount;
    }

    public void setActiveProductsCount(Long activeProductsCount) {
        this.activeProductsCount = activeProductsCount;
    }
}
