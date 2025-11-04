package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

public class AddressResponseDTO {
    private Long id;
    private String label;
    private String fullAddress;
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    private String contactPhone;
    private Boolean primaryAddress;
    private LocalDateTime createdAt;

    public AddressResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }

    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Boolean getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Boolean primaryAddress) { this.primaryAddress = primaryAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}