package com.PBL6.Ecommerce.dto.review;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ProductReview response
 * 
 * Includes:
 * - Review details (rating, comment, images)
 * - User info (hide sensitive data)
 * - Seller response
 * - Timestamps
 */
public class ProductReviewDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private List<String> images;
    private Boolean verifiedPurchase;
    private UserSimpleDTO user;
    private String sellerResponse;
    private LocalDateTime sellerResponseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ProductReviewDTO() {}

    public ProductReviewDTO(Long id, Integer rating, String comment, List<String> images, 
                           Boolean verifiedPurchase, UserSimpleDTO user, String sellerResponse, 
                           LocalDateTime sellerResponseDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
        this.verifiedPurchase = verifiedPurchase;
        this.user = user;
        this.sellerResponse = sellerResponse;
        this.sellerResponseDate = sellerResponseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getVerifiedPurchase() {
        return verifiedPurchase;
    }

    public void setVerifiedPurchase(Boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }

    public UserSimpleDTO getUser() {
        return user;
    }

    public void setUser(UserSimpleDTO user) {
        this.user = user;
    }

    public String getSellerResponse() {
        return sellerResponse;
    }

    public void setSellerResponse(String sellerResponse) {
        this.sellerResponse = sellerResponse;
    }

    public LocalDateTime getSellerResponseDate() {
        return sellerResponseDate;
    }

    public void setSellerResponseDate(LocalDateTime sellerResponseDate) {
        this.sellerResponseDate = sellerResponseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner class: UserSimpleDTO (hide sensitive data)
    public static class UserSimpleDTO {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;

        public UserSimpleDTO() {}

        public UserSimpleDTO(Long id, String username, String fullName, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
        }

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}
