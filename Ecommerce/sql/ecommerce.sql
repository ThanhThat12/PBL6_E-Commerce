-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 09, 2025 lúc 09:06 AM
-- Phiên bản máy phục vụ: 10.5.28-MariaDB
-- Phiên bản PHP: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `ecommerce1`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `carts`
--

CREATE TABLE `carts` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `cart_items`
--

CREATE TABLE `cart_items` (
  `id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `cart_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `orders`
--

CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','PENDING','PROCESSING') DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `shop_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `order_items`
--

CREATE TABLE `order_items` (
  `id` bigint(20) NOT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `products`
--

CREATE TABLE `products` (
  `id` bigint(20) NOT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `description` text DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `main_image` varchar(500) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  `shop_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_attributes`
--

CREATE TABLE `product_attributes` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_images`
--

CREATE TABLE `product_images` (
  `id` bigint(20) NOT NULL,
  `color` varchar(50) DEFAULT NULL,
  `image_url` varchar(500) NOT NULL,
  `product_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variants`
--

CREATE TABLE `product_variants` (
  `id` bigint(20) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `sku` varchar(100) NOT NULL,
  `stock` int(11) NOT NULL,
  `product_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `product_variant_values`
--

CREATE TABLE `product_variant_values` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `product_attribute_id` bigint(20) NOT NULL,
  `variant_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `shops`
--

CREATE TABLE `shops` (
  `id` bigint(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `owner_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `activated` bit(1) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `facebook_id` varchar(100) DEFAULT NULL,
  `password` varchar(60) NOT NULL,
  `phone_number` varchar(100) DEFAULT NULL,
  `role` tinyint(4) NOT NULL,
  `username` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `activated`, `email`, `facebook_id`, `password`, `phone_number`, `role`, `username`) VALUES
(1, b'1', 'thanhthat120704@gmail.com', NULL, '$2a$10$Aq94o/k0q6DuE.A9cIAkeewuvnwybXzbiZY0vGOPNSSmVtaRU4HaK', NULL, 2, 'thanhthat120704'),
(2, b'1', 'nguyenthat.20072004@gmail.com', NULL, '$2a$10$T20rEX1ve6hUrhXEGuzYVug9mjlZTnkX.IwnHomtLLFAO3YdLTVK.', NULL, 2, 'nguyenthat.20072004'),
(3, b'1', 'thanhthat20041207@gmail.com', NULL, '$2a$10$eDx8DBq.c5reZbitaN3Rg.w99GaI.SOydC7U0x/ptIYe0NPHLtp5y', NULL, 2, 'thanhthat20041207');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `verifications`
--

CREATE TABLE `verifications` (
  `id` bigint(20) NOT NULL,
  `contact` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `expiry_time` datetime(6) DEFAULT NULL,
  `otp` varchar(255) DEFAULT NULL,
  `verified` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `carts`
--
ALTER TABLE `carts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKb5o626f86h46m4s7ms6ginnop` (`user_id`);

--
-- Chỉ mục cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKpcttvuq4mxppo8sxggjtn5i2c` (`cart_id`),
  ADD KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`);

--
-- Chỉ mục cho bảng `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK21gttsw5evi5bbsvleui69d7r` (`shop_id`),
  ADD KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`);

--
-- Chỉ mục cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  ADD KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`);

--
-- Chỉ mục cho bảng `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  ADD KEY `FK7kp8sbhxboponhx3lxqtmkcoj` (`shop_id`);

--
-- Chỉ mục cho bảng `product_attributes`
--
ALTER TABLE `product_attributes`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `product_images`
--
ALTER TABLE `product_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKqnq71xsohugpqwf3c9gxmsuy` (`product_id`);

--
-- Chỉ mục cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKq935p2d1pbjm39n0063ghnfgn` (`sku`),
  ADD KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`);

--
-- Chỉ mục cho bảng `product_variant_values`
--
ALTER TABLE `product_variant_values`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKqc833aad6m0eqre1jdw0ystmv` (`product_attribute_id`),
  ADD KEY `FKpbp54igkobql1af2fpow658fu` (`variant_id`);

--
-- Chỉ mục cho bảng `shops`
--
ALTER TABLE `shops`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrduswa89ayj0poad3l70nag19` (`owner_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  ADD UNIQUE KEY `UKjmubronqnn4q0cwe2egqsgvnl` (`facebook_id`),
  ADD UNIQUE KEY `UK9q63snka3mdh91as4io72espi` (`phone_number`);

--
-- Chỉ mục cho bảng `verifications`
--
ALTER TABLE `verifications`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `carts`
--
ALTER TABLE `carts`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `orders`
--
ALTER TABLE `orders`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `products`
--
ALTER TABLE `products`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `product_attributes`
--
ALTER TABLE `product_attributes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `product_images`
--
ALTER TABLE `product_images`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `product_variant_values`
--
ALTER TABLE `product_variant_values`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `shops`
--
ALTER TABLE `shops`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `verifications`
--
ALTER TABLE `verifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `carts`
--
ALTER TABLE `carts`
  ADD CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`);

--
-- Các ràng buộc cho bảng `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `FK21gttsw5evi5bbsvleui69d7r` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  ADD CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Các ràng buộc cho bảng `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `FK7kp8sbhxboponhx3lxqtmkcoj` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  ADD CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- Các ràng buộc cho bảng `product_images`
--
ALTER TABLE `product_images`
  ADD CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Các ràng buộc cho bảng `product_variants`
--
ALTER TABLE `product_variants`
  ADD CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Các ràng buộc cho bảng `product_variant_values`
--
ALTER TABLE `product_variant_values`
  ADD CONSTRAINT `FKpbp54igkobql1af2fpow658fu` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  ADD CONSTRAINT `FKqc833aad6m0eqre1jdw0ystmv` FOREIGN KEY (`product_attribute_id`) REFERENCES `product_attributes` (`id`);

--
-- Các ràng buộc cho bảng `shops`
--
ALTER TABLE `shops`
  ADD CONSTRAINT `FKrduswa89ayj0poad3l70nag19` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
