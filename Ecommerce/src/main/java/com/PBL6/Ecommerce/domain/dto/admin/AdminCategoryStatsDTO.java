package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO for overall category statistics (admin dashboard)
 */
public class AdminCategoryStatsDTO {
    private Long totalCategories;
    private Long totalProducts; // All active products across all categories
    private Long productsSold; // Total quantity sold from COMPLETED orders
    
    public AdminCategoryStatsDTO(Long totalCategories, Long totalProducts, Long productsSold) {
        this.totalCategories = totalCategories != null ? totalCategories : 0L;
        this.totalProducts = totalProducts != null ? totalProducts : 0L;
        this.productsSold = productsSold != null ? productsSold : 0L;
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
}
