package com.PBL6.Ecommerce.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    //  Tìm products theo categoryId
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findByCategoryIdAndShopId(Long categoryId, Long shopId);

    
    //  Tìm products theo shopId
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    
    //  Tìm products theo userId (seller) - SỬA p.shop.user.id → p.shop.owner.id
    @Query("SELECT p FROM Product p WHERE p.shop.owner.id = :userId")
    List<Product> findByUserId(@Param("userId") Long userId);
    
    //  Xóa products theo userId (seller) - SỬA p.shop.user.id → p.shop.owner.id
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.shop.owner.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // ✅ Đếm số products theo categoryId
    long countByCategoryId(Long categoryId);

    // Tìm theo tên (có thể tìm kiếm một phần)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Tìm theo shop (không phân trang)
    List<Product> findByShopId(Long shopId);
    
    // Tìm theo shop entity
    @Query("SELECT p FROM Product p WHERE p.shop = :shop")
    List<Product> findByShop(@Param("shop") Shop shop);

    // Tìm sản phẩm đang hoạt động
    Page<Product> findByIsActiveTrue(Pageable pageable);

    //  Tìm sản phẩm chờ duyệt (is_active = false)
    Page<Product> findByIsActiveFalse(Pageable pageable);
    
    //  Đếm sản phẩm chờ duyệt
    long countByIsActiveFalse();

    
    
    // Tìm theo khoảng giá
    @Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);
    
    // Tìm kiếm phức tạp với rating filter (bao gồm shop name)
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(p.shop.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:shopId IS NULL OR p.shop.id = :shopId) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.basePrice <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating)")
    Page<Product> findProductsWithFilters(@Param("name") String name,
                                        @Param("categoryId") Long categoryId,
                                        @Param("shopId") Long shopId,
                                        @Param("isActive") Boolean isActive,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        @Param("minRating") BigDecimal minRating,
                                        Pageable pageable);
    
    // ========== SEARCH SUGGESTIONS QUERIES ==========
    
    /**
     * Find product name suggestions (for autocomplete)
     * Returns distinct product names starting with the query
     */
    @Query("SELECT DISTINCT p.name FROM Product p WHERE " +
           "p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT(:query, '%')) " +
           "ORDER BY p.soldCount DESC")
    List<String> findProductNameSuggestions(@Param("query") String query, Pageable pageable);
    
    /**
     * Find products for suggestion (mini product cards)
     * Returns top products matching query, ordered by relevance
     */
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.soldCount DESC, p.rating DESC")
    List<Product> findProductsForSuggestion(@Param("query") String query, Pageable pageable);
    
    /**
     * Find products by name OR shop name (for combined search)
     * Returns products where product name or shop name matches query
     */
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.shop.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY p.soldCount DESC, p.rating DESC")
    List<Product> findProductsByNameOrShopName(@Param("query") String query, Pageable pageable);
    
    /**
     * Search products by name or description (for grouped search)
     * Returns all matching products without pagination
     */
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY p.soldCount DESC, p.rating DESC")
    List<Product> searchByNameOrDescription(@Param("query") String query);
    
    /**
     * Count products by shop name matching query
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.shop.id = :shopId")
    long countActiveByShopId(@Param("shopId") Long shopId);
    
    // Đếm số sản phẩm theo shop
    long countByShopId(Long shopId);
    
    // Seller Dashboard - Đếm số sản phẩm được tạo trong khoảng thời gian
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.createdAt BETWEEN :startDate AND :endDate")
    Long countByShopIdAndCreatedAtBetween(@Param("shopId") Long shopId, 
                                          @Param("startDate") java.time.LocalDateTime startDate, 
                                          @Param("endDate") java.time.LocalDateTime endDate);
    
    // Tìm theo category và trạng thái active
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

     //  Tìm sản phẩm của seller theo trạng thái
    @Query("SELECT p FROM Product p WHERE p.shop.owner.id = :sellerId AND p.isActive = :isActive")
    Page<Product> findBySellerIdAndIsActive(@Param("sellerId") Long sellerId, 
                                          @Param("isActive") Boolean isActive, 
                                          Pageable pageable);
    
    //  Đếm sản phẩm của seller theo trạng thái
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.owner.id = :sellerId AND p.isActive = :isActive")
    long countBySellerIdAndIsActive(@Param("sellerId") Long sellerId, @Param("isActive") Boolean isActive);

     // Tìm sản phẩm theo shop ID và trạng thái
    Page<Product> findByShopIdAndIsActive(Long shopId, Boolean isActive, Pageable pageable);
    List<Product> findByShopIdAndIsActive(Long shopId, Boolean isActive);
    // ========== IMAGE-RELATED QUERIES ==========

    /**
     * Find products with main images
     */
    @Query("SELECT p FROM Product p WHERE p.mainImage IS NOT NULL")
    Page<Product> findProductsWithMainImages(Pageable pageable);

    /**
     * Find products without main images
     */
    @Query("SELECT p FROM Product p WHERE p.mainImage IS NULL AND p.shop.owner.id = :sellerId")
    List<Product> findProductsWithoutMainImagesBySeller(@Param("sellerId") Long sellerId);

    /**
     * Find product by main image public_id (for deletion verification)
     */
    @Query("SELECT p FROM Product p WHERE p.mainImagePublicId = :publicId")
    List<Product> findByMainImagePublicId(@Param("publicId") String publicId);

    // Đơn giản hóa: Chỉ lấy Product entities, logic xử lý sẽ ở Service
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
    Page<Product> findAllWithCategory(Pageable pageable);

    //ADMIN Lấy products theo category name
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE c.name = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    //ADMIN Lấy products theo isActive status
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.isActive = :isActive")
    Page<Product> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    //ADMIN Search products by name
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * PUBLIC Find best-selling products (active, ordered by soldCount DESC)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.soldCount DESC, p.rating DESC")
    Page<Product> findBestSellingProducts(Pageable pageable);

    /**
     * PUBLIC Find top-rated products (active, rating > 0, ordered by rating DESC)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.rating > 0 " +
           "ORDER BY p.rating DESC, p.reviewCount DESC, p.soldCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);

    /**
     * PUBLIC Find top-rated products with minimum rating filter
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.rating >= :minRating " +
           "ORDER BY p.rating DESC, p.reviewCount DESC, p.soldCount DESC")
    Page<Product> findTopRatedProductsWithMinRating(@Param("minRating") BigDecimal minRating, Pageable pageable);
    
    /**
     * Update sold_count for a product based on completed order items
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.soldCount = " +
           "(SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.variant.product.id = p.id AND o.status = 'COMPLETED') " +
           "WHERE p.id = :productId")
    void updateSoldCount(@Param("productId") Long productId);
    
    /**
     * Update sold_count for all products based on completed order items
     */
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.soldCount = " +
           "(SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.variant.product.id = p.id AND o.status = 'COMPLETED')")
    void updateAllSoldCounts();

       /**
        * Narrow update for rating, reviewCount and updatedAt only.
        * Reduces lock contention compared to full-entity save.
        */
       @Modifying
       @Transactional
       @Query("UPDATE Product p SET p.rating = :rating, p.reviewCount = :reviewCount, p.updatedAt = :updatedAt WHERE p.id = :productId")
       int updateRatingAndReviewCount(@Param("productId") Long productId,
                                                           @Param("rating") BigDecimal rating,
                                                           @Param("reviewCount") Integer reviewCount,
                                                           @Param("updatedAt") java.time.LocalDateTime updatedAt);
    
}
