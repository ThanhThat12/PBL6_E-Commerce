package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để hiển thị thông tin shop PUBLIC
 * Dùng cho: Guest xem shop, Buyer xem shop của seller, Product listing
 * 
 * KHÔNG chứa: KYC info, GHN credentials, owner details, address IDs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO {
    // ========== Basic Info ==========
    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    
    // ========== Branding (public) ==========
    private String logoUrl;
    private String bannerUrl;
    
    // ========== Address (text only, no IDs for privacy) ==========
    private String address;           // Full address string
    private String provinceName;
    private String districtName;
    private String wardName;
    
    // ========== Rating (public) ==========
    private BigDecimal rating;
    private Integer reviewCount;
    
    // ========== Contact (public for buyers to contact) ==========
    private String shopPhone;
    private String shopEmail;
    private Long ownerId;
}
