-- ==========================================
-- QUICK FIX: Add voucher date columns
-- ==========================================
-- Chạy script này trong MySQL để fix lỗi voucher

USE ecommerce; -- Thay tên database của bạn

-- Step 1: Add columns (nullable first)
ALTER TABLE vouchers 
ADD COLUMN IF NOT EXISTS start_date DATETIME NULL,
ADD COLUMN IF NOT EXISTS end_date DATETIME NULL;

-- Step 2: Update existing vouchers with valid dates
UPDATE vouchers 
SET 
    start_date = COALESCE(created_at, NOW()),
    end_date = DATE_ADD(COALESCE(created_at, NOW()), INTERVAL 30 DAY)
WHERE start_date IS NULL OR end_date IS NULL;

-- Step 3: Make NOT NULL (optional, comment out if you want them nullable)
-- ALTER TABLE vouchers 
-- MODIFY COLUMN start_date DATETIME NOT NULL,
-- MODIFY COLUMN end_date DATETIME NOT NULL;

-- Step 4: Verify
SELECT id, code, start_date, end_date, created_at 
FROM vouchers 
LIMIT 5;

SELECT 'Voucher table fixed successfully!' as status;
