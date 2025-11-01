package com.PBL6.Ecommerce.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a product review
 * 
 * Validation:
 * - Rating: 1-5 sao
 * - Comment: optional, max 1000 chars
 * - Images: optional, URLs
 * - Can only update within 7 days
 */
public class UpdateReviewRequest {

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1 sao")
    @Max(value = 5, message = "Rating phải đến 5 sao")
    private Integer rating;

    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    private String comment;

    private java.util.List<String> images;

    // Constructors
    public UpdateReviewRequest() {}

    public UpdateReviewRequest(Integer rating, String comment, java.util.List<String> images) {
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    // Getters & Setters
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

    public java.util.List<String> getImages() {
        return images;
    }

    public void setImages(java.util.List<String> images) {
        this.images = images;
    }
}
