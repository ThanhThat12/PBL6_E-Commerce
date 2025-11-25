package com.PBL6.Ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image Delete Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDeleteResponse {
    
    private boolean success;
    private String message;
    private String deletedPublicId;
}
