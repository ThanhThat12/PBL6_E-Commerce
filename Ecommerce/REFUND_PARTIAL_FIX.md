# Fix Logic Refund - Hỗ trợ Partial Refund (Refund từng phần)

## ❌ Vấn đề cũ

Khi khách hàng tạo refund cho **1 món** trong đơn hàng có **nhiều món**, hệ thống đang:

- Hủy **toàn bộ đơn hàng** (set status = CANCELLED)
- Không lưu thông tin món hàng cụ thể nào được refund
- Không thể quản lý số lượng refund cho từng món

**Ví dụ:**

- Đơn hàng #29: 2 mũ + 1 áo
- Khách muốn trả 1 mũ
- Hệ thống cũ: Hủy toàn bộ đơn → 2 mũ + 1 áo đều bị hủy ❌

## ✅ Giải pháp mới

### 1. Tạo bảng `refund_items`

Lưu chi tiết món hàng được refund:

```sql
CREATE TABLE refund_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,              -- Số lượng trả
    refund_amount DECIMAL(15,2) NOT NULL, -- Số tiền hoàn cho món này
    reason VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (refund_id) REFERENCES refunds(id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);
```

### 2. Thêm entity `RefundItem`

```java
@Entity
@Table(name = "refund_items")
public class RefundItem {
    @ManyToOne Refund refund;
    @ManyToOne OrderItem orderItem;
    Integer quantity;           // Số lượng trả
    BigDecimal refundAmount;    // Số tiền hoàn
    String reason;
}
```

### 3. Cập nhật `Refund` entity

```java
@OneToMany(mappedBy = "refund")
private List<RefundItem> refundItems = new ArrayList<>();
```

### 4. Logic xử lý mới trong `RefundService`

#### A. Tạo refund với danh sách món cụ thể

```java
public Refund createRefundRequestWithItems(
    Order order,
    Map<Long, Integer> refundItemsData,  // Map<OrderItemId, Quantity>
    String reason,
    String imageUrl
)
```

**Cách dùng:**

```java
// Ví dụ: Trả 1 mũ (orderItemId=10, quantity=1)
Map<Long, Integer> items = new HashMap<>();
items.put(10L, 1); // OrderItem ID 10, trả 1 cái

Refund refund = refundService.createRefundRequestWithItems(
    order,
    items,
    "Sản phẩm bị lỗi",
    "https://example.com/image.jpg"
);
```

#### B. Xử lý refund thông minh

```java
public void processRefund(Refund refund) {
    // ...

    // Kiểm tra full refund hay partial refund
    boolean isFullRefund = checkIfFullRefund(refund);

    if (isFullRefund) {
        // Refund toàn bộ → Hủy đơn
        order.setStatus(CANCELLED);
    } else {
        // Refund một phần → GIỮ NGUYÊN trạng thái đơn
        logger.info("Partial refund - Order remains active");
    }
}
```

**Logic kiểm tra Full Refund:**

1. **Nếu có `refundItems`:**

   - Đếm số món và số lượng đã refund
   - So sánh với tổng món và tổng số lượng trong đơn
   - Full refund = Refund hết tất cả món + số lượng

2. **Nếu không có `refundItems` (backward compatible):**
   - So sánh `refund.amount` với `order.totalAmount`
   - Full refund = refund.amount ≥ order.totalAmount (cho phép sai số ±1000 VND)

## 📝 Cách sử dụng

### Cách 1: Refund toàn bộ đơn (như cũ)

```java
refundService.createRefundRequest(
    order,
    order.getTotalAmount(),
    "Hủy toàn bộ đơn",
    null
);
```

→ Đơn hàng sẽ bị **CANCELLED**

### Cách 2: Refund một phần (MỚI)

