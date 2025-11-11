package com.PBL6.Ecommerce.domain.dto;

/**
 * DTO for updating GHN credentials for a Shop
 */
public class GhnCredentialsDTO {
    private String ghnToken;
    private String ghnShopId;
    private Integer ghnServiceId;
    private Integer ghnServiceTypeId;

    public GhnCredentialsDTO() {}

    public String getGhnToken() { return ghnToken; }
    public void setGhnToken(String ghnToken) { this.ghnToken = ghnToken; }

    public String getGhnShopId() { return ghnShopId; }
    public void setGhnShopId(String ghnShopId) { this.ghnShopId = ghnShopId; }

    public Integer getGhnServiceId() { return ghnServiceId; }
    public void setGhnServiceId(Integer ghnServiceId) { this.ghnServiceId = ghnServiceId; }

    public Integer getGhnServiceTypeId() { return ghnServiceTypeId; }
    public void setGhnServiceTypeId(Integer ghnServiceTypeId) { this.ghnServiceTypeId = ghnServiceTypeId; }
}
