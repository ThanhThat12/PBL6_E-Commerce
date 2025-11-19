package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.ProductVariantValue;

@Repository
public interface ProductVariantValueRepository extends JpaRepository<ProductVariantValue, Long> {
    
    // Tìm variant values theo variant ID
    List<ProductVariantValue> findByVariantId(Long variantId);
    
    // Tìm variant values theo product attribute ID
    List<ProductVariantValue> findByProductAttributeId(Long productAttributeId);
    
    // Tìm variant values theo variant và attribute
    @Query("SELECT pvv FROM ProductVariantValue pvv WHERE pvv.variant.id = :variantId AND pvv.productAttribute.id = :attributeId")
    List<ProductVariantValue> findByVariantIdAndProductAttributeId(@Param("variantId") Long variantId, @Param("attributeId") Long attributeId);
    
    /**
     * Find a variant value by product ID and value name.
     * This is used to resolve Group 1 variant value names (e.g., "Red", "Small", "Cotton")
     * to their corresponding ProductVariantValue entities for batch variant image upload.
     * 
     * @param productId The product ID
     * @param valueName The variant value name (case-insensitive)
     * @return Optional containing the variant value if found
     */
    @Query("SELECT pvv FROM ProductVariantValue pvv " +
           "JOIN pvv.variant v " +
           "WHERE v.product.id = :productId " +
           "AND LOWER(pvv.value) = LOWER(:valueName)")
    Optional<ProductVariantValue> findByProductIdAndValueName(
        @Param("productId") Long productId,
        @Param("valueName") String valueName);
    
    // Xóa variant values theo variant ID
    void deleteByVariantId(Long variantId);
    
    // Đếm số variant values theo attribute
    long countByProductAttributeId(Long productAttributeId);
}