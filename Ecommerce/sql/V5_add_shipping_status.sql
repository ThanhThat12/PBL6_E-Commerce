-- Migration script: Add SHIPPING status and auto-complete feature
-- Date: 2025-11-18
-- Description: Thêm trạng thái SHIPPING, tận dụng bảng shipments để track thời gian giao hàng

-- Nếu status là ENUM, update enum values (cho MySQL):
-- ALTER TABLE orders MODIFY COLUMN status ENUM(
--     'PENDING',
--     'PROCESSING', 
--     'SHIPPING',
--     'COMPLETED',
--     'CANCELLED'
-- ) DEFAULT 'PENDING';

-- Nếu status là VARCHAR, không cần sửa gì

-- Thêm index cho orders.status để tìm kiếm nhanh
CREATE INDEX idx_orders_status ON orders(status);

-- Thêm index cho shipments.created_at để auto-complete kiểm tra thời gian
CREATE INDEX idx_shipments_created_at ON shipments(created_at);

-- Ghi chú:
-- PENDING: Chờ xác nhận (sau khi thanh toán)
-- PROCESSING: Đang xử lý (seller xác nhận)
-- SHIPPING: Đang giao hàng (seller đã giao cho đơn vị vận chuyển, có shipment record)
-- COMPLETED: Hoàn thành (buyer xác nhận hoặc tự động sau 1 ngày kể từ shipment.created_at)
-- CANCELLED: Đã hủy

-- Workflow:
-- 1. Buyer thanh toán → Order.status = PENDING
-- 2. Seller xác nhận → Order.status = PROCESSING
-- 3. Seller tạo vận đơn GHN → Order.status = SHIPPING, Shipment record được tạo
-- 4. Buyer xác nhận đã nhận → Order.status = COMPLETED
--    HOẶC tự động sau 1 ngày từ Shipment.created_at → Order.status = COMPLETED


-- Ghi chú:
-- PENDING: Chờ xác nhận (sau khi thanh toán)
-- PROCESSING: Đang xử lý (seller xác nhận)
-- SHIPPING: Đang giao hàng (seller đã giao cho đơn vị vận chuyển)
-- COMPLETED: Hoàn thành (buyer xác nhận hoặc tự động sau 1 ngày)
-- CANCELLED: Đã hủy
