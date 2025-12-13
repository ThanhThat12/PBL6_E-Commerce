package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.entity.product.ProductVariantValue;

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

    /**
     * Check if a specific attribute value exists for a product.
     * Used to validate variant image upload requests.
     * 
     * @param productId The product ID
     * @param attributeId The attribute ID (e.g., Color attribute)
     * @param value The attribute value (e.g., "Red", "Blue")
     * @return true if the value exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(pvv) > 0 THEN true ELSE false END " +
           "FROM ProductVariantValue pvv " +
           "JOIN pvv.variant v " +
           "WHERE v.product.id = :productId " +
           "AND pvv.productAttribute.id = :attributeId " +
           "AND pvv.value = :value")
    boolean existsByProductIdAndAttributeIdAndValue(
        @Param("productId") Long productId,
        @Param("attributeId") Long attributeId,
        @Param("value") String value);

    /**
     * Find all distinct values for a specific attribute in a product.
     * Used to build the list of available variant values for image upload.
     * 
     * @param productId The product ID
     * @param attributeId The attribute ID
     * @return List of distinct attribute values
     */
    @Query("SELECT DISTINCT pvv.value " +
           "FROM ProductVariantValue pvv " +
           "JOIN pvv.variant v " +
           "WHERE v.product.id = :productId " +
           "AND pvv.productAttribute.id = :attributeId " +
           "ORDER BY pvv.value")
    List<String> findDistinctValuesByProductIdAndAttributeId(
        @Param("productId") Long productId,
        @Param("attributeId") Long attributeId);

    /**
     * Get all primary attribute values for a product.
     * This is the equivalent of the SQL query you provided:
     * SELECT DISTINCT pvv.value AS primary_value
     * FROM product_variant_values pvv
     * JOIN product_variants v ON v.id = pvv.variant_id
     * JOIN product_primary_attributes ppa ON ppa.attribute_id = pvv.product_attribute_id
     * WHERE v.product_id = :productId
     */
    @Query("SELECT DISTINCT pvv.value " +
           "FROM ProductVariantValue pvv " +
           "JOIN pvv.variant v " +
           "JOIN ProductPrimaryAttribute ppa ON ppa.attribute.id = pvv.productAttribute.id " +
           "WHERE v.product.id = :productId " +
           "AND ppa.product.id = :productId " +
           "ORDER BY pvv.value")
    List<String> findPrimaryAttributeValuesByProductId(@Param("productId") Long productId);

    /**
     * Check if a primary attribute value exists for a product.
     * More accurate than the general existsByProductIdAndAttributeIdAndValue method.
     */
    @Query("SELECT CASE WHEN COUNT(pvv) > 0 THEN true ELSE false END " +
           "FROM ProductVariantValue pvv " +
           "JOIN pvv.variant v " +
           "JOIN ProductPrimaryAttribute ppa ON ppa.attribute.id = pvv.productAttribute.id " +
           "WHERE v.product.id = :productId " +
           "AND ppa.product.id = :productId " +
           "AND pvv.value = :value")
    boolean existsPrimaryAttributeValueByProductIdAndValue(
        @Param("productId") Long productId,
        @Param("value") String value);
}