package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;

/**
 * DTO for overall category statistics (admin dashboard)
 */
public class AdminCategoryStatsDTO {
    private Long totalCategories;
    private Long totalProducts; // All active products across all categories
    private Long productsSold; // Total quantity sold from COMPLETED orders
    
    // For sales by category
    private String categoryName;
    private BigDecimal totalRevenue;
    private Long orderCount;
    
    // Constructor for overall stats
    public AdminCategoryStatsDTO(Long totalCategories, Long totalProducts, Long productsSold) {
        this.totalCategories = totalCategories != null ? totalCategories : 0L;
        this.totalProducts = totalProducts != null ? totalProducts : 0L;
        this.productsSold = productsSold != null ? productsSold : 0L;
    }
    
    // Constructor for sales by category
    public AdminCategoryStatsDTO(String categoryName, BigDecimal totalRevenue, Long orderCount) {
        this.categoryName = categoryName;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.orderCount = orderCount != null ? orderCount : 0L;
    }
    
    // Getters and Setters
    public Long getTotalCategories() {
        return totalCategories;
    }
    
    public void setTotalCategories(Long totalCategories) {
        this.totalCategories = totalCategories;
    }
    
    public Long getTotalProducts() {
        return totalProducts;
    }
    
    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    public Long getProductsSold() {
        return productsSold;
    }
    
    public void setProductsSold(Long productsSold) {
        this.productsSold = productsSold;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Long getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
