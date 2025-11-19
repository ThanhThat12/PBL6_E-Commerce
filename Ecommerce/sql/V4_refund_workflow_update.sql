-- Migration script: Update Refund workflow
-- Date: 2025-11-18
-- Description: Thêm cột requires_return và cập nhật RefundStatus enum

-- Thêm cột requires_return vào bảng refunds
ALTER TABLE refunds 
ADD COLUMN requires_return BOOLEAN DEFAULT FALSE 
COMMENT 'Có yêu cầu trả hàng hay không';

-- Cập nhật status enum với các trạng thái mới
-- MySQL không hỗ trợ ALTER ENUM trực tiếp, cần tạo lại constraint
-- Hoặc sử dụng VARCHAR nếu đang dùng VARCHAR cho status

-- Nếu status là ENUM, chạy lệnh này (cho MySQL 5.7+):
-- ALTER TABLE refunds MODIFY COLUMN status ENUM(
--     'PENDING',
--     'APPROVED_WAITING_RETURN', 
--     'RETURNING',
--     'APPROVED_REFUNDING',
--     'COMPLETED',
--     'REJECTED'
-- ) DEFAULT 'PENDING';

-- Nếu status là VARCHAR, không cần sửa gì, chỉ cần update data:
-- Update các status cũ sang status mới
UPDATE refunds SET status = 'PENDING' WHERE status = 'REQUESTED';
UPDATE refunds SET status = 'APPROVED_REFUNDING' WHERE status = 'APPROVED';

-- Thêm index cho tìm kiếm nhanh
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_refunds_requires_return ON refunds(requires_return);

-- Ghi chú:
-- PENDING: Chờ duyệt (khách vừa tạo yêu cầu)
-- APPROVED_WAITING_RETURN: Đã duyệt - Chờ trả hàng (shop yêu cầu khách gửi hàng về)
-- RETURNING: Đang trả hàng (khách đã gửi hàng)
-- APPROVED_REFUNDING: Đã duyệt - Đang hoàn tiền (hàng OK hoặc không cần trả)
-- COMPLETED: Hoàn tiền thành công
-- REJECTED: Từ chối hoàn tiền
