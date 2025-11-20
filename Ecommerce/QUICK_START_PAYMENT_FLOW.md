# QUICK START GUIDE - Hoàn thiện hệ thống thanh toán

## Bước 1: Update OrderService (SportyPay) - 5 phút

### File: `OrderService.java`

1. **Thêm WalletService vào constructor**:
```java
private final WalletService walletService; // Thêm field này

public OrderService(...existing params..., WalletService walletService) {
    // ...existing assignments...
    this.walletService = walletService; // Thêm dòng này
}
```

2. **Update method `updateOrderAfterWalletPayment`**:

Tìm method này và thêm code sau vào ngay sau dòng `Order saved = orderRepository.save(order);`:

```java
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
```

**✅ Xong! SportyPay flow hoàn thành.**

---

## Bước 2: Tạo Admin Payment Controller - 30 phút

### File mới: `AdminPaymentController.java`

Tạo file trong `src/main/java/com/PBL6/Ecommerce/controller/`:

```java
package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.service.OrderService;
import com.PBL6.Ecommerce.service.SettlementService;
import com.PBL6.Ecommerce.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminPaymentController.class);
    
    private final WalletService walletService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final SettlementService settlementService;
    
    public AdminPaymentController(WalletService walletService,
                                 OrderService orderService,
                                 OrderRepository orderRepository,
                                 SettlementService settlementService) {
        this.walletService = walletService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.settlementService = settlementService;
    }
    
    /**
     * Endpoint 1: Xác nhận đã nhận tiền COD
     * POST /api/admin/payment/confirm-cod/{orderId}
     */
    @PostMapping("/confirm-cod/{orderId}")
    public ResponseEntity<ResponseDTO<String>> confirmCodPayment(@PathVariable Long orderId) {
        try {
            logger.info("Admin confirming COD payment for order: {}", orderId);
            
            Order order = orderService.getOrderById(orderId);
            
            // Validate order is COD
            if (!"COD".equalsIgnoreCase(order.getMethod())) {
                return ResponseDTO.error(400, "BAD_REQUEST", 
                    "Order #" + orderId + " is not a COD order. Payment method: " + order.getMethod());
            }
            
            // Check if already confirmed
            if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
                return ResponseDTO.error(400, "BAD_REQUEST", 
                    "COD payment for order #" + orderId + " already confirmed");
            }
            
            // Deposit to admin wallet
            walletService.depositToAdminWallet(
                order.getTotalAmount(),
                order,
                "COD"
            );
            
            // Update order payment status
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setPaidAt(new Date());
            orderRepository.save(order);
            
            logger.info("✅ COD payment confirmed for order #{}, amount: {}", 
                       orderId, order.getTotalAmount());
            
            return ResponseDTO.success(null, 
                "COD payment confirmed successfully for order #" + orderId);
            
        } catch (Exception e) {
            logger.error("Error confirming COD payment for order {}: {}", orderId, e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", 
                "Failed to confirm COD payment: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint 2: Chạy quyết toán thủ công
     * POST /api/admin/payment/settlement/run?returnPeriodDays=7
     */
    @PostMapping("/settlement/run")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> runSettlement(
            @RequestParam(defaultValue = "7") int returnPeriodDays) {
        try {
            logger.info("Admin running settlement with return period: {} days", returnPeriodDays);
            
            // Get summary before settlement
            SettlementService.SettlementSummary beforeSummary = 
                settlementService.getSettlementSummary(returnPeriodDays);
            
            // Run settlement
            int settledCount = settlementService.settleBatch(returnPeriodDays);
            
            // Get admin wallet balance after settlement
            BigDecimal adminBalance = walletService.getAdminBalance();
            
            // Prepare response
            Map<String, Object> result = new HashMap<>();
            result.put("settledOrdersCount", settledCount);
            result.put("totalSellerPayout", beforeSummary.getTotalSellerPayout());
            result.put("totalPlatformFees", beforeSummary.getTotalPlatformFees());
            result.put("adminWalletBalance", adminBalance);
            result.put("returnPeriodDays", returnPeriodDays);
            
            logger.info("✅ Settlement completed: {} orders settled, total payout: {}", 
                       settledCount, beforeSummary.getTotalSellerPayout());
            
            return ResponseDTO.success(result, 
                "Settlement completed successfully. " + settledCount + " orders settled.");
            
        } catch (Exception e) {
            logger.error("Error running settlement: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", 
                "Failed to run settlement: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint 3: Xem số dư ví admin
     * GET /api/admin/payment/wallet/balance
     */
    @GetMapping("/wallet/balance")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getAdminWalletBalance() {
        try {
            BigDecimal balance = walletService.getAdminBalance();
            
            Map<String, Object> result = new HashMap<>();
            result.put("balance", balance);
            result.put("currency", "VND");
            
            return ResponseDTO.success(result, "Admin wallet balance retrieved successfully");
            
        } catch (Exception e) {
            logger.error("Error getting admin wallet balance: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", 
                "Failed to get admin wallet balance: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint 4: Xem summary các orders sẽ được quyết toán
     * GET /api/admin/payment/settlement/preview?returnPeriodDays=7
     */
    @GetMapping("/settlement/preview")
    public ResponseEntity<ResponseDTO<SettlementService.SettlementSummary>> getSettlementPreview(
            @RequestParam(defaultValue = "7") int returnPeriodDays) {
        try {
            SettlementService.SettlementSummary summary = 
                settlementService.getSettlementSummary(returnPeriodDays);
            
            return ResponseDTO.success(summary, 
                "Found " + summary.getEligibleOrdersCount() + " orders eligible for settlement");
            
        } catch (Exception e) {
            logger.error("Error getting settlement preview: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", 
                "Failed to get settlement preview: " + e.getMessage());
        }
    }
}
```

