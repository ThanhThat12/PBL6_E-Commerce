package com.PBL6.Ecommerce.domain.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for multi-shop order creation result
 */
public class MultiShopOrderResult {
    private List<Long> orderIds;
    private BigDecimal totalAmount;
    private String method;

    public MultiShopOrderResult() {}

    public MultiShopOrderResult(List<Long> orderIds, BigDecimal totalAmount, String method) {
        this.orderIds = orderIds;
        this.totalAmount = totalAmount;
        this.method = method;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<Long> orderIds) {
        this.orderIds = orderIds;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
