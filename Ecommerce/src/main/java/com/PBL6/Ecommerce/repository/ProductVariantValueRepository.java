package com.PBL6.Ecommerce.repository;

import com.PBL6.Ecommerce.domain.ProductVariantValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantValueRepository extends JpaRepository<ProductVariantValue, Long> {
    
    // Tìm variant values theo variant ID
    List<ProductVariantValue> findByVariantId(Long variantId);
    
    // Tìm variant values theo product attribute ID
    List<ProductVariantValue> findByProductAttributeId(Long productAttributeId);
    
    // Tìm variant values theo variant và attribute
    @Query("SELECT pvv FROM ProductVariantValue pvv WHERE pvv.variant.id = :variantId AND pvv.productAttribute.id = :attributeId")
    List<ProductVariantValue> findByVariantIdAndProductAttributeId(@Param("variantId") Long variantId, @Param("attributeId") Long attributeId);
    
    // Xóa variant values theo variant ID
    void deleteByVariantId(Long variantId);
    
    // Đếm số variant values theo attribute
    long countByProductAttributeId(Long productAttributeId);
}