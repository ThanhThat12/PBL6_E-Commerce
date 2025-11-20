-- Migration: Create refund_items table
-- Date: 2025-11-19
-- Purpose: Store specific items being refunded (partial refund support)

CREATE TABLE IF NOT EXISTS refund_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    refund_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_refund_items_refund FOREIGN KEY (refund_id) 
        REFERENCES refunds(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_items_order_item FOREIGN KEY (order_item_id) 
        REFERENCES order_items(id) ON DELETE CASCADE,
    
    INDEX idx_refund_items_refund_id (refund_id),
    INDEX idx_refund_items_order_item_id (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment
ALTER TABLE refund_items COMMENT = 'Chi tiết món hàng được hoàn trả (hỗ trợ refund từng phần)';
