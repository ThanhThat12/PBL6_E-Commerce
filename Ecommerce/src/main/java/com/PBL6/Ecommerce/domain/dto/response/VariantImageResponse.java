package com.PBL6.Ecommerce.domain.dto.response;

import com.PBL6.Ecommerce.domain.entity.product.ProductImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for variant-specific product images.
 * Each variant image is associated with a specific attribute value (e.g., Color="Red").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantImageResponse {
    
    /**
     * Image ID
     */
    private Long id;
    
    /**
     * Attribute value that this image represents (e.g., "Red", "Blue", "Green")
     */
    private String attributeValue;
    
    /**
     * Full URL to the image
     */
    private String imageUrl;
    
    /**
     * Cloudinary public_id for deletion
     */
    private String publicId;

    /**
     * Create VariantImageResponse from ProductImage entity
     */
    public static VariantImageResponse from(ProductImage image) {
        if (image == null) {
            return null;
        }
        
        return VariantImageResponse.builder()
                .id(image.getId())
                .attributeValue(image.getVariantAttributeValue())
                .imageUrl(image.getImageUrl())
                .publicId(image.getPublicId())
                .build();
    }
}
