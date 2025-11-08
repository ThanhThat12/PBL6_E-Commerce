package com.PBL6.Ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PBL6.Ecommerce.domain.VariantImage;

@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, Long> {

    // Get all images for a variant, ordered by display_order
    List<VariantImage> findByVariantIdOrderByDisplayOrder(Long variantId);

    // Get main image (displayOrder = 0) for variant
    Optional<VariantImage> findByVariantIdAndDisplayOrder(Long variantId, Integer displayOrder);

    // Get only active images for variant
    List<VariantImage> findByVariantIdAndIsActiveTrue(Long variantId);

    // Delete all images for a variant
    void deleteByVariantId(Long variantId);

    // Check if image belongs to variant
    boolean existsByVariantIdAndId(Long variantId, Long imageId);
}
