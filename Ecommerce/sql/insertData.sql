use ecommerce1;
-- =========================================
-- 1) USERS
-- =========================================
INSERT INTO `users` (id, username, password, email, phone_number, full_name, role, activated)
VALUES
(4, 'buyer3', '$2a$10$VF0QNgfdGG1Ilw/qI48h9.K/F/ekKWxB/VjCaar72UtC9GI8CsGwu', 'buyer3@ecommerce.com','0900000004','Buyer Three', 2, b'1'),
(5, 'huy', '$2a$10$vkpyL7NvxkgodsKlgtt8juamD57OttQzB/0zISWP1JYJYYRX3rSRe', 'buinhathuy263@gmail.com','0900000005','Buyer Four', 2, b'1');

-- =========================================
-- 2) SHOPS (owner_id references users.id)
-- =========================================
INSERT INTO `shops` (id, name, description, address, phone, email, status, rating, review_count, owner_id)
VALUES
(1, 'TechZone',  'Electronics & Gadgets',      '123 Nguyen Van Linh, Da Nang', '0987000002', 'shop-tech@ecom.com', 'ACTIVE', 4.70, 12, 2),
(2, 'HomeStyle', 'Home & Kitchen Essentials',  '45 Tran Hung Dao, Ha Noi',     '0987000003', 'shop-home@ecom.com', 'ACTIVE', 4.50, 8, 3);

-- =========================================
-- 3) CATEGORIES (already present in your schema; include safe inserts in case missing)
-- =========================================
INSERT IGNORE INTO `categories` (id, name) VALUES
(1, 'Accessories'), (2, 'Bags'), (3, 'Clothing'), (4, 'Fitness Equipment'), (5, 'Shoes'), (6, 'Sports Equipment');

-- =========================================
-- 4) PRODUCTS
-- Note: base_price decimal, category_id references categories.id, shop_id references shops.id
-- =========================================
INSERT INTO `products` (id, name, description, base_price, main_image, product_condition, is_active, rating, review_count, sold_count, category_id, shop_id)
VALUES
(1, 'Wireless Mouse',       'Ergonomic 2.4GHz wireless mouse', 150000.00, 'mouse_main.jpg', 'NEW', b'1', 4.80, 10, 120, 1, 1),
(2, 'Bluetooth Headphones', 'Over-ear noise-cancelling headphones', 650000.00, 'headphone_main.jpg', 'NEW', b'1', 4.60, 15, 80, 1, 1),
(3, 'Ceramic Coffee Mug',   'Handmade ceramic mug 300ml', 90000.00, 'mug_main.jpg', 'NEW', b'1', 4.90, 8, 200, 1, 2);

-- =========================================
-- 5) PRODUCT_IMAGES
-- =========================================
INSERT INTO `product_images` (id, product_id, image_url, color)
VALUES
(1, 1, 'mouse_1.jpg', 'black'),
(2, 1, 'mouse_2.jpg', 'white'),
(3, 2, 'headphone_1.jpg', 'black'),
(4, 2, 'headphone_2.jpg', 'blue'),
(5, 3, 'mug_1.jpg', 'green'),
(6, 3, 'mug_2.jpg', 'beige');

-- =========================================
-- 6) PRODUCT_ATTRIBUTES (already present; safe inserts)
-- =========================================
INSERT IGNORE INTO `product_attributes` (id, name) VALUES
(1, 'Size'), (2, 'Color'), (3, 'Material');

-- =========================================
-- 7) PRODUCT_VARIANTS
-- =========================================
INSERT INTO `product_variants` (id, product_id, sku, price, stock)
VALUES
(1, 1, 'WM-BLACK-001', 150000.00, 50),   -- mouse black
(2, 1, 'WM-WHITE-001', 155000.00, 40),   -- mouse white
(3, 2, 'BH-BLACK-001', 650000.00, 25),   -- headphones black
(4, 3, 'MUG-GREEN-001', 90000.00, 120),  -- mug green
(5, 3, 'MUG-BEIGE-001', 95000.00, 80);   -- mug beige

-- =========================================
-- 8) PRODUCT_VARIANT_VALUES
-- Map each variant to attribute values
-- =========================================
INSERT INTO `product_variant_values` (id, variant_id, product_attribute_id, value)
VALUES
(1, 1, 2, 'Black'),
(2, 2, 2, 'White'),
(3, 3, 2, 'Black'),
(4, 4, 2, 'Green'),
(5, 5, 2, 'Beige');

-- =========================================
-- 9) CARTS
-- =========================================
INSERT INTO `carts` (id, user_id)
VALUES
(1, 3),  -- buyer1
(2, 4);  -- buyer2

-- =========================================
-- 10) CART_ITEMS
-- columns: cart_id, product_id, variant_id, quantity, price_snapshot
-- =========================================
INSERT INTO `cart_items` (id, cart_id, product_id, variant_id, quantity, price_snapshot)
VALUES
(1, 1, 1, 1, 2, 150000.00),  -- buyer1 has 2 x WM-BLACK
(2, 2, 3, 4, 1, 90000.00);   -- buyer2 has 1 x MUG-GREEN

-- =========================================
-- 11) WALLETS
-- =========================================
INSERT INTO `wallets` (id, user_id, balance)
VALUES
(1, 2, 1000000.00), -- seller1
(2, 3, 500000.00),  -- seller2
(3, 4, 300000.00),  -- buyer1
(4, 5, 200000.00);  -- buyer2

