package com.PBL6.Ecommerce.constant;

/**
 * Refund status enum
 */
public enum RefundStatus {
    REQUESTED("Yêu cầu hoàn tiền"),
    APPROVED("Đã duyệt"),
    COMPLETED("Hoàn tiền thành công"),
    REJECTED("Từ chối hoàn tiền");

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
