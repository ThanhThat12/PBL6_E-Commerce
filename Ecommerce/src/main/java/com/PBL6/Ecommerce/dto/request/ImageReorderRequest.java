package com.PBL6.Ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for reordering gallery images
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageReorderRequest {
    
    @NotNull(message = "Image orders are required")
    private List<ImageOrderItem> imageOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageOrderItem {
        @NotNull(message = "Image ID is required")
        private Long imageId;
        
        @NotNull(message = "Display order is required")
        private Integer displayOrder;
    }
}
