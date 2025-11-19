package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CheckoutShippingRequestDTO {
    @NotNull
    private Long shopId;

    // buyer's address id stored in addresses table
    private Long addressId;

    @NotEmpty
    private List<Item> items;

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
}
