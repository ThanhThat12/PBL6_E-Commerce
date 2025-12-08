-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ecommerce
-- ------------------------------------------------------
-- Server version	8.0.36

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
  `full_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Số nhà, tên đường',
  `province_id` int DEFAULT NULL COMMENT 'GHN Province ID',
  `district_id` int DEFAULT NULL COMMENT 'GHN District ID',
  `ward_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `province_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên tỉnh/thành phố',
  `district_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên quận/huyện',
  `ward_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên phường/xã',
  `contact_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `primary_address` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `type_address` enum('HOME','OTHER','SHIPPING','STORE') COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_primary` (`primary_address`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User delivery addresses with GHN location data';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (1,5,'47 Âu Cơ, Quận Liên Chiểu, Đà Nẵng',203,1530,'40503','Đà Nẵng','Quận Liên Chiểu','Phường Hòa Khánh Bắc','0912345678',_binary '','2025-11-11 11:30:48.393055','huy','STORE'),(3,6,'17 Âu Cơ, Quận Liên Chiểu, Đà Nẵng',203,1530,'40503','Đà Nẵng','Quận Liên Chiểu','Phường Hòa Khánh Bắc','0912345678',_binary '','2025-11-28 11:58:39.011902','Huy','HOME'),(4,6,'218 Đường Lê Duẩn, Thành phố Huế, Thừa Thiên Huế',223,1585,'330115','Thừa Thiên Huế','Thành phố Huế','Phường Phú Thuận','0912345678',_binary '\0','2025-11-28 12:31:37.978783','ốc','HOME');
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
  `price_snapshot` decimal(38,2) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cart items';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,1,1,1,2,150000.00,'2025-11-11 04:29:25.500158','2025-11-11 04:29:25.500158',NULL),(2,2,3,4,1,90000.00,'2025-11-11 04:29:25.500158','2025-11-11 04:29:25.500158',NULL),(6,3,1,1,1,150000.00,'2025-11-11 11:42:05.739944','2025-11-11 11:42:05.739944',1),(30,3,6,6,1,80000.00,'2025-11-29 13:05:32.755584','2025-11-29 13:05:32.755584',6),(49,4,6,6,1,80000.00,'2025-12-05 16:22:45.798411','2025-12-05 16:22:45.798411',6),(50,4,4,8,1,150000.00,'2025-12-05 16:22:56.697985','2025-12-05 16:22:56.697985',8);
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
INSERT INTO `carts` VALUES (1,3,'2025-11-11 04:29:25.492158','2025-11-11 04:29:25.492158'),(2,4,'2025-11-11 04:29:25.492158','2025-11-11 04:29:25.492158'),(3,5,'2025-11-11 11:30:25.295261','2025-11-11 11:30:25.316571'),(4,6,'2025-11-11 13:21:12.165520','2025-11-11 13:21:12.168783');
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
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `variant_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Tên variant: Size M, Red',
  `quantity` int NOT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  `status` enum('COMPLETED','RETURNED','RETURN_REQUESTED') COLLATE utf8mb4_general_ci DEFAULT NULL,
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
INSERT INTO `order_items` VALUES (1,1,6,6,'SKU-6',1,80000.00,'COMPLETED'),(2,2,6,6,'SKU-6',1,80000.00,'COMPLETED'),(3,3,6,6,'SKU-6',1,80000.00,'RETURN_REQUESTED'),(4,4,6,6,'SKU-6',3,80000.00,'COMPLETED');
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
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` enum('PENDING','PROCESSING','SHIPPING','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PENDING',
  `payment_status` enum('UNPAID','PAID','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'UNPAID' COMMENT 'Trạng thái thanh toán',
  `paid_at` datetime DEFAULT NULL COMMENT 'Thời điểm thanh toán',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `receiver_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receiver_phone` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receiver_address` text COLLATE utf8mb4_general_ci,
  `shipping_fee` decimal(38,2) DEFAULT NULL,
  `province_id` int DEFAULT NULL,
  `district_id` int DEFAULT NULL,
  `ward_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_shipment` (`shipment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_orders_district_id` (`district_id`),
  KEY `idx_orders_ward_code` (`ward_code`),
  KEY `idx_orders_province_id` (`province_id`),
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
INSERT INTO `orders` VALUES (1,6,3,NULL,NULL,109001.00,'MOMO','SHIPPING','PAID','2025-12-03 21:52:42','2025-12-03 21:48:28.187000','2025-12-03 21:48:28.187000','ốc','0912345678','218 Đường Lê Duẩn, Thành phố Huế, Thừa Thiên Huế',29001.00,NULL,NULL,NULL),(2,6,3,NULL,NULL,95501.00,'COD','SHIPPING','UNPAID',NULL,'2025-12-05 11:44:51.389000','2025-12-05 12:53:51.073117','Huy','0912345678','17 Âu Cơ, Quận Liên Chiểu, Đà Nẵng',15501.00,NULL,NULL,NULL),(3,6,3,NULL,NULL,95501.00,'COD','COMPLETED','UNPAID',NULL,'2025-12-05 13:22:24.193000','2025-12-05 15:12:02.204000','Huy','0912345678','17 Âu Cơ, Quận Liên Chiểu, Đà Nẵng',15501.00,203,1530,'40503'),(4,6,3,NULL,NULL,240000.00,'COD','PENDING','UNPAID',NULL,'2025-12-05 15:11:49.225000','2025-12-05 15:11:49.225000','Huy','0912345678','17 Âu Cơ, Quận Liên Chiểu, Đà Nẵng',0.00,203,1530,'40503');
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
  `request_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Unique request ID',
  `order_id_momo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo order ID',
  `amount` decimal(19,2) NOT NULL COMMENT 'Payment amount',
  `trans_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo transaction ID',
  `status` enum('FAILED','PENDING','REFUNDED','SUCCESS') COLLATE utf8mb4_general_ci NOT NULL,
  `result_code` int DEFAULT NULL COMMENT 'MoMo result code',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Response message',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MoMo signature',
  `pay_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Payment URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `request_id` (`request_id`),
  UNIQUE KEY `unique_request_id` (`request_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_trans_id` (`trans_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MoMo payment transactions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transactions`
--

LOCK TABLES `payment_transactions` WRITE;
/*!40000 ALTER TABLE `payment_transactions` DISABLE KEYS */;
INSERT INTO `payment_transactions` VALUES (7,1,'ORD-1-a3054e0d-5fe3-4240-9818-e4421e62e305_1764773308417_9900','ORD-1-a3054e0d-5fe3-4240-9818-e4421e62e305',109001.00,'4622592801','SUCCESS',0,'Thành công.','adbb34938c1e9c1bfadef241732d0d115457a0deb8346c12e598a8e17781117e','https://test-payment.momo.vn/v2/gateway/pay?t=TU9NT0JLVU4yMDE4MDUyOXxPUkQtMS1hMzA1NGUwZC01ZmUzLTQyNDAtOTgxOC1lNDQyMWU2MmUzMDU&s=2f1a7d4610cd228e6c438bd82315a9cf5e02a9a82e03a301f7e82bf99244b2df','2025-12-03 21:48:28','2025-12-03 21:52:42');
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
  `fee_percent` decimal(5,2) DEFAULT NULL,
  `fee_amount` decimal(15,2) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_seller` (`seller_id`),
  CONSTRAINT `fk_fee_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_fee_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Platform fees';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platform_fees`
--

LOCK TABLES `platform_fees` WRITE;
/*!40000 ALTER TABLE `platform_fees` DISABLE KEYS */;
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
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Size, Color, Material, etc.',
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
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `display_order` int DEFAULT NULL,
  `image_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `public_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `uploaded_at` datetime(6) DEFAULT NULL,
  `variant_attribute_value` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_product_variant_image` (`product_id`,`variant_attribute_value`,`image_type`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product images';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (1,1,'mouse_1.jpg','black',NULL,'',NULL,NULL,NULL),(2,1,'mouse_2.jpg','white',NULL,'',NULL,NULL,NULL),(3,2,'headphone_1.jpg','black',NULL,'',NULL,NULL,NULL),(4,2,'headphone_2.jpg','blue',NULL,'',NULL,NULL,NULL),(5,3,'mug_1.jpg','green',NULL,'',NULL,NULL,NULL),(6,3,'mug_2.jpg','beige',NULL,'',NULL,NULL,NULL),(19,7,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764342793/products/gallery/product_7_gallery_1764342791431.jpg',NULL,0,'GALLERY','products/gallery/product_7_gallery_1764342791431','2025-11-28 22:13:14.555574',NULL),(20,7,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764342795/products/gallery/product_7_gallery_1764342794573.jpg',NULL,1,'GALLERY','products/gallery/product_7_gallery_1764342794573','2025-11-28 22:13:15.821860',NULL),(21,7,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764342812/misc/product_7_variant_%C4%90%E1%BB%8F_1764342811361.jpg',NULL,0,'VARIANT','misc/product_7_variant_Đỏ_1764342811361','2025-11-28 22:13:18.837754','Đỏ'),(22,7,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764342808/products/gallery/product_7_gallery_1764342805419.jpg',NULL,2,'GALLERY','products/gallery/product_7_gallery_1764342805419','2025-11-28 22:13:29.892078',NULL),(23,7,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764342810/products/gallery/product_7_gallery_1764342809903.jpg',NULL,3,'GALLERY','products/gallery/product_7_gallery_1764342809903','2025-11-28 22:13:31.169775',NULL);
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_primary_attributes`
--

DROP TABLE IF EXISTS `product_primary_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_primary_attributes` (
  `product_id` bigint NOT NULL,
  `attribute_id` bigint NOT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FKi0al542l77r4g5fi8jex0fct9` (`attribute_id`),
  CONSTRAINT `FKi0al542l77r4g5fi8jex0fct9` FOREIGN KEY (`attribute_id`) REFERENCES `product_attributes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_primary_attributes`
--

LOCK TABLES `product_primary_attributes` WRITE;
/*!40000 ALTER TABLE `product_primary_attributes` DISABLE KEYS */;
INSERT INTO `product_primary_attributes` VALUES (7,2);
/*!40000 ALTER TABLE `product_primary_attributes` ENABLE KEYS */;
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
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Nội dung review',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'JSON array URLs ảnh upload',
  `seller_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Shop trả lời review',
  `seller_response_date` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `verified_purchase` bit(1) NOT NULL DEFAULT b'1',
  `images_count` int DEFAULT '0',
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
INSERT INTO `product_reviews` VALUES (1,1,4,1,5,'Excellent mouse, very responsive and battery lasts long.','[\"rev1_img1.jpg\"]',NULL,NULL,'2025-11-11 04:29:25','2025-11-11 04:29:25',_binary '',0),(2,3,5,NULL,4,'Nice mug, color slightly different but good quality.','[\"rev2_img1.jpg\"]','Thanks for the feedback!','2025-11-11 04:29:25','2025-11-11 04:29:25','2025-11-11 04:29:25',_binary '',0);
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
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'M, Red, Cotton, etc.',
  PRIMARY KEY (`id`),
  KEY `idx_variant` (`variant_id`),
  KEY `idx_attribute` (`product_attribute_id`),
  CONSTRAINT `fk_variantvalue_attribute` FOREIGN KEY (`product_attribute_id`) REFERENCES `product_attributes` (`id`),
  CONSTRAINT `fk_variantvalue_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Variant attribute values';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variant_values`
--

LOCK TABLES `product_variant_values` WRITE;
/*!40000 ALTER TABLE `product_variant_values` DISABLE KEYS */;
INSERT INTO `product_variant_values` VALUES (1,1,2,'Black'),(2,2,2,'White'),(3,3,2,'Black'),(4,4,2,'Green'),(5,5,2,'Beige'),(6,9,2,'Đỏ');
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
  `sku` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_sku` (`sku`),
  KEY `idx_product` (`product_id`),
  CONSTRAINT `fk_variant_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Product variants (size, color combinations)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variants`
--

LOCK TABLES `product_variants` WRITE;
/*!40000 ALTER TABLE `product_variants` DISABLE KEYS */;
INSERT INTO `product_variants` VALUES (1,1,'WM-BLACK-001',150000.00,50),(2,1,'WM-WHITE-001',155000.00,40),(3,2,'BH-BLACK-001',650000.00,25),(4,3,'MUG-GREEN-001',90000.00,111),(5,3,'MUG-BEIGE-001',95000.00,80),(6,6,'SKU-6',80000.00,15),(7,5,'SKU-5',500000.00,20),(8,4,'SKU-4',150000.00,20),(9,7,'ĐỎ',19990.00,10);
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
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `main_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `product_condition` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `main_image_public_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `weight_grams` int DEFAULT NULL,
  `width_cm` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_active` (`is_active`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_product_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Products';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Wireless Mouse','Ergonomic 2.4GHz wireless mouse',150000.00,'mouse_main.jpg','NEW',_binary '',5.00,1,120,1,1,'2025-11-11 04:29:25.452081','2025-11-11 04:29:25.623608',NULL,NULL,NULL,NULL,NULL),(2,'Bluetooth Headphones','Over-ear noise-cancelling headphones',650000.00,'headphone_main.jpg','NEW',_binary '',4.60,15,80,1,1,'2025-11-11 04:29:25.452081','2025-11-11 04:29:25.452081',NULL,NULL,NULL,NULL,NULL),(3,'Ceramic Coffee Mug','Handmade ceramic mug 300ml',90000.00,'mug_main.jpg','NEW',_binary '',4.00,1,200,1,2,'2025-11-11 04:29:25.452081','2025-11-11 04:29:25.623608',NULL,NULL,NULL,NULL,NULL),(4,'Áo Thun Thể Thao','Áo thun co giãn, thoáng khí, phù hợp tập gym',150000.00,'https://example.com/images/ao-thun.jpg','NEW',_binary '',0.00,0,0,1,3,'2025-11-18 09:17:02.428453','2025-12-02 17:33:21.283541',1,1,NULL,100,30),(5,'Giày Chạy Bộ','Giày chạy bộ nhẹ, đế êm, chống trơn trượt',500000.00,'https://example.com/images/giay-chay.jpg','NEW',_binary '',0.00,0,0,5,3,'2025-11-18 09:17:02.428453','2025-12-02 17:33:21.282691',1,1,NULL,100,20),(6,'Bình Nước Thể Thao','Bình nước dung tích 1L, giữ nhiệt tốt',80000.00,'https://res.cloudinary.com/dejjhkhl1/image/upload/v1764343762/products/main/product_6_main_1764343760204.jpg','NEW',_binary '',0.00,0,0,4,3,'2025-11-18 09:17:02.428453','2025-12-02 17:33:21.280897',1,1,'products/main/product_6_main_1764343760204',200,10),(7,'Bánh mì','ko có',20000.00,NULL,'NEW',_binary '\0',0.00,0,0,2,3,'2025-11-28 22:09:31.034599','2025-11-28 22:09:31.034599',NULL,NULL,NULL,NULL,NULL);
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
  `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID refresh token',
  `user_id` bigint NOT NULL,
  `expiry_date` datetime(6) NOT NULL COMMENT 'Token expiration time',
  `revoked` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Token đã bị thu hồi?',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expiry` (`expiry_date`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Refresh tokens for JWT authentication';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
INSERT INTO `refresh_tokens` VALUES (148,'28121a7b-eb08-407c-8eb9-dbee4586d0f9',5,'2026-01-04 08:49:25.887483',_binary '\0','2025-12-05 08:49:25.887483'),(150,'350a5108-a265-4ec5-ac70-a6d5cf5e3297',6,'2026-01-04 09:22:42.969551',_binary '\0','2025-12-05 09:22:42.969551');
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refund_items`
--

DROP TABLE IF EXISTS `refund_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `refund_id` bigint NOT NULL,
  `order_item_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `refund_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_refund_items_refund_id` (`refund_id`),
  KEY `idx_refund_items_order_item_id` (`order_item_id`),
  CONSTRAINT `fk_refund_items_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_refund_items_refund` FOREIGN KEY (`refund_id`) REFERENCES `refunds` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chi tiết món hàng được hoàn trả (hỗ trợ refund từng phần)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refund_items`
--

LOCK TABLES `refund_items` WRITE;
/*!40000 ALTER TABLE `refund_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `refund_items` ENABLE KEYS */;
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
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `amount` decimal(15,2) NOT NULL,
  `status` enum('REQUESTED','APPROVED','REJECTED','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'REQUESTED',
  `transaction_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `requires_return` bit(1) DEFAULT NULL,
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
INSERT INTO `refunds` VALUES (1,3,'Return Request - Product: SKU-6, Quantity: 1, Method: SHIP_BACK, Reason: sao, Images: \n[Kết quả kiểm tra]: Hàng đã nhận và kiểm tra OK',NULL,80000.00,'COMPLETED',NULL,'2025-12-05 15:49:16','2025-12-05 16:04:40',_binary '');
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
  `ghn_order_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `expected_delivery` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tracking_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ghn_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_ghn_code` (`ghn_order_code`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `fk_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Shipments';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipments`
--

LOCK TABLES `shipments` WRITE;
/*!40000 ALTER TABLE `shipments` DISABLE KEYS */;
INSERT INTO `shipments` VALUES (1,3,'ORD-3',NULL,'READY_TO_PICK',NULL,'2025-12-05 13:43:02','2025-12-05 13:43:02','{}'),(2,1,'ORD-1',NULL,'READY_TO_PICK',NULL,'2025-12-05 14:13:46','2025-12-05 14:13:46','{}');
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
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SĐT liên hệ',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Email shop',
  `status` enum('ACTIVE','INACTIVE','PENDING') COLLATE utf8mb4_general_ci NOT NULL,
  `rating` decimal(3,2) DEFAULT '5.00' COMMENT 'Rating shop (0-5)',
  `review_count` int DEFAULT '0' COMMENT 'Số reviews',
  `owner_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `banner_public_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `banner_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ghn_shop_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ghn_token` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `logo_public_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `logo_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_shop_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Seller shops';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shops`
--

LOCK TABLES `shops` WRITE;
/*!40000 ALTER TABLE `shops` DISABLE KEYS */;
INSERT INTO `shops` VALUES (1,'TechZone','Electronics & Gadgets','123 Nguyen Van Linh, Da Nang','0987000002','shop-tech@ecom.com','ACTIVE',5.00,1,2,'2025-11-11 04:29:25.440359',NULL,NULL,NULL,NULL,NULL,NULL),(2,'HomeStyle','Home & Kitchen Essentials','45 Tran Hung Dao, Ha Noi','0987000003','shop-home@ecom.com','ACTIVE',4.00,1,3,'2025-11-11 04:29:25.440359',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Shop của Seller 5','Shop chuyên bán đồ thể thao','123 Đường ABC, Quận 1','0909123456','seller5@shop.com','ACTIVE',5.00,0,5,'2025-11-18 09:17:02.420187',NULL,NULL,'6122946','2f8bcdb9-c512-11f0-be82-329861df0aee',NULL,NULL);
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
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `facebook_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `google_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` tinyint NOT NULL COMMENT '0=BUYER, 1=SELLER, 2=ADMIN',
  `activated` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `avatar_public_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
INSERT INTO `users` VALUES (1,'admin','$2a$10$Aq94o/k0q6DuE.A9cIAkeewuvnwybXzbiZY0vGOPNSSmVtaRU4HaK','admin@ecommerce.com',NULL,NULL,NULL,NULL,NULL,2,_binary '','2025-11-11 04:29:22.203885','2025-11-11 04:29:22.203885',NULL),(2,'seller1','$2a$10$eDx8DBq.c5reZbitaN3Rg.w99GaI.SOydC7U0x/ptIYe0NPHLtp5y','seller1@ecommerce.com',NULL,NULL,NULL,NULL,NULL,1,_binary '','2025-11-11 04:29:22.203885','2025-11-11 04:29:22.203885',NULL),(3,'buyer1','$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu','buyer1@ecommerce.com',NULL,NULL,NULL,NULL,NULL,0,_binary '','2025-11-11 04:29:22.203885','2025-11-11 04:29:22.203885',NULL),(4,'buyer3','$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu','buyer3@ecommerce.com','0900000004','Buyer Three',NULL,NULL,NULL,2,_binary '','2025-11-11 04:29:25.429135','2025-11-11 04:29:25.429135',NULL),(5,'huy','$2a$10$vkpyL7NvxkgodsKlgtt8juamD57OttQzB/0zISWP1JYJYYRX3rSRe','buinhathuy263@gmail.com','0900000005','Buyer Four',NULL,NULL,NULL,1,_binary '','2025-11-11 04:29:25.429135','2025-11-11 06:10:22.584472',NULL),(6,'thanh','$2a$10$vkpyL7NvxkgodsKlgtt8juamD57OttQzB/0zISWP1JYJYYRX3rSRe','giacatdu1412@gmail.com',NULL,NULL,NULL,NULL,NULL,2,_binary '','2025-11-11 13:21:06.951999','2025-11-29 00:13:03.240572',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verifications`
--

DROP TABLE IF EXISTS `verifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Email or phone',
  `otp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `verified` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `expiry_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OTP verifications';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verifications`
--

LOCK TABLES `verifications` WRITE;
/*!40000 ALTER TABLE `verifications` DISABLE KEYS */;
INSERT INTO `verifications` VALUES (1,'buyer1@ecommerce.com','123456',_binary '','2025-11-11 04:29:25.615569','2025-11-11 04:39:25.000000'),(2,'0900000005','654321',_binary '\0','2025-11-11 04:29:25.615569','2025-11-11 04:39:25.000000'),(3,'giacatdu1412@gmail.com','373369',_binary '','2025-11-11 13:19:34.446075','2025-11-11 13:24:34.446075'),(4,'huynhat512@gmail.com','991627',_binary '\0','2025-11-28 21:57:39.173224','2025-11-28 22:02:39.173224');
/*!40000 ALTER TABLE `verifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_products`
--

DROP TABLE IF EXISTS `voucher_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `voucher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkyatudiorxc8sk0a1oguwqy56` (`product_id`),
  KEY `FKnnuryb7j97j3tygj31618swx1` (`voucher_id`),
  CONSTRAINT `FKkyatudiorxc8sk0a1oguwqy56` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKnnuryb7j97j3tygj31618swx1` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_products`
--

LOCK TABLES `voucher_products` WRITE;
/*!40000 ALTER TABLE `voucher_products` DISABLE KEYS */;
/*!40000 ALTER TABLE `voucher_products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_users`
--

DROP TABLE IF EXISTS `voucher_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `voucher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgcq0hnw08m7r3kkg82ggxi40i` (`user_id`),
  KEY `FKhch97p4o3m93b69juyxcm58j6` (`voucher_id`),
  CONSTRAINT `FKgcq0hnw08m7r3kkg82ggxi40i` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhch97p4o3m93b69juyxcm58j6` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_users`
--

LOCK TABLES `voucher_users` WRITE;
/*!40000 ALTER TABLE `voucher_users` DISABLE KEYS */;
/*!40000 ALTER TABLE `voucher_users` ENABLE KEYS */;
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
  `code` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  `min_order_value` decimal(38,2) DEFAULT NULL,
  `max_uses` int DEFAULT NULL,
  `valid_from` datetime DEFAULT NULL,
  `valid_to` datetime DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','EXPIRED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
  `applicable_type` enum('ALL','SPECIFIC_PRODUCTS','SPECIFIC_USERS','TOP_BUYERS') COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_type` enum('FIXED_AMOUNT','PERCENTAGE') COLLATE utf8mb4_general_ci NOT NULL,
  `discount_value` decimal(38,2) NOT NULL,
  `max_discount_amount` decimal(38,2) DEFAULT NULL,
  `top_buyers_count` int DEFAULT NULL,
  `usage_limit` int NOT NULL,
  `used_count` int NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
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
INSERT INTO `vouchers` VALUES (1,1,'TECH10K','Giảm 10k cho đơn >= 200k',10000.00,200000.00,100,'2025-11-11 04:29:25','2026-02-09 04:29:25','ACTIVE','ALL',NULL,'FIXED_AMOUNT',0.00,NULL,NULL,0,0,NULL,NULL),(2,2,'HOME15K','Giảm 15k cho đơn >= 80k',15000.00,80000.00,50,'2025-11-11 04:29:25','2026-01-10 04:29:25','ACTIVE','ALL',NULL,'FIXED_AMOUNT',0.00,NULL,NULL,0,0,NULL,NULL);
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
  `type` enum('DEPOSIT','WITHDRAWAL','REFUND','ORDER_PAYMENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `related_order_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wallet` (`wallet_id`),
  KEY `idx_order` (`related_order_id`),
  CONSTRAINT `fk_transaction_order` FOREIGN KEY (`related_order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_transaction_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Wallet transactions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet_transactions`
--

LOCK TABLES `wallet_transactions` WRITE;
/*!40000 ALTER TABLE `wallet_transactions` DISABLE KEYS */;
INSERT INTO `wallet_transactions` VALUES (1,6,10000.00,'DEPOSIT','Nạp tiền qua MoMo - TransId: 4622611429, OrderId: WALLET-6-1764773055344',NULL,'2025-12-03 21:45:47'),(2,6,80000.00,'DEPOSIT','Hoàn tiền đơn hàng #3',NULL,'2025-12-05 16:04:40');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User wallets for COD and refunds';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallets`
--

LOCK TABLES `wallets` WRITE;
/*!40000 ALTER TABLE `wallets` DISABLE KEYS */;
INSERT INTO `wallets` VALUES (1,2,1000000.00,'2025-11-11 04:29:25','2025-12-03 21:31:50'),(2,3,500000.00,'2025-11-11 04:29:25','2025-12-03 21:31:50'),(3,4,300000.00,'2025-11-11 04:29:25','2025-12-03 21:31:50'),(4,5,200000.00,'2025-11-11 04:29:25','2025-12-03 21:31:50'),(6,6,90000.00,'2025-11-28 19:57:00','2025-12-05 16:04:40');
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

-- Dump completed on 2025-12-05 16:26:24
