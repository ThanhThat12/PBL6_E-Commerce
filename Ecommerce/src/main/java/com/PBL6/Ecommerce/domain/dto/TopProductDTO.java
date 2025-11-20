package com.PBL6.Ecommerce.domain.dto;

public class TopProductDTO {
    private Long productId;
    private String productName;
    private String mainImage;
    private Long totalQuantitySold;
    private Long totalOrders;

    public TopProductDTO() {
    }

    public TopProductDTO(Long productId, String productName, String mainImage, 
                         Long totalQuantitySold, Long totalOrders) {
        this.productId = productId;
        this.productName = productName;
        this.mainImage = mainImage;
        this.totalQuantitySold = totalQuantitySold;
        this.totalOrders = totalOrders;
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

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(Long totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
}
