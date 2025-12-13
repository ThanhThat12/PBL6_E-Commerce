package com.PBL6.Ecommerce.domain.dto.order;

import java.math.BigDecimal;

public class OrderCalculateResponse {
    
    private BigDecimal subtotal;           // Tổng tiền hàng
    private BigDecimal shippingFee;        // Phí vận chuyển
    private BigDecimal discount;           // Giảm giá từ voucher
    private BigDecimal tax;                // Thuế (nếu có)
    private BigDecimal total;              // Tổng cộng
    private String voucherCode;            // Mã voucher đã áp dụng
    private String voucherDescription;     // Mô tả voucher
    private boolean voucherValid;          // Voucher có hợp lệ không
    private String message;                // Thông báo

    // Constructors
    public OrderCalculateResponse() {
    }

    public OrderCalculateResponse(BigDecimal subtotal, BigDecimal shippingFee, 
                                 BigDecimal discount, BigDecimal tax, BigDecimal total) {
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
    }

    // Static factory method for error response
    public static OrderCalculateResponse error(String message) {
        OrderCalculateResponse response = new OrderCalculateResponse();
        response.setMessage(message);
        return response;
    }

    // Static factory method for success response
    public static OrderCalculateResponse success(BigDecimal subtotal, BigDecimal shippingFee, 
                                                BigDecimal discount, BigDecimal tax, BigDecimal total) {
        OrderCalculateResponse response = new OrderCalculateResponse(subtotal, shippingFee, discount, tax, total);
        response.setMessage("Tính toán thành công");
        return response;
    }

    // Getters and Setters
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getVoucherDescription() {
        return voucherDescription;
    }

    public void setVoucherDescription(String voucherDescription) {
        this.voucherDescription = voucherDescription;
    }

    public boolean isVoucherValid() {
        return voucherValid;
    }

    public void setVoucherValid(boolean voucherValid) {
        this.voucherValid = voucherValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OrderCalculateResponse{" +
                "subtotal=" + subtotal +
                ", shippingFee=" + shippingFee +
                ", discount=" + discount +
                ", tax=" + tax +
                ", total=" + total +
                ", voucherCode='" + voucherCode + '\'' +
                ", voucherValid=" + voucherValid +
                ", message='" + message + '\'' +
                '}';
    }
}
