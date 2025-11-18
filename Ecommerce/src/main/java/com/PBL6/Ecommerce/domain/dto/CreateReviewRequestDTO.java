package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating a new product review
 */
public class CreateReviewRequestDTO {
    
    @NotNull(message = "Product ID không được null")
    private Long productId;
    
    @NotNull(message = "Order ID không được null") 
    private Long orderId;
    
    @NotNull(message = "Rating không được null")
    @Min(value = 1, message = "Rating phải từ 1-5")
    @Max(value = 5, message = "Rating phải từ 1-5")
    private Integer rating;
    
    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    private String comment;
    
    private List<String> images;

    // Constructors
    public CreateReviewRequestDTO() {}

    public CreateReviewRequestDTO(Long productId, Long orderId, Integer rating, String comment, List<String> images) {
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}