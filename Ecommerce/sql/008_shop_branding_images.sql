-- Migration: Add shop branding image fields
-- Feature: 002-cloudinary-image-upload Phase 5
-- Date: 2025-11-19
-- Description: Add logo_url, logo_public_id, banner_url, banner_public_id columns to shops table

-- Add shop branding image columns
ALTER TABLE shops
ADD COLUMN logo_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for shop logo (400x400 with transparency)',
ADD COLUMN logo_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public ID for shop logo',
ADD COLUMN banner_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for shop banner (1200x400)',
ADD COLUMN banner_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public ID for shop banner';

-- Add indexes for logo and banner public IDs (for deletion operations)
CREATE INDEX idx_shop_logo_public_id ON shops(logo_public_id);
CREATE INDEX idx_shop_banner_public_id ON shops(banner_public_id);

-- Verify migration
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'shops'
  AND COLUMN_NAME IN ('logo_url', 'logo_public_id', 'banner_url', 'banner_public_id')
ORDER BY ORDINAL_POSITION;
