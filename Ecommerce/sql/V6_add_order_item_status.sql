-- Migration script: Add status column to order_items table
-- Date: 2025-11-18
-- Description: Thêm cột status cho order_items để track return/refund status per product

-- Thêm cột status vào order_items
ALTER TABLE order_items 
ADD COLUMN status VARCHAR(50) DEFAULT 'COMPLETED' AFTER quantity;

-- Update existing records to COMPLETED
UPDATE order_items SET status = 'COMPLETED' WHERE status IS NULL;

-- Thêm index cho status để query nhanh
CREATE INDEX idx_order_items_status ON order_items(status);

-- Ghi chú:
-- COMPLETED: Đã giao / hoàn thành
-- RETURN_REQUESTED: Buyer yêu cầu trả hàng
-- RETURNED: Hàng đã trả + refund thành công
