-- Fix refunds table to match entity definition
-- 1. Update status enum to match Java entity
-- 2. Add requires_return column if missing

-- Step 1: Update status enum
ALTER TABLE refunds 
MODIFY COLUMN status ENUM('REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED') 
DEFAULT 'REQUESTED';

-- Step 2: Update existing status values if any
UPDATE refunds SET status = 'REQUESTED' 
WHERE status NOT IN ('REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED');

-- Step 3: Add requires_return column if it doesn't exist
ALTER TABLE refunds 
ADD COLUMN IF NOT EXISTS requires_return BOOLEAN DEFAULT FALSE 
AFTER image_url;

-- Step 4: Verify the changes
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'refunds'
ORDER BY ORDINAL_POSITION;
