package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "request_id", nullable = false, unique = true, length = 255)
    private String requestId;

    @Column(name = "order_id_momo", length = 255)
    private String orderIdMomo;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "trans_id", length = 50)
    private String transId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "result_code")
    private Integer resultCode;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "response_time")
    private LocalDateTime responseTime;

    @Column(name = "signature", length = 500)
    private String signature;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "MOMO";

    @Column(name = "pay_type", length = 50)
    private String payType;

    @Column(name = "pay_url", columnDefinition = "TEXT")
    private String payUrl;

    @Column(name = "deep_link", columnDefinition = "TEXT")
    private String deeplink;

    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING,      // Chờ thanh toán
        PROCESSING,   // Đang xử lý
        SUCCESS,      // Thành công
        FAILED,       // Thất bại
        CANCELLED,    // Đã hủy
        EXPIRED,      // Hết hạn
        REFUNDED      // Đã hoàn tiền
    }

    // Constructors
    public PaymentTransaction() {
    }

    public PaymentTransaction(Order order, String requestId, BigDecimal amount) {
        this.order = order;
        this.requestId = requestId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOrderIdMomo() {
        return orderIdMomo;
    }

    public void setOrderIdMomo(String orderIdMomo) {
        this.orderIdMomo = orderIdMomo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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

    public LocalDateTime getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", transId='" + transId + '\'' +
                ", status=" + status +
                ", resultCode=" + resultCode +
                '}';
    }
}
