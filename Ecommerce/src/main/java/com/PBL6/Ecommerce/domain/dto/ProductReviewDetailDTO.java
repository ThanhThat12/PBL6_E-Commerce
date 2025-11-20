package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

public class ProductReviewDetailDTO {
    private Long id;
    private String comment;
    private Integer rating;
    private String images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String username;
    private Long orderId;
    private Boolean verifiedPurchase;
    private String sellerResponse;
    private LocalDateTime sellerResponseDate;

    // Constructor
    public ProductReviewDetailDTO(Long id, String comment, Integer rating, String images, 
                                 LocalDateTime createdAt, LocalDateTime updatedAt, Long userId, 
                                 String username, Long orderId, Boolean verifiedPurchase, 
                                 String sellerResponse, LocalDateTime sellerResponseDate) {
        this.id = id;
        this.comment = comment;
        this.rating = rating;
        this.images = images;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.username = username;
        this.orderId = orderId;
        this.verifiedPurchase = verifiedPurchase;
        this.sellerResponse = sellerResponse;
        this.sellerResponseDate = sellerResponseDate;
    }

    // Getters & Setters (tương tự AdminReviewDTO, thêm field images)
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    
    // ... các getters/setters khác tương tự AdminReviewDTO
}