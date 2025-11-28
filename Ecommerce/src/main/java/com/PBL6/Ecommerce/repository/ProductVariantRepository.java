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
    
    /**
     * Find all variants for a product that have a specific Group 1 value name
     * Used for batch variant image upload where images are associated by Group 1 value (e.g., "Red", "Blue")
     * 
     * @param productId The product ID
     * @param valueName The Group 1 variant value name (e.g., "Red")
     * @param attributeId The Group 1 attribute ID (e.g., Color attribute ID)
     * @return List of ProductVariants matching the Group 1 value
     */
    @Query("SELECT DISTINCT pv FROM ProductVariant pv " +
           "JOIN pv.productVariantValues pvv " +
           "WHERE pv.product.id = :productId " +
           "AND pvv.productAttribute.id = :attributeId " +
           "AND pvv.value = :valueName")
    List<ProductVariant> findByProductIdAndAttributeValue(
            @Param("productId") Long productId,
            @Param("attributeId") Long attributeId,
            @Param("valueName") String valueName);
}