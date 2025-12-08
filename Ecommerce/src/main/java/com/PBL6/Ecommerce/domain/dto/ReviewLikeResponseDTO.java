package com.PBL6.Ecommerce.domain.dto;

/**
 * Response DTO for like/unlike operations
 */
public class ReviewLikeResponseDTO {
    
    private Long reviewId;
    private Boolean liked;
    private Long likesCount;
    
    // Constructors
    public ReviewLikeResponseDTO() {}
    
    public ReviewLikeResponseDTO(Long reviewId, Boolean liked, Long likesCount) {
        this.reviewId = reviewId;
        this.liked = liked;
        this.likesCount = likesCount;
    }
    
    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }
}
