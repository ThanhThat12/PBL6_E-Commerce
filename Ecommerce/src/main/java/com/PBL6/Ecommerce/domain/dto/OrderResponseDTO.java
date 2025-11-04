package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponseDTO {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private Object ghn; // minimal info about GHN

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
}