# Hệ Thống Voucher Hoàn Chỉnh

## Tổng Quan
Hệ thống voucher cho phép shop tạo và quản lý các mã giảm giá với nhiều loại áp dụng khác nhau, và người dùng có thể xem các voucher khả dụng khi thanh toán.

## Các Loại Voucher

### 1. Applicability Types (Loại Áp Dụng)
- **ALL**: Áp dụng cho tất cả người dùng
- **SPECIFIC_PRODUCTS**: Chỉ áp dụng cho các sản phẩm cụ thể
- **SPECIFIC_USERS**: Chỉ áp dụng cho người dùng được chỉ định
- **TOP_BUYERS**: Áp dụng cho top N khách hàng mua nhiều nhất

### 2. Discount Types (Loại Giảm Giá)
- **PERCENTAGE**: Giảm theo phần trăm (có thể set giá trị giảm tối đa)
- **FIXED_AMOUNT**: Giảm giá cố định

## Tính Năng Chính

### 1. Quản Lý Voucher (Seller)

#### Tạo Voucher Mới
```http
POST /api/seller/vouchers
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "NEWYEAR2024",
  "description": "Giảm giá đầu năm",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "minOrderValue": 100000,
  "maxDiscountAmount": 50000,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "usageLimit": 100,
  "applicableType": "ALL",
  "productIds": [],
  "userIds": [],
  "topBuyersCount": null
}
```

#### Các Loại Voucher Cụ Thể

**Voucher cho sản phẩm cụ thể:**
```json
{
  "applicableType": "SPECIFIC_PRODUCTS",
  "productIds": [1, 2, 3],
  "userIds": [],
  "topBuyersCount": null
}
```

**Voucher cho người dùng cụ thể:**
```json
{
  "applicableType": "SPECIFIC_USERS",
  "productIds": [],
  "userIds": [10, 20, 30],
  "topBuyersCount": null
}
```

**Voucher cho top buyers:**
```json
{
  "applicableType": "TOP_BUYERS",
  "productIds": [],
  "userIds": [],
  "topBuyersCount": 5
}
```

#### Lấy Danh Sách Voucher
```http
GET /api/seller/vouchers
Authorization: Bearer {token}
```

#### Lấy Voucher Đang Active
```http
GET /api/seller/vouchers/active
Authorization: Bearer {token}
```

#### Vô Hiệu Hóa Voucher
```http
PATCH /api/seller/vouchers/{id}/deactivate
Authorization: Bearer {token}
```

### 2. Xem Voucher Khả Dụng (User - Checkout)

#### Endpoint
```http
GET /api/seller/vouchers/available?shopId={shopId}&productIds={id1,id2,id3}&cartTotal={total}
Authorization: Bearer {token}
```

#### Ví Dụ Request
```http
GET /api/seller/vouchers/available?shopId=1&productIds=101,102,103&cartTotal=500000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "code": "NEWYEAR2024",
      "description": "Giảm giá đầu năm",
      "shopId": 1,
      "shopName": "Shop ABC",
      "discountType": "PERCENTAGE",
      "discountValue": 20,
      "minOrderValue": 100000,
      "maxDiscountAmount": 50000,
      "startDate": "2024-01-01T00:00:00",
      "endDate": "2024-01-31T23:59:59",
      "usageLimit": 100,
      "usedCount": 45,
      "applicableType": "ALL",
      "isActive": true,
      "createdAt": "2023-12-20T10:00:00",
      "productIds": [],
      "userIds": [],
      "previewDiscount": {
        "cartTotal": 500000,
        "discountAmount": 50000,
        "finalTotal": 450000
      }
    },
    {
      "id": 2,
      "code": "FLASH50K",
      "description": "Giảm ngay 50K",
      "shopId": 1,
      "shopName": "Shop ABC",
      "discountType": "FIXED_AMOUNT",
      "discountValue": 50000,
      "minOrderValue": 200000,
      "maxDiscountAmount": null,
      "startDate": "2024-01-15T00:00:00",
      "endDate": "2024-01-20T23:59:59",
      "usageLimit": 50,
      "usedCount": 10,
      "applicableType": "SPECIFIC_PRODUCTS",
      "isActive": true,
      "createdAt": "2024-01-10T08:00:00",
      "productIds": [101, 102],
      "userIds": [],
      "previewDiscount": {
        "cartTotal": 500000,
        "discountAmount": 50000,
        "finalTotal": 450000
      }
    }
  ]
}
```

## Logic Kiểm Tra Khả Dụng

