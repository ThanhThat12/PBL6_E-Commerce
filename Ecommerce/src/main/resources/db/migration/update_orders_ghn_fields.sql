-- Migration script to update orders table for GHN integration
-- Changes text province/district/ward to GHN numeric codes

-- Add new columns with GHN codes
ALTER TABLE orders 
ADD COLUMN province_id INT NULL,
ADD COLUMN district_id INT NULL, 
ADD COLUMN ward_code VARCHAR(20) NULL;

-- Update existing records with default Đà Nẵng - Liên Chiểu codes
UPDATE orders 
SET province_id = 202,     -- Đà Nẵng province ID
    district_id = 1542,    -- Liên Chiểu district ID
    ward_code = '550107'   -- Hòa Khánh Bắc ward code
WHERE province_id IS NULL;

-- Drop old text columns
ALTER TABLE orders 
DROP COLUMN province,
DROP COLUMN district, 
DROP COLUMN ward;

-- Add indexes for performance
CREATE INDEX idx_orders_district_id ON orders(district_id);
CREATE INDEX idx_orders_ward_code ON orders(ward_code);