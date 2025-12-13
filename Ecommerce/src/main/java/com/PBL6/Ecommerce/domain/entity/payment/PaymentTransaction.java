package com.PBL6.Ecommerce.domain.entity.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.PBL6.Ecommerce.constant.PaymentTransactionStatus;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.constant.PaymentStatus;
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "request_id", nullable = false, unique = true, length = 100)
    private String requestId;

    @Column(name = "order_id_momo", length = 50)
    private String orderIdMomo;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "trans_id", length = 50)
    private String transId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

    @Column(name = "result_code")
    private Integer resultCode;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "signature", length = 255)
    private String signature;

    @Column(name = "pay_url", columnDefinition = "TEXT")
    private String payUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();



    // Constructors
    public PaymentTransaction() {
    }

    public PaymentTransaction(Order order, String requestId, BigDecimal amount) {
        this.order = order;
        this.requestId = requestId;
        this.amount = amount;
        this.status = PaymentTransactionStatus.PENDING;
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

    public PaymentTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentTransactionStatus status) {
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
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