```java
// Đơn hàng có:
// - OrderItem #10: 2 mũ @ 50,000đ
// - OrderItem #11: 1 áo @ 100,000đ
// Tổng: 200,000đ

// Khách muốn trả 1 mũ
Map<Long, Integer> refundItems = new HashMap<>();
refundItems.put(10L, 1); // Trả 1 mũ

Refund refund = refundService.createRefundRequestWithItems(
    order,
    refundItems,
    "Mũ bị rách",
    "https://image.com/broken-hat.jpg"
);

// Kết quả:
// - Refund amount: 50,000đ (1 mũ)
// - Đơn hàng: GIỮ NGUYÊN (không bị CANCELLED)
// - RefundItems: [OrderItem#10, quantity=1, amount=50,000]
```

### Cách 3: Refund nhiều món

```java
// Trả 1 mũ + 1 áo
Map<Long, Integer> refundItems = new HashMap<>();
refundItems.put(10L, 1); // 1 mũ
refundItems.put(11L, 1); // 1 áo

Refund refund = refundService.createRefundRequestWithItems(
    order,
    refundItems,
    "Không vừa size",
    null
);

// Kết quả:
// - Refund amount: 150,000đ
// - Đơn hàng: GIỮ NGUYÊN (vì còn 1 mũ chưa trả)
```

## 🔧 Migration Database

Chạy file SQL:

```sql
-- File: sql/migration_refund_items.sql
mysql -u root -p ecommerce_db < sql/migration_refund_items.sql
```

Hoặc trong MySQL Workbench:

```sql
SOURCE D:/PBL6/PBL6_E-Commerce/Ecommerce/sql/migration_refund_items.sql;
```

## 📊 Kết quả

### Trước khi fix:

| Order ID | Items       | Refund | Order Status |
| -------- | ----------- | ------ | ------------ |
| 29       | 2 mũ + 1 áo | 1 mũ   | CANCELLED ❌ |

### Sau khi fix:

| Order ID | Items       | Refund      | Order Status | Note           |
| -------- | ----------- | ----------- | ------------ | -------------- |
| 29       | 2 mũ + 1 áo | 1 mũ        | DELIVERED ✅ | Partial refund |
| 30       | 2 mũ + 1 áo | 2 mũ + 1 áo | CANCELLED ✅ | Full refund    |

## 🎯 Tóm tắt thay đổi

### Files mới:

1. ✅ `RefundItem.java` - Entity mới
2. ✅ `RefundItemRepository.java` - Repository mới
3. ✅ `sql/migration_refund_items.sql` - Migration DB

### Files đã sửa:

1. ✅ `Refund.java` - Thêm relationship với RefundItem
2. ✅ `RefundService.java` - Thêm logic partial refund
   - `createRefundRequestWithItems()` - Tạo refund với items cụ thể
   - `processRefund()` - Logic kiểm tra full/partial refund

### Logic thay đổi:

- ✅ **Trước**: Refund bất kỳ → Hủy toàn bộ đơn
- ✅ **Sau**: Refund một phần → Giữ nguyên đơn
- ✅ **Sau**: Refund toàn bộ → Mới hủy đơn

## ⚠️ Lưu ý

1. **Backward compatible**: Code cũ vẫn hoạt động (không có refundItems)
2. **Frontend cần update**: Gửi danh sách items khi tạo refund
3. **Validation**: Số lượng refund không được vượt quá số lượng đã mua

## 🚀 Next Steps

1. ✅ Chạy migration SQL
2. ✅ Restart backend
3. 🔄 Update frontend để gửi `refundItemsData` khi tạo refund
4. 🔄 Test các trường hợp:
   - Refund 1 món trong đơn nhiều món
   - Refund toàn bộ đơn
   - Refund một phần số lượng của 1 món

## 📝 API Example

### Request tạo refund (cần update frontend)

```json
POST /api/refunds

{
  "orderId": 29,
  "reason": "Sản phẩm bị lỗi",
  "imageUrl": "https://...",
  "refundItems": [
    {
      "orderItemId": 10,
      "quantity": 1
    }
  ]
}
```

Backend sẽ tự tính `totalAmount` dựa trên giá của các items.
