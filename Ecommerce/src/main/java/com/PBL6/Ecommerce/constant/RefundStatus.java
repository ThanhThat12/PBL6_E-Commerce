package com.PBL6.Ecommerce.constant;

/**
 * Refund status enum - Workflow chuẩn
 */
public enum RefundStatus {
    PENDING("Chờ duyệt"),                              // Khách vừa tạo yêu cầu
    APPROVED_WAITING_RETURN("Đã duyệt - Chờ trả hàng"), // Shop chấp nhận, yêu cầu khách trả hàng
    RETURNING("Đang trả hàng"),                         // Khách đã gửi hàng về
    APPROVED_REFUNDING("Đã duyệt - Đang hoàn tiền"),     // Hàng OK hoặc không cần trả, chuẩn bị refund
    COMPLETED("Hoàn tiền thành công"),                  // Đã hoàn tiền xong
    REJECTED("Từ chối hoàn tiền");                      // Shop từ chối

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
