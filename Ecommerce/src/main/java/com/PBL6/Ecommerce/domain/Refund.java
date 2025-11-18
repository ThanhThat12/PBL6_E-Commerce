package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "reason")
    private String reason;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RefundStatus status = RefundStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private WalletTransaction transaction;

    // Lưu ảnh bằng chứng từ khách (JSON array nếu nhiều ảnh)
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    
    // Cờ đánh dấu có cần trả hàng không
    @Column(name = "requires_return")
    private Boolean requiresReturn = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public enum RefundStatus {
        PENDING,                     // Chờ duyệt
        APPROVED_WAITING_RETURN,     // Đã duyệt - Chờ trả hàng
        RETURNING,                   // Đang trả hàng
        APPROVED_REFUNDING,          // Đã duyệt - Đang hoàn tiền
        COMPLETED,                   // Hoàn tiền thành công
        REJECTED                     // Từ chối
    }

    // Constructors
    public Refund() {
    }

    public Refund(Order order, BigDecimal amount, String reason) {
        this.order = order;
        this.amount = amount;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public WalletTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(WalletTransaction transaction) {
        this.transaction = transaction;
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

    public Boolean getRequiresReturn() {
        return requiresReturn;
    }

    public void setRequiresReturn(Boolean requiresReturn) {
        this.requiresReturn = requiresReturn;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
