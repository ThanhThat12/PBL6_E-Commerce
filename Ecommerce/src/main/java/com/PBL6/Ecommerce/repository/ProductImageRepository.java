package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    /**
     * Find all images for a product, ordered by display_order
     */
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);



    /**
     * Find images by product and variant value name
     */
    List<ProductImage> findByProductIdAndVariantValueName(Long productId, String variantValueName);

    /**
     * Find images by product and variant value ID (optimized query)
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.variantValue.id = :variantValueId " +
           "ORDER BY pi.displayOrder")
    List<ProductImage> findByProductIdAndVariantValueId(
        @Param("productId") Long productId,
        @Param("variantValueId") Long variantValueId);

    /**
     * Find images by product and variant
     */
    List<ProductImage> findByProductIdAndVariantIdOrderByDisplayOrderAsc(Long productId, Long variantId);

    /**
     * Find images by product with no variant (main gallery)
     */
    List<ProductImage> findByProductIdAndVariantIdIsNullOrderByDisplayOrderAsc(Long productId);

    /**
     * Find image by public_id
     */
    Optional<ProductImage> findByPublicId(String publicId);

    /**
     * Delete all images for a product
     */
    void deleteByProductId(Long productId);

    /**
     * Delete image by public_id
     */
    void deleteByPublicId(String publicId);

    /**
     * Count images for a product
     */
    Long countByProductId(Long productId);

    /**
     * Find max display_order for a product
     */
    @Query("SELECT MAX(pi.displayOrder) FROM ProductImage pi WHERE pi.product.id = :productId")
    Integer findMaxDisplayOrderByProductId(@Param("productId") Long productId);
}