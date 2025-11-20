package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShopRegistrationDTO {
    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 255, message = "Tên shop không được vượt quá 255 ký tự")
    private String name;

    // nếu chọn address đã có sẵn
    private Long addressId;

    // nếu thêm địa chỉ mới — các trường tương ứng với Address entity
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

    // nếu true -> đánh dấu primaryAddress
    private Boolean primaryAddress;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    // Constructors
    public ShopRegistrationDTO() {}

    public ShopRegistrationDTO(String name, String fullAddress, String description) {
        this.name = name;
        this.fullAddress = fullAddress;
        this.description = description;
    }

    // Getters / Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }

    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public Boolean getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Boolean primaryAddress) { this.primaryAddress = primaryAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
