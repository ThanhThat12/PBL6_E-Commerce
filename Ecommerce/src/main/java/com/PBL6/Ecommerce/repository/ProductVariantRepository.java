package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    Optional<ProductVariant> findBySku(String sku);
    boolean existsBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.stock > 0")
    List<ProductVariant> findInStockVariantsByProductId(@Param("productId") Long productId);
    
    // Tính tổng stock cho một product
    @Query("SELECT COALESCE(SUM(pv.stock), 0) FROM ProductVariant pv WHERE pv.product.id = :productId")
    Long getTotalStockByProductId(@Param("productId") Long productId);
}