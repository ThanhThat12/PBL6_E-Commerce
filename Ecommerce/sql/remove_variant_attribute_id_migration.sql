-- Migration: Remove variant_attribute_id column from product_images table
-- Date: 2025-11-25
-- Reason: Simplified variant image handling - only using variant_attribute_value string now

USE ecommerce;

-- Step 1: Drop the existing unique constraint that includes variant_attribute_id
ALTER TABLE product_images 
DROP INDEX uq_product_variant_image;

-- Step 2: Drop the index on variant_attribute_id and variant_attribute_value
ALTER TABLE product_images 
DROP INDEX idx_variant_attr;

-- Step 3: Drop the foreign key constraint
ALTER TABLE product_images 
DROP FOREIGN KEY FKk7xlxfwab6kl5tqy04hvfu8t1;

-- Step 4: Drop the variant_attribute_id column
ALTER TABLE product_images 
DROP COLUMN variant_attribute_id;

-- Step 5: Create new unique constraint without variant_attribute_id
ALTER TABLE product_images 
ADD CONSTRAINT uq_product_variant_image_new 
UNIQUE (product_id, variant_attribute_value, image_type);

-- Step 6: Add new index for variant attribute value lookup
ALTER TABLE product_images 
ADD INDEX idx_variant_value (variant_attribute_value);

-- Verify the changes
DESCRIBE product_images;
SHOW INDEX FROM product_images;