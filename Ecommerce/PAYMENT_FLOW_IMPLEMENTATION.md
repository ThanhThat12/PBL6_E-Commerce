# PAYMENT FLOW SYSTEM IMPLEMENTATION

## Tổng quan
Hệ thống quản lý luồng tiền từ buyer → admin → seller với 3 phương thức thanh toán:
- **MoMo**: Buyer thanh toán qua cổng MoMo
- **SportyPay**: Buyer thanh toán bằng ví (wallet)  
- **COD**: Thanh toán khi nhận hàng

## Kiến trúc luồng tiền

### 1. Buyer → Admin Wallet
Khi buyer thanh toán thành công, tiền sẽ vào ví admin:

#### a. MoMo Payment ✅ (Đã hoàn thành)
- **File**: `CheckoutServiceImpl.java`
- **Method**: `processMomoCallback()`
- **Chức năng**: Khi MoMo callback thành công (resultCode = 0):
  - Cập nhật `payment_transactions` status = SUCCESS
  - Cập nhật `orders`: paymentStatus = PAID  
  - **Gọi `walletService.depositToAdminWallet()`** 
  - Tạo bản ghi `wallet_transactions` với type = DEPOSIT

#### b. SportyPay (Wallet Payment) ⚠️ (Cần bổ sung)
- **File**: `OrderService.java`
- **Method**: `updateOrderAfterWalletPayment()`
- **Cần thêm**:
```java
// Sau khi trừ tiền từ ví buyer, cộng tiền vào ví admin
walletService depositToAdminWallet(
    order.getTotalAmount(),
    order,
    "SPORTYPAY"
);
```

#### c. COD ⚠️ (Cần tạo mới API)
- **Cần tạo endpoint**: `POST /api/admin/orders/{orderId}/confirm-cod`
- **Logic**:
  - Shipper giao hàng và thu tiền
  - Admin xác nhận đã nhận tiền từ shipper
  - Cộng tiền vào ví admin
  - Ghi nhận transaction

### 2. Admin → Seller Wallet
Sau khi đơn hàng hoàn thành và hết thời gian trả hàng, admin chuyển tiền cho seller:

#### Settlement Service (Quyết toán) ⚠️ (Cần tạo mới)
**File mới**: `SettlementService.java`

**Chức năng**:
1. Tìm các đơn hàng đủ điều kiện quyết toán:
   - Status = COMPLETED
   - Đã qua thời gian cho phép trả hàng (ví dụ: 7 ngày sau khi hoàn thành)
   - Chưa được quyết toán
   
2. Tính toán số tiền seller nhận được:
   - `sellerAmount = orderTotal - platformFee`
   - Platform fee có thể lấy từ bảng `platform_fee` theo order ID
   
3. Chuyển tiền từ admin → seller:
   - Gọi `walletService.transferFromAdminToSeller()`
   - Ghi nhận 2 transactions:
     - Admin wallet: type = PAYMENT_TO_SELLER (withdrawal)
     - Seller wallet: type = PAYMENT_TO_SELLER (deposit)

## Các file đã cập nhật

### 1. WalletService.java ✅
**Đã thêm các methods**:
- `depositToAdminWallet()`: Nạp tiền vào ví admin từ buyer
- `transferFromAdminToSeller()`: Chuyển tiền từ admin sang seller
- `getAdminBalance()`: Lấy số dư ví admin
- `getAdminUser()`: Tìm admin user

### 2. CheckoutServiceImpl.java ✅  
**Đã cập nhật**:
- Inject `WalletService`
- Trong `processMomoCallback()`: Thêm logic deposit to admin wallet khi payment success

### 3. WalletTransaction.java ✅
**Đã có enum TransactionType**:
- DEPOSIT
- WITHDRAWAL  
- REFUND
- ORDER_PAYMENT
- PAYMENT_TO_SELLER ✅ (đã có sẵn)
- PLATFORM_FEE

## Các file cần tạo / cập nhật

