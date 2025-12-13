package com.PBL6.Ecommerce.domain.entity.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RefundItem - Chi tiết món hàng được hoàn trả
 * Lưu thông tin về món hàng cụ thể và số lượng được refund
 */
@Entity
@Table(name = "refund_items")
public class RefundItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Số lượng trả

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount; // Số tiền hoàn cho món này

    @Column(name = "reason")
    private String reason; // Lý do cụ thể cho món này (nếu khác với refund chung)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public RefundItem() {
    }

    public RefundItem(Refund refund, OrderItem orderItem, Integer quantity, BigDecimal refundAmount) {
        this.refund = refund;
        this.orderItem = orderItem;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Refund getRefund() {
        return refund;
    }

    public void setRefund(Refund refund) {
        this.refund = refund;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
        return "RefundItem{" +
                "id=" + id +
                ", refundId=" + (refund != null ? refund.getId() : null) +
                ", orderItemId=" + (orderItem != null ? orderItem.getId() : null) +
                ", quantity=" + quantity +
                ", refundAmount=" + refundAmount +
                '}';
    }
}
