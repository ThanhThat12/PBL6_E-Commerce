package com.PBL6.Ecommerce.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image Transformation Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageTransformationResponse {
    
    private String thumbnail;
    private String medium;
    private String large;
    private String original;
}
