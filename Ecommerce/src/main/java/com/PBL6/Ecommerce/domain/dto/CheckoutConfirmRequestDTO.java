package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutConfirmRequestDTO {
    
    @NotNull(message = "Address ID không được để trống")
    private Long addressId;
    
    @NotNull(message = "Shop ID không được để trống")
    private Long shopId;
    
    @NotNull(message = "Service ID không được để trống")
    private Integer serviceId;
    
    private Integer serviceTypeId; // optional
    
    @NotNull(message = "Cart item IDs không được để trống")
    private Long[] cartItemIds;
    
    @NotNull(message = "Payment method không được để trống")
    private String paymentMethod; // "COD" hoặc "ONLINE"
    
    private String note; // Ghi chú từ buyer
    
    // Constructors
    public CheckoutConfirmRequestDTO() {}
    
    public CheckoutConfirmRequestDTO(Long addressId, Long shopId, Integer serviceId,
                                     Integer serviceTypeId, Long[] cartItemIds, 
                                     String paymentMethod, String note) {
        this.addressId = addressId;
        this.shopId = shopId;
        this.serviceId = serviceId;
        this.serviceTypeId = serviceTypeId;
        this.cartItemIds = cartItemIds;
        this.paymentMethod = paymentMethod;
        this.note = note;
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
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}