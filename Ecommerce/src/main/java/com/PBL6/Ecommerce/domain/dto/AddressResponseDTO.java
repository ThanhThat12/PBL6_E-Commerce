package com.PBL6.Ecommerce.domain.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Address response with full details including GHN location names and timestamps")
public class AddressResponseDTO {
    @Schema(example = "1", description = "Address unique identifier")
    private Long id;
    
    @Schema(example = "123 Đường Nguyễn Huệ, Quận 1", description = "Full delivery address")
    private String fullAddress;
    
    @Schema(example = "202", description = "GHN Province ID")
    private Integer provinceId;
    
    @Schema(example = "1442", description = "GHN District ID")
    private Integer districtId;
    
    @Schema(example = "21308", description = "GHN Ward Code")
    private String wardCode;
    
    @Schema(example = "Ho Chi Minh", description = "Human-readable province name from GHN")
    private String provinceName;
    
    @Schema(example = "District 1", description = "Human-readable district name from GHN")
    private String districtName;
    
    @Schema(example = "Ward 1", description = "Human-readable ward name from GHN")
    private String wardName;
    
    @Schema(example = "Nguyễn Văn A", description = "Contact person name")
    private String contactName;
    
    @Schema(example = "0912345678", description = "Contact person phone number")
    private String contactPhone;
    
    @Schema(example = "true", description = "Is this the primary/default address (only for HOME type)")
    private Boolean primaryAddress;
    
    @Schema(example = "HOME", description = "Address type: HOME (buyer delivery) or STORE (seller warehouse)", allowableValues = {"HOME", "STORE"})
    private String typeAddress;
    
    @Schema(example = "2025-12-14T10:30:00", description = "When this address was created (ISO 8601 format)")
    private LocalDateTime createdAt;
    
    @Schema(example = "2025-12-14T14:22:15", description = "Last update time (auto-updated when address changes, ISO 8601 format)")
    private LocalDateTime updatedAt;

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

    public Boolean getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Boolean primaryAddress) { this.primaryAddress = primaryAddress; }
    
    public String getTypeAddress() { return typeAddress; }
    public void setTypeAddress(String typeAddress) { this.typeAddress = typeAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}