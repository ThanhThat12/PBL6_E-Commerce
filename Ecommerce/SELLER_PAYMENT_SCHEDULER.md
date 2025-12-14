# Hệ Thống Tự Động Chuyển Tiền Cho Seller

## 📋 Tổng Quan

Hệ thống tự động xử lý chuyển tiền từ admin wallet sang seller wallet sau khi đơn hàng được hoàn thành (customer xác nhận đã nhận hàng).

## 🔄 Luồng Hoạt Động

### 1. Khi Customer Xác Nhận Đã Nhận Hàng
- Order status → `COMPLETED`
- `updated_at` được cập nhật thành thời gian hiện tại

### 2. Scheduler Tự Động Xử Lý
- **Tần suất chạy**: Mỗi 30 giây (có thể điều chỉnh)
- **Thời gian chờ**: 2 phút sau `updated_at` (có thể điều chỉnh)
- **Điều kiện**:
  - Order có status = `COMPLETED`
  - `updated_at` < (thời gian hiện tại - 2 phút)
  - Chưa có transaction `PAYMENT_TO_SELLER` cho order này

### 3. Tính Toán Số Tiền
- **Seller nhận**: 90% của `total_amount`
- **Phí nền tảng**: 10% của `total_amount`

### 4. Tạo Giao Dịch

#### Transaction 1: PAYMENT_TO_SELLER (Admin → Seller)
```
Type: PAYMENT_TO_SELLER
Wallet: Admin wallet (user_id = 1)
Amount: -(total_amount × 0.90)
Effect: Giảm balance của admin, tăng balance của seller
Description: "Chuyển tiền cho seller #{seller_id} - đơn hàng #{order_id} (đã trừ 10% phí dịch vụ)"
```

#### Transaction 2: PAYMENT_TO_SELLER (Seller Receive)
```
Type: PAYMENT_TO_SELLER
Wallet: Seller wallet
Amount: +(total_amount × 0.90)
Effect: Tăng balance của seller
Description: "Nhận tiền từ đơn hàng #{order_id} (90% sau khi trừ phí nền tảng)"
```

#### Transaction 3: PLATFORM_FEE (Tracking Only)
```
Type: PLATFORM_FEE
Wallet: Admin wallet (user_id = 1)
Amount: +(total_amount × 0.10)
Effect: Không thay đổi balance (chỉ để thống kê)
Description: "Phí nền tảng từ đơn hàng #{order_id} (10%)"
```

## 📁 Files Được Tạo/Cập Nhật

### 1. WalletTransactionScheduler.java
**Path**: `src/main/java/com/PBL6/Ecommerce/scheduler/WalletTransactionScheduler.java`

**Chức năng**:
- Scheduled task chạy định kỳ mỗi 30 giây
- Tìm orders đủ điều kiện để chuyển tiền
- Gọi `walletService.transferFromAdminToSeller()` cho mỗi order

**Cấu hình**:
```java
// Thời gian chờ (đơn vị: phút)
private static final int WAITING_PERIOD_MINUTES = 2;

// Cron expression (chạy mỗi 30 giây)
@Scheduled(cron = "*/30 * * * * *")

// Production: chạy mỗi 5 phút
// @Scheduled(cron = "0 */5 * * * *")
```

### 2. WalletService.java
**Path**: `src/main/java/com/PBL6/Ecommerce/service/WalletService.java`

**Method mới**: `transferFromAdminToSeller(Long sellerId, BigDecimal totalAmount, Order order)`

**Chức năng**:
- Kiểm tra admin wallet có đủ balance
- Tính toán 90% cho seller, 10% platform fee
- Tạo 3 transactions:
  1. Admin PAYMENT_TO_SELLER (rút tiền)
  2. Seller PAYMENT_TO_SELLER (nhận tiền)
  3. Admin PLATFORM_FEE (tracking)
- Transaction đảm bảo atomic (rollback nếu lỗi)

### 3. OrderRepository.java
**Path**: `src/main/java/com/PBL6/Ecommerce/repository/OrderRepository.java`

**Query mới**: `findCompletedOrdersReadyForPayment(Date cutoffTime)`

**Chức năng**:
- Tìm orders với:
  - status = `COMPLETED`
  - `updated_at` < cutoffTime
  - Chưa có transaction `PAYMENT_TO_SELLER`

### 4. WalletTransactionRepository.java
**Path**: `src/main/java/com/PBL6/Ecommerce/repository/WalletTransactionRepository.java`

**Query mới**: `existsPaymentToSellerForOrder(Long orderId)`

**Chức năng**: Kiểm tra order đã được chuyển tiền chưa

## ⚙️ Cách Điều Chỉnh Thời Gian

### Thay đổi thời gian chờ

