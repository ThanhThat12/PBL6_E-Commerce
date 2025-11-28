-- =====================================================
-- MIGRATE VOUCHERS TABLE TO NEW SCHEMA
-- =====================================================

-- Step 1: Backup existing data (if needed)
-- CREATE TABLE vouchers_backup AS SELECT * FROM vouchers;

-- Step 2: Migrate data from old columns to new columns
-- Copy discount_amount -> discount_value (if discount_value is NULL)
UPDATE vouchers 
SET discount_value = discount_amount
WHERE discount_value IS NULL AND discount_amount IS NOT NULL;

-- Copy valid_from -> start_date (if start_date is NULL)
UPDATE vouchers 
SET start_date = valid_from
WHERE start_date IS NULL AND valid_from IS NOT NULL;

-- Copy valid_to -> end_date (if end_date is NULL)
UPDATE vouchers 
SET end_date = valid_to
WHERE end_date IS NULL AND valid_to IS NOT NULL;

-- Copy max_uses -> usage_limit (if usage_limit is NULL)
UPDATE vouchers 
SET usage_limit = max_uses
WHERE usage_limit IS NULL AND max_uses IS NOT NULL;

-- Step 3: Drop old columns
ALTER TABLE vouchers
DROP COLUMN IF EXISTS discount_amount,
DROP COLUMN IF EXISTS valid_from,
DROP COLUMN IF EXISTS valid_to,
DROP COLUMN IF EXISTS max_uses;

-- Step 4: Ensure new columns have correct types and constraints
-- (Already exist from previous migration, just verify)

-- Step 5: Update status enum if needed
-- ALTER TABLE vouchers 
-- MODIFY COLUMN status ENUM('ACTIVE', 'EXPIRED', 'UPCOMING') NOT NULL DEFAULT 'ACTIVE';

-- Step 6: Verify final schema
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'vouchers'
ORDER BY ORDINAL_POSITION;

-- Expected columns after migration:
-- 1. id - bigint(20) PK AI
-- 2. code - varchar(255)
-- 3. description - varchar(255)
-- 4. shop_id - bigint(20)
-- 5. discount_type - varchar(255) or ENUM
-- 6. discount_value - decimal(38,2)
-- 7. min_order_value - decimal(38,2)
-- 8. max_discount_amount - decimal(38,2)
-- 9. start_date - datetime
-- 10. end_date - datetime
-- 11. usage_limit - int(11)
-- 12. used_count - int(11)
-- 13. applicable_type - varchar(255)
-- 14. top_buyers_count - int(11)
-- 15. created_at - datetime
-- 16. status - enum('ACTIVE','EXPIRED','UPCOMING')

SELECT '✅ Vouchers table migration completed!' as status;