-- =========================================
-- 12) ORDERS
-- Insert orders with shipment_id = NULL for now (shipments created after)
-- =========================================
INSERT INTO `orders` (id, user_id, shop_id, voucher_id, shipment_id, total_amount, method, status, payment_status, momo_trans_id, paid_at)
VALUES
(1, 2, 1, NULL, NULL, 300000.00, 'MOMO', 'PROCESSING', 'PAID', 'MOMO-TRX-001', NOW()),  -- buyer1 order at TechZone
(2, 2, 2, NULL, NULL, 95000.00, 'MOMO', 'PENDING', 'UNPAID', NULL, NULL);                -- buyer2 order at HomeStyle
-- =========================================
-- 13) ORDER_ITEMS
-- =========================================
INSERT INTO `order_items` (id, order_id, product_id, variant_id, variant_name, quantity, price)
VALUES
(1, 1, 1, 1, 'Black', 2, 150000.00),  -- order 1: 2 x WM-BLACK
(2, 2, 3, 4, 'Green', 1, 90000.00);   -- order 2: 1 x MUG-GREEN

-- =========================================
-- 14) SHIPMENTS
-- Now create shipments that reference order_id (orders exist)
-- =========================================
INSERT INTO `shipments` (id, order_id, ghn_order_code, shipping_fee, service_type, expected_delivery, status, receiver_name, receiver_phone, receiver_address, province, district, ward, tracking_url)
VALUES
(1, 1, 'GHN0001', 25000.00, 'Standard', DATE_ADD(NOW(), INTERVAL 3 DAY), 'PICKED_UP', 'Buyer One', '0900000004', 'No.10, Le Loi, Da Nang', 'Da Nang', 'Hai Chau', 'Ward 1', 'https://ghn.example/track/GHN0001'),
(2, 2, 'GHN0002', 15000.00, 'Standard', DATE_ADD(NOW(), INTERVAL 4 DAY), 'PENDING', 'Buyer Two', '0900000005', 'No.5, Tran Hung Dao, Ha Noi', 'Ha Noi', 'Hoan Kiem', 'Ward 2', 'https://ghn.example/track/GHN0002');

-- =========================================
-- 15) UPDATE orders -> set shipment_id
-- =========================================
UPDATE `orders` SET shipment_id = 1 WHERE id = 1;
UPDATE `orders` SET shipment_id = 2 WHERE id = 2;

-- =========================================
-- 16) VOUCHERS
-- =========================================
INSERT INTO `vouchers` (id, shop_id, code, description, discount_amount, min_order_value, max_uses, valid_from, valid_to, status)
VALUES
(1, 1, 'TECH10K', 'Giảm 10k cho đơn >= 200k', 10000.00, 200000.00, 100, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'ACTIVE'),
(2, 2, 'HOME15K', 'Giảm 15k cho đơn >= 80k', 15000.00, 80000.00, 50, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 'ACTIVE');

-- =========================================
-- 17) WALLET_TRANSACTIONS
-- Types: DEPOSIT, WITHDRAWAL, REFUND, ORDER_PAYMENT
-- =========================================
INSERT INTO `wallet_transactions` (id, wallet_id, amount, type, description, related_order_id)
VALUES
(1, 3, -300000.00, 'ORDER_PAYMENT', 'Payment for Order #1 (MOMO)', 1),  -- buyer1 paid from outside wallet but record for accounting
(2, 4, 0.00, 'ORDER_PAYMENT', 'COD placeholder for Order #2', 2),
(3, 1, 270000.00, 'DEPOSIT', 'Seller revenue for Order #1 (after fees maybe)', 1),
(4, 2, 90000.00, 'DEPOSIT', 'Seller revenue for Order #2 (COD)', 2);

-- =========================================
-- 18) REFUNDS (example none processed yet; include sample requested refund)
-- =========================================
INSERT INTO `refunds` (id, order_id, reason, amount, status, transaction_id)
VALUES
(1, 1, 'Product damaged', 150000.00, 'REQUESTED', NULL);

-- =========================================
-- 19) PLATFORM_FEES
-- =========================================
INSERT INTO `platform_fees` (id, order_id, seller_id, fee_percent, fee_amount)
VALUES
(1, 1, 2, 5.00, 15000.00),  -- 5% fee on order 1 (example)
(2, 2, 3, 5.00, 4750.00);   -- 5% fee on order 2

-- =========================================
-- 20) USER_VOUCHERS (usage record)
-- =========================================
INSERT INTO `user_vouchers` (id, voucher_id, user_id, order_id, used_at)
VALUES
(1, 1, 4, 1, NOW()); -- buyer1 used TECH10K on order 1 (example)

-- =========================================
-- 21) VERIFICATIONS (OTP)
-- =========================================
INSERT INTO `verifications` (id, contact, otp, verified, expiry_time)
VALUES
(1, 'buyer1@ecommerce.com', '123456', b'1', DATE_ADD(NOW(), INTERVAL 10 MINUTE)),
(2, '0900000005', '654321', b'0', DATE_ADD(NOW(), INTERVAL 10 MINUTE));

-- =========================================
-- 22) PRODUCT_REVIEWS
-- Note: unique (user_id, product_id) enforced; reviews include order_id to show purchase
-- =========================================
INSERT INTO `product_reviews` (id, product_id, user_id, order_id, rating, comment, images, seller_response, seller_response_date)
VALUES
(1, 1, 4, 1, 5, 'Excellent mouse, very responsive and battery lasts long.', '["rev1_img1.jpg"]', NULL, NULL),
(2, 3, 5, 2, 4, 'Nice mug, color slightly different but good quality.', '["rev2_img1.jpg"]', 'Thanks for the feedback!', NOW());

-- =========================================
-- Final: quick sanity checks (optional)
-- =========================================
-- SELECT COUNT(*) FROM users;
-- SELECT COUNT(*) FROM shops;
-- SELECT COUNT(*) FROM products;
-- SELECT COUNT(*) FROM orders;
