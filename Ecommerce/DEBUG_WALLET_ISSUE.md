# DEBUG: Wallet Transaction có nhưng balance không tăng

## Vấn đề
- ✅ Wallet transaction được tạo thành công
- ❌ Admin wallet balance không tăng

## Các nguyên nhân có thể

### 1. Không có Admin User trong database
**Triệu chứng**: Exception "Admin user not found" bị catch đi

**Kiểm tra**:
```sql
SELECT * FROM users WHERE role = 'ADMIN';
```

**Giải pháp**: Tạo admin user nếu chưa có:
```sql
-- Tạo admin user mới (password: admin123)
INSERT INTO users (username, email, password, role, activated, created_at) 
VALUES (
    'admin',
    'admin@ecommerce.com',
    '$2a$10$slYQm8tOjj.HmFD8iXH8BuVzN8Xq4Q5rZnFZFkO9YPBQV.VSkWKLC', -- bcrypt hash of 'admin123'
    'ADMIN',
    1,
    NOW()
);
```

### 2. Transaction bị rollback do exception
**Triệu chứng**: WalletTransaction được tạo nhưng Wallet không được save

**Kiểm tra logs**: Tìm trong console/logs:
```
[ERROR] Failed to deposit to admin wallet
```

### 3. Wallet balance được cập nhật nhưng không save
**Nguyên nhân**: Có thể do caching hoặc transaction isolation

## 🔧 Giải pháp

### Bước 1: Verify Admin User exists
```sql
-- Check admin user
SELECT id, username, email, role FROM users WHERE role = 'ADMIN';

-- Nếu không có, tạo admin
INSERT INTO users (username, email, password, role, activated, created_at) 
VALUES ('admin', 'admin@ecommerce.com', 'hashed_password', 'ADMIN', 1, NOW());
```

### Bước 2: Thêm debug logs vào WalletService

Update method `depositToAdminWallet()`:

```java
@Transactional
public Wallet depositToAdminWallet(BigDecimal amount, Order order, String paymentMethod) {
    try {
        logger.info("🔵 [START] depositToAdminWallet - amount: {}, order: {}, method: {}", 
                   amount, order.getId(), paymentMethod);
        
        // 1. Get admin user
        User adminUser = getAdminUser();
        logger.info("✅ Found admin user: {} (ID: {})", adminUser.getUsername(), adminUser.getId());
        
        // 2. Get or create admin wallet
        Wallet adminWallet = getOrCreateWallet(adminUser);
        BigDecimal oldBalance = adminWallet.getBalance();
        logger.info("📊 Admin wallet ID: {}, Current balance: {}", adminWallet.getId(), oldBalance);
        
        // 3. Update balance
        adminWallet.deposit(amount);
        logger.info("💰 After deposit: {}", adminWallet.getBalance());
        
        // 4. Save wallet
        adminWallet = walletRepository.save(adminWallet);
        logger.info("💾 Wallet saved. New balance in DB: {}", adminWallet.getBalance());
        
        // 5. Create transaction record
        WalletTransaction transaction = new WalletTransaction(
            adminWallet,
            WalletTransaction.TransactionType.DEPOSIT,
            amount,
            String.format("Nhận thanh toán từ đơn hàng #%d qua %s", order.getId(), paymentMethod)
        );
        transaction.setRelatedOrder(order);
        WalletTransaction savedTx = walletTransactionRepository.save(transaction);
        logger.info("📝 Transaction saved with ID: {}", savedTx.getId());
        
        // 6. Verify balance
        Wallet verifyWallet = walletRepository.findById(adminWallet.getId()).orElse(null);
        logger.info("✅ [SUCCESS] Admin wallet deposit completed. Balance in DB: {}", 
                   verifyWallet != null ? verifyWallet.getBalance() : "NULL");
        
        return adminWallet;
        
    } catch (Exception e) {
        logger.error("❌ [ERROR] depositToAdminWallet failed: {}", e.getMessage(), e);
        throw e;
    }
}
```

### Bước 3: Check lại Wallet.deposit() method

Verify trong `Wallet.java`:

