package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutCalculateFeeRequestDTO {
    
    @NotNull(message = "Address ID không được để trống")
    private Long addressId;
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    @NotNull(message = "Service ID không được để trống")
    private Integer serviceId;
    
    private Integer serviceTypeId; // optional
    
    private Long[] cartItemIds;
    
    // Constructors
    public CheckoutCalculateFeeRequestDTO() {}
    
    public CheckoutCalculateFeeRequestDTO(Long addressId, Long shopId, Integer serviceId, 
                                          Integer serviceTypeId, Long[] cartItemIds) {
        this.addressId = addressId;
        this.shopId = shopId;
        this.serviceId = serviceId;
        this.serviceTypeId = serviceTypeId;
        this.cartItemIds = cartItemIds;
    }
    
    // Getters and Setters
    public Long getAddressId() {
        return addressId;
    }
    
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
    
    public Long getShopId() {
        return shopId;
    }
    
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
    
    public Integer getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
    
    public Integer getServiceTypeId() {
        return serviceTypeId;
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }
    
    public Long[] getCartItemIds() {
        return cartItemIds;
    }
    
    public void setCartItemIds(Long[] cartItemIds) {
        this.cartItemIds = cartItemIds;
    }
}