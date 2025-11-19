package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutInitRequestDTO {
    @NotNull
    private Long shopId;
    
    @NotNull
    private Long addressId; // địa chỉ HOME của buyer
    
    @NotNull
    private Long[] cartItemIds; // hoặc productVariantIds để tính tổng weight

    // getters/setters
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public Long[] getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(Long[] cartItemIds) { this.cartItemIds = cartItemIds; }
}