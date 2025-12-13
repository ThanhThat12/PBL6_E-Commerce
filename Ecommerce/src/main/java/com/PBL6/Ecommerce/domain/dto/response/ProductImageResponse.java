package com.PBL6.Ecommerce.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Image Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    
    private Long id;
    private String url;
    private String publicId;
    private Integer displayOrder;
    private Long variantId;
    private String variantValueName;
    private ImageTransformationResponse transformations;
}
