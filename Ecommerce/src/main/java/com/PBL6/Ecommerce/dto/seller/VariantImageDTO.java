package com.PBL6.Ecommerce.dto.seller;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Variant Image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantImageDTO {
    private Long id;
    private Long variantId;
    private String imageUrl;
    private Integer displayOrder;
    private String altText;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
