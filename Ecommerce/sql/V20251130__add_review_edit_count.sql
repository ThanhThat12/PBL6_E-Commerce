-- Migration: Add edit_count to product_reviews
-- Date: 2025-11-30
-- Description: Track number of times a review has been edited (max 1 edit allowed within 30 days)

-- Add edit_count column to product_reviews table
ALTER TABLE product_reviews 
ADD COLUMN IF NOT EXISTS edit_count INT DEFAULT 0 COMMENT 'Number of times the review has been edited (max 1)';

-- Set default value for existing reviews
UPDATE product_reviews SET edit_count = 0 WHERE edit_count IS NULL;
