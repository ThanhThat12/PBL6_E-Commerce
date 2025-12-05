package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for Buyer submitting seller registration application
 * Phase 1: COD only - no MoMo fields
 */
public class SellerRegistrationRequestDTO {
    
    // ========== Shop Basic Info ==========
    @NotBlank(message = "Tên shop không được để trống")
    @Size(min = 3, max = 100, message = "Tên shop phải từ 3-100 ký tự")
    private String shopName;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotBlank(message = "Số điện thoại shop không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)")
    private String shopPhone;

    @NotBlank(message = "Email shop không được để trống")
    @Email(message = "Email không hợp lệ")
    private String shopEmail;

    // ========== Shop Address ==========
    // Option 1: Use existing address
    private Long addressId;

    // Option 2: Create new address
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String fullAddress;

    private Integer provinceId;
    private Integer districtId;
    private String wardCode;

    @Size(max = 100)
    private String provinceName;
    @Size(max = 100)
    private String districtName;
    @Size(max = 100)
    private String wardName;

    @Size(max = 30)
    private String contactPhone;
    @Size(max = 100)
    private String contactName;

    private Boolean primaryAddress;

    // ========== Shop Branding (optional) ==========
    private String logoUrl;
    private String logoPublicId;
    private String bannerUrl;
    private String bannerPublicId;

    // ========== KYC - Identity Verification ==========
    @NotBlank(message = "Số CMND/CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{9}$|^[0-9]{12}$", message = "Số CMND (9 số) hoặc CCCD (12 số) không hợp lệ")
    private String idCardNumber;

    @NotBlank(message = "Ảnh mặt trước CMND/CCCD không được để trống")
    private String idCardFrontUrl;
    private String idCardFrontPublicId;

    @NotBlank(message = "Ảnh mặt sau CMND/CCCD không được để trống")
    private String idCardBackUrl;
    private String idCardBackPublicId;

    // Optional but recommended
    private String selfieWithIdUrl;
    private String selfieWithIdPublicId;

    @NotBlank(message = "Họ tên trên CMND/CCCD không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String idCardName;

    // ========== Payment Methods - Phase 1: COD only ==========
    // acceptCod defaults to true, no need to include in request

    // Constructors
    public SellerRegistrationRequestDTO() {}

    // Getters and Setters
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

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Boolean getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(Boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoPublicId() {
        return logoPublicId;
    }

    public void setLogoPublicId(String logoPublicId) {
        this.logoPublicId = logoPublicId;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerPublicId() {
        return bannerPublicId;
    }

    public void setBannerPublicId(String bannerPublicId) {
        this.bannerPublicId = bannerPublicId;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getIdCardFrontUrl() {
        return idCardFrontUrl;
    }

    public void setIdCardFrontUrl(String idCardFrontUrl) {
        this.idCardFrontUrl = idCardFrontUrl;
    }

    public String getIdCardFrontPublicId() {
        return idCardFrontPublicId;
    }

    public void setIdCardFrontPublicId(String idCardFrontPublicId) {
        this.idCardFrontPublicId = idCardFrontPublicId;
    }

    public String getIdCardBackUrl() {
        return idCardBackUrl;
    }

    public void setIdCardBackUrl(String idCardBackUrl) {
        this.idCardBackUrl = idCardBackUrl;
    }

    public String getIdCardBackPublicId() {
        return idCardBackPublicId;
    }

    public void setIdCardBackPublicId(String idCardBackPublicId) {
        this.idCardBackPublicId = idCardBackPublicId;
    }

    public String getSelfieWithIdUrl() {
        return selfieWithIdUrl;
    }

    public void setSelfieWithIdUrl(String selfieWithIdUrl) {
        this.selfieWithIdUrl = selfieWithIdUrl;
    }

    public String getSelfieWithIdPublicId() {
        return selfieWithIdPublicId;
    }

    public void setSelfieWithIdPublicId(String selfieWithIdPublicId) {
        this.selfieWithIdPublicId = selfieWithIdPublicId;
    }

    public String getIdCardName() {
        return idCardName;
    }

    public void setIdCardName(String idCardName) {
        this.idCardName = idCardName;
    }
}
