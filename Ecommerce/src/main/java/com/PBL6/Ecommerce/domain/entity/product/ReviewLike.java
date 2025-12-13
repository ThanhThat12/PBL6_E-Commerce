package com.PBL6.Ecommerce.domain.entity.product;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.PBL6.Ecommerce.domain.entity.user.User;

/**
 * Entity for Review Like - allows users to mark reviews as helpful
 */
@Entity
@Table(name = "review_like", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
public class ReviewLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public ReviewLike() {}
    
    public ReviewLike(User user, ProductReview review) {
        this.user = user;
        this.review = review;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProductReview getReview() {
        return review;
    }

    public void setReview(ProductReview review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
