package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating GHN credentials for a Shop
 */
public class GhnCredentialsDTO {
    @NotBlank
    private String ghnToken;
    
    @NotBlank
    private String ghnShopId;
    
    private String ghnClientId;

    public GhnCredentialsDTO() {}

    public String getGhnToken() { return ghnToken; }
    public void setGhnToken(String ghnToken) { this.ghnToken = ghnToken; }
    
    public String getGhnShopId() { return ghnShopId; }
    public void setGhnShopId(String ghnShopId) { this.ghnShopId = ghnShopId; }
    
    public String getGhnClientId() { return ghnClientId; }
    public void setGhnClientId(String ghnClientId) { this.ghnClientId = ghnClientId; }
}
