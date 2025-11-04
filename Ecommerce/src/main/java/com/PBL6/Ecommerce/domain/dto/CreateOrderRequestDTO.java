package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequestDTO {
    @NotNull
    private Long userId;

    // optional reference to existing order id in your system (used to link shipment)
    private String orderReference;

    @NotEmpty
    private List<Item> items;

    @NotBlank
    private String toName;
    @NotBlank
    private String toPhone;
    @NotBlank
    private String toDistrictId;
    @NotBlank
    private String toWardCode;
    @NotBlank
    private String toAddress;

    private Integer weightGrams = 1000;
    private BigDecimal codAmount;

    public static class Item {
        @NotNull
        private Long variantId;
        @Min(1)
        private Integer quantity;
        // getters/setters
        public Long getVariantId(){return variantId;}
        public void setVariantId(Long v){this.variantId=v;}
        public Integer getQuantity(){return quantity;}
        public void setQuantity(Integer q){this.quantity=q;}
    }

    // getters / setters (only ones used)
    public Long getUserId(){return userId;}
    public void setUserId(Long u){this.userId=u;}
    public String getToName(){return toName;}
    public void setToName(String v){this.toName=v;}
    public String getToPhone(){return toPhone;}
    public void setToPhone(String v){this.toPhone=v;}
    public String getToDistrictId(){return toDistrictId;}
    public void setToDistrictId(String v){this.toDistrictId=v;}
    public String getToWardCode(){return toWardCode;}
    public void setToWardCode(String v){this.toWardCode=v;}
    public String getToAddress(){return toAddress;}
    public void setToAddress(String v){this.toAddress=v;}
    public Integer getWeightGrams(){return weightGrams;}
    public void setWeightGrams(Integer w){this.weightGrams=w;}
    public BigDecimal getCodAmount(){return codAmount;}
    public void setCodAmount(BigDecimal c){this.codAmount=c;}
    public List<Item> getItems(){return items;}
    public void setItems(List<Item> items){this.items = items;}
    public String getOrderReference(){ return orderReference; }
    public void setOrderReference(String orderReference){ this.orderReference = orderReference; }
}