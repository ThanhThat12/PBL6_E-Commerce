-- Migration: Add shipping dimensions to products table
-- Date: 2024-11-07
-- Purpose: Add weight and dimension fields for GHN shipping fee calculation

-- Add shipping dimension columns to products table
ALTER TABLE products 
ADD COLUMN weight_grams INT COMMENT 'Trọng lượng sản phẩm (gram)',
ADD COLUMN length_cm INT COMMENT 'Chiều dài (cm)',
ADD COLUMN width_cm INT COMMENT 'Chiều rộng (cm)',
ADD COLUMN height_cm INT COMMENT 'Chiều cao (cm)';

-- Add index for better query performance on shop_id (if not exists)
CREATE INDEX IF NOT EXISTS idx_products_shop_id ON products(shop_id);

-- Add index for product variants sold_count (for top selling products query)
CREATE INDEX IF NOT EXISTS idx_product_variants_sold_count ON product_variants(product_id, sold_count);

-- Update existing products with default dimensions (optional)
-- UPDATE products SET 
--   weight_grams = 500,  -- Default 500g
--   length_cm = 20,      -- Default 20cm
--   width_cm = 15,       -- Default 15cm
--   height_cm = 10       -- Default 10cm
-- WHERE weight_grams IS NULL;

-- Verify changes
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'products' 
  AND COLUMN_NAME IN ('weight_grams', 'length_cm', 'width_cm', 'height_cm');
