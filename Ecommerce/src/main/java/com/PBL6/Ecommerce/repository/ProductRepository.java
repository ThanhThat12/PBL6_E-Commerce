package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // ✅ Tìm products theo categoryId
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findByCategoryIdAndShopId(Long categoryId, Long shopId);

    
    // ✅ Tìm products theo shopId
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    
    // ✅ Tìm products theo userId (seller) - SỬA p.shop.user.id → p.shop.owner.id
    @Query("SELECT p FROM Product p WHERE p.shop.owner.id = :userId")
    List<Product> findByUserId(@Param("userId") Long userId);
    
    // ✅ Xóa products theo userId (seller) - SỬA p.shop.user.id → p.shop.owner.id
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
    List<Product> findByShop(@Param("shop") com.PBL6.Ecommerce.domain.Shop shop);

    // Tìm sản phẩm đang hoạt động
    Page<Product> findByIsActiveTrue(Pageable pageable);

    // 🆕 Tìm sản phẩm chờ duyệt (is_active = false)
    Page<Product> findByIsActiveFalse(Pageable pageable);
    
    // 🆕 Đếm sản phẩm chờ duyệt
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
     * Count products by shop name matching query
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.shop.id = :shopId")
    long countActiveByShopId(@Param("shopId") Long shopId);
    
    // Đếm số sản phẩm theo shop
    long countByShopId(Long shopId);
    
    // Tìm theo category và trạng thái active
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

     // 🆕 Tìm sản phẩm của seller theo trạng thái
    @Query("SELECT p FROM Product p WHERE p.shop.owner.id = :sellerId AND p.isActive = :isActive")
    Page<Product> findBySellerIdAndIsActive(@Param("sellerId") Long sellerId, 
                                          @Param("isActive") Boolean isActive, 
                                          Pageable pageable);
    
    // 🆕 Đếm sản phẩm của seller theo trạng thái
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
    
}
