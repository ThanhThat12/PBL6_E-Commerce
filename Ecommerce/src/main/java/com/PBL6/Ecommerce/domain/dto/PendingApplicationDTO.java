package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

import com.PBL6.Ecommerce.domain.Shop.ShopStatus;

/**
 * DTO for Admin to view pending seller applications
 * Includes full KYC data for verification
 */
public class PendingApplicationDTO {
    
    private Long shopId;
    
    // ========== Shop Info ==========
    private String shopName;
    private String description;
    private String shopPhone;
    private String shopEmail;
    private String logoUrl;
    
    // ========== Address ==========
    private String fullAddress;
    private String provinceName;
    private String districtName;
    private String wardName;
    
    // ========== Owner Info ==========
    private Long ownerId;
    private String ownerUsername;
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerAvatar;
    private LocalDateTime ownerCreatedAt; // Account age
    
    // ========== KYC - Full data for admin verification ==========
    private String idCardNumber;
    private String idCardName;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String selfieWithIdUrl;
    
    // ========== Application Status ==========
    private ShopStatus status;
    private LocalDateTime submittedAt;
    
    // ========== Payment Methods ==========
    private Boolean acceptCod;
    
    // Constructors
    public PendingApplicationDTO() {}

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerFullName() {
        return ownerFullName;
    }

    public void setOwnerFullName(String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getOwnerAvatar() {
        return ownerAvatar;
    }

    public void setOwnerAvatar(String ownerAvatar) {
        this.ownerAvatar = ownerAvatar;
    }

    public LocalDateTime getOwnerCreatedAt() {
        return ownerCreatedAt;
    }

    public void setOwnerCreatedAt(LocalDateTime ownerCreatedAt) {
        this.ownerCreatedAt = ownerCreatedAt;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getIdCardName() {
        return idCardName;
    }

    public void setIdCardName(String idCardName) {
        this.idCardName = idCardName;
    }

    public String getIdCardFrontUrl() {
        return idCardFrontUrl;
    }

    public void setIdCardFrontUrl(String idCardFrontUrl) {
        this.idCardFrontUrl = idCardFrontUrl;
    }

    public String getIdCardBackUrl() {
        return idCardBackUrl;
    }

    public void setIdCardBackUrl(String idCardBackUrl) {
        this.idCardBackUrl = idCardBackUrl;
    }

    public String getSelfieWithIdUrl() {
        return selfieWithIdUrl;
    }

    public void setSelfieWithIdUrl(String selfieWithIdUrl) {
        this.selfieWithIdUrl = selfieWithIdUrl;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public void setStatus(ShopStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Boolean getAcceptCod() {
        return acceptCod;
    }

    public void setAcceptCod(Boolean acceptCod) {
        this.acceptCod = acceptCod;
    }
}
