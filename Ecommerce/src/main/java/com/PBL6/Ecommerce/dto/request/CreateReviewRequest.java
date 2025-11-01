package com.PBL6.Ecommerce.dto.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a product review
 * 
 * Validation:
 * - Rating: 1-5 sao
 * - Comment: optional, max 1000 chars
 * - Images: optional, URLs uploaded to S3/Cloudinary
 * - Order must be COMPLETED
 * - User must own the order
 * - One review per product per user
 */
public class CreateReviewRequest {

    @NotNull(message = "Order ID không được để trống")
    private Long orderId;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1 sao")
    @Max(value = 5, message = "Rating phải đến 5 sao")
    private Integer rating;

    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    private String comment;

    private List<String> images; // URLs uploaded to S3/Cloudinary

    // Constructors
    public CreateReviewRequest() {}

    public CreateReviewRequest(Long orderId, Integer rating, String comment, List<String> images) {
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    // Getters & Setters
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
