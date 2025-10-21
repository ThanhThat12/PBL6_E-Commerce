package com.PBL6.Ecommerce.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.PBL6.Ecommerce.domain.Product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm theo tên (có thể tìm kiếm một phần)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Tìm theo category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    // Tìm theo category (không phân trang)
    List<Product> findByCategoryId(Long categoryId);
    
    // Tìm theo shop
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    
    // Tìm theo shop (không phân trang)
    List<Product> findByShopId(Long shopId);
    
    // Tìm sản phẩm đang hoạt động
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
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
    
    // Đếm số sản phẩm theo category
    long countByCategoryId(Long categoryId);
    
    // Đếm số sản phẩm theo shop
    long countByShopId(Long shopId);
    
    // Tìm theo category và trạng thái active
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
}