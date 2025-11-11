package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import java.util.List;

public class CheckoutFeeRequestDTO {
    @NotNull
    private Long shopId;

    // buyer address id
    @NotNull
    private Long addressId;

    @NotEmpty
    private List<Item> items;

    // optional COD amount
    private Integer codAmount;

    // service selection chosen by buyer
    private Integer serviceId;
    private Integer serviceTypeId;

    public static class Item {
        @NotNull
        private Long variantId;
        @Min(1)
        private Integer quantity;

        public Long getVariantId(){return variantId;}
        public void setVariantId(Long v){this.variantId=v;}
        public Integer getQuantity(){return quantity;}
        public void setQuantity(Integer q){this.quantity=q;}
    }

    public Long getShopId(){return shopId;}
    public void setShopId(Long s){this.shopId=s;}
    public Long getAddressId(){return addressId;}
    public void setAddressId(Long a){this.addressId=a;}
    public List<Item> getItems(){return items;}
    public void setItems(List<Item> items){this.items = items;}
    public Integer getCodAmount(){return codAmount;}
    public void setCodAmount(Integer c){this.codAmount=c;}
    public Integer getServiceId(){return serviceId;}
    public void setServiceId(Integer s){this.serviceId=s;}
    public Integer getServiceTypeId(){return serviceTypeId;}
    public void setServiceTypeId(Integer s){this.serviceTypeId=s;}
}
