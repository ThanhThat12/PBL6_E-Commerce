-- Cloudinary Image Management Service Migration
-- Feature: 002-cloudinary-image-upload
-- Date: 2025-11-18
-- Description: Add image fields to existing tables and create new tables for gallery images and retry queue

-- Product: Add main image fields
ALTER TABLE products 
ADD COLUMN main_image VARCHAR(500) NULL COMMENT 'Cloudinary URL for main product image',
ADD COLUMN main_image_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public_id for deletion';

-- ProductImage: New gallery table
CREATE TABLE product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL COMMENT 'Optional: for variant-specific images',
    image_url VARCHAR(500) NOT NULL COMMENT 'Full Cloudinary URL with transformations',
    public_id VARCHAR(255) NOT NULL COMMENT 'Cloudinary public_id for deletion',
    display_order INT NOT NULL DEFAULT 0 COMMENT 'Order of display in gallery (0-based)',
    color VARCHAR(50) NULL COMMENT 'Optional: color tag for variant images',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    INDEX idx_product_id (product_id),
    INDEX idx_variant_id (variant_id),
    INDEX idx_display_order (product_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User: Add avatar fields
ALTER TABLE users
ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for user avatar',
ADD COLUMN avatar_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public_id for deletion';

-- Shop: Add logo and banner fields
ALTER TABLE shops
ADD COLUMN logo_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for shop logo',
ADD COLUMN logo_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public_id for deletion',
ADD COLUMN banner_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for shop banner',
ADD COLUMN banner_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public_id for deletion';

-- ProductReview: Add images JSON column
ALTER TABLE product_reviews
ADD COLUMN images JSON NULL COMMENT 'Array of image objects with url and public_id',
ADD COLUMN images_count INT NOT NULL DEFAULT 0 COMMENT 'Count of images in JSON array';

-- ImageRetryQueue: New retry queue table for failed operations
CREATE TABLE image_retry_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type ENUM('UPLOAD', 'DELETE') NOT NULL COMMENT 'Type of operation to retry',
    entity_type VARCHAR(50) NOT NULL COMMENT 'Entity type: PRODUCT, USER, SHOP, REVIEW',
    entity_id BIGINT NOT NULL COMMENT 'ID of the entity',
    public_id VARCHAR(255) NULL COMMENT 'Cloudinary public_id (for delete operations)',
    file_data BLOB NULL COMMENT 'File data (for upload operations)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Number of retry attempts',
    last_error TEXT NULL COMMENT 'Last error message from Cloudinary',
    next_retry_at TIMESTAMP NOT NULL COMMENT 'Next scheduled retry time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_next_retry (next_retry_at),
    INDEX idx_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify migration success
SELECT 'Migration completed successfully' AS status;