Trong `WalletTransactionScheduler.java`:
```java
// Đổi từ 2 phút sang thời gian khác
private static final int WAITING_PERIOD_MINUTES = 2; // Ví dụ: 1440 cho 24 giờ
```

### Thay đổi tần suất chạy

```java
// Mỗi 30 giây (testing)
@Scheduled(cron = "*/30 * * * * *")

// Mỗi 1 phút
@Scheduled(cron = "0 * * * * *")

// Mỗi 5 phút
@Scheduled(cron = "0 */5 * * * *")

// Mỗi 10 phút
@Scheduled(cron = "0 */10 * * * *")

// Mỗi giờ
@Scheduled(cron = "0 0 * * * *")
```

### Cron Expression Format
```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12)
│ │ │ │ │ ┌───────────── day of week (0-7)
│ │ │ │ │ │
* * * * * *
```

## 🔍 Logging

Scheduler có logging chi tiết:

```
🔄 [Scheduler] Starting seller payment processing...
📅 [Scheduler] Cutoff time: 2024-01-15 10:00:00 (waiting period: 2 minutes)
📦 [Scheduler] Found 3 orders ready for seller payment
💰 [Scheduler] Processing order #123...
✅ [Scheduler] Successfully processed order #123
🎯 [Scheduler] Completed seller payment processing. Success: 3, Failed: 0, Total: 3
```

## 📊 Ví Dụ

### Kịch Bản:
- Customer mua hàng 1,000,000 VND
- Order #100, Shop ID: 5
- Customer xác nhận nhận hàng lúc 10:00:00
- Scheduler chạy lúc 10:02:30 (sau 2 phút 30 giây)

### Kết Quả:

#### Admin Wallet Transactions:
| ID | Type | Amount | Description | Balance Change |
|----|------|--------|-------------|----------------|
| 201 | PAYMENT_TO_SELLER | -900,000 | Chuyển tiền cho seller #5 - đơn hàng #100 (đã trừ 10% phí dịch vụ) | -900,000 |
| 202 | PLATFORM_FEE | +100,000 | Phí nền tảng từ đơn hàng #100 (10%) | 0 (tracking only) |

#### Seller Wallet Transactions:
| ID | Type | Amount | Description | Balance Change |
|----|------|--------|-------------|----------------|
| 203 | PAYMENT_TO_SELLER | +900,000 | Nhận tiền từ đơn hàng #100 (90% sau khi trừ phí nền tảng) | +900,000 |

### Tổng Kết:
- Admin balance: Giảm 900,000 VND
- Seller balance: Tăng 900,000 VND
- Platform fee tracked: 100,000 VND

## ⚠️ Lưu Ý

1. **Transaction Atomic**: Tất cả 3 transactions được tạo trong 1 transaction DB, nếu 1 cái fail thì rollback hết

2. **Duplicate Prevention**: Sử dụng query `NOT EXISTS (PAYMENT_TO_SELLER)` để đảm bảo không xử lý đơn hàng 2 lần

3. **Balance Check**: Kiểm tra admin wallet có đủ tiền trước khi chuyển

4. **Error Handling**: Nếu 1 order fail, scheduler tiếp tục xử lý các order khác

5. **Timezone**: Sử dụng server timezone, đảm bảo cấu hình đúng

## 🚀 Production Deployment

1. **Thay đổi tần suất chạy** từ 30 giây sang 5 phút:
```java
@Scheduled(cron = "0 */5 * * * *")
```

2. **Tăng thời gian chờ** từ 2 phút lên ít nhất 24 giờ:
```java
private static final int WAITING_PERIOD_MINUTES = 1440; // 24 hours
```

3. **Enable monitoring**: Theo dõi logs để đảm bảo scheduler hoạt động đúng

4. **Database indexes**: Đảm bảo có index trên:
   - `orders.status`
   - `orders.updated_at`
   - `wallet_transactions.related_order_id`
   - `wallet_transactions.type`

## 🔧 Troubleshooting

### Scheduler không chạy?
- Kiểm tra `@EnableScheduling` trong `EcommerceApplication.java`
- Kiểm tra logs có xuất hiện "Starting seller payment processing" không

### Orders không được xử lý?
- Kiểm tra `updated_at` đã đủ thời gian chờ chưa
- Kiểm tra query `findCompletedOrdersReadyForPayment()` có trả về results không
- Kiểm tra đã có transaction `PAYMENT_TO_SELLER` cho order đó chưa

### Admin wallet không đủ tiền?
- Kiểm tra admin balance trong DB
- Cân nhắc tăng admin balance hoặc điều chỉnh logic business

### Transaction bị duplicate?
- Kiểm tra query `NOT EXISTS` trong `findCompletedOrdersReadyForPayment()`
- Kiểm tra `@Transactional` annotation có hoạt động không
