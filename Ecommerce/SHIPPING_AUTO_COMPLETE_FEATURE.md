# Tính năng Chờ Giao Hàng và Tự động Hoàn thành Đơn

## Tổng quan

Thêm trạng thái **SHIPPING** (Đang giao hàng) vào Order workflow và tự động hoàn thành đơn hàng sau 1 ngày nếu buyer không xác nhận.

## OrderStatus Flow

```
PENDING (Chờ xác nhận)
    ↓
PROCESSING (Đang xử lý)
    ↓
SHIPPING (Đang giao hàng)  ← Buyer có thể xác nhận đã nhận
    ↓
COMPLETED (Hoàn thành)
```

**Hoặc bất kỳ lúc nào:** → CANCELLED (Đã hủy)

## Các thay đổi

### 1. Order Entity

- **Thêm status mới**: `SHIPPING` vào `Order.OrderStatus` enum
- **Không thêm cột mới**: Tận dụng bảng `shipments` có sẵn với cột `created_at` để track thời gian bắt đầu giao hàng

### 2. OrderService - Methods mới

#### `markAsShipping(Long orderId)`

- **Mục đích**: Seller chuyển đơn hàng sang trạng thái SHIPPING
- **Điều kiện**: Order phải đang ở trạng thái PENDING hoặc PROCESSING
- **Hành động**:
  - Cập nhật `order.status = SHIPPING`
  - Shipment record đã được tạo từ GHN service

#### `confirmReceived(Long orderId, Long userId)`

- **Mục đích**: Buyer xác nhận đã nhận hàng
- **Điều kiện**: Order phải đang ở trạng thái SHIPPING
- **Hành động**: Chuyển `order.status = COMPLETED`

#### `autoCompleteShippingOrders()`

- **Mục đích**: Tự động hoàn thành đơn hàng sau 1 ngày
- **Logic**:
  1. Tìm tất cả orders có `status = SHIPPING`
  2. Kiểm tra `order.shipment.created_at < now() - 1 day`
  3. Nếu đúng → chuyển `order.status = COMPLETED`
- **Được gọi bởi**: Scheduled task mỗi 1 giờ

### 3. API Endpoints

#### Buyer APIs (BuyerOrderController)

**POST** `/api/orders/{id}/confirm-received`

- **Auth**: BUYER role
- **Mô tả**: Buyer xác nhận đã nhận hàng
- **Response**: Order với status = COMPLETED

```json
// Request
POST /api/orders/123/confirm-received
Authorization: Bearer <token>

// Response
{
  "success": true,
  "message": "Xác nhận đã nhận hàng thành công",
  "data": {
    "id": 123,
    "status": "COMPLETED",
    ...
  }
}
```

#### Seller APIs (OrdersController)

**PATCH** `/api/seller/orders/{id}/mark-shipping`

- **Auth**: SELLER role
- **Mô tả**: Seller đánh dấu đơn hàng đang giao
- **Response**: Order với status = SHIPPING

```json
// Request
PATCH /api/seller/orders/123/mark-shipping
Authorization: Bearer <seller-token>

// Response
{
  "success": true,
  "message": "Đã chuyển đơn hàng sang trạng thái đang giao hàng",
  "data": {
    "id": 123,
    "status": "SHIPPING",
    ...
  }
}
```

### 4. Scheduled Task

**File**: `OrderAutoCompleteScheduler.java`

- **Cron**: `0 0 * * * *` (Mỗi giờ vào phút 0)
- **Chức năng**: Gọi `orderService.autoCompleteShippingOrders()`
- **Log**:
  ```
  === Starting auto-complete shipping orders task ===
  Auto-completed order: 123 (shipment created: 2025-11-17T10:00:00)
  Auto-completed 5 orders out of 20 shipping orders
  === Auto-complete task completed successfully ===
  ```

### 5. Database Migration

**File**: `sql/V5_add_shipping_status.sql`

```sql
-- Thêm index cho tìm kiếm nhanh
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);
```

**Lưu ý**:

- Không thêm cột `shipped_at` vào `orders`
- Tận dụng `shipments.created_at` để track thời gian giao hàng
- Nếu dùng ENUM cho `status`, cần uncomment câu lệnh ALTER TABLE

## Workflow Chi tiết

### 1. Buyer đặt hàng và thanh toán

```
Order.status = PENDING
Order.paymentStatus = PAID
```

### 2. Seller xác nhận đơn (optional)

```
Order.status = PROCESSING
```

### 3. Seller tạo vận đơn GHN

```
POST /api/seller/orders/{id}/mark-shipping
→ Order.status = SHIPPING
→ Shipment record được tạo với created_at = now()
```

### 4a. Buyer xác nhận đã nhận (Manual)

```
POST /api/orders/{id}/confirm-received
→ Order.status = COMPLETED
```

### 4b. Tự động hoàn thành sau 1 ngày (Auto)

```
Scheduler chạy mỗi giờ:
→ Tìm orders với status = SHIPPING
→ Check: shipment.created_at < now() - 1 day
→ Order.status = COMPLETED
```

## Testing

### Test Manual Confirm

1. Tạo order và thanh toán → status = PENDING
2. Seller mark shipping → status = SHIPPING
3. Buyer confirm received → status = COMPLETED ✅

### Test Auto Complete

1. Tạo order và mark shipping
2. Update `shipments.created_at = now() - 25 hours`
3. Đợi scheduled task chạy (hoặc restart app)
4. Verify order status = COMPLETED ✅

### Test Query

```sql
-- Tìm orders cần auto-complete
SELECT o.id, o.status, s.created_at
FROM orders o
JOIN shipments s ON o.id = s.order_id
WHERE o.status = 'SHIPPING'
  AND s.created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);
```

## Configuration

Enable scheduling trong `EcommerceApplication.java`:

```java
@SpringBootApplication
@EnableScheduling  // ← Đã thêm
public class EcommerceApplication {
    ...
}
```

## Lợi ích

1. **UX tốt hơn**: Buyer không cần đợi mãi mà vẫn có thể chủ động xác nhận
2. **Tự động hóa**: Giảm công việc thủ công cho admin
3. **Tận dụng DB**: Không thêm cột mới, dùng shipments table có sẵn
4. **Scalable**: Scheduled task chạy định kỳ, không làm chậm API

## Roadmap

- [ ] Notification cho buyer khi order status thay đổi
- [ ] Extend auto-complete time từ 1 ngày sang 3/7 ngày (configurable)
- [ ] Tracking history cho order status changes
