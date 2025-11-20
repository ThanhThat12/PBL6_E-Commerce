package com.PBL6.Ecommerce.domain.dto;

/**
 * DTO to receive a GHN service selection from frontend
 */
public class GhnServiceSelectionDTO {
    private Integer serviceId;
    private Integer serviceTypeId;

    public GhnServiceSelectionDTO() {}

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public Integer getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Integer serviceTypeId) { this.serviceTypeId = serviceTypeId; }
}
