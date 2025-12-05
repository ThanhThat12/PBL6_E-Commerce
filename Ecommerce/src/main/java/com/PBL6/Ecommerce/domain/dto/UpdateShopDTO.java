package com.PBL6.Ecommerce.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật thông tin shop
 * Supports updating: name, description, status, contact, branding, address, GHN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShopDTO {
    // Basic info
    private String name;
    private String description;
    private String status;
    
    // Contact info
    private String shopPhone;
    private String shopEmail;
    
    // Branding - logo and banner
    private String logoUrl;
    private String logoPublicId;
    private String bannerUrl;
    private String bannerPublicId;
    
    // Address - Option 1: Use existing address ID
    private Long pickupAddressId;
    
    // Address - Option 2: Create/update full address
    private String fullAddress;
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String contactPhone;
    private String contactName;
    
    // GHN credentials
    private String ghnShopId;
    private String ghnToken;
}
