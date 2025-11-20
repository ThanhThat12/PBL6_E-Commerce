package com.PBL6.Ecommerce.constant;

/**
 * Payment transaction status enum
 * 
 * PENDING: Đang chờ xử lý
 * SUCCESS: Thành công
 * FAILED: Thất bại
 * REFUNDED: Đã hoàn tiền
 */
public enum PaymentTransactionStatus {
    PENDING("Đang chờ xử lý"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    PaymentTransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
