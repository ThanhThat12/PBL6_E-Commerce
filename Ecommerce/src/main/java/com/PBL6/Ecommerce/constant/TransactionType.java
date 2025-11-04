package com.PBL6.Ecommerce.constant;

/**
 * Wallet transaction type
 * 
 * DEPOSIT: Nạp tiền vào ví
 * REFUND: Hoàn tiền từ đơn hàng
 * ORDER_PAYMENT: Thanh toán đơn hàng qua ví
 */
public enum TransactionType {
    DEPOSIT("Nạp tiền"),
    REFUND("Hoàn tiền"),
    ORDER_PAYMENT("Thanh toán đơn hàng");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
