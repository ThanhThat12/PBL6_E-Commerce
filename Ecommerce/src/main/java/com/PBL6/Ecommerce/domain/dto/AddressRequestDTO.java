package com.PBL6.Ecommerce.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO to create/update user address with auto-primary logic for HOME type")

public class AddressRequestDTO {
    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    @Schema(example = "123 Đường Nguyễn Huệ, Quận 1, TP.HCM", description = "Full delivery address including street, number")
    public String fullAddress;

    @Schema(example = "202", description = "GHN Province ID (use for better accuracy)")
    public Integer provinceId;
    
    @Schema(example = "1442", description = "GHN District ID (use for better accuracy)")
    public Integer districtId;
    
    @Schema(example = "21308", description = "GHN Ward Code (use for better accuracy)")
    public String wardCode;

    @Schema(example = "Ho Chi Minh", description = "Human-readable province name (auto-resolved if ID not provided)")
    public String provinceName;
    
    @Schema(example = "District 1", description = "Human-readable district name (auto-resolved if ID not provided)")
    public String districtName;
    
    @Schema(example = "Ward 1", description = "Human-readable ward name (auto-resolved if code not provided)")
    public String wardName;

    @Size(max = 100, message = "Tên người nhận không được quá 100 ký tự")
    @Schema(example = "Nguyễn Văn A", description = "Contact person name for this address")
    public String contactName;

    @NotBlank(message = "Số điện thoại liên hệ không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    @Schema(example = "0912345678", description = "Vietnamese phone number (0xxx or +84xx format)")
    public String contactPhone;
    
    /**
     * Type of address: HOME or STORE only
     * - HOME: Địa chỉ nhận hàng của buyer (có thể có nhiều, chọn 1 primary)
     * - STORE: Địa chỉ kho/cửa hàng của seller (chỉ có 1, không có primary)
     * Default: HOME if not specified
     */
    @Pattern(regexp = "^(HOME|STORE)$", message = "Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE")
    @Schema(example = "HOME", description = "Address type: HOME (buyer delivery) or STORE (seller warehouse). Default: HOME", allowableValues = {"HOME", "STORE"})
    public String typeAddress;
    
    /**
     * Primary address flag - CHỈ áp dụng cho type_address = HOME
     * Business Logic:
     * - Buyer có thể set 1 địa chỉ HOME làm mặc định (primary=true)
     * - STORE address không dùng primary (seller chỉ có 1 STORE duy nhất)
     * - Khi set primary=true cho HOME mới, TẤT CẢ các địa chỉ HOME khác sẽ tự động unset (primary=false)
     * - GHN checkout sẽ tự động dùng địa chỉ primary=true cho buyer
     */
    @Schema(example = "true", description = "Set as primary/default address. ONLY for HOME type. Auto-unsets other HOME primary addresses.", defaultValue = "false")
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
