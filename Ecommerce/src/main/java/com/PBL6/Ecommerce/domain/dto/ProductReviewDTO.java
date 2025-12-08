package com.PBL6.Ecommerce.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Product Review Response
 */
public class ProductReviewDTO {
    private Long id;
    
    @NotNull(message = "Rating không được null")
    @Min(value = 1, message = "Rating phải từ 1-5")
    @Max(value = 5, message = "Rating phải từ 1-5")
    private Integer rating;
    
    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    private String comment;
    
    private List<String> images;
    
    private Boolean verifiedPurchase;
    
    private String sellerResponse;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sellerResponseDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // User info
    private Long userId;
    private String userName;
    private String userFullName;
    private String userAvatarUrl;
    
    // Product info
    private Long productId;
    private String productName;
    private String productImage;     // Main image of the product
    private String variantInfo;      // Variant description (e.g., "Màu: Đỏ, Size: L")
    
    // Order info
    private Long orderId;
    private LocalDateTime purchaseDate;  // When the order was completed
    
    // Like info
    private Long likesCount;
    private Boolean isLikedByCurrentUser;
    
    // Review constraints info (for UI)
    private Boolean canEdit;           // Can user edit this review (only 1 time within 30 days)
    private Integer editCount;         // Number of times edited (max 1)
    private Integer daysRemainingToEdit; // Days remaining to edit (from review creation date)

    // Constructors
    public ProductReviewDTO() {}

    public ProductReviewDTO(Long id, Integer rating, String comment, List<String> images, 
                           Boolean verifiedPurchase, String sellerResponse, 
                           LocalDateTime sellerResponseDate, LocalDateTime createdAt, 
                           LocalDateTime updatedAt, Long userId, String userName, 
                           String userFullName, String userAvatarUrl, Long productId, 
                           String productName, Long orderId) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
        this.verifiedPurchase = verifiedPurchase;
        this.sellerResponse = sellerResponse;
        this.sellerResponseDate = sellerResponseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userName = userName;
        this.userFullName = userFullName;
        this.userAvatarUrl = userAvatarUrl;
        this.productId = productId;
        this.productName = productName;
        this.orderId = orderId;
    }

    // Getters and Setters
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

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

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getVariantInfo() {
        return variantInfo;
    }

    public void setVariantInfo(String variantInfo) {
        this.variantInfo = variantInfo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    public Boolean getIsLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    public void setIsLikedByCurrentUser(Boolean isLikedByCurrentUser) {
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }
    
    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
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
}