**✅ Xong! Admin APIs hoàn thành.**

---

## Bước 3: Test toàn bộ flow - 30 phút

### Test 1: MoMo Payment (Đã ready)
```bash
# Tạo order với MoMo
POST http://localhost:8080/api/payment/momo/create
{
  "orderId": 1,
  "orderInfo": "Test order"
}

# Thanh toán trên MoMo và đợi callback
# Kiểm tra logs: "✅ Successfully deposited... to admin wallet"

# Check database
SELECT * FROM wallet_transactions 
WHERE description LIKE '%MOMO%' 
ORDER BY created_at DESC LIMIT 1;
```

### Test 2: SportyPay Payment (Sau khi update OrderService)
```bash
# Tạo order với SportyPay
POST http://localhost:8080/api/checkout/sportypay
{
  "userId": 1,
  "items": [...],
  "totalAmount": 50000
}

# Kiểm tra logs: "✅ Successfully deposited... to admin wallet"

# Check database
SELECT * FROM wallet_transactions 
WHERE description LIKE '%SPORTYPAY%' 
ORDER BY created_at DESC LIMIT 1;
```

### Test 3: COD Confirmation
```bash
# Xác nhận COD
POST http://localhost:8080/api/admin/payment/confirm-cod/123
Authorization: Bearer {admin_token}

# Response:
{
  "status": "success",
  "message": "COD payment confirmed successfully for order #123"
}

# Check database
SELECT * FROM orders WHERE id = 123; -- paymentStatus = PAID
SELECT * FROM wallet_transactions 
WHERE description LIKE '%COD%' 
ORDER BY created_at DESC LIMIT 1;
```

### Test 4: Settlement
```bash
# Preview settlement
GET http://localhost:8080/api/admin/payment/settlement/preview?returnPeriodDays=7
Authorization: Bearer {admin_token}

# Response sẽ show:
{
  "eligibleOrdersCount": 5,
  "totalOrderAmount": 500000,
  "totalPlatformFees": 50000,
  "totalSellerPayout": 450000
}

# Chạy settlement
POST http://localhost:8080/api/admin/payment/settlement/run?returnPeriodDays=7
Authorization: Bearer {admin_token}

# Check database
SELECT * FROM wallet_transactions 
WHERE type = 'PAYMENT_TO_SELLER' 
ORDER BY created_at DESC;
```

