package com.PBL6.Ecommerce.constant;

/**
 * Status for individual order items
 * Track return/refund status per product in an order
 */
public enum OrderItemStatus {
    COMPLETED,          // Đã giao / hoàn thành
    RETURN_REQUESTED,   // Buyer yêu cầu trả hàng
    RETURNED            // Hàng đã trả + refund thành công
}
