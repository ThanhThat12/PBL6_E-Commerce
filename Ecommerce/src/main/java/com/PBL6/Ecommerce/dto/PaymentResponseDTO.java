package com.PBL6.Ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponseDTO {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("payUrl")
    private String payUrl;
    
    @JsonProperty("deeplink")
    private String deeplink;
    
    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;
    
    @JsonProperty("deeplinkMiniApp")
    private String deeplinkMiniApp;
    
    @JsonProperty("signature")
    private String signature;

    // Constructors
    public PaymentResponseDTO() {
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

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getDeeplinkMiniApp() {
        return deeplinkMiniApp;
    }

    public void setDeeplinkMiniApp(String deeplinkMiniApp) {
        this.deeplinkMiniApp = deeplinkMiniApp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    // Helper method to check if payment request was successful
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    @Override
    public String toString() {
        return "PaymentResponseDTO{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", resultCode=" + resultCode +
                ", message='" + message + '\'' +
                ", payUrl='" + payUrl + '\'' +
                '}';
    }
}
