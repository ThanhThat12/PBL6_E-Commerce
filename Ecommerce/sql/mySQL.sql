-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: ecommerce1
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `label` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Nhà riêng, Văn phòng, etc.',
  `full_address` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Số nhà, tên đường',
  `province_id` int DEFAULT NULL COMMENT 'GHN Province ID',
  `district_id` int DEFAULT NULL COMMENT 'GHN District ID',
  `ward_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `province_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên tỉnh/thành phố',
  `district_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên quận/huyện',
  `ward_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên phường/xã',
  `contact_phone` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `primary_address` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `contact_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_primary` (`primary_address`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User delivery addresses with GHN location data';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (1,6,'Nguyen Van A','123 Random Street',248,1966,'180315','Bắc Giang','Huyện Lạng Giang','Xã Tân Hưng','0923992001',_binary '','2025-11-06 00:04:24.157501',NULL),(2,5,'Nguyen Van A','1123fff',253,1946,'600307','Bạc Liêu','Huyện Hồng Dân','Xã Ninh Thạnh Lợi A','0923992001',_binary '\0','2025-11-06 14:10:57.073263',NULL);
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL COMMENT 'Variant trong cart',
  `quantity` int NOT NULL,
  `price_snapshot` decimal(10,2) DEFAULT NULL COMMENT 'Giá khi thêm vào cart',
  `added_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `product_variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_variant` (`cart_id`,`variant_id`),
  KEY `idx_cart` (`cart_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_variant` (`variant_id`),
  KEY `idx_cart_product` (`cart_id`,`product_id`),
  KEY `idx_added_at` (`added_at`),
  KEY `FKn1s4l7h0vm4o259wpu7ft0y2y` (`product_variant_id`),
  CONSTRAINT `fk_cartitem_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cartitem_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_cartitem_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKn1s4l7h0vm4o259wpu7ft0y2y` FOREIGN KEY (`product_variant_id`) REFERENCES `product_variants` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cart items';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,1,1,1,2,150000.00,'2025-11-02 07:13:56.002221','2025-11-02 07:13:56.002221',NULL),(2,2,3,4,1,90000.00,'2025-11-02 07:13:56.002221','2025-11-02 07:13:56.002221',NULL),(3,3,NULL,NULL,1,NULL,'2025-11-04 10:15:20.256307','2025-11-04 10:15:20.256307',3);
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_user_updated` (`user_id`,`updated_at`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shopping carts';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,3,'2025-11-02 07:13:55.999968','2025-11-02 07:13:55.999968'),(2,4,'2025-11-02 07:13:55.999968','2025-11-02 07:13:55.999968'),(3,5,'2025-11-04 10:14:49.478146','2025-11-04 10:14:49.488620'),(4,6,'2025-11-05 21:17:16.211226','2025-11-05 21:17:16.215568');
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product categories';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Accessories'),(2,'Bags'),(3,'Clothing'),(4,'Fitness Equipment'),(5,'Shoes'),(6,'Sports Equipment');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL COMMENT 'Variant đã đặt',
  `variant_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên variant: Size M, Red',
  `quantity` int NOT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_variant` (`variant_id`),
  CONSTRAINT `fk_orderitem_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_orderitem_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_orderitem_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Order items';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,1,'Black',2,150000.00),(4,4,2,3,'BH-BLACK-001',1,650000.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `shop_id` bigint DEFAULT NULL,
  `voucher_id` bigint DEFAULT NULL,
  `shipment_id` bigint DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `method` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` enum('PENDING','PROCESSING','COMPLETED','CANCELLED') COLLATE utf8mb4_general_ci DEFAULT 'PENDING',
  `payment_status` enum('UNPAID','PAID','FAILED') COLLATE utf8mb4_general_ci DEFAULT 'UNPAID' COMMENT 'Trạng thái thanh toán',
  `momo_trans_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  CONSTRAINT `fk_order_shipment` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`id`),
  CONSTRAINT `fk_order_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_order_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Orders';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,2,1,NULL,1,300000.00,'MOMO','PROCESSING','PAID','MOMO-TRX-001','2025-11-02 07:13:56','2025-11-02 07:13:56.014790','2025-11-02 07:13:56.031510'),(4,6,1,NULL,4,680000.00,'COD','PENDING','UNPAID',NULL,NULL,'2025-11-06 00:14:50.213173','2025-11-06 00:14:50.213173');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_transactions`
--

DROP TABLE IF EXISTS `payment_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `request_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Unique request ID',
  `order_id_momo` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo order ID',
  `amount` decimal(19,2) NOT NULL COMMENT 'Payment amount',
  `trans_id` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo transaction ID',
  `status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',
  `result_code` int DEFAULT NULL COMMENT 'MoMo result code',
  `message` text COLLATE utf8mb4_general_ci COMMENT 'Response message',
  `signature` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo signature',
  `pay_url` text COLLATE utf8mb4_general_ci COMMENT 'Payment URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `request_id` (`request_id`),
  UNIQUE KEY `unique_request_id` (`request_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_trans_id` (`trans_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MoMo payment transactions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transactions`
--

LOCK TABLES `payment_transactions` WRITE;
/*!40000 ALTER TABLE `payment_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `platform_fees`
--

DROP TABLE IF EXISTS `platform_fees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_fees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `fee_percent` decimal(38,2) DEFAULT NULL,
  `fee_amount` decimal(38,2) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_seller` (`seller_id`),
  CONSTRAINT `fk_fee_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_fee_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Platform fees';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platform_fees`
--

LOCK TABLES `platform_fees` WRITE;
/*!40000 ALTER TABLE `platform_fees` DISABLE KEYS */;
INSERT INTO `platform_fees` VALUES (1,1,2,5.00,15000.00,'2025-11-02 07:13:56');
/*!40000 ALTER TABLE `platform_fees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_attributes`
--

DROP TABLE IF EXISTS `product_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attributes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Size, Color, Material, etc.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product attribute types';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_attributes`
--

LOCK TABLES `product_attributes` WRITE;
/*!40000 ALTER TABLE `product_attributes` DISABLE KEYS */;
INSERT INTO `product_attributes` VALUES (1,'Size'),(2,'Color'),(3,'Material');
/*!40000 ALTER TABLE `product_attributes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `color` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product images';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (1,1,'mouse_1.jpg','black'),(2,1,'mouse_2.jpg','white'),(3,2,'headphone_1.jpg','black'),(4,2,'headphone_2.jpg','blue'),(5,3,'mug_1.jpg','green'),(6,3,'mug_2.jpg','beige');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_reviews`
--

DROP TABLE IF EXISTS `product_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL COMMENT 'Chỉ review được khi đã mua',
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_general_ci COMMENT 'Nội dung review',
  `images` text COLLATE utf8mb4_general_ci COMMENT 'JSON array URLs ảnh upload',
  `seller_response` text COLLATE utf8mb4_general_ci COMMENT 'Shop trả lời review',
  `seller_response_date` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `verified_purchase` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product` (`user_id`,`product_id`),
  UNIQUE KEY `unique_user_product_review` (`user_id`,`product_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product reviews';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_reviews`
--

LOCK TABLES `product_reviews` WRITE;
/*!40000 ALTER TABLE `product_reviews` DISABLE KEYS */;
INSERT INTO `product_reviews` VALUES (1,1,4,1,5,'Excellent mouse, very responsive and battery lasts long.','[\"rev1_img1.jpg\"]',NULL,NULL,'2025-11-02 07:13:56','2025-11-02 07:13:56',_binary ''),(2,3,5,NULL,4,'Nice mug, color slightly different but good quality.','[\"rev2_img1.jpg\"]','Thanks for the feedback!','2025-11-02 07:13:56','2025-11-02 07:13:56','2025-11-02 07:13:56',_binary '');
/*!40000 ALTER TABLE `product_reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variant_values`
--

DROP TABLE IF EXISTS `product_variant_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variant_values` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `variant_id` bigint NOT NULL,
  `product_attribute_id` bigint NOT NULL,
  `value` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'M, Red, Cotton, etc.',
  PRIMARY KEY (`id`),
  KEY `idx_variant` (`variant_id`),
  KEY `idx_attribute` (`product_attribute_id`),
  CONSTRAINT `fk_variantvalue_attribute` FOREIGN KEY (`product_attribute_id`) REFERENCES `product_attributes` (`id`),
  CONSTRAINT `fk_variantvalue_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Variant attribute values';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variant_values`
--

LOCK TABLES `product_variant_values` WRITE;
/*!40000 ALTER TABLE `product_variant_values` DISABLE KEYS */;
INSERT INTO `product_variant_values` VALUES (1,1,2,'Black'),(2,2,2,'White'),(3,3,2,'Black'),(4,4,2,'Green'),(5,5,2,'Beige');
/*!40000 ALTER TABLE `product_variant_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `sku` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_sku` (`sku`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_variant_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product variants (size, color combinations)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variants`
--

LOCK TABLES `product_variants` WRITE;
/*!40000 ALTER TABLE `product_variants` DISABLE KEYS */;
INSERT INTO `product_variants` VALUES (1,1,'WM-BLACK-001',150000.00,50),(2,1,'WM-WHITE-001',155000.00,40),(3,2,'BH-BLACK-001',650000.00,23),(4,3,'MUG-GREEN-001',90000.00,120),(5,3,'MUG-BEIGE-001',95000.00,80);
/*!40000 ALTER TABLE `product_variants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `main_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `product_condition` enum('NEW','USED') COLLATE utf8mb4_general_ci DEFAULT 'NEW',
  `is_active` bit(1) NOT NULL DEFAULT b'1',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT 'Average rating (0-5)',
  `review_count` int DEFAULT '0' COMMENT 'Số lượng reviews',
  `sold_count` int DEFAULT '0' COMMENT 'Đã bán bao nhiêu',
  `category_id` bigint NOT NULL,
  `shop_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `height_cm` int DEFAULT NULL,
  `length_cm` int DEFAULT NULL,
  `weight_grams` int DEFAULT NULL,
  `width_cm` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_active` (`is_active`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_product_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Products';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Wireless Mouse','Ergonomic 2.4GHz wireless mouse',150000.00,'mouse_main.jpg','NEW',_binary '',5.00,1,120,1,1,'2025-11-02 07:13:55.980903','2025-11-02 07:13:56.058632',NULL,NULL,NULL,NULL),(2,'Bluetooth Headphones','Over-ear noise-cancelling headphones',650000.00,'headphone_main.jpg','NEW',_binary '',4.60,15,80,1,1,'2025-11-02 07:13:55.980903','2025-11-02 07:13:55.980903',NULL,NULL,NULL,NULL),(3,'Ceramic Coffee Mug','Handmade ceramic mug 300ml',90000.00,'ecommerce/avatars/ecommerce/avatars/user_103692_f4f9174a-0d56-4a51-ab6d-8d86c7cda861','NEW',_binary '',4.00,1,200,1,2,'2025-11-02 07:13:55.980903','2025-11-06 13:51:16.796313',NULL,NULL,NULL,NULL),(4,'Giày thể thao','Giày thể thao',299000.00,NULL,'NEW',_binary '\0',0.00,0,0,5,3,'2025-11-07 19:51:13.693225','2025-11-07 19:51:13.721643',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID refresh token',
  `user_id` bigint NOT NULL,
  `expiry_date` datetime(6) NOT NULL COMMENT 'Token expiration time',
  `revoked` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Token đã bị thu hồi?',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expiry` (`expiry_date`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refresh tokens for JWT authentication';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
INSERT INTO `refresh_tokens` VALUES (32,'8fd91c3c-1bd9-44ae-84cd-f0e5c008de5c',5,'2025-12-06 16:49:16.085672',_binary '\0','2025-11-06 16:49:16.085672'),(40,'281e4805-78d4-4d57-bf70-6906bcb2d89d',6,'2025-12-07 12:43:55.525932',_binary '\0','2025-11-07 12:43:55.525932');
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refunds`
--

DROP TABLE IF EXISTS `refunds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `status` enum('REQUESTED','APPROVED','REJECTED','COMPLETED') COLLATE utf8mb4_general_ci DEFAULT 'REQUESTED',
  `transaction_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_transaction` (`transaction_id`),
  CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_refund_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `wallet_transactions` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refunds';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refunds`
--

LOCK TABLES `refunds` WRITE;
/*!40000 ALTER TABLE `refunds` DISABLE KEYS */;
INSERT INTO `refunds` VALUES (1,1,'Product damaged',150000.00,'REQUESTED',NULL,'2025-11-02 07:13:56','2025-11-02 07:13:56');
/*!40000 ALTER TABLE `refunds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipments`
--

DROP TABLE IF EXISTS `shipments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `ghn_order_code` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `shipping_fee` decimal(38,2) DEFAULT NULL,
  `service_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `expected_delivery` datetime DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `receiver_phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `receiver_address` text COLLATE utf8mb4_general_ci NOT NULL,
  `province` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `district` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ward` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tracking_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ghn_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_ghn_code` (`ghn_order_code`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `fk_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shipments';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipments`
--

LOCK TABLES `shipments` WRITE;
/*!40000 ALTER TABLE `shipments` DISABLE KEYS */;
INSERT INTO `shipments` VALUES (1,1,'GHN0001',25000.00,'Standard','2025-11-05 07:13:56','PICKED_UP','Buyer One','0900000004','No.10, Le Loi, Da Nang','Da Nang','Hai Chau','Ward 1','https://ghn.example/track/GHN0001','2025-11-02 07:13:56','2025-11-02 07:13:56',NULL),(4,4,'GHN0002',30000.00,'truck','2025-11-01 16:59:59','CREATED','Nguyễn Văn A','0923992001','123 Random Street, Xã Tân Hưng, Huyện Lạng Giang, Bắc Giang','Bắc Giang','Huyện Lạng Giang','Xã Tân Hưng',NULL,'2025-11-06 00:14:51',NULL,'{\"code\":200,\"code_message_value\":\"\",\"data\":{\"order_code\":\"L4GM8N\",\"sort_code\":\"0-000-0-00\",\"trans_type\":\"truck\",\"ward_encode\":\"\",\"district_encode\":\"\",\"fee\":{\"main_service\":22000,\"insurance\":0,\"cod_fee\":0,\"station_do\":0,\"station_pu\":0,\"return\":0,\"r2s\":0,\"return_again\":0,\"coupon\":0,\"document_return\":0,\"double_check\":0,\"double_check_deliver\":0,\"pick_remote_areas_fee\":0,\"deliver_remote_areas_fee\":0,\"pick_remote_areas_fee_return\":0,\"deliver_remote_areas_fee_return\":0,\"cod_failed_fee\":0},\"total_fee\":22000,\"expected_delivery_time\":\"2025-11-01T16:59:59Z\",\"operation_partner\":\"\"},\"message\":\"Success\",\"message_display\":\"Tạo đơn hàng thành công. Mã đơn hàng: L4GM8N\"}');
/*!40000 ALTER TABLE `shipments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shops`
--

DROP TABLE IF EXISTS `shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shops` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SĐT liên hệ',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Email shop',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_general_ci NOT NULL,
  `rating` decimal(3,2) DEFAULT '5.00' COMMENT 'Rating shop (0-5)',
  `review_count` int DEFAULT '0' COMMENT 'Số reviews',
  `owner_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `pickup_address_id` bigint DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_status` (`status`),
  KEY `fk_shop_pickup_address` (`pickup_address_id`),
  CONSTRAINT `fk_shop_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_pickup_address` FOREIGN KEY (`pickup_address_id`) REFERENCES `addresses` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Seller shops';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shops`
--

LOCK TABLES `shops` WRITE;
/*!40000 ALTER TABLE `shops` DISABLE KEYS */;
INSERT INTO `shops` VALUES (1,'TechZone','Electronics & Gadgets','0987000002','shop-tech@ecom.com','ACTIVE',5.00,1,2,'2025-11-02 07:13:55.973201',NULL,NULL),(2,'HomeStyle','Home & Kitchen Essentials','0987000003','shop-home@ecom.com','ACTIVE',4.00,1,3,'2025-11-02 07:13:55.973201',NULL,NULL),(3,'My Awesome Shop','Best products in town',NULL,NULL,'ACTIVE',5.00,0,6,'2025-11-05 22:40:29.936933',NULL,NULL);
/*!40000 ALTER TABLE `shops` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vouchers`
--

DROP TABLE IF EXISTS `user_vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `voucher_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_voucher_order` (`user_id`,`order_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `fk_uservoucher_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_uservoucher_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_uservoucher_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User voucher usage';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vouchers`
--

LOCK TABLES `user_vouchers` WRITE;
/*!40000 ALTER TABLE `user_vouchers` DISABLE KEYS */;
INSERT INTO `user_vouchers` VALUES (1,1,4,1,'2025-11-02 07:13:56');
/*!40000 ALTER TABLE `user_vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone_number` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `facebook_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `google_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` tinyint NOT NULL COMMENT '0=BUYER, 1=SELLER, 2=ADMIN',
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User accounts';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$Aq94o/k0q6DuE.A9cIAkeewuvnwybXzbiZY0vGOPNSSmVtaRU4HaK','admin@ecommerce.com',NULL,NULL,NULL,NULL,NULL,2,_binary '','2025-11-02 04:13:27.497749','2025-11-02 04:13:27.497749'),(2,'seller1','$2a$10$eDx8DBq.c5reZbitaN3Rg.w99GaI.SOydC7U0x/ptIYe0NPHLtp5y','seller1@ecommerce.com',NULL,NULL,NULL,NULL,NULL,1,_binary '','2025-11-02 04:13:27.497749','2025-11-02 04:13:27.497749'),(3,'buyer1','$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu','buyer1@ecommerce.com',NULL,NULL,NULL,NULL,NULL,0,_binary '','2025-11-02 04:13:27.497749','2025-11-02 04:13:27.497749'),(4,'buyer3','$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu','buyer3@ecommerce.com','0900000004','Buyer Three',NULL,NULL,NULL,2,_binary '','2025-11-02 07:13:55.946120','2025-11-02 07:13:55.946120'),(5,'huy','$2a$10$vkpyL7NvxkgodsKlgtt8juamD57OttQzB/0zISWP1JYJYYRX3rSRe','buinhathuy263@gmail.com','0900000005','Buyer Four','https://res.cloudinary.com/dejjhkhl1/image/upload/v1762310086/ecommerce/avatars/ecommerce/avatars/user_5_ee00b954-40ad-44d0-98a0-0f68b3bf645b.jpg',NULL,NULL,2,_binary '','2025-11-02 07:13:55.946120','2025-11-05 09:34:46.878511'),(6,'BakaCirno','$2a$10$vkpyL7NvxkgodsKlgtt8juamD57OttQzB/0zISWP1JYJYYRX3rSRe','funky@gmail.com','0987654321','Nguyễn Văn A',NULL,NULL,NULL,1,_binary '','2025-11-05 21:17:13.056549','2025-11-06 21:39:45.462126');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `variant_images`
--

DROP TABLE IF EXISTS `variant_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variant_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alt_text` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `display_order` int NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf92ibo1g13vjcvau47cwrr6ac` (`variant_id`),
  CONSTRAINT `FKf92ibo1g13vjcvau47cwrr6ac` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `variant_images`
--

LOCK TABLES `variant_images` WRITE;
/*!40000 ALTER TABLE `variant_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `variant_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verifications`
--

DROP TABLE IF EXISTS `verifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Email or phone',
  `otp` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `verified` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `expiry_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTP verifications';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verifications`
--

LOCK TABLES `verifications` WRITE;
/*!40000 ALTER TABLE `verifications` DISABLE KEYS */;
INSERT INTO `verifications` VALUES (1,'buyer1@ecommerce.com','123456',_binary '','2025-11-02 07:13:56.055274','2025-11-02 07:23:56.000000'),(2,'0900000005','654321',_binary '\0','2025-11-02 07:13:56.055274','2025-11-02 07:23:56.000000'),(3,'funky@gmail.com','137370',_binary '','2025-11-05 21:16:10.869619','2025-11-05 21:21:10.869619');
/*!40000 ALTER TABLE `verifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shop_id` bigint NOT NULL,
  `code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  `min_order_value` decimal(10,2) DEFAULT '0.00',
  `max_uses` int DEFAULT NULL,
  `valid_from` datetime DEFAULT NULL,
  `valid_to` datetime DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','EXPIRED') COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_code` (`code`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_code` (`code`),
  CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Discount vouchers';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,1,'TECH10K','Giảm 10k cho đơn >= 200k',10000.00,200000.00,100,'2025-11-02 07:13:56','2026-01-31 07:13:56','ACTIVE'),(2,2,'HOME15K','Giảm 15k cho đơn >= 80k',15000.00,80000.00,50,'2025-11-02 07:13:56','2026-01-01 07:13:56','ACTIVE');
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet_transactions`
--

DROP TABLE IF EXISTS `wallet_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wallet_id` bigint NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `type` enum('DEPOSIT','WITHDRAWAL','REFUND','ORDER_PAYMENT') COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `related_order_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wallet` (`wallet_id`),
  KEY `idx_order` (`related_order_id`),
  CONSTRAINT `fk_transaction_order` FOREIGN KEY (`related_order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Wallet transactions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet_transactions`
--

LOCK TABLES `wallet_transactions` WRITE;
/*!40000 ALTER TABLE `wallet_transactions` DISABLE KEYS */;
INSERT INTO `wallet_transactions` VALUES (1,3,-300000.00,'ORDER_PAYMENT','Payment for Order #1 (MOMO)',1,'2025-11-02 07:13:56'),(3,1,270000.00,'DEPOSIT','Seller revenue for Order #1 (after fees maybe)',1,'2025-11-02 07:13:56');
/*!40000 ALTER TABLE `wallet_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallets`
--

DROP TABLE IF EXISTS `wallets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `balance` decimal(15,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user` (`user_id`),
  CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User wallets for COD and refunds';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallets`
--

LOCK TABLES `wallets` WRITE;
/*!40000 ALTER TABLE `wallets` DISABLE KEYS */;
INSERT INTO `wallets` VALUES (1,2,1000000.00,'2025-11-02 07:13:56','2025-11-02 07:13:56'),(2,3,500000.00,'2025-11-02 07:13:56','2025-11-02 07:13:56'),(3,4,300000.00,'2025-11-02 07:13:56','2025-11-02 07:13:56'),(4,5,200000.00,'2025-11-02 07:13:56','2025-11-02 07:13:56');
/*!40000 ALTER TABLE `wallets` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-07 19:56:19
