-- ============================================
-- WALLET BALANCE DEBUG & FIX SCRIPT
-- ============================================

-- 1. CHECK ADMIN USER EXISTS
SELECT '=== ADMIN USER CHECK ===' as step;
SELECT id, username, email, role, activated 
FROM users 
WHERE role = 'ADMIN';

-- Nếu không có admin, bỏ comment dòng dưới để tạo:
-- INSERT INTO users (username, email, password, role, activated, created_at) 
-- VALUES ('admin', 'admin@ecommerce.com', '$2a$10$password_hash_here', 'ADMIN', 1, NOW());

-- 2. CHECK ADMIN WALLET EXISTS
SELECT '=== ADMIN WALLET CHECK ===' as step;
SELECT w.id as wallet_id, w.user_id, w.balance, u.username, u.role
FROM wallets w
JOIN users u ON w.user_id = u.id
WHERE u.role = 'ADMIN';

-- Nếu không có wallet, bỏ comment để tạo:
-- INSERT INTO wallets (user_id, balance, created_at, updated_at)
-- SELECT id, 0.00, NOW(), NOW() FROM users WHERE role = 'ADMIN' AND NOT EXISTS (
--     SELECT 1 FROM wallets WHERE user_id = users.id
-- );

-- 3. CHECK WALLET TRANSACTIONS
SELECT '=== RECENT WALLET TRANSACTIONS (ADMIN) ===' as step;
SELECT 
    wt.id,
    wt.type,
    wt.amount,
    wt.description,
    wt.created_at,
    w.balance as current_wallet_balance
FROM wallet_transactions wt
JOIN wallets w ON wt.wallet_id = w.id
JOIN users u ON w.user_id = u.id
WHERE u.role = 'ADMIN'
ORDER BY wt.created_at DESC
LIMIT 10;

-- 4. CALCULATE EXPECTED BALANCE FROM TRANSACTIONS
SELECT '=== BALANCE VERIFICATION ===' as step;
SELECT 
    w.id as wallet_id,
    w.balance as current_balance,
    COALESCE(SUM(
        CASE 
            WHEN wt.type IN ('DEPOSIT', 'REFUND') THEN wt.amount
            WHEN wt.type IN ('WITHDRAWAL', 'ORDER_PAYMENT', 'PAYMENT_TO_SELLER') THEN -wt.amount
            ELSE 0
        END
    ), 0) as calculated_balance,
    (w.balance - COALESCE(SUM(
        CASE 
            WHEN wt.type IN ('DEPOSIT', 'REFUND') THEN wt.amount
            WHEN wt.type IN ('WITHDRAWAL', 'ORDER_PAYMENT', 'PAYMENT_TO_SELLER') THEN -wt.amount
            ELSE 0
        END
    ), 0)) as difference
FROM wallets w
LEFT JOIN wallet_transactions wt ON wt.wallet_id = w.id
WHERE w.user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1)
GROUP BY w.id, w.balance;

-- 5. FIX: RECALCULATE AND UPDATE ADMIN WALLET BALANCE
SELECT '=== FIX: RECALCULATING BALANCE ===' as step;

-- Backup current balance first
CREATE TEMPORARY TABLE IF NOT EXISTS wallet_backup AS
SELECT * FROM wallets WHERE user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);

-- Update wallet balance based on transactions
UPDATE wallets w
SET w.balance = (
    SELECT COALESCE(SUM(
        CASE 
            WHEN wt.type IN ('DEPOSIT', 'REFUND') THEN wt.amount
            WHEN wt.type IN ('WITHDRAWAL', 'ORDER_PAYMENT', 'PAYMENT_TO_SELLER') THEN -wt.amount
            ELSE 0
        END
    ), 0)
    FROM wallet_transactions wt
    WHERE wt.wallet_id = w.id
),
w.updated_at = NOW()
WHERE w.user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);

-- 6. VERIFY FIX
SELECT '=== VERIFICATION AFTER FIX ===' as step;
SELECT 
    w.id as wallet_id,
    w.balance as updated_balance,
    (SELECT SUM(amount) FROM wallet_transactions WHERE wallet_id = w.id AND type = 'DEPOSIT') as total_deposits,
    (SELECT SUM(amount) FROM wallet_transactions WHERE wallet_id = w.id AND type IN ('WITHDRAWAL', 'PAYMENT_TO_SELLER')) as total_withdrawals,
    w.updated_at
FROM wallets w
WHERE w.user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);

-- 7. CHECK ALL WALLETS SUMMARY
SELECT '=== ALL WALLETS SUMMARY ===' as step;
SELECT 
    u.username,
    u.role,
    w.balance,
    COUNT(wt.id) as transaction_count,
    MAX(wt.created_at) as last_transaction
FROM users u
LEFT JOIN wallets w ON w.user_id = u.id
LEFT JOIN wallet_transactions wt ON wt.wallet_id = w.id
WHERE u.role IN ('ADMIN', 'SELLER', 'BUYER')
GROUP BY u.id, u.username, u.role, w.balance
ORDER BY u.role, u.username;

-- 8. CHECK PAYMENT TRANSACTIONS VS WALLET TRANSACTIONS
SELECT '=== PAYMENT vs WALLET TRANSACTIONS MATCHING ===' as step;
SELECT 
    o.id as order_id,
    o.total_amount,
    o.payment_method,
    o.payment_status,
    pt.status as payment_tx_status,
    COUNT(wt.id) as wallet_tx_count,
    SUM(CASE WHEN wt.type = 'DEPOSIT' THEN wt.amount ELSE 0 END) as deposited_to_admin
FROM orders o
LEFT JOIN payment_transactions pt ON pt.order_id = o.id
LEFT JOIN wallet_transactions wt ON wt.related_order_id = o.id AND wt.type = 'DEPOSIT'
WHERE o.payment_status = 'PAID'
GROUP BY o.id, o.total_amount, o.payment_method, o.payment_status, pt.status
ORDER BY o.id DESC
LIMIT 20;

-- ============================================
-- RESULTS INTERPRETATION
-- ============================================
/*
Sau khi chạy script này, check:

1. Admin user tồn tại? 
   - Nếu không → uncomment dòng INSERT admin user

2. Admin wallet tồn tại?
   - Nếu không → uncomment dòng INSERT wallet

3. Balance difference != 0?
   - Nếu có → Script đã tự động fix (UPDATE wallet balance)

4. wallet_tx_count = 0 trong bảng cuối?
   - Nghĩa là có payment thành công nhưng không tạo wallet transaction
   - Check logs để xem có exception không

5. deposited_to_admin = 0?
   - Nghĩa là wallet transaction được tạo nhưng không phải type DEPOSIT
   - Hoặc không link đúng order_id

NEXT STEPS:
1. Chạy script này
2. Restart ứng dụng
3. Test lại payment flow
4. Check logs với emoji mới (🔵 ✅ 💰 etc.)
*/
