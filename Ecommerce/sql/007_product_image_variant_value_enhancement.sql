-- Migration: Product Image - Variant Value Association Enhancement
-- Date: 2025-11-19
-- Description: Add direct association between ProductImage and ProductVariantValue for optimized queries
--              Rename 'color' to 'variant_value_name' for attribute-agnostic design
--              Add unique constraint to enforce 1 image per (product_id, variant_value_id)

-- Step 1: Add new variant_value_id column
ALTER TABLE product_images
ADD COLUMN variant_value_id BIGINT NULL COMMENT 'Direct reference to Group 1 variant value (e.g., Color, Size, Material)';

-- Step 2: Rename color column to variant_value_name for clarity (attribute-agnostic)
ALTER TABLE product_images
CHANGE COLUMN color variant_value_name VARCHAR(100) NULL COMMENT 'Display-friendly name of variant value (denormalized cache)';

-- Step 3: Add foreign key constraint
ALTER TABLE product_images
ADD CONSTRAINT fk_product_image_variant_value 
    FOREIGN KEY (variant_value_id) 
    REFERENCES product_variant_values(id) 
    ON DELETE SET NULL
    COMMENT 'Cascade: SET NULL (preserve image, disassociate from variant value)';

-- Step 4: Add index for performance on variant_value_id queries
CREATE INDEX idx_product_image_variant_value 
ON product_images(variant_value_id)
COMMENT 'Optimize queries for images by variant value (e.g., find all Red images)';

-- Step 5: Add unique constraint to enforce 1 image per (product, variant value)
-- This ensures each variant value (e.g., Red, Blue) has exactly one image
ALTER TABLE product_images
ADD CONSTRAINT uq_product_variant_value 
    UNIQUE (product_id, variant_value_id)
    COMMENT 'Enforce business rule: 1 image per product variant value';

-- Step 6: Add composite index for common query patterns
CREATE INDEX idx_product_variant_lookup 
ON product_images(product_id, variant_value_id, display_order)
COMMENT 'Optimize queries for ordered variant images by product';

-- Verification queries (for testing):
-- SELECT COUNT(*) FROM product_images WHERE variant_value_id IS NOT NULL;
-- SELECT pi.*, pvv.value FROM product_images pi 
--   LEFT JOIN product_variant_values pvv ON pi.variant_value_id = pvv.id 
--   WHERE pi.product_id = ?;

-- Migration notes:
-- - All existing rows will have variant_value_id = NULL (backward compatible)
-- - Application must populate variant_value_id for new uploads
-- - Old 'color' data is preserved in 'variant_value_name' column
-- - Unique constraint only applies to non-NULL variant_value_id values
