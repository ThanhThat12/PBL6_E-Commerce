package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    
    /**
     * Find like by user and review
     */
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);
    
    /**
     * Check if user liked a review
     */
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
    
    /**
     * Count likes for a review
     */
    Long countByReviewId(Long reviewId);
    
    /**
     * Delete all likes for a review (when review is deleted)
     */
    void deleteByReviewId(Long reviewId);
}
