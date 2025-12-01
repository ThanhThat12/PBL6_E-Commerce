package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponseDTO {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private Object ghn; // minimal info about GHN

    // Shipping/receiver fields
    private BigDecimal shippingFee;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String province;
    private String district;
    private String ward;
    private String serviceType;

    public OrderResponseDTO() {}
    public OrderResponseDTO(Long orderId, String status, BigDecimal totalAmount, LocalDateTime createdAt, Object ghn) {
        this.orderId = orderId; this.status = status; this.totalAmount = totalAmount; this.createdAt = createdAt; this.ghn = ghn;
    }
    // getters/setters
    public Long getOrderId(){return orderId;}
    public void setOrderId(Long v){this.orderId=v;}
    public String getStatus(){return status;}
    public void setStatus(String v){this.status=v;}
    public BigDecimal getTotalAmount(){return totalAmount;}
    public void setTotalAmount(BigDecimal v){this.totalAmount=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public Object getGhn(){return ghn;}
    public void setGhn(Object v){this.ghn=v;}

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
}