package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.util.Date;

import com.PBL6.Ecommerce.domain.entity.order.Order.OrderStatus;
import com.PBL6.Ecommerce.domain.entity.order.Order.PaymentStatus;

public class AdminOrderDTO {
    private Long orderId;
    private String customerName;
    private String receiverPhone;
    private Date orderDate;
    private BigDecimal totalAmount;
    private PaymentStatus payment;
    private OrderStatus status;
    private String location;

    // Constructors
    public AdminOrderDTO() {
    }

    public AdminOrderDTO(Long orderId, String customerName, String receiverPhone, Date orderDate,
                         BigDecimal totalAmount, PaymentStatus payment, OrderStatus status, String location) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.receiverPhone = receiverPhone;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.payment = payment;
        this.status = status;
        this.location = location;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPayment() {
        return payment;
    }

    public void setPayment(PaymentStatus payment) {
        this.payment = payment;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
