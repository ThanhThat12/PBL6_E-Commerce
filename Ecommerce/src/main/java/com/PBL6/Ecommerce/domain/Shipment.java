package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // liên kết đến order (client_order_code sẽ dùng format ORDER_{orderId} khi tạo từ hệ thống)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "ghn_order_code", length = 80)
    private String ghnOrderCode;

    @Column(name = "status", length = 50)
    private String status;

    @Lob
    @Column(name = "ghn_payload", columnDefinition = "TEXT")
    private String ghnPayload;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate
    public void onUpdate() { updatedAt = LocalDateTime.now(); }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getGhnOrderCode() { return ghnOrderCode; }
    public void setGhnOrderCode(String ghnOrderCode) { this.ghnOrderCode = ghnOrderCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGhnPayload() { return ghnPayload; }
    public void setGhnPayload(String ghnPayload) { this.ghnPayload = ghnPayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}