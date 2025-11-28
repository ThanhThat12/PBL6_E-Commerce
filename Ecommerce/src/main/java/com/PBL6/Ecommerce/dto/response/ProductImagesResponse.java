package com.PBL6.Ecommerce.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comprehensive response DTO containing all product images.
 * Includes main image, gallery images, primary attribute info, and variant-specific images.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImagesResponse {
    
    /**
     * Main product image URL (displayed in product card and listings)
     */
    private String mainImage;
    
    /**
     * List of gallery images (ordered by display_order)
     */
    private List<GalleryImageResponse> galleryImages;
    
    /**
     * Primary attribute information (e.g., Color attribute with values ["Red", "Blue"])
     * Null if product has no primary attribute
     */
    private PrimaryAttributeDTO primaryAttribute;
    
    /**
     * Map of variant-specific images keyed by attribute value
     * Example: {"Red": {...}, "Blue": {...}}
     * Empty map if product has no variant images or no primary attribute
     */
    private Map<String, VariantImageResponse> variantImages;
}
