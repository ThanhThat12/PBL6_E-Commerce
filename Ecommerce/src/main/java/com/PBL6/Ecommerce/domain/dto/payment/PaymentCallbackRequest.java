package com.PBL6.Ecommerce.domain.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentCallbackRequest {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("orderType")
    private String orderType;
    
    @JsonProperty("transId")
    private Long transId;
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("payType")
    private String payType;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("signature")
    private String signature;

    // Constructors
    public PaymentCallbackRequest() {
    }

    // Getters and Setters
    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    // Helper method to check if payment was successful
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    @Override
    public String toString() {
        return "PaymentCallbackRequest{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", transId=" + transId +
                ", resultCode=" + resultCode +
                ", message='" + message + '\'' +
                ", payType='" + payType + '\'' +
                '}';
    }
}
