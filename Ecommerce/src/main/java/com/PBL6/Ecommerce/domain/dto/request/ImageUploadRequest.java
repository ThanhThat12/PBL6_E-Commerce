package com.PBL6.Ecommerce.domain.dto.request;

import com.PBL6.Ecommerce.validator.ValidImageFile;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image Upload Request DTO
 */
@Data
public class ImageUploadRequest {
    
    @ValidImageFile(message = "Invalid image file")
    private MultipartFile file;
    
    /**
     * Optional: Variant ID for product variant-specific images
     */
    private Long variantId;
    
    /**
     * Optional: Color tag for images
     */
    private String color;
}
