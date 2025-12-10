package com.PBL6.Ecommerce.domain.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for admin order detail view
 * Contains comprehensive order information including customer, shop, items, and payment details
 */
public class AdminOrderDetailDTO {
    
    // Thông Tin Chung
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String orderStatus;        // PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED
    private String paymentStatus;      // UNPAID, PAID, FAILED
    private LocalDateTime paidAt;
    
    // Thông Tin Cửa Hàng
    private Long shopId;
    private String shopName;
    
    // Thông Tin Khách Hàng
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Thông Tin Người Nhận & Giao Hàng
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal shippingFee;
    private String paymentMethod;      // COD, MOMO, SPORTYPAY
    
    // Chi Tiết Sản Phẩm
    private List<AdminOrderItemDTO> items;
    
    // Thanh Toán & Giảm Giá
    private BigDecimal subtotal;       // Tổng tiền hàng (sum từ order_items)
    private BigDecimal discount;       // Giảm giá (từ voucher)
    private BigDecimal totalAmount;    // Tổng thanh toán
    private Long voucherId;
    private String voucherCode;

    // Constructors
    public AdminOrderDetailDTO() {
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<AdminOrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<AdminOrderItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}
