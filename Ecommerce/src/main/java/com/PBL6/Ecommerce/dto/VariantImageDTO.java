package com.PBL6.Ecommerce.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Variant Image
 * Used for transferring variant image data between layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantImageDTO {
    private Long id;
    private Long productId;
    private Long variantId;
    private String imageUrl;
    private String color;
    private Integer displayOrder;
    private String altText;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
