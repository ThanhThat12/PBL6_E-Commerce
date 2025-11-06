package com.PBL6.Ecommerce.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "platform_fees")
public class PlatformFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "fee_percent")
    private BigDecimal feePercent;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public BigDecimal getFeePercent() { return feePercent; }
    public void setFeePercent(BigDecimal feePercent) { this.feePercent = feePercent; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
}
