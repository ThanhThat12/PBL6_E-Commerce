-- ============================================================================
-- Seller Registration Feature - Phase 1 (COD only)
-- Adds KYC, payment, and review tracking fields to shops table
-- ============================================================================

-- ========== Contact Info ==========
ALTER TABLE shops ADD COLUMN IF NOT EXISTS shop_phone VARCHAR(15) NULL COMMENT 'Shop contact phone number';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS shop_email VARCHAR(255) NULL COMMENT 'Shop contact email';

-- ========== KYC - Identity Verification ==========
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_number VARCHAR(20) NULL COMMENT 'CMND (9 digits) or CCCD (12 digits)';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_front_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for front of ID card';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_front_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public ID for front of ID card';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_back_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for back of ID card';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_back_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public ID for back of ID card';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS selfie_with_id_url VARCHAR(500) NULL COMMENT 'Cloudinary URL for selfie holding ID card';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS selfie_with_id_public_id VARCHAR(255) NULL COMMENT 'Cloudinary public ID for selfie';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS id_card_name VARCHAR(100) NULL COMMENT 'Full name as shown on ID card';

-- ========== Payment Methods - Phase 1: COD only ==========
ALTER TABLE shops ADD COLUMN IF NOT EXISTS accept_cod BOOLEAN DEFAULT TRUE COMMENT 'Whether shop accepts COD payment';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS cod_fee_percentage DECIMAL(5,2) DEFAULT 2.00 COMMENT 'COD fee percentage charged by shop';

-- ========== Review Tracking (Admin approval workflow) ==========
ALTER TABLE shops ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP NULL COMMENT 'When seller submitted registration';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP NULL COMMENT 'When admin reviewed the application';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS reviewed_by BIGINT NULL COMMENT 'Admin user ID who reviewed the application';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(1000) NULL COMMENT 'Reason for rejection (if status=REJECTED)';

-- ========== Shop Rating ==========
ALTER TABLE shops ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2) DEFAULT 5.00 COMMENT 'Shop average rating (1.00 - 5.00)';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS review_count INT DEFAULT 0 COMMENT 'Total number of reviews for shop';

-- ========== Update ShopStatus Enum (if needed) ==========
-- The PENDING and REJECTED statuses should be added to the enum in Java code
-- MySQL ENUM: ALTER TABLE shops MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'REJECTED', 'CLOSED') DEFAULT 'PENDING';

-- ========== Indexes for Performance ==========
CREATE INDEX IF NOT EXISTS idx_shops_submitted_at ON shops(submitted_at);
CREATE INDEX IF NOT EXISTS idx_shops_reviewed_by ON shops(reviewed_by);
CREATE INDEX IF NOT EXISTS idx_shops_accept_cod ON shops(accept_cod);
CREATE INDEX IF NOT EXISTS idx_shops_status_submitted ON shops(status, submitted_at);

-- ========== Foreign Key for reviewed_by ==========
-- Only add if not exists
-- ALTER TABLE shops ADD CONSTRAINT fk_shops_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================================
-- Verification Query
-- ============================================================================
-- Run this to verify columns were added:
-- DESCRIBE shops;
-- SHOW COLUMNS FROM shops LIKE 'id_card%';
-- SHOW COLUMNS FROM shops LIKE 'submitted%';
-- SHOW COLUMNS FROM shops LIKE 'reviewed%';
