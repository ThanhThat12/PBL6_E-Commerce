package com.PBL6.Ecommerce.domain.entity.product;

import java.time.LocalDateTime;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Product Review Entity
 * 
 * Stores product reviews with seller response support
 * - Rating: 1-5 stars
 * - Comment: Optional text review
 * - Images: JSON array of URLs
 * - Seller can reply to review
 * - Auto-calculated: product.rating, shop.rating via triggers
 */
@Entity
@Table(name = "product_reviews", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "product_id"}, 
                           name = "unique_user_product_review")
       })
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "LONGTEXT")
    private String comment;

    @Column(columnDefinition = "JSON")
    private String images; // JSON array: [{"url": "...", "publicId": "..."}, ...]

    @Column(name = "images_count", columnDefinition = "INT DEFAULT 0")
    private Integer imagesCount = 0;

    @Column(nullable = false, columnDefinition = "bit(1) default 1")
    private Boolean verifiedPurchase = true;

    @Column(columnDefinition = "LONGTEXT")
    private String sellerResponse;

    @Column(name = "seller_response_date")
    private LocalDateTime sellerResponseDate;

    @Column(name = "edit_count", columnDefinition = "INT DEFAULT 0")
    private Integer editCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public ProductReview() {}

    public ProductReview(Product product, User user, Order order, Integer rating, String comment, String images) {
        this.product = product;
        this.user = user;
        this.order = order;
        this.rating = rating;
        this.comment = comment;
        this.images = images;
        this.verifiedPurchase = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getImagesCount() {
        return imagesCount;
    }

    public void setImagesCount(Integer imagesCount) {
        this.imagesCount = imagesCount;
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

    public Integer getEditCount() {
        return editCount;
    }

    public void setEditCount(Integer editCount) {
        this.editCount = editCount;
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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
