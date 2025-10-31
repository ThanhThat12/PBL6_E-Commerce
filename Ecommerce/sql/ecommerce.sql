-- =====================================================
-- COMPLETE ECOMMERCE DATABASE WITH REVIEWS
-- Date: October 30, 2025
-- Version: 2.1 (includes Product Reviews)
-- Compatible: MySQL 5.7+ / MariaDB 10.2+
-- =====================================================


-- ⚠️ This will DROP and CREATE the entire database!
-- Use this for fresh installation only!


SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";
SET FOREIGN_KEY_CHECKS = 0;


-- =====================================================
-- DATABASE CREATION
-- =====================================================


DROP DATABASE IF EXISTS `ecommerce1`;
CREATE DATABASE `ecommerce1`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;


USE `ecommerce1`;


-- =====================================================
-- TABLE: users
-- =====================================================


CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(60) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone_number` varchar(100) DEFAULT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `facebook_id` varchar(100) DEFAULT NULL,
  `google_id` varchar(100) DEFAULT NULL,
  `role` tinyint(4) NOT NULL COMMENT '0=BUYER, 1=SELLER, 2=ADMIN',
  `activated` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_username` (`username`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `unique_phone` (`phone_number`),
  UNIQUE KEY `unique_facebook` (`facebook_id`),
  UNIQUE KEY `unique_google` (`google_id`),
  KEY `idx_google_id` (`google_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User accounts';


-- Sample users
INSERT INTO `users` (`username`, `password`, `email`, `role`, `activated`) VALUES
('admin', '$2a$10$Aq94o/k0q6DuE.A9cIAkeewuvnwybXzbiZY0vGOPNSSmVtaRU4HaK', 'admin@ecommerce.com', 2, 1),
('seller1', '$2a$10$eDx8DBq.c5reZbitaN3Rg.w99GaI.SOydC7U0x/ptIYe0NPHLtp5y', 'seller1@ecommerce.com', 1, 1),
('buyer1', '$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu', 'buyer1@ecommerce.com', 0, 1);


-- =====================================================
-- TABLE: refresh_tokens
-- =====================================================


CREATE TABLE `refresh_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token` varchar(128) NOT NULL COMMENT 'UUID refresh token',
  `user_id` bigint(20) NOT NULL,
  `expiry_date` datetime(6) NOT NULL COMMENT 'Token expiration time',
  `revoked` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Token đã bị thu hồi?',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expiry` (`expiry_date`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refresh tokens for JWT authentication';


-- =====================================================
-- TABLE: addresses
-- =====================================================


CREATE TABLE `addresses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `label` varchar(100) DEFAULT NULL COMMENT 'Nhà riêng, Văn phòng, etc.',
  `full_address` varchar(500) DEFAULT NULL COMMENT 'Số nhà, tên đường',
  `province_id` int(11) DEFAULT NULL COMMENT 'GHN Province ID',
  `district_id` int(11) DEFAULT NULL COMMENT 'GHN District ID',
  `ward_code` varchar(20) DEFAULT NULL COMMENT 'GHN Ward Code',
  `province_name` varchar(100) DEFAULT NULL COMMENT 'Tên tỉnh/thành phố',
  `district_name` varchar(100) DEFAULT NULL COMMENT 'Tên quận/huyện',
  `ward_name` varchar(100) DEFAULT NULL COMMENT 'Tên phường/xã',
  `contact_phone` varchar(30) DEFAULT NULL,
  `primary_address` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_primary` (`primary_address`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User delivery addresses with GHN location data';


-- =====================================================
-- TABLE: shops
-- =====================================================


CREATE TABLE `shops` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `address` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL COMMENT 'SĐT liên hệ',
  `email` varchar(100) DEFAULT NULL COMMENT 'Email shop',
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `rating` decimal(3,2) DEFAULT 5.00 COMMENT 'Rating shop (0-5)',
  `review_count` int(11) DEFAULT 0 COMMENT 'Số reviews',
  `owner_id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_shop_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Seller shops';


-- =====================================================
-- TABLE: categories
-- =====================================================


CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product categories';


-- Sample categories
INSERT INTO `categories` (`name`) VALUES
('Accessories'),
('Bags'),
('Clothing'),
('Fitness Equipment'),
('Shoes'),
('Sports Equipment');


-- =====================================================
-- TABLE: products
-- =====================================================


CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `main_image` varchar(500) DEFAULT NULL,
  `product_condition` enum('NEW','USED') DEFAULT 'NEW',
  `is_active` bit(1) NOT NULL DEFAULT b'1',
  `rating` decimal(3,2) DEFAULT 0.00 COMMENT 'Average rating (0-5)',
  `review_count` int(11) DEFAULT 0 COMMENT 'Số lượng reviews',
  `sold_count` int(11) DEFAULT 0 COMMENT 'Đã bán bao nhiêu',
  `category_id` bigint(20) NOT NULL,
  `shop_id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_active` (`is_active`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_product_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Products';


-- =====================================================
-- TABLE: product_images
-- =====================================================


CREATE TABLE `product_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `color` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product images';


-- =====================================================
-- TABLE: product_attributes
-- =====================================================


CREATE TABLE `product_attributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'Size, Color, Material, etc.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product attribute types';


-- Sample attributes
INSERT INTO `product_attributes` (`name`) VALUES
('Size'),
('Color'),
('Material');


-- =====================================================
-- TABLE: product_variants
-- =====================================================


CREATE TABLE `product_variants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `sku` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_sku` (`sku`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_variant_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product variants (size, color combinations)';


-- =====================================================
-- TABLE: product_variant_values
-- =====================================================


CREATE TABLE `product_variant_values` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `variant_id` bigint(20) NOT NULL,
  `product_attribute_id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL COMMENT 'M, Red, Cotton, etc.',
  PRIMARY KEY (`id`),
  KEY `idx_variant` (`variant_id`),
  KEY `idx_attribute` (`product_attribute_id`),
  CONSTRAINT `fk_variantvalue_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_variantvalue_attribute` FOREIGN KEY (`product_attribute_id`) REFERENCES `product_attributes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Variant attribute values';


-- =====================================================
-- TABLE: carts
-- =====================================================


CREATE TABLE `carts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_user_updated` (`user_id`,`updated_at`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shopping carts';


-- =====================================================
-- TABLE: cart_items
-- =====================================================


CREATE TABLE `cart_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cart_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `variant_id` bigint(20) DEFAULT NULL COMMENT 'Variant trong cart',
  `quantity` int(11) NOT NULL,
  `price_snapshot` decimal(10,2) DEFAULT NULL COMMENT 'Giá khi thêm vào cart',
  `added_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_variant` (`cart_id`,`variant_id`),
  KEY `idx_cart` (`cart_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_variant` (`variant_id`),
  KEY `idx_cart_product` (`cart_id`,`product_id`),
  KEY `idx_added_at` (`added_at`),
  CONSTRAINT `fk_cartitem_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cartitem_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_cartitem_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cart items';


-- =====================================================
-- TABLE: vouchers
-- =====================================================


CREATE TABLE `vouchers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  `min_order_value` decimal(10,2) DEFAULT 0.00,
  `max_uses` int(11) DEFAULT NULL,
  `valid_from` datetime DEFAULT NULL,
  `valid_to` datetime DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','EXPIRED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_code` (`code`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_code` (`code`),
  CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Discount vouchers';


-- =====================================================
-- TABLE: wallets
-- =====================================================


CREATE TABLE `wallets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `balance` decimal(15,2) DEFAULT 0.00,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user` (`user_id`),
  CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User wallets for COD and refunds';


-- =====================================================
-- TABLE: orders
-- =====================================================


CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `shop_id` bigint(20) DEFAULT NULL,
  `voucher_id` bigint(20) DEFAULT NULL,
  `shipment_id` bigint(20) DEFAULT NULL,
  `total_amount` decimal(15,2) DEFAULT NULL,
  `method` enum('COD','MOMO') DEFAULT 'COD',
  `status` enum('PENDING','PROCESSING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  `payment_status` enum('UNPAID','PAID','FAILED') DEFAULT 'UNPAID' COMMENT 'Trạng thái thanh toán',
  `momo_trans_id` varchar(100) DEFAULT NULL COMMENT 'Mã giao dịch MOMO',
  `paid_at` datetime DEFAULT NULL COMMENT 'Thời điểm thanh toán',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_shipment` (`shipment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_order_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  CONSTRAINT `fk_order_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`),
  CONSTRAINT `fk_order_shipment` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Orders';


-- =====================================================
-- TABLE: order_items
-- =====================================================


CREATE TABLE `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `variant_id` bigint(20) DEFAULT NULL COMMENT 'Variant đã đặt',
  `variant_name` varchar(200) DEFAULT NULL COMMENT 'Tên variant: Size M, Red',
  `quantity` int(11) NOT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_variant` (`variant_id`),
  CONSTRAINT `fk_orderitem_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_orderitem_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_orderitem_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Order items';


-- =====================================================
-- TABLE: shipments
-- =====================================================


CREATE TABLE `shipments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `ghn_order_code` varchar(100) DEFAULT NULL,
  `shipping_fee` decimal(10,2) DEFAULT 0.00,
  `service_type` varchar(100) DEFAULT NULL,
  `expected_delivery` datetime DEFAULT NULL,
  `status` enum('PENDING','PICKED_UP','IN_TRANSIT','DELIVERED','CANCELLED','RETURNED') DEFAULT 'PENDING',
  `receiver_name` varchar(255) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `receiver_address` text NOT NULL,
  `province` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `tracking_url` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_ghn_code` (`ghn_order_code`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `fk_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shipments';


-- =====================================================
-- TABLE: wallet_transactions
-- =====================================================


CREATE TABLE `wallet_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `wallet_id` bigint(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `type` enum('DEPOSIT','WITHDRAWAL','REFUND','ORDER_PAYMENT') NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `related_order_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wallet` (`wallet_id`),
  KEY `idx_order` (`related_order_id`),
  CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transaction_order` FOREIGN KEY (`related_order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Wallet transactions';


-- =====================================================
-- TABLE: refunds
-- =====================================================


CREATE TABLE `refunds` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `status` enum('REQUESTED','APPROVED','REJECTED','COMPLETED') DEFAULT 'REQUESTED',
  `transaction_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_transaction` (`transaction_id`),
  CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_refund_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `wallet_transactions` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refunds';


-- =====================================================
-- TABLE: platform_fees
-- =====================================================


CREATE TABLE `platform_fees` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `seller_id` bigint(20) NOT NULL,
  `fee_percent` decimal(5,2) DEFAULT 5.00,
  `fee_amount` decimal(15,2) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_seller` (`seller_id`),
  CONSTRAINT `fk_fee_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_fee_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Platform fees';


-- =====================================================
-- TABLE: user_vouchers
-- =====================================================


CREATE TABLE `user_vouchers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `voucher_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_voucher_order` (`user_id`,`order_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `fk_uservoucher_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`),
  CONSTRAINT `fk_uservoucher_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_uservoucher_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User voucher usage';


-- =====================================================
-- TABLE: verifications (OTP)
-- =====================================================


CREATE TABLE `verifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) DEFAULT NULL COMMENT 'Email or phone',
  `otp` varchar(255) DEFAULT NULL,
  `verified` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `expiry_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTP verifications';


-- =====================================================
-- TABLE: product_reviews (NEW - ESSENTIAL FEATURE)
-- =====================================================


CREATE TABLE `product_reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL COMMENT 'Chỉ review được khi đã mua',
  `rating` tinyint(1) NOT NULL COMMENT '1-5 stars',
  `comment` text DEFAULT NULL COMMENT 'Nội dung review',
  `images` text DEFAULT NULL COMMENT 'JSON array URLs ảnh upload',
  `seller_response` text DEFAULT NULL COMMENT 'Shop trả lời review',
  `seller_response_date` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product` (`user_id`, `product_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product reviews';

CREATE TABLE IF NOT EXISTS `payment_transactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `request_id` varchar(50) NOT NULL UNIQUE COMMENT 'Unique request ID',
  `order_id_momo` varchar(50) DEFAULT NULL COMMENT 'MoMo order ID',
  `amount` decimal(19,2) NOT NULL COMMENT 'Payment amount',
  `trans_id` varchar(50) DEFAULT NULL COMMENT 'MoMo transaction ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, EXPIRED, REFUNDED',
  `result_code` int(11) DEFAULT NULL COMMENT 'MoMo result code',
  `message` text DEFAULT NULL COMMENT 'Response message',
  `response_time` datetime DEFAULT NULL COMMENT 'Response timestamp',
  `signature` varchar(500) DEFAULT NULL COMMENT 'MoMo signature',
  `payment_method` varchar(50) DEFAULT 'MOMO' COMMENT 'Payment method',
  `pay_type` varchar(50) DEFAULT NULL COMMENT 'Payment type',
  `pay_url` text DEFAULT NULL COMMENT 'Payment URL',
  `deep_link` text DEFAULT NULL COMMENT 'Deep link for mobile',
  `qr_code_url` text DEFAULT NULL COMMENT 'QR code URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_request_id` (`request_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_trans_id` (`trans_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_momo` (`order_id_momo`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MoMo payment transactions';
-- =====================================================
-- TRIGGERS: AUTO-UPDATE RATING
-- =====================================================


DELIMITER //


-- Trigger 1: Update product rating when review added
DROP TRIGGER IF EXISTS `update_product_rating_insert` //
CREATE TRIGGER `update_product_rating_insert`
AFTER INSERT ON `product_reviews`
FOR EACH ROW
BEGIN
  UPDATE `products`
  SET
    `rating` = (SELECT AVG(`rating`) FROM `product_reviews` WHERE `product_id` = NEW.product_id),
    `review_count` = (SELECT COUNT(*) FROM `product_reviews` WHERE `product_id` = NEW.product_id)
  WHERE `id` = NEW.product_id;
END //


-- Trigger 2: Update product rating when review updated
DROP TRIGGER IF EXISTS `update_product_rating_update` //
CREATE TRIGGER `update_product_rating_update`
AFTER UPDATE ON `product_reviews`
FOR EACH ROW
BEGIN
  UPDATE `products`
  SET
    `rating` = (SELECT AVG(`rating`) FROM `product_reviews` WHERE `product_id` = NEW.product_id),
    `review_count` = (SELECT COUNT(*) FROM `product_reviews` WHERE `product_id` = NEW.product_id)
  WHERE `id` = NEW.product_id;
END //


-- Trigger 3: Update product rating when review deleted
DROP TRIGGER IF EXISTS `update_product_rating_delete` //
CREATE TRIGGER `update_product_rating_delete`
AFTER DELETE ON `product_reviews`
FOR EACH ROW
BEGIN
  UPDATE `products`
  SET
    `rating` = COALESCE((SELECT AVG(`rating`) FROM `product_reviews` WHERE `product_id` = OLD.product_id), 0),
    `review_count` = (SELECT COUNT(*) FROM `product_reviews` WHERE `product_id` = OLD.product_id)
  WHERE `id` = OLD.product_id;
END //


-- Trigger 4: Update shop rating from product reviews
DROP TRIGGER IF EXISTS `update_shop_rating` //
CREATE TRIGGER `update_shop_rating`
AFTER INSERT ON `product_reviews`
FOR EACH ROW
BEGIN
  DECLARE shop_id_val bigint(20);
 
  SELECT `shop_id` INTO shop_id_val FROM `products` WHERE `id` = NEW.product_id;
 
  UPDATE `shops`
  SET
    `rating` = (
      SELECT AVG(pr.rating)
      FROM `product_reviews` pr
      JOIN `products` p ON pr.product_id = p.id
      WHERE p.shop_id = shop_id_val
    ),
    `review_count` = (
      SELECT COUNT(*)
      FROM `product_reviews` pr
      JOIN `products` p ON pr.product_id = p.id
      WHERE p.shop_id = shop_id_val
    )
  WHERE `id` = shop_id_val;
END //


DELIMITER ;


-- =====================================================
-- FINAL SETUP
-- =====================================================


SET FOREIGN_KEY_CHECKS = 1;


-- Verification
SELECT 'DATABASE CREATED SUCCESSFULLY!' as status;
SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'ecommerce_v2';


-- =====================================================
-- SUMMARY
-- =====================================================
/*
✅ Complete database with all tables
✅ Product Reviews feature included
✅ Auto-update rating triggers
✅ Sample data (users, categories)
✅ Foreign keys and indexes
✅ COD + MOMO payment support
✅ Variant tracking in cart & orders
✅ GHN shipping integration ready
✅ Wallet system for refunds
✅ Compatible: MySQL 5.7+ / MariaDB 10.2+


TABLES CREATED (24 total):
1. users
2. refresh_tokens
3. addresses
4. shops
5. categories
6. products
7. product_images
8. product_attributes
9. product_variants
10. product_variant_values
11. carts
12. cart_items
13. vouchers
14. wallets
15. orders
16. order_items
17. shipments
18. wallet_transactions
19. refunds
20. platform_fees
21. user_vouchers
22. verifications
23. product_reviews ⭐ NEW
24. (4 triggers for auto-rating)


DEFAULT LOGIN:
- Admin: admin@ecommerce.com / password: admin123
- Seller: seller1@ecommerce.com / password: seller123
- Buyer: buyer1@ecommerce.com / password: buyer123
*/



