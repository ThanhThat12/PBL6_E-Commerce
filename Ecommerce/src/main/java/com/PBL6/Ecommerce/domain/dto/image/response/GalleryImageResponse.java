package com.PBL6.Ecommerce.domain.dto.image.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gallery Image Response DTO
 * Represents a single gallery image with its metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageResponse {
    
    private Long id;
    private String url;
    private String publicId;
    private Integer displayOrder;
    private String variantAttributeValue; // Optional: variant-specific images
}


