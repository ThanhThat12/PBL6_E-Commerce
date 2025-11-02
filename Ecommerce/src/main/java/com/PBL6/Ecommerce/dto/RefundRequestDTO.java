package com.PBL6.Ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundRequestDTO {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("transId")
    private Long transId;
    
    @JsonProperty("lang")
    private String lang = "vi";
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("signature")
    private String signature;

    // Constructors
    public RefundRequestDTO() {
    }

    public RefundRequestDTO(String partnerCode, String orderId, String requestId, 
                           Long amount, Long transId, String description, String signature) {
        this.partnerCode = partnerCode;
        this.orderId = orderId;
        this.requestId = requestId;
        this.amount = amount;
        this.transId = transId;
        this.description = description;
        this.signature = signature;
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

    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "RefundRequestDTO{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", transId=" + transId +
                ", description='" + description + '\'' +
                '}';
    }
}
