package com.PBL6.Ecommerce.constant;

/**
 * Payment status enum
 * 
 * UNPAID: Chưa thanh toán
 * PAID: Đã thanh toán
 * FAILED: Thanh toán thất bại
 */
public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    FAILED("Thanh toán thất bại");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
