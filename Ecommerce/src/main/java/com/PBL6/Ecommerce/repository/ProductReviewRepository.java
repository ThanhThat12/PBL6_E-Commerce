package com.PBL6.Ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.ProductReview;

/**
 * Repository for ProductReview entity
 * 
 * Provides data access methods for product reviews
 */
@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    /**
     * Find all reviews for a product with pagination
     */
    Page<ProductReview> findByProductId(Long productId, Pageable pageable);

    /**
     * Find reviews by product and rating
     */
    Page<ProductReview> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);

    /**
     * Find reviews by user
     */
    Page<ProductReview> findByUserId(Long userId, Pageable pageable);

    /**
     * Check if user already reviewed a product
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Find all reviews for products belonging to a shop
     * Used for shop reviews listing
     */
    @Query("SELECT pr FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId " +
           "ORDER BY pr.createdAt DESC")
    Page<ProductReview> findByProductShopId(@Param("shopId") Long shopId, Pageable pageable);

    /**
     * Count reviews for a product
     */
    long countByProductId(Long productId);

    /**
     * Get average rating for a product
     */
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Count total reviews for a shop
     */
    @Query("SELECT COUNT(pr) FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId")
    long countByProductShopId(@Param("shopId") Long shopId);

    /**
     * Get average rating for a shop
     */
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId")
    Double getAverageRatingByProductShopId(@Param("shopId") Long shopId);

    /**
     * Count reviews by shop and rating
     */
    @Query("SELECT COUNT(pr) FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId AND pr.rating = :rating")
    long countByProductShopIdAndRating(@Param("shopId") Long shopId, @Param("rating") Integer rating);
}
