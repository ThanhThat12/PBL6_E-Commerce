package com.PBL6.Ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequestDTO {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("accessKey")
    private String accessKey;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private String amount;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("redirectUrl")
    private String redirectUrl;
    
    @JsonProperty("ipnUrl")
    private String ipnUrl;
    
    @JsonProperty("requestType")
    private String requestType;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("lang")
    private String lang = "vi";
    
    @JsonProperty("signature")
    private String signature;

    // Constructors
    public PaymentRequestDTO() {
    }

    public PaymentRequestDTO(String partnerCode, String accessKey, String requestId, 
                            String amount, String orderId, String orderInfo, 
                            String redirectUrl, String ipnUrl, String requestType, 
                            String extraData, String signature) {
        this.partnerCode = partnerCode;
        this.accessKey = accessKey;
        this.requestId = requestId;
        this.amount = amount;
        this.orderId = orderId;
        this.orderInfo = orderInfo;
        this.redirectUrl = redirectUrl;
        this.ipnUrl = ipnUrl;
        this.requestType = requestType;
        this.extraData = extraData;
        this.signature = signature;
    }

    // Getters and Setters
    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "PaymentRequestDTO{" +
                "partnerCode='" + partnerCode + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount='" + amount + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", requestType='" + requestType + '\'' +
                '}';
    }
}
