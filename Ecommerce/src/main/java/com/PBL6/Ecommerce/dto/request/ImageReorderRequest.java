package com.PBL6.Ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Image Reorder Request DTO
 */
@Data
public class ImageReorderRequest {
    
    @NotNull(message = "Image IDs are required")
    private List<Long> imageIds;
}
