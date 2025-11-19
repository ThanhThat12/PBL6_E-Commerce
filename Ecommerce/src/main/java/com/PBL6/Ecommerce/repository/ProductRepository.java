package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO;
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
    
    // Tìm kiếm phức tạp
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:shopId IS NULL OR p.shop.id = :shopId) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> findProductsWithFilters(@Param("name") String name,
                                        @Param("categoryId") Long categoryId,
                                        @Param("shopId") Long shopId,
                                        @Param("isActive") Boolean isActive,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        Pageable pageable);
    
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

    // 🆕 Admin: Lấy danh sách sản phẩm với thông tin tổng hợp (phân trang)
    @Query(value = "SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO(" +
           "p.id, " +
           "p.name, " +
           "p.mainImage, " +
           "c.name, " +
           "p.basePrice, " +
           "COALESCE(SUM(pv.stock), 0L), " +
           "p.isActive, " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0L), " +
           "COALESCE(AVG(pr.rating), 0.0)) " +
           "FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.productVariants pv " +
           "LEFT JOIN OrderItem oi ON oi.variant = pv " +
           "LEFT JOIN oi.order o " +
           "LEFT JOIN ProductReview pr ON pr.product = p " +
           "GROUP BY p.id, p.name, p.mainImage, c.name, p.basePrice, p.isActive",
           countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p")
    Page<AdminListProductDTO> findAllProductsForAdmin(Pageable pageable);

    // 🆕 Admin: Lấy danh sách sản phẩm theo category
    @Query(value = "SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO(" +
           "p.id, " +
           "p.name, " +
           "p.mainImage, " +
           "c.name, " +
           "p.basePrice, " +
           "COALESCE(SUM(pv.stock), 0L), " +
           "p.isActive, " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0L), " +
           "COALESCE(AVG(pr.rating), 0.0)) " +
           "FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.productVariants pv " +
           "LEFT JOIN OrderItem oi ON oi.variant = pv " +
           "LEFT JOIN oi.order o " +
           "LEFT JOIN ProductReview pr ON pr.product = p " +
           "WHERE c.name = :categoryName " +
           "GROUP BY p.id, p.name, p.mainImage, c.name, p.basePrice, p.isActive",
           countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p LEFT JOIN p.category c WHERE c.name = :categoryName")
    Page<AdminListProductDTO> findProductsByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    // 🆕 Admin: Lấy danh sách sản phẩm theo status
    @Query(value = "SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO(" +
           "p.id, " +
           "p.name, " +
           "p.mainImage, " +
           "c.name, " +
           "p.basePrice, " +
           "COALESCE(SUM(pv.stock), 0L), " +
           "p.isActive, " +
           "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0L), " +
           "COALESCE(AVG(pr.rating), 0.0)) " +
           "FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.productVariants pv " +
           "LEFT JOIN OrderItem oi ON oi.variant = pv " +
           "LEFT JOIN oi.order o " +
           "LEFT JOIN ProductReview pr ON pr.product = p " +
           "WHERE p.isActive = :isActive " +
           "GROUP BY p.id, p.name, p.mainImage, c.name, p.basePrice, p.isActive",
           countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p WHERE p.isActive = :isActive")
    Page<AdminListProductDTO> findProductsByStatus(@Param("isActive") Boolean isActive, Pageable pageable);

//search with name
       @Query(value = "SELECT new com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO(" +
              "p.id, " +
              "p.name, " +
              "p.mainImage, " +
              "c.name, " +
              "p.basePrice, " +
              "COALESCE(SUM(pv.stock), 0L), " +
              "p.isActive, " +
              "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0L), " +
              "COALESCE(AVG(pr.rating), 0.0)) " +
              "FROM Product p " +
              "LEFT JOIN p.category c " +
              "LEFT JOIN p.productVariants pv " +
              "LEFT JOIN OrderItem oi ON oi.variant = pv " +
              "LEFT JOIN oi.order o " +
              "LEFT JOIN ProductReview pr ON pr.product = p " +
              "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
              "GROUP BY p.id, p.name, p.mainImage, c.name, p.basePrice, p.isActive",
              countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p " +
                     "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
       Page<AdminListProductDTO> findAllProductsForAdminWithSearch(
       @Param("name") String name, Pageable pageable);

    // 🆕 Admin Stats: Đếm tổng số sản phẩm
    @Query("SELECT COUNT(p) FROM Product p")
    Long countTotalProducts();

    // 🆕 Admin Stats: Đếm sản phẩm Active
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    Long countActiveProducts();

    // 🆕 Admin Stats: Đếm sản phẩm Pending
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = false")
    Long countPendingProducts();

    // 🆕 Admin Stats: Tổng sản phẩm đã bán (COMPLETED orders)
    @Query("SELECT COALESCE(SUM(oi.quantity), 0L) FROM OrderItem oi " +
           "LEFT JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED'")
    Long countTotalProductsSold();
    
}