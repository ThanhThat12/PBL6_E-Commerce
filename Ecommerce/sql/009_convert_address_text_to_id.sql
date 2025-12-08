-- Migration: Convert address from text to ID fields
-- Remove province, district, ward text columns and use ID fields only

-- Step 1: Drop text columns since we already have ID columns
ALTER TABLE orders DROP COLUMN province;
ALTER TABLE orders DROP COLUMN district;  
ALTER TABLE orders DROP COLUMN ward;

-- Note: province_id, district_id, ward_code columns already exist in the schema
-- No need to add them again

-- Optional: Add indexes for better performance
CREATE INDEX idx_orders_province_id ON orders(province_id);
CREATE INDEX idx_orders_district_id ON orders(district_id);
CREATE INDEX idx_orders_ward_code ON orders(ward_code);

-- Optional: Add comments for clarity
ALTER TABLE orders 
MODIFY COLUMN province_id INT COMMENT 'Province ID from GHN API',
MODIFY COLUMN district_id INT COMMENT 'District ID from GHN API',
MODIFY COLUMN ward_code VARCHAR(255) COMMENT 'Ward code from GHN API';