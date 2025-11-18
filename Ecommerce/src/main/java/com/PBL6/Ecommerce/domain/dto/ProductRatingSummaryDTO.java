package com.PBL6.Ecommerce.domain.dto;

import java.util.Map;

/**
 * DTO for product rating summary
 */
public class ProductRatingSummaryDTO {
    
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> starCounts; // {5: 10, 4: 5, 3: 2, 2: 1, 1: 0}

    // Constructors
    public ProductRatingSummaryDTO() {}

    public ProductRatingSummaryDTO(Double averageRating, Long totalReviews, Map<Integer, Long> starCounts) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.starCounts = starCounts;
    }

    // Getters and Setters
    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Map<Integer, Long> getStarCounts() {
        return starCounts;
    }

    public void setStarCounts(Map<Integer, Long> starCounts) {
        this.starCounts = starCounts;
    }
}