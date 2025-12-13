package com.PBL6.Ecommerce.domain.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundResponseDTO {
    
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
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    @JsonProperty("signature")
    private String signature;

    // Constructors
    public RefundResponseDTO() {
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

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    // Helper method to check if refund was successful
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    @Override
    public String toString() {
        return "RefundResponseDTO{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", transId=" + transId +
                ", resultCode=" + resultCode +
                ", message='" + message + '\'' +
                '}';
    }
}
