package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

import com.PBL6.Ecommerce.domain.entity.shop.Shop.ShopStatus;

/**
 * DTO for returning registration status to the applicant (Buyer)
 * Sensitive data (KYC images) are NOT included for security
 */
public class RegistrationStatusDTO {
    
    private Long shopId;
    private String shopName;
    private String description;
    private String shopPhone;
    private String shopEmail;
    
    private ShopStatus status;
    private String statusDescription;
    
    // Masked ID card number (e.g., "***456789")
    private String maskedIdCardNumber;
    private String idCardName;
    
    // Address summary
    private String fullAddress;
    
    // Logo
    private String logoUrl;
    
    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    
    // Rejection reason (only if status = REJECTED)
    private String rejectionReason;
    
    // Payment methods
    private Boolean acceptCod;
    
    // Constructors
    public RegistrationStatusDTO() {}

    // Static factory method to mask sensitive data
    public static String maskIdCardNumber(String idCardNumber) {
        if (idCardNumber == null || idCardNumber.length() < 4) {
            return "***";
        }
        int visibleLength = 4;
        int maskedLength = idCardNumber.length() - visibleLength;
        return "*".repeat(maskedLength) + idCardNumber.substring(maskedLength);
    }

    // Getters and Setters
    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public String getShopEmail() {
        return shopEmail;
    }

    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public void setStatus(ShopStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(ShopStatus status) {
        switch (status) {
            case PENDING:
                this.statusDescription = "Đang chờ duyệt";
                break;
            case ACTIVE:
                this.statusDescription = "Đã được duyệt - Shop đang hoạt động";
                break;
            case REJECTED:
                this.statusDescription = "Bị từ chối";
                break;
            case SUSPENDED:
                this.statusDescription = "Đã bị tạm ngưng";
                break;
            case CLOSED:
                this.statusDescription = "Đã đóng cửa";
                break;
            default:
                this.statusDescription = "Không xác định";
        }
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getMaskedIdCardNumber() {
        return maskedIdCardNumber;
    }

    public void setMaskedIdCardNumber(String maskedIdCardNumber) {
        this.maskedIdCardNumber = maskedIdCardNumber;
    }

    public String getIdCardName() {
        return idCardName;
    }

    public void setIdCardName(String idCardName) {
        this.idCardName = idCardName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Boolean getAcceptCod() {
        return acceptCod;
    }

    public void setAcceptCod(Boolean acceptCod) {
        this.acceptCod = acceptCod;
    }
}
