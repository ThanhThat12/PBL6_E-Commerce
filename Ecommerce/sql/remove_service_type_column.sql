-- Remove service_type column from orders table
-- Date: 2025-11-28

ALTER TABLE orders DROP COLUMN service_type;
