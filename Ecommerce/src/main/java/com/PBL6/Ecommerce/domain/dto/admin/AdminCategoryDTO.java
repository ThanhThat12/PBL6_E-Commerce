package com.PBL6.Ecommerce.domain.dto.admin;

/**
 * DTO for admin category list with statistics
 */
public class AdminCategoryDTO {
    private Long id;
    private String name;
    private Long totalProducts; // Products with is_active = 1
    private Long totalSoldProducts; // Total quantity sold from COMPLETED orders
    
    public AdminCategoryDTO(Long id, String name, Long totalProducts, Long totalSoldProducts) {
        this.id = id;
        this.name = name;
        this.totalProducts = totalProducts != null ? totalProducts : 0L;
        this.totalSoldProducts = totalSoldProducts != null ? totalSoldProducts : 0L;
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
    
    public Long getTotalProducts() {
        return totalProducts;
    }
    
    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    public Long getTotalSoldProducts() {
        return totalSoldProducts;
    }
    
    public void setTotalSoldProducts(Long totalSoldProducts) {
        this.totalSoldProducts = totalSoldProducts;
    }
}
