package com.PBL6.Ecommerce.constant;

/**
 * Payment method enum
 * 
 * COD: Cash on Delivery (thanh toán khi nhận hàng)
 * MOMO: MOMO e-wallet (thanh toán online qua MOMO)
 */
public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    MOMO("MOMO e-wallet");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
