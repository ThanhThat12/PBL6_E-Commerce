package com.PBL6.Ecommerce.domain.dto.response;

import com.PBL6.Ecommerce.constant.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Image Upload Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    
    private Long id;
    private String url;
    private String publicId;
    private Integer width;
    private Integer height;
    private Long size;
    private Map<TransformationType, String> transformations;
    private String message;
}