### Test 5: Check Admin Wallet Balance
```bash
GET http://localhost:8080/api/admin/payment/wallet/balance
Authorization: Bearer {admin_token}

# Response:
{
  "balance": 1500000.00,
  "currency": "VND"
}
```

---

## Bước 4 (Optional): Auto Settlement Scheduler - 15 phút

### File mới: `SettlementScheduler.java`

Tạo trong `src/main/java/com/PBL6/Ecommerce/scheduler/`:

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
    private static final int RETURN_PERIOD_DAYS = 7;
    
    private final SettlementService settlementService;
    
    public SettlementScheduler(SettlementService settlementService) {
        this.settlementService = settlementService;
    }
    
    /**
     * Chạy quyết toán tự động mỗi ngày lúc 2:00 AM
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM mỗi ngày
    public void runDailySettlement() {
        logger.info("🔄 Starting daily automatic settlement (return period: {} days)", RETURN_PERIOD_DAYS);
        
        try {
            int settledCount = settlementService.settleBatch(RETURN_PERIOD_DAYS);
            logger.info("✅ Daily settlement completed successfully. {} orders settled", settledCount);
        } catch (Exception e) {
            logger.error("❌ Daily settlement failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Optional: Chạy mỗi giờ để demo (comment out nếu không cần)
     */
    // @Scheduled(cron = "0 0 * * * *") // Mỗi giờ
    public void runHourlySettlement() {
        logger.info("🔄 Running hourly settlement for testing");
        try {
            int settledCount = settlementService.settleBatch(RETURN_PERIOD_DAYS);
            logger.info("✅ Hourly settlement: {} orders settled", settledCount);
        } catch (Exception e) {
            logger.error("❌ Hourly settlement failed: {}", e.getMessage(), e);
        }
    }
}
```

### Enable Scheduling

Thêm vào `Application.java` hoặc config class:

```java
@EnableScheduling
public class EcommerceApplication {
    // ...
}
```

**✅ Xong! Auto settlement hoàn thành.**

---

## Tổng kết

### ✅ Sau khi làm xong 3 bước:
1. MoMo → Admin Wallet: **Hoạt động**
2. SportyPay → Admin Wallet: **Hoạt động**
3. COD → Admin Wallet: **Hoạt động** (với admin confirmation)
4. Admin → Seller Wallet: **Hoạt động** (manual + auto)

### 🎯 API Endpoints mới:
- `POST /api/admin/payment/confirm-cod/{orderId}`
- `POST /api/admin/payment/settlement/run`
- `GET /api/admin/payment/wallet/balance`
- `GET /api/admin/payment/settlement/preview`

### 📊 Database Tracking:
Tất cả giao dịch được ghi nhận trong `wallet_transactions`:
- Type: DEPOSIT (buyer → admin)
- Type: PAYMENT_TO_SELLER (admin → seller)
- Related order ID
- Timestamp
- Description

---

## Troubleshooting

### Lỗi: Admin user not found
**Nguyên nhân**: Chưa có user với role ADMIN
**Giải pháp**: Tạo admin user trong database:
```sql
INSERT INTO users (username, email, role, activated, password) 
VALUES ('admin', 'admin@example.com', 'ADMIN', true, '...');
```

### Lỗi: Insufficient balance (admin wallet)
**Nguyên nhân**: Ví admin không đủ tiền để chuyển cho seller
**Giải pháp**: 
1. Kiểm tra xem đã deposit vào admin wallet chưa
2. Check wallet_transactions để xem luồng tiền
3. Verify platform_fee không lớn hơn order total

### Lỗi: Settlement không chạy
**Nguyên nhân**: Orders chưa đủ điều kiện
**Kiểm tra**:
- Order status = COMPLETED?
- Payment status = PAID?
- updatedAt > returnPeriodDays?

---

**Chúc bạn implement thành công! 🎉**
