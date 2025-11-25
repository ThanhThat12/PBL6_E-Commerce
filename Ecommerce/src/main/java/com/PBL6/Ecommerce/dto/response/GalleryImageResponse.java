package com.PBL6.Ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for gallery image information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageResponse {
    private Long imageId;
    private String imageUrl;
    private String publicId;
    private Integer displayOrder;
}
