package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    // Lấy tất cả reviews của một sản phẩm
    Page<ProductReview> findByProductId(Long productId, Pageable pageable);
    
    // Lấy reviews của sản phẩm theo rating
    Page<ProductReview> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);
    
    // Lấy tất cả reviews của một user
    Page<ProductReview> findByUserId(Long userId, Pageable pageable);
    
    // Lấy tất cả reviews của các sản phẩm thuộc một shop
    @Query("SELECT pr FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId " +
           "ORDER BY pr.createdAt DESC")
    Page<ProductReview> findByProductShopId(@Param("shopId") Long shopId, Pageable pageable);
    
    // Kiểm tra user đã review sản phẩm này chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Lấy review của user cho sản phẩm cụ thể
    Optional<ProductReview> findByUserIdAndProductId(Long userId, Long productId);
    
    // Đếm số reviews của sản phẩm
    long countByProductId(Long productId);
    
    // Lấy rating trung bình của sản phẩm
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // Đếm reviews theo từng rating level cho một sản phẩm
    @Query("SELECT pr.rating, COUNT(pr) FROM ProductReview pr " +
           "WHERE pr.product.id = :productId " +
           "GROUP BY pr.rating " +
           "ORDER BY pr.rating DESC")
    Object[][] countByProductIdGroupByRating(@Param("productId") Long productId);
    
    // Lấy reviews có seller response
    @Query("SELECT pr FROM ProductReview pr " +
           "WHERE pr.product.id = :productId " +
           "AND pr.sellerResponse IS NOT NULL")
    Page<ProductReview> findByProductIdWithSellerResponse(@Param("productId") Long productId, Pageable pageable);

    // ========== REVIEW IMAGE QUERIES ==========

    /**
     * Find reviews with images
     */
    @Query("SELECT pr FROM ProductReview pr WHERE pr.images IS NOT NULL AND pr.images != '[]'")
    Page<ProductReview> findReviewsWithImages(Pageable pageable);

    /**
     * Find reviews with images for a specific product
     */
    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = :productId AND pr.images IS NOT NULL AND pr.images != '[]'")
    Page<ProductReview> findReviewsWithImagesByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * Count reviews with images for a product
     */
    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId AND pr.images IS NOT NULL AND pr.images != '[]'")
    long countReviewsWithImagesByProductId(@Param("productId") Long productId);

}
