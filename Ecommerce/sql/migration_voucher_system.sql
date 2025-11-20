-- Migration script for Voucher System
-- Cập nhật bảng vouchers với các trường mới

ALTER TABLE `vouchers` 
ADD COLUMN `code` VARCHAR(50) NOT NULL UNIQUE AFTER `id`,
ADD COLUMN `description` TEXT NOT NULL AFTER `code`,
ADD COLUMN `shop_id` BIGINT NOT NULL AFTER `description`,
ADD COLUMN `discount_type` VARCHAR(20) NOT NULL AFTER `shop_id`,
ADD COLUMN `discount_value` DECIMAL(10,2) NOT NULL AFTER `discount_type`,
ADD COLUMN `min_order_value` DECIMAL(10,2) AFTER `discount_value`,
ADD COLUMN `max_discount_amount` DECIMAL(10,2) AFTER `min_order_value`,
ADD COLUMN `start_date` DATETIME NOT NULL AFTER `max_discount_amount`,
ADD COLUMN `end_date` DATETIME NOT NULL AFTER `start_date`,
ADD COLUMN `usage_limit` INT NOT NULL AFTER `end_date`,
ADD COLUMN `used_count` INT NOT NULL DEFAULT 0 AFTER `usage_limit`,
ADD COLUMN `applicable_type` VARCHAR(30) NOT NULL AFTER `used_count`,
ADD COLUMN `top_buyers_count` INT AFTER `applicable_type`,
-- Use string-backed status to match JPA enum storage (Vouchers.Status)
ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER `top_buyers_count`,
ADD COLUMN `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `status`,
ADD CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops`(`id`) ON DELETE CASCADE;

-- Tạo bảng voucher_products (quan hệ nhiều-nhiều giữa voucher và product)
CREATE TABLE IF NOT EXISTS `voucher_products` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voucher_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_product` (`voucher_id`, `product_id`),
  CONSTRAINT `fk_vp_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vp_product` FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Tạo bảng voucher_users (quan hệ nhiều-nhiều giữa voucher và user)
CREATE TABLE IF NOT EXISTS `voucher_users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voucher_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_user` (`voucher_id`, `user_id`),
  CONSTRAINT `fk_vu_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vu_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Tạo index cho hiệu suất
CREATE INDEX `idx_voucher_code` ON `vouchers`(`code`);
CREATE INDEX `idx_voucher_shop` ON `vouchers`(`shop_id`);
CREATE INDEX `idx_voucher_dates` ON `vouchers`(`start_date`, `end_date`);
CREATE INDEX `idx_voucher_status` ON `vouchers`(`status`);