### 1. Kiểm Tra Cơ Bản
- ✅ Voucher đang active (`isActive = true`)
- ✅ Trong thời gian hiệu lực (`startDate <= now <= endDate`)
- ✅ Chưa hết lượt sử dụng (`usedCount < usageLimit`)

### 2. Kiểm Tra Áp Dụng Theo Loại

#### ALL
- Luôn áp dụng cho mọi người dùng

#### SPECIFIC_PRODUCTS
- Kiểm tra ít nhất 1 sản phẩm trong giỏ hàng thuộc danh sách productIds của voucher
- Query: `SELECT COUNT(*) > 0 FROM voucher_products WHERE voucher_id = ? AND product_id IN (?)`

#### SPECIFIC_USERS
- Kiểm tra userId của người dùng có trong danh sách userIds của voucher không
- Query: `SELECT COUNT(*) > 0 FROM voucher_users WHERE voucher_id = ? AND user_id = ?`

#### TOP_BUYERS
- Lấy danh sách top N khách hàng mua nhiều nhất
- Kiểm tra userId có trong danh sách top buyers không
- Query: `SELECT user_id, SUM(total_price) as total_spent FROM orders WHERE status = 'COMPLETED' GROUP BY user_id ORDER BY total_spent DESC LIMIT ?`

### 3. Tính Toán Preview Discount

#### Kiểm Tra Min Order Value
```java
if (cartTotal < voucher.getMinOrderValue()) {
    return new VoucherPreviewDiscountDTO(cartTotal, 0, cartTotal);
}
```

#### Tính Discount Amount
**PERCENTAGE:**
```java
discountAmount = cartTotal * (discountValue / 100);
if (maxDiscountAmount != null) {
    discountAmount = min(discountAmount, maxDiscountAmount);
}
```

**FIXED_AMOUNT:**
```java
discountAmount = discountValue;
```

#### Final Total
```java
finalTotal = cartTotal - discountAmount;
```

## Database Schema

### Table: vouchers
```sql
ALTER TABLE vouchers 
ADD COLUMN code VARCHAR(50) UNIQUE NOT NULL,
ADD COLUMN description TEXT,
ADD COLUMN discount_type VARCHAR(20) NOT NULL,
ADD COLUMN discount_value DECIMAL(10, 2) NOT NULL,
ADD COLUMN min_order_value DECIMAL(10, 2),
ADD COLUMN max_discount_amount DECIMAL(10, 2),
ADD COLUMN start_date DATETIME NOT NULL,
ADD COLUMN end_date DATETIME NOT NULL,
ADD COLUMN usage_limit INT DEFAULT 0,
ADD COLUMN used_count INT DEFAULT 0,
ADD COLUMN applicable_type VARCHAR(30) NOT NULL,
ADD COLUMN top_buyers_count INT,
ADD COLUMN is_active BOOLEAN DEFAULT TRUE,
ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN shop_id BIGINT NOT NULL,
ADD FOREIGN KEY (shop_id) REFERENCES shops(id);

CREATE INDEX idx_vouchers_code ON vouchers(code);
CREATE INDEX idx_vouchers_shop ON vouchers(shop_id);
CREATE INDEX idx_vouchers_dates ON vouchers(start_date, end_date);
```

### Table: voucher_products
```sql
CREATE TABLE voucher_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_voucher_product (voucher_id, product_id)
);

CREATE INDEX idx_voucher_product_voucher ON voucher_products(voucher_id);
CREATE INDEX idx_voucher_product_product ON voucher_products(product_id);
```

### Table: voucher_users
```sql
CREATE TABLE voucher_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_voucher_user (voucher_id, user_id)
);

CREATE INDEX idx_voucher_user_voucher ON voucher_users(voucher_id);
CREATE INDEX idx_voucher_user_user ON voucher_users(user_id);
```

## Flow Sử Dụng

### 1. Seller Tạo Voucher
1. Seller đăng nhập
2. POST `/api/seller/vouchers` với thông tin voucher
3. Hệ thống lưu voucher vào database
4. Nếu có productIds → lưu vào `voucher_products`
5. Nếu có userIds → lưu vào `voucher_users`

### 2. User Xem Voucher Khi Checkout
1. User thêm sản phẩm vào giỏ hàng
2. User vào trang thanh toán
3. Frontend gọi API: `GET /api/seller/vouchers/available?shopId={shopId}&productIds={ids}&cartTotal={total}`
4. Backend kiểm tra:
   - Voucher đang active
   - Trong thời gian hiệu lực
   - Chưa hết lượt sử dụng
   - Áp dụng được cho user/products
5. Backend tính toán preview discount
6. Trả về danh sách voucher + preview discount
7. User chọn voucher muốn áp dụng
8. Frontend áp dụng discount vào đơn hàng

