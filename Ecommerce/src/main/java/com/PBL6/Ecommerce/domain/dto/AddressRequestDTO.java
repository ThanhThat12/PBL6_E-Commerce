package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressRequestDTO {
    @NotBlank(message = "Label không được để trống")
    @Size(max = 100, message = "Label không được vượt quá 100 ký tự")
    public String label;
    
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
    
    public boolean primaryAddress; // true nếu muốn đánh dấu primary
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
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
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() {
        return contactPhone;
    }
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    public boolean isPrimaryAddress() {
        return primaryAddress;
    }
    public void setPrimaryAddress(boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }
    
}
