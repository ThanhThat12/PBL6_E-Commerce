package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full Shop DTO with all information for seller dashboard
 * Includes: basic info, contact, address, GHN credentials, KYC status, rating, owner info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDetailDTO {
    
    // ========== Basic Info ==========
    private Long shopId;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    
    // ========== Shop Contact ==========
    private String shopPhone;
    private String shopEmail;
    
    // ========== Shop Branding ==========
    private String logoUrl;
    private String logoPublicId;
    private String bannerUrl;
    private String bannerPublicId;
    
    // ========== Address Info (from Address entity with TypeAddress.STORE) ==========
    private Long addressId;
    private String fullAddress;
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String contactPhone;  // Pickup contact phone
    private String contactName;   // Pickup contact name
    
    // ========== GHN Credentials ==========
    private String ghnShopId;
    private String ghnToken;
    private boolean ghnConfigured;  // Convenience flag: true if both ghnShopId and ghnToken are set
    
    // ========== KYC Status (masked for seller view) ==========
    private String maskedIdCardNumber;  // e.g., "****5678" 
    private String idCardName;
    private boolean kycVerified;  // true if shop is ACTIVE (approved)
    
    // KYC images - only show for PENDING/REJECTED status (seller reviewing their own submission)
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieWithIdUrl;
    
    // ========== Payment Methods ==========
    private Boolean acceptCod;
    private BigDecimal codFeePercentage;
    
    // ========== Rating ==========
    private BigDecimal rating;
    private Integer reviewCount;
    
    // ========== Owner Info ==========
    private Long ownerId;
    private String ownerUsername;
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
    private LocalDateTime ownerCreatedAt;
    
    // ========== Review/Approval Tracking ==========
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String rejectionReason;
    
    /**
     * Mask ID card number for display
     * Input: "123456789012" -> "********9012"
     */
    public static String maskIdCardNumber(String idCardNumber) {
        if (idCardNumber == null || idCardNumber.length() < 4) {
            return "****";
        }
        int visibleLength = 4;
        String masked = "*".repeat(idCardNumber.length() - visibleLength);
        return masked + idCardNumber.substring(idCardNumber.length() - visibleLength);
    }
}
