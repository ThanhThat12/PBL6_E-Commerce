-- ============================================
-- FIX VOUCHER DATE MIGRATION ERROR
-- ============================================

-- Problem: Hibernate trying to add start_date/end_date columns with NOT NULL
-- But existing data has '0000-00-00 00:00:00' which is invalid

-- Solution: Fix the data first, then let Hibernate migrate

-- 1. Check current vouchers data
SELECT id, name, created_at, updated_at 
FROM vouchers 
LIMIT 10;

-- 2. Add columns as NULLABLE first (before Hibernate does it)
ALTER TABLE vouchers 
ADD COLUMN IF NOT EXISTS start_date DATETIME NULL,
ADD COLUMN IF NOT EXISTS end_date DATETIME NULL;

-- 3. Update existing vouchers with valid dates
-- Set start_date = created_at and end_date = 30 days from now
UPDATE vouchers 
SET 
    start_date = COALESCE(created_at, NOW()),
    end_date = DATE_ADD(COALESCE(created_at, NOW()), INTERVAL 30 DAY)
WHERE start_date IS NULL OR end_date IS NULL;

-- 4. Now make them NOT NULL (if needed by Hibernate)
ALTER TABLE vouchers 
MODIFY COLUMN start_date DATETIME NOT NULL,
MODIFY COLUMN end_date DATETIME NOT NULL;

-- 5. Verify
SELECT id, name, start_date, end_date, created_at 
FROM vouchers 
LIMIT 10;

-- ============================================
-- Alternative: If you want to keep columns nullable
-- ============================================
/*
If you prefer to allow NULL dates, modify your Vouchers.java entity:

@Column(name = "start_date")
private LocalDateTime startDate;  // Remove nullable = false

@Column(name = "end_date")  
private LocalDateTime endDate;  // Remove nullable = false

Then run:
ALTER TABLE vouchers 
ADD COLUMN IF NOT EXISTS start_date DATETIME NULL,
ADD COLUMN IF NOT EXISTS end_date DATETIME NULL;
*/
