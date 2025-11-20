package com.PBL6.Ecommerce.domain.dto.admin;

public class AdminProductStats {
    private Long totalProducts;
    private Long activeProducts;
    private Long pendingProducts;
    private Long totalProductsSold;

    public AdminProductStats(Long totalProducts, Long activeProducts, Long pendingProducts, Long totalProductsSold) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.pendingProducts = pendingProducts;
        this.totalProductsSold = totalProductsSold;
    }

    // Getters and Setters
    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(Long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public Long getPendingProducts() {
        return pendingProducts;
    }

    public void setPendingProducts(Long pendingProducts) {
        this.pendingProducts = pendingProducts;
    }

    public Long getTotalProductsSold() {
        return totalProductsSold;
    }

    public void setTotalProductsSold(Long totalProductsSold) {
        this.totalProductsSold = totalProductsSold;
    }
}
