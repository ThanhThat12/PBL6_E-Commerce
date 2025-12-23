package com.PBL6.Ecommerce.domain.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Recent Activity in Admin Dashboard
 * Shows recent important events in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    
    /**
     * Activity type
     */
    private ActivityType type;
    
    /**
     * Activity message/description
     */
    private String message;
    
    /**
     * When the activity occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Time ago text (e.g., "2 phút trước", "1 giờ trước")
     */
    private String timeAgo;
    
    /**
     * Icon name for frontend (optional)
     */
    private String icon;
    
    /**
     * Related entity ID (optional, for linking)
     */
    private Long relatedId;
    
    /**
     * Activity types
     */
    public enum ActivityType {
        NEW_ORDER("Đơn hàng mới từ khách hàng", "shopping-bag"),
        PRODUCT_UPDATED("Sản phẩm được cập nhật kho", "package"),
        NEW_CUSTOMER("Khách hàng mới đăng ký", "user-plus"),
        ORDER_COMPLETED("Thanh toán được xử lý thành công", "credit-card");
        
        private final String defaultMessage;
        private final String icon;
        
        ActivityType(String defaultMessage, String icon) {
            this.defaultMessage = defaultMessage;
            this.icon = icon;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
        
        public String getIcon() {
            return icon;
        }
    }
}
