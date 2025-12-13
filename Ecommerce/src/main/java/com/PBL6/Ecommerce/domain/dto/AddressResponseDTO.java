package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

import com.PBL6.Ecommerce.constant.TypeAddress;

public class AddressResponseDTO {
    private Long id;
    private String fullAddress;
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    
    // Human-readable names from GHN
    private String provinceName;
    private String districtName;
    private String wardName;
    
    private String contactName;
    private String contactPhone;
    private TypeAddress typeAddress;
    private Boolean primaryAddress;
    private LocalDateTime createdAt;

    public AddressResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public TypeAddress getTypeAddress() { return typeAddress; }
    public void setTypeAddress(TypeAddress typeAddress) { this.typeAddress = typeAddress; }

    public Boolean getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Boolean primaryAddress) { this.primaryAddress = primaryAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}