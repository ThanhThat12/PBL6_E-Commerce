package com.PBL6.Ecommerce.dto.profile;

import com.PBL6.Ecommerce.constant.TypeAddress;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new address
 * Used for POST /api/addresses
 */
@Schema(description = "Create address request")
public class CreateAddressRequest {
    
    @Size(max = 100, message = "Label must not exceed 100 characters")
    @Schema(description = "Address label (e.g., 'Home', 'Office')", example = "Home", required = false)
    private String label;
    
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    @Schema(description = "Contact person name", example = "John Doe")
    private String contactName;
    
    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^(03|05|07|08|09)\\d{8}$", message = "Phone number must be Vietnamese format")
    @Schema(description = "Contact phone number", example = "0901234567")
    private String contactPhone;
    
    @NotBlank(message = "Full address is required")
    @Size(max = 500, message = "Full address must not exceed 500 characters")
    @Schema(description = "Full street address", example = "123 Main Street")
    private String fullAddress;
    
    @NotNull(message = "Province ID is required")
    @Schema(description = "GHN Province ID", example = "201")
    private Integer provinceId;
    
    @NotNull(message = "District ID is required")
    @Schema(description = "GHN District ID", example = "1482")
    private Integer districtId;
    
    @NotBlank(message = "Ward code is required")
    @Schema(description = "GHN Ward code", example = "21002")
    private String wardCode;
    
    @Size(max = 100, message = "Province name must not exceed 100 characters")
    @Schema(description = "Province name (resolved from GHN)", example = "Hà Nội", required = false)
    private String provinceName;
    
    @Size(max = 100, message = "District name must not exceed 100 characters")
    @Schema(description = "District name (resolved from GHN)", example = "Ba Đình", required = false)
    private String districtName;
    
    @Size(max = 100, message = "Ward name must not exceed 100 characters")
    @Schema(description = "Ward name (resolved from GHN)", example = "Phường Ngọc Hà", required = false)
    private String wardName;
    
    @Schema(description = "Address type", example = "HOME", defaultValue = "HOME")
    private TypeAddress typeAddress = TypeAddress.HOME;
    
    @Schema(description = "Set as primary address", example = "false", defaultValue = "false")
    private Boolean primaryAddress = false;

    // Getters and Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

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

    public TypeAddress getTypeAddress() { return typeAddress; }
    public void setTypeAddress(TypeAddress typeAddress) { this.typeAddress = typeAddress; }

    public Boolean getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Boolean primaryAddress) { this.primaryAddress = primaryAddress; }
}
