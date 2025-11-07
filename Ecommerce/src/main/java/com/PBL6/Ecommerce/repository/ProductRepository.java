package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.Product.ProductStatus;
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
    
    
    // ✅ Tìm sản phẩm theo trạng thái
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    // ✅ Tìm sản phẩm đang hoạt động (ACTIVE)
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    Page<Product> findActiveProducts(Pageable pageable);

    // ✅ Tìm sản phẩm chờ duyệt (PENDING)
    @Query("SELECT p FROM Product p WHERE p.status = 'PENDING'")
    Page<Product> findPendingProducts(Pageable pageable);
    
    // ✅ Đếm sản phẩm chờ duyệt
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'PENDING'")
    long countPendingProducts();
    
    // ✅ Đếm sản phẩm theo trạng thái
    long countByStatus(ProductStatus status);

    
    
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
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> findProductsWithFilters(@Param("name") String name,
                                        @Param("categoryId") Long categoryId,
                                        @Param("shopId") Long shopId,
                                        @Param("status") ProductStatus status,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice,
                                        Pageable pageable);
    
    // Đếm số sản phẩm theo shop
    long countByShopId(Long shopId);
    
    // ✅ Tìm theo category và trạng thái ACTIVE
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    Page<Product> findByCategoryIdAndActiveStatus(@Param("categoryId") Long categoryId, Pageable pageable);

    // ✅ Tìm sản phẩm của seller theo trạng thái
    @Query("SELECT p FROM Product p WHERE p.shop.owner.id = :sellerId AND p.status = :status")
    Page<Product> findBySellerIdAndStatus(@Param("sellerId") Long sellerId, 
                                          @Param("status") ProductStatus status, 
                                          Pageable pageable);
    
    // ✅ Đếm sản phẩm của seller theo trạng thái
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.owner.id = :sellerId AND p.status = :status")
    long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") ProductStatus status);

    // ✅ Tìm sản phẩm theo shop ID và trạng thái
    Page<Product> findByShopIdAndStatus(Long shopId, ProductStatus status, Pageable pageable);
    List<Product> findByShopIdAndStatus(Long shopId, ProductStatus status);
    
}