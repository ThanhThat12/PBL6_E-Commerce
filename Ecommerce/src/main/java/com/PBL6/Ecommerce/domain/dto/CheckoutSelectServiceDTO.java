package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutSelectServiceDTO {
    @NotNull
    private Long shopId;
    
    @NotNull
    private Long addressId;
    
    @NotNull
    private Integer serviceId; // service_id user đã chọn
    
    private Integer serviceTypeId;

    // getters/setters
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
    
    public Integer getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Integer serviceTypeId) { this.serviceTypeId = serviceTypeId; }
}