### 1. SettlementService.java ⚠️ CẦN TẠO
```java
package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PlatformFee;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.PlatformFeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SettlementService {
    
    private final OrderRepository orderRepository;
    private final PlatformFeeRepository platformFeeRepository;
    private final WalletService walletService;
    
    // Constructor injection
    
    /**
     * Tìm các đơn hàng đủ điều kiện quyết toán
     * - Status = COMPLETED
     * - Đã qua returnPeriodDays kể từ khi hoàn thành
     */
    public List<Order> findOrdersEligibleForSettlement(int returnPeriodDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(returnPeriodDays);
        
        // Cần thêm method này vào OrderRepository:
        // findCompletedOrdersBeforeDate(LocalDateTime date)
        return orderRepository.findCompletedOrdersBeforeDate(cutoffDate);
    }
    
    /**
     * Quyết toán cho một đơn hàng
     */
    public void settleOrder(Order order) {
        // Lấy seller ID từ shop
        Long sellerId = order.getShop().getOwner().getId();
        
        // Lấy platform fee
        PlatformFee platformFee = platformFeeRepository.findByOrderId(order.getId())
                .orElse(new PlatformFee()); // Nếu không có fee thì = 0
        
        BigDecimal feeAmount = platformFee.getFeeAmount() != null 
                ? platformFee.getFeeAmount() 
                : BigDecimal.ZERO;
        
        // Tính số tiền seller nhận = tổng đơn hàng - phí sàn
        BigDecimal sellerAmount = order.getTotalAmount().subtract(feeAmount);
        
        // Chuyển tiền từ admin → seller
        walletService.transferFromAdminToSeller(
            sellerId,
            sellerAmount,
            order,
            feeAmount
        );
        
        // Cập nhật trạng thái order (thêm field settled_at)
        // order.setSettledAt(LocalDateTime.now());
        // orderRepository.save(order);
    }
    
    /**
     * Quyết toán hàng loạt (batch settlement)
     */
    public int settleBatch(int returnPeriodDays) {
        List<Order> orders = findOrdersEligibleForSettlement(returnPeriodDays);
        
        int successCount = 0;
        for (Order order : orders) {
            try {
                settleOrder(order);
                successCount++;
            } catch (Exception e) {
                // Log error nhưng tiếp tục xử lý orders khác
                logger.error("Failed to settle order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        return successCount;
    }
}
```

### 2. AdminOrderController.java ⚠️ CẦN BỔ SUNG
Thêm endpoints:

```java
/**
 * Admin xác nhận đã nhận tiền COD cho đơn hàng
 * POST /api/admin/orders/{orderId}/confirm-cod
 */
@PostMapping("/{orderId}/confirm-cod")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseDTO<String>> confirmCodPayment(@PathVariable Long orderId) {
    Order order = orderService.getOrderById(orderId);
    
    // Kiểm tra xem đơn hàng có phải COD không
    if (!"COD".equalsIgnoreCase(order.getMethod())) {
        return ResponseDTO.error(400, "BAD_REQUEST", "Order is not COD");
    }
    
    // Kiểm tra xem đã xác nhận chưa
    if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
        return ResponseDTO.error(400, "BAD_REQUEST", "COD already confirmed");
    }
    
    // Cộng tiền vào ví admin
    walletService.depositToAdminWallet(
        order.getTotalAmount(),
        order,
        "COD"
    );
    
    // Cập nhật trạng thái thanh toán
    order.setPaymentStatus(Order.PaymentStatus.PAID);
    order.setPaidAt(new Date());
    orderRepository.save(order);
    
    return ResponseDTO.success("COD payment confirmed successfully");
}

/**
 * Chạy quyết toán thủ công cho seller
 * POST /api/admin/settlement/run
 */
@PostMapping("/settlement/run")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseDTO<Map<String, Object>>> runSettlement(
        @RequestParam(defaultValue = "7") int returnPeriodDays) {
    
    int settledCount = settlementService.settleBatch(returnPeriodDays);
    
    Map<String, Object> result = new HashMap<>();
    result.put("settledOrdersCount", settledCount);
    result.put("adminWalletBalance", walletService.getAdminBalance());
    
    return ResponseDTO.success(result, "Settlement completed successfully");
}

/**
 * Lấy số dư ví admin
 * GET /api/admin/wallet/balance
 */
@GetMapping("/wallet/balance")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseDTO<BigDecimal>> getAdminWalletBalance() {
    BigDecimal balance = walletService.getAdminBalance();
    return ResponseDTO.success(balance, "Admin wallet balance retrieved successfully");
}
```

### 3. OrderService.java ⚠️ CẦN BỔ SUNG
Thêm inject WalletService và update method:

```java
private final WalletService walletService;

// Trong constructor thêm WalletService

/**
 * Update phương thức này để deposit vào admin wallet
 */
@Transactional
public Order updateOrderAfterWalletPayment(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    // Mark as PAID
    order.setPaymentStatus(Order.PaymentStatus.PAID);
    order.setPaidAt(new Date());
    
    Order saved = orderRepository.save(order);
    
    // 💰 DEPOSIT TO ADMIN WALLET - SportyPay payment
    try {
        walletService.depositToAdminWallet(
            order.getTotalAmount(),
            order,
            "SPORTYPAY"
        );
        logger.info("✅ Successfully deposited {} to admin wallet for order #{}",
                   order.getTotalAmount(), order.getId());
    } catch (Exception e) {
        logger.error("❌ Failed to deposit to admin wallet: {}", e.getMessage(), e);
    }
    
    // Clear cart
    try {
        clearCartAfterSuccessfulPayment(order.getUser().getId(), orderId);
    } catch (Exception e) {
        logger.error("❌ Error clearing cart: {}", e.getMessage());
    }
    
    return saved;
}
```

### 4. OrderRepository.java ⚠️ CẦN BỔ SUNG
Thêm query method:

```java
/**
 * Tìm đơn hàng đã hoàn thành trước một ngày nhất định (cho settlement)
 */
@Query("SELECT o FROM Order o WHERE o.status = 'COMPLETED' " +
       "AND o.updatedAt < :cutoffDate " +
       "AND o.settledAt IS NULL")
List<Order> findCompletedOrdersBeforeDate(@Param("cutoffDate") LocalDateTime cutoffDate);
```

### 5. Order.java ⚠️ CẦN BỔ SUNG  
Thêm field để track settlement:

```java
@Column(name = "settled_at")
@Temporal(TemporalType.TIMESTAMP)
private Date settledAt; // Thời điểm đã quyết toán cho seller

// Getter/Setter
```

### 6. Migration SQL ⚠️ CẦN TẠO
Tạo file migration để thêm column `settled_at`:

```sql
ALTER TABLE orders ADD COLUMN settled_at TIMESTAMP NULL;
```

## Scheduled Job (Tự động quyết toán)

Tạo `SettlementScheduler.java` để tự động chạy quyết toán định kỳ:

```java
package com.PBL6.Ecommerce.scheduler;

import com.PBL6.Ecommerce.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SettlementScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
    
    private final SettlementService settlementService;
    
    public SettlementScheduler(SettlementService settlementService) {
        this.settlementService = settlementService;
    }
    
    /**
     * Chạy quyết toán tự động mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM mỗi ngày
    public void runDailySettlement() {
        logger.info("Starting daily settlement job");
        
        int returnPeriodDays = 7; // 7 ngày sau khi hoàn thành
        int settledCount = settlementService.settleBatch(returnPeriodDays);
        
        logger.info("Daily settlement completed. Settled {} orders", settledCount);
    }
}
```

## Testing Checklist

### 1. MoMo Payment Flow ✅
- [ ] Buyer tạo order qua MoMo
- [ ] MoMo callback thành công
- [ ] Check `payment_transactions` status = SUCCESS
- [ ] Check `orders` paymentStatus = PAID
- [ ] Check `wallets` admin balance tăng lên
- [ ] Check `wallet_transactions` có bản ghi DEPOSIT cho admin

### 2. SportyPay Flow ⚠️
- [ ] Buyer tạo order, thanh toán bằng SportyPay
- [ ] Trừ tiền từ ví buyer
- [ ] Cộng tiền vào ví admin
- [ ] Check `wallet_transactions` đầy đủ

### 3. COD Flow ⚠️
- [ ] Buyer tạo order COD
- [ ] Shipper giao hàng thu tiền
- [ ] Admin xác nhận qua API `/api/admin/orders/{orderId}/confirm-cod`
- [ ] Check admin wallet tăng lên
- [ ] Check `wallet_transactions`

### 4. Settlement Flow ⚠️
- [ ] Order status = COMPLETED
- [ ] Sau 7 ngày, chạy settlement (manual hoặc scheduled)
- [ ] Check `platform_fee` được tính đúng
- [ ] Check admin wallet giảm
- [ ] Check seller wallet tăng
- [ ] Check `wallet_transactions` cho cả admin và seller

## Database Schema

### wallets
```sql
CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### wallet_transactions  
```sql
CREATE TABLE wallet_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('DEPOSIT', 'WITHDRAWAL', 'REFUND', 'ORDER_PAYMENT', 'PAYMENT_TO_SELLER', 'PLATFORM_FEE') NOT NULL,
    description TEXT,
    related_order_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    FOREIGN KEY (related_order_id) REFERENCES orders(id)
);
```

### platform_fees (nếu chưa có)
```sql
CREATE TABLE platform_fees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    fee_amount DECIMAL(15,2) NOT NULL,
    fee_percentage DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

## API Endpoints Summary

### Admin APIs
- `POST /api/admin/orders/{orderId}/confirm-cod` - Xác nhận nhận tiền COD
- `POST /api/admin/settlement/run` - Chạy quyết toán thủ công
- `GET /api/admin/wallet/balance` - Xem số dư ví admin

### Wallet APIs (existing)
- `GET /api/wallet/balance` - Xem số dư ví (buyer/seller)
- `GET /api/wallet/transactions` - Lịch sử giao dịch

## Các bước implement tiếp theo

1. ✅ **Đã hoàn thành**:
   - WalletService với admin wallet management
   - MoMo callback deposit to admin

2. ⚠️ **Cần làm tiếp**:
   - Cập nhật OrderService.updateOrderAfterWalletPayment() (SportyPay)
   - Tạo SettlementService
   - Tạo AdminOrderController endpoints
   - Thêm settled_at column vào orders table
   - Tạo SettlementScheduler
   - Testing toàn bộ flow

## Notes
- Tất cả wallet operations đều có transaction logging
- Admin wallet được tự động tạo khi cần (getOrCreateWallet)
- Platform fee calculation cần được config rõ ràng
- Cần có monitoring/alerting cho settlement job
- Xem xét thêm retry mechanism cho failed settlements
