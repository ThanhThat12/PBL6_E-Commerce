package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressRequestDTO {
    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    public String fullAddress;

    // prefer sending these ids/codes if available
    public Integer provinceId;
    public Integer districtId;
    public String wardCode;

    // optional: user can send human-readable names instead, service will resolve via GHN
    public String provinceName;
    public String districtName;
    public String wardName;

    // optional recipient name to display with this address
    @Size(max = 100, message = "Tên người nhận không được quá 100 ký tự")
    public String contactName;

    @NotBlank(message = "Số điện thoại liên hệ không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    public String contactPhone;
    
    /**
     * Type of address: HOME or STORE only
     * - HOME: Địa chỉ nhận hàng của buyer (có thể có nhiều, chọn 1 primary)
     * - STORE: Địa chỉ kho/cửa hàng của seller (chỉ có 1, không có primary)
     * Default: HOME if not specified
     */
    @Pattern(regexp = "^(HOME|STORE)$", message = "Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE")
    public String typeAddress;
    
    /**
     * Primary address flag - CHỈ áp dụng cho type_address = HOME
     * - Buyer có thể set 1 địa chỉ HOME làm mặc định
     * - STORE address không dùng primary (seller chỉ có 1 STORE)
     * - Khi set primary=true, các địa chỉ HOME khác sẽ tự động unset
     */
    public boolean primaryAddress;
    
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
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() {
        return contactPhone;
    }
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    public String getTypeAddress() {
        return typeAddress;
    }
    public void setTypeAddress(String typeAddress) {
        this.typeAddress = typeAddress;
    }
    public boolean isPrimaryAddress() {
        return primaryAddress;
    }
    public void setPrimaryAddress(boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }
}
