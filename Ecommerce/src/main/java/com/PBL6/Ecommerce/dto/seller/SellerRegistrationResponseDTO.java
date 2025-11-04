package com.PBL6.Ecommerce.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Seller Registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegistrationResponseDTO {
    
    private Long shopId;
    private String shopName;
    private String message;
    private boolean autoApproved;
    
    public static SellerRegistrationResponseDTO success(Long shopId, String shopName) {
        return SellerRegistrationResponseDTO.builder()
            .shopId(shopId)
            .shopName(shopName)
            .message("Đăng ký seller thành công!")
            .autoApproved(true)
            .build();
    }
}
