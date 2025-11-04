package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequestDTO {
    // userId is set automatically from JWT token, so no @NotNull validation needed
    private Long userId;

    // optional reference to existing order id in your system (used to link shipment)
    private String orderReference;

    @NotEmpty
    private List<Item> items;

    @NotBlank
    private String toDistrictId;
    @NotBlank
    private String toWardCode;

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập điện thoại người nhận")
    private String receiverPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ người nhận")
    private String receiverAddress;

    private String province;
    private String district;
    private String ward;

    private Integer weightGrams = 1000;
    private BigDecimal codAmount;

    // Shipping fee and voucher discount (from frontend calculation)
    private BigDecimal shippingFee;
    private BigDecimal voucherDiscount;
    private String voucherCode;

    private String method;

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

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    // getters / setters (only ones used)
    public Long getUserId(){return userId;}
    public void setUserId(Long u){this.userId=u;}
    public String getToDistrictId(){return toDistrictId;}
    public void setToDistrictId(String v){this.toDistrictId=v;}
    public String getToWardCode(){return toWardCode;}
    public void setToWardCode(String v){this.toWardCode=v;}
    public String getReceiverName(){return receiverName;}
    public void setReceiverName(String v){this.receiverName=v;}
    public String getReceiverPhone(){return receiverPhone;}
    public void setReceiverPhone(String v){this.receiverPhone=v;}
    public String getReceiverAddress(){return receiverAddress;}
    public void setReceiverAddress(String v){this.receiverAddress=v;}
    public String getProvince(){return province;}
    public void setProvince(String v){this.province=v;}
    public String getDistrict(){return district;}
    public void setDistrict(String v){this.district=v;}
    public String getWard(){return ward;}
    public void setWard(String v){this.ward=v;}
    public Integer getWeightGrams(){return weightGrams;}
    public void setWeightGrams(Integer w){this.weightGrams=w;}
    public BigDecimal getCodAmount(){return codAmount;}
    public void setCodAmount(BigDecimal c){this.codAmount=c;}
    public List<Item> getItems(){return items;}
    public void setItems(List<Item> items){this.items = items;}
    public String getOrderReference(){ return orderReference; }
    public void setOrderReference(String orderReference){ this.orderReference = orderReference; }
    
    // Getters and setters for shipping and voucher
    public BigDecimal getShippingFee(){return shippingFee;}
    public void setShippingFee(BigDecimal s){this.shippingFee=s;}
    public BigDecimal getVoucherDiscount(){return voucherDiscount;}
    public void setVoucherDiscount(BigDecimal v){this.voucherDiscount=v;}
    public String getVoucherCode(){return voucherCode;}
    public void setVoucherCode(String c){this.voucherCode=c;}
}