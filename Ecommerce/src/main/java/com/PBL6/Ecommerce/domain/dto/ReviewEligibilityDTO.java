package com.PBL6.Ecommerce.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO for Review Eligibility Check Response
 * Checks if user can review a product and provides relevant info
 */
public class ReviewEligibilityDTO {
    
    private Long productId;
    private Long orderId;
    
    // Review eligibility status
    private Boolean canReview;           // Can create new review
    private Boolean hasReviewed;         // Already reviewed this product
    private Boolean hasPurchased;        // Has a COMPLETED order containing this product
    
    // Time constraints
    private Integer daysRemainingToReview;  // Days remaining to create review (30 days from order completion)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewDeadline;    // Deadline to submit review
    
    // Existing review info (if hasReviewed = true)
    private Long existingReviewId;
    private Boolean canEditReview;        // Can edit existing review (only 1 time within 30 days)
    private Integer editCount;            // Number of times already edited
    private Integer daysRemainingToEdit;  // Days remaining to edit
    
    // Message for user
    private String message;

    // Constructors
    public ReviewEligibilityDTO() {}

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

    public Boolean getCanReview() {
        return canReview;
    }

    public void setCanReview(Boolean canReview) {
        this.canReview = canReview;
    }

    public Boolean getHasReviewed() {
        return hasReviewed;
    }

    public void setHasReviewed(Boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }

    public Boolean getHasPurchased() {
        return hasPurchased;
    }

    public void setHasPurchased(Boolean hasPurchased) {
        this.hasPurchased = hasPurchased;
    }

    public Integer getDaysRemainingToReview() {
        return daysRemainingToReview;
    }

    public void setDaysRemainingToReview(Integer daysRemainingToReview) {
        this.daysRemainingToReview = daysRemainingToReview;
    }

    public LocalDateTime getReviewDeadline() {
        return reviewDeadline;
    }

    public void setReviewDeadline(LocalDateTime reviewDeadline) {
        this.reviewDeadline = reviewDeadline;
    }

    public Long getExistingReviewId() {
        return existingReviewId;
    }

    public void setExistingReviewId(Long existingReviewId) {
        this.existingReviewId = existingReviewId;
    }

    public Boolean getCanEditReview() {
        return canEditReview;
    }

    public void setCanEditReview(Boolean canEditReview) {
        this.canEditReview = canEditReview;
    }

    public Integer getEditCount() {
        return editCount;
    }

    public void setEditCount(Integer editCount) {
        this.editCount = editCount;
    }

    public Integer getDaysRemainingToEdit() {
        return daysRemainingToEdit;
    }

    public void setDaysRemainingToEdit(Integer daysRemainingToEdit) {
        this.daysRemainingToEdit = daysRemainingToEdit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