### 3. Áp Dụng Voucher (Khi Đặt Hàng)
1. User chọn voucher và đặt hàng
2. Backend kiểm tra lại voucher còn hợp lệ
3. Tính toán discount
4. Tạo đơn hàng với giá đã giảm
5. Tăng `used_count` của voucher

## Security Configuration

```java
// Seller endpoints - chỉ SELLER mới truy cập được
.requestMatchers(HttpMethod.POST, "/api/seller/vouchers").hasRole("SELLER")
.requestMatchers(HttpMethod.GET, "/api/seller/vouchers").hasRole("SELLER")
.requestMatchers(HttpMethod.GET, "/api/seller/vouchers/active").hasRole("SELLER")
.requestMatchers(HttpMethod.PATCH, "/api/seller/vouchers/*/deactivate").hasRole("SELLER")

// Available vouchers endpoint - tất cả user đã đăng nhập đều truy cập được
.requestMatchers(HttpMethod.GET, "/api/seller/vouchers/available").authenticated()
```

## Ví Dụ Frontend Integration

### React/Vue - Checkout Page
```javascript
// Lấy vouchers khả dụng
async function getAvailableVouchers(shopId, productIds, cartTotal) {
  const response = await fetch(
    `/api/seller/vouchers/available?shopId=${shopId}&productIds=${productIds.join(',')}&cartTotal=${cartTotal}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  const result = await response.json();
  return result.data; // Array of VoucherDTO with previewDiscount
}

// Hiển thị vouchers
function VoucherSelector({ shopId, cartItems, cartTotal }) {
  const [vouchers, setVouchers] = useState([]);
  const [selectedVoucher, setSelectedVoucher] = useState(null);
  
  useEffect(() => {
    const productIds = cartItems.map(item => item.productId);
    getAvailableVouchers(shopId, productIds, cartTotal)
      .then(setVouchers);
  }, [shopId, cartItems, cartTotal]);
  
  return (
    <div>
      <h3>Chọn Voucher</h3>
      {vouchers.map(voucher => (
        <div key={voucher.id} className="voucher-item">
          <input 
            type="radio" 
            name="voucher"
            onChange={() => setSelectedVoucher(voucher)}
          />
          <div>
            <strong>{voucher.code}</strong> - {voucher.description}
            <p>Giảm: {voucher.previewDiscount.discountAmount.toLocaleString()}đ</p>
            <p>Tổng sau giảm: {voucher.previewDiscount.finalTotal.toLocaleString()}đ</p>
          </div>
        </div>
      ))}
    </div>
  );
}
```

## Testing

### 1. Test Seller APIs
```bash
# Tạo voucher ALL
curl -X POST http://localhost:8081/api/seller/vouchers \
  -H "Authorization: Bearer {seller_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ALL2024",
    "description": "Voucher cho tất cả",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "minOrderValue": 50000,
    "maxDiscountAmount": 30000,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "usageLimit": 1000,
    "applicableType": "ALL"
  }'

# Lấy danh sách vouchers
curl -X GET http://localhost:8081/api/seller/vouchers \
  -H "Authorization: Bearer {seller_token}"
```

### 2. Test User APIs
```bash
# Xem vouchers khả dụng
curl -X GET "http://localhost:8081/api/seller/vouchers/available?shopId=1&productIds=101,102&cartTotal=500000" \
  -H "Authorization: Bearer {user_token}"
```

## Lưu Ý Quan Trọng

1. **Endpoint Path**: `/api/seller/vouchers/available` hiện tại yêu cầu authentication nhưng không yêu cầu role cụ thể, cho phép cả BUYER và SELLER truy cập.

2. **Preview Discount**: Chỉ là tính toán preview, không lưu vào database. Khi thực sự áp dụng voucher (đặt hàng), cần tính toán lại để đảm bảo tính chính xác.

3. **Top Buyers**: Tính toán dựa trên tổng giá trị đơn hàng đã COMPLETED. Có thể customize logic này theo yêu cầu (ví dụ: theo số lượng đơn hàng, theo thời gian, etc.)

4. **Validation**: Khi user thực sự áp dụng voucher, cần validate lại toàn bộ điều kiện để tránh race condition hoặc fraud.

5. **Performance**: Với số lượng voucher lớn, nên thêm cache cho việc kiểm tra applicability.

## Tài Liệu Liên Quan

- `migration_voucher_system.sql`: Script migration database
- `Vouchers.java`: Entity class
- `VoucherService.java`: Business logic
- `VoucherController.java`: REST API endpoints
- `VoucherDTO.java`: Response DTO với preview discount
- `SecurityConfig.java`: Cấu hình security cho endpoints
