package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for updating a product review
 */
public class UpdateReviewRequestDTO {
    
    @NotNull(message = "Rating không được null")
    @Min(value = 1, message = "Rating phải từ 1-5")
    @Max(value = 5, message = "Rating phải từ 1-5")
    private Integer rating;
    
    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    private String comment;
    
    private List<String> images;

    // Constructors
    public UpdateReviewRequestDTO() {}

    public UpdateReviewRequestDTO(Integer rating, String comment, List<String> images) {
        this.rating = rating;
        this.comment = comment;
        this.images = images;
    }

    // Getters and Setters
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