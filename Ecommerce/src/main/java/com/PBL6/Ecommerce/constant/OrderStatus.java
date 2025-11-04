package com.PBL6.Ecommerce.constant;

/**
 * Order status enum
 * 
 * PENDING: Chờ xử lý (vừa tạo đơn)
 * PROCESSING: Đang xử lý (shop xác nhận, đang chuẩn bị)
 * COMPLETED: Hoàn thành (khách đã nhận hàng)
 * CANCELLED: Đã hủy
 */
public enum OrderStatus {
    PENDING("Chờ xử lý"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