```java
public void deposit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Deposit amount must be positive");
    }
    this.balance = this.balance.add(amount);
    this.updatedAt = LocalDateTime.now();
    
    // DEBUG LOG
    logger.info("💵 Wallet.deposit called: old={}, add={}, new={}", 
               this.balance.subtract(amount), amount, this.balance);
}
```

### Bước 4: Force flush sau khi save

Thêm `@Transactional` và flush:

```java
@Transactional
public Wallet depositToAdminWallet(BigDecimal amount, Order order, String paymentMethod) {
    // ... existing code ...
    
    // Save and flush to force DB write
    adminWallet = walletRepository.saveAndFlush(adminWallet);
    
    // ... rest of code ...
}
```

### Bước 5: Kiểm tra query trực tiếp

Sau khi thanh toán, chạy query:

```sql
-- Check wallet
SELECT w.id, w.user_id, w.balance, u.username, u.role
FROM wallets w
JOIN users u ON w.user_id = u.id
WHERE u.role = 'ADMIN';

-- Check transactions
SELECT wt.*, w.balance as wallet_balance
FROM wallet_transactions wt
JOIN wallets w ON wt.wallet_id = w.id
JOIN users u ON w.user_id = u.id
WHERE u.role = 'ADMIN'
ORDER BY wt.created_at DESC
LIMIT 5;

-- So sánh
SELECT 
    w.balance as current_balance,
    COALESCE(SUM(CASE WHEN wt.type IN ('DEPOSIT', 'REFUND') THEN wt.amount ELSE 0 END), 0) as total_in,
    COALESCE(SUM(CASE WHEN wt.type IN ('WITHDRAWAL', 'PAYMENT_TO_SELLER', 'ORDER_PAYMENT') THEN wt.amount ELSE 0 END), 0) as total_out
FROM wallets w
LEFT JOIN wallet_transactions wt ON wt.wallet_id = w.id
WHERE w.user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1)
GROUP BY w.id, w.balance;
```

## 🔍 Testing checklist

1. ✅ Check có admin user không
```sql
SELECT * FROM users WHERE role = 'ADMIN';
```

2. ✅ Check admin wallet tồn tại
```sql
SELECT * FROM wallets WHERE user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);
```

3. ✅ Tạo order và thanh toán
4. ✅ Check logs xem có lỗi không
5. ✅ Check wallet_transactions có record mới
```sql
SELECT * FROM wallet_transactions ORDER BY created_at DESC LIMIT 1;
```

6. ✅ Check wallet balance
```sql
SELECT balance FROM wallets WHERE user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);
```

## 🎯 Quick Fix

Nếu wallet balance không được cập nhật, có thể manually update:

```sql
-- Tính tổng deposits cho admin wallet
SET @admin_user_id = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1);
SET @admin_wallet_id = (SELECT id FROM wallets WHERE user_id = @admin_user_id);

-- Tính balance đúng từ transactions
UPDATE wallets w
SET w.balance = (
    SELECT COALESCE(
        SUM(CASE 
            WHEN wt.type IN ('DEPOSIT', 'REFUND', 'PAYMENT_TO_SELLER') THEN wt.amount
            WHEN wt.type IN ('WITHDRAWAL', 'ORDER_PAYMENT') THEN -wt.amount
            ELSE 0
        END), 
        0
    )
    FROM wallet_transactions wt
    WHERE wt.wallet_id = w.id
)
WHERE w.id = @admin_wallet_id;

-- Verify
SELECT * FROM wallets WHERE id = @admin_wallet_id;
```

## 💡 Lưu ý

1. **TransactionType mapping**:
   - DEPOSIT: Tăng balance (+)
   - REFUND: Tăng balance (+)
   - WITHDRAWAL: Giảm balance (-)
   - ORDER_PAYMENT: Giảm balance (-)
   - PAYMENT_TO_SELLER: 
     - Admin wallet: Giảm (-)
     - Seller wallet: Tăng (+)

2. **Check @Transactional**:
   - Đảm bảo method có `@Transactional`
   - Nếu exception được catch, transaction có thể bị rollback

3. **Isolation level**:
   - Default là READ_COMMITTED
   - Có thể cần REPEATABLE_READ cho consistency
