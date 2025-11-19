package com.PBL6.Ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Batch Image Upload Request DTO
 */
@Data
public class BatchImageUploadRequest {
    
    @NotNull(message = "Files are required")
    @Size(min = 1, max = 10, message = "Must upload between 1 and 10 images")
    private List<MultipartFile> files;
    
    /**
     * Optional: Variant ID for variant-specific gallery
     */
    private Long variantId;
}
