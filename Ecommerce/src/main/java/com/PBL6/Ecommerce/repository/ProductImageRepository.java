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
     * Find images by product and variant attribute value name (e.g., "Red", "Blue")
     */
    List<ProductImage> findByProductIdAndVariantAttributeValue(Long productId, String variantAttributeValue);

    /**
     * Find images by product and variant attribute ID (optimized query)
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.variantAttribute.id = :variantAttributeId " +
           "ORDER BY pi.displayOrder")
    List<ProductImage> findByProductIdAndVariantAttributeId(
        @Param("productId") Long productId,
        @Param("variantAttributeId") Long variantAttributeId);

    /**
     * Find images by product and variant attribute (FK to ProductVariantValue)
     */
    List<ProductImage> findByProductIdAndVariantAttributeIdOrderByDisplayOrderAsc(Long productId, Long variantAttributeId);

    /**
     * Find images by product and image type
     */
    List<ProductImage> findByProductIdAndImageTypeOrderByDisplayOrderAsc(Long productId, String imageType);

    /**
     * Find images by product with no variant (gallery only - imageType = 'GALLERY')
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.imageType = 'GALLERY' " +
           "ORDER BY pi.displayOrder")
    List<ProductImage> findGalleryImagesByProductId(@Param("productId") Long productId);

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

    /**
     * Find variant image by product, attribute ID, and attribute value (e.g., Color="Red")
     * Used for checking if variant image exists before upload/replacement
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.variantAttribute.id = :attributeId " +
           "AND pi.variantAttributeValue = :attributeValue")
    Optional<ProductImage> findByProductIdAndVariantAttributeIdAndVariantAttributeValue(
        @Param("productId") Long productId,
        @Param("attributeId") Long attributeId,
        @Param("attributeValue") String attributeValue);

    /**
     * Find all images by product and image type (GALLERY or VARIANT)
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.imageType = :imageType " +
           "ORDER BY pi.displayOrder")
    List<ProductImage> findByProductIdAndImageType(
        @Param("productId") Long productId,
        @Param("imageType") String imageType);
}