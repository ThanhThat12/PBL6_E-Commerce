-- Fix NULL type_address in addresses table
-- This script updates all addresses with NULL or empty type_address to 'HOME' as default

-- Step 1: Check how many addresses have NULL type_address
SELECT COUNT(*) as null_type_count 
FROM addresses 
WHERE type_address IS NULL OR type_address = '';

-- Step 2: Show addresses that will be updated
SELECT id, user_id, full_address, type_address, primary_address, created_at
FROM addresses 
WHERE type_address IS NULL OR type_address = ''
ORDER BY id;

-- Step 3: Update NULL type_address to 'HOME' (default for buyer addresses)
UPDATE addresses 
SET type_address = 'HOME' 
WHERE type_address IS NULL OR type_address = '';

-- Step 4: Verify the update
SELECT COUNT(*) as fixed_count 
FROM addresses 
WHERE type_address = 'HOME';

-- Step 5: Add NOT NULL constraint to prevent future NULL values (optional, recommended)
-- ALTER TABLE addresses 
-- MODIFY COLUMN type_address VARCHAR(50) NOT NULL DEFAULT 'HOME';

-- Note: If you have STORE addresses that were mistakenly set to NULL,
-- you may need to manually update them after identifying which users are sellers:
-- UPDATE addresses a
-- INNER JOIN users u ON a.user_id = u.id
-- SET a.type_address = 'STORE'
-- WHERE u.role = 'SELLER' AND a.type_address = 'HOME';
