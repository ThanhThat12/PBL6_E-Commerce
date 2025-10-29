# 📊 API THỐNG KÊ THU NHẬP SHOP - SELLER

## 📋 **Tổng quan**

API cho phép **SELLER** xem thống kê thu nhập của shop theo tháng để hiển thị biểu đồ cột tại Frontend.

**Đặc điểm:**
- 📈 **Thống kê theo tháng**: Doanh thu của 12 tháng trong năm
- 💰 **Chỉ tính đơn COMPLETED**: Chỉ đếm đơn hàng đã hoàn thành
- 🔐 **Tự động nhận diện shop**: Dựa vào JWT token
- 📅 **Hỗ trợ filter theo năm**: Xem thống kê của năm bất kỳ

---

## 🔍 **API: Lấy thống kê thu nhập**

### **GET /api/seller/shop/analytics**

Lấy thống kê thu nhập của shop theo tháng (chỉ đơn hàng COMPLETED).

#### **Request**

```http
GET http://localhost:8081/api/seller/shop/analytics
Authorization: Bearer <JWT_TOKEN>
```

hoặc với tham số năm:

```http
GET http://localhost:8081/api/seller/shop/analytics?year=2024
Authorization: Bearer <JWT_TOKEN>
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| year | Integer | No | Năm cần thống kê (mặc định: năm hiện tại) |

**Body:** Không cần

---

#### **Response Success (200)**

```json
{
  "status": 200,
  "error": null,
  "message": "Lấy thống kê shop thành công",
  "data": {
    "totalRevenue": 25750000.00,
    "totalCompletedOrders": 15,
    "monthlyRevenue": [
      {
        "year": 2025,
        "month": 1,
        "monthName": "Tháng 1",
        "revenue": 3500000.00,
        "orderCount": 3
      },
      {
        "year": 2025,
        "month": 2,
        "monthName": "Tháng 2",
        "revenue": 2100000.00,
        "orderCount": 2
      },
      {
        "year": 2025,
        "month": 3,
        "monthName": "Tháng 3",
        "revenue": 4200000.00,
        "orderCount": 4
      },
      {
        "year": 2025,
        "month": 4,
        "monthName": "Tháng 4",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 5,
        "monthName": "Tháng 5",
        "revenue": 5800000.00,
        "orderCount": 5
      },
      {
        "year": 2025,
        "month": 6,
        "monthName": "Tháng 6",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 7,
        "monthName": "Tháng 7",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 8,
        "monthName": "Tháng 8",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 9,
        "monthName": "Tháng 9",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 10,
        "monthName": "Tháng 10",
        "revenue": 10150000.00,
        "orderCount": 1
      },
      {
        "year": 2025,
        "month": 11,
        "monthName": "Tháng 11",
        "revenue": 0.00,
        "orderCount": 0
      },
      {
        "year": 2025,
        "month": 12,
        "monthName": "Tháng 12",
        "revenue": 0.00,
        "orderCount": 0
      }
    ]
  }
}
```

**ShopAnalyticsDTO Fields:**
| Field | Type | Description |
|-------|------|-------------|
| totalRevenue | BigDecimal | Tổng doanh thu (chỉ đơn COMPLETED) |
| totalCompletedOrders | Long | Tổng số đơn hàng COMPLETED |
| monthlyRevenue | Array | Mảng 12 tháng với doanh thu từng tháng |

**MonthlyRevenueDTO Fields:**
| Field | Type | Description |
|-------|------|-------------|
| year | Integer | Năm |
| month | Integer | Tháng (1-12) |
| monthName | String | Tên tháng (Tháng 1, Tháng 2, ...) |
| revenue | BigDecimal | Doanh thu tháng đó |
| orderCount | Long | Số đơn hàng COMPLETED tháng đó |

---

#### **Cách tính doanh thu:**

```sql
-- Chỉ tính các đơn hàng có status = 'COMPLETED'
SELECT 
    shop_id,
    YEAR(created_at) as year,
    MONTH(created_at) as month,
    SUM(total_amount) as revenue,
    COUNT(*) as order_count
FROM orders
WHERE shop_id = 1 
  AND status = 'COMPLETED'
GROUP BY shop_id, YEAR(created_at), MONTH(created_at)
ORDER BY year, month
```

**Lưu ý:**
- ✅ Chỉ đếm đơn `status = 'COMPLETED'`
- ✅ Không tính đơn `PENDING`, `PROCESSING`, `CANCELLED`
- ✅ Luôn trả về đủ 12 tháng (tháng không có doanh thu = 0)

---

#### **Response Error**

**1. Seller chưa có shop (404)**
```json
{
  "status": 404,
  "error": "Seller chưa có shop",
  "message": "Lấy thống kê shop thất bại",
  "data": null
}
```

**2. User không phải SELLER (403)**
```json
{
  "status": 403,
  "error": "Người dùng không phải là seller",
  "message": "Lấy thống kê shop thất bại",
  "data": null
}
```

**3. Token không hợp lệ (401)**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "data": null
}
```

---

## 🧪 **Test với Postman**

### **Test Case 1: Lấy thống kê năm hiện tại**

**Step 1: Login**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "thanhthat120704",
  "password": "123456"
}
```

**Step 2: GET analytics**
```http
GET http://localhost:8081/api/seller/shop/analytics
Authorization: Bearer <token_từ_step_1>
```

**Expected:**
- `totalRevenue`: Tổng doanh thu của tất cả đơn COMPLETED
- `totalCompletedOrders`: Số lượng đơn COMPLETED
- `monthlyRevenue`: Array 12 tháng (năm 2025)

---

### **Test Case 2: Lấy thống kê năm 2024**

```http
GET http://localhost:8081/api/seller/shop/analytics?year=2024
Authorization: Bearer <token>
```

**Expected:**
- Dữ liệu của 12 tháng năm 2024
- Tháng không có dữ liệu sẽ có `revenue = 0`, `orderCount = 0`

---

### **Test Case 3: Shop chưa có đơn hàng nào**

```http
GET http://localhost:8081/api/seller/shop/analytics
Authorization: Bearer <token>
```

**Expected:**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy thống kê shop thành công",
  "data": {
    "totalRevenue": 0.00,
    "totalCompletedOrders": 0,
    "monthlyRevenue": [
      {"year": 2025, "month": 1, "monthName": "Tháng 1", "revenue": 0.00, "orderCount": 0},
      {"year": 2025, "month": 2, "monthName": "Tháng 2", "revenue": 0.00, "orderCount": 0},
      // ... 10 tháng còn lại
    ]
  }
}
```

---

## 📊 **Hiển thị biểu đồ cột tại Frontend**

### **Chart.js Example:**

```javascript
// Gọi API
const response = await fetch('http://localhost:8081/api/seller/shop/analytics', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
const analytics = data.data;

// Chuẩn bị dữ liệu cho Chart.js
const chartData = {
  labels: analytics.monthlyRevenue.map(m => m.monthName),
  datasets: [{
    label: 'Doanh thu (VND)',
    data: analytics.monthlyRevenue.map(m => m.revenue),
    backgroundColor: 'rgba(54, 162, 235, 0.5)',
    borderColor: 'rgba(54, 162, 235, 1)',
    borderWidth: 1
  }]
};

// Tạo biểu đồ
const ctx = document.getElementById('revenueChart').getContext('2d');
new Chart(ctx, {
  type: 'bar',
  data: chartData,
  options: {
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return value.toLocaleString('vi-VN') + ' đ';
          }
        }
      }
    }
  }
});
```

---

## 🔄 **Luồng hoạt động**

```
┌─────────────────────────────────────────────────────────┐
│  1. Seller đăng nhập                                    │
│     → JWT token chứa username                           │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  2. Client gọi GET /api/seller/shop/analytics           │
│     → Gửi kèm JWT token + year (optional)               │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  3. Backend extract username từ token                   │
│     username = "thanhthat120704"                        │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  4. Tìm User theo username                              │
│     SELECT * FROM users WHERE username = ?              │
│     → user_id = 1, role = SELLER                        │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  5. Tìm Shop theo owner_id                              │
│     SELECT * FROM shops WHERE owner_id = 1              │
│     → shop_id = 1                                       │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  6. Tính tổng doanh thu (chỉ COMPLETED)                 │
│     SELECT SUM(total_amount) FROM orders                │
│     WHERE shop_id = 1 AND status = 'COMPLETED'          │
│     → totalRevenue = 25750000                           │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  7. Đếm số đơn COMPLETED                                │
│     SELECT COUNT(*) FROM orders                         │
│     WHERE shop_id = 1 AND status = 'COMPLETED'          │
│     → totalCompletedOrders = 15                         │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  8. Lấy doanh thu theo tháng (năm 2025)                 │
│     SELECT YEAR, MONTH, SUM(total_amount), COUNT(*)     │
│     FROM orders                                         │
│     WHERE shop_id = 1 AND status = 'COMPLETED'          │
│       AND YEAR(created_at) = 2025                       │
│     GROUP BY YEAR, MONTH                                │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  9. Fill đủ 12 tháng (tháng không có = 0)               │
│     Tháng 1: 3500000 (3 đơn)                            │
│     Tháng 2: 2100000 (2 đơn)                            │
│     Tháng 3: 4200000 (4 đơn)                            │
│     Tháng 4: 0 (0 đơn)                                  │
│     ...                                                 │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  10. Trả về JSON với ShopAnalyticsDTO                   │
│      → ResponseDTO với status 200                       │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 **SQL Query tham khảo**

### **Tính tổng doanh thu:**
```sql
SELECT COALESCE(SUM(total_amount), 0) as total_revenue
FROM orders
WHERE shop_id = 1 
  AND status = 'COMPLETED';
```

### **Đếm số đơn COMPLETED:**
```sql
SELECT COUNT(*) as completed_orders
FROM orders
WHERE shop_id = 1 
  AND status = 'COMPLETED';
```

### **Doanh thu theo tháng:**
```sql
SELECT 
    YEAR(created_at) as year,
    MONTH(created_at) as month,
    COALESCE(SUM(total_amount), 0) as revenue,
    COUNT(*) as order_count
FROM orders
WHERE shop_id = 1 
  AND status = 'COMPLETED'
  AND YEAR(created_at) = 2025
GROUP BY YEAR(created_at), MONTH(created_at)
ORDER BY MONTH(created_at);
```

---

## 💡 **Lưu ý quan trọng**

### **Về thống kê:**
- ✅ **Chỉ tính đơn COMPLETED**: Đơn PENDING, PROCESSING, CANCELLED không được tính
- ✅ **Luôn trả về 12 tháng**: Tháng không có doanh thu sẽ có giá trị 0
- ✅ **Dựa vào created_at**: Thống kê theo ngày tạo đơn hàng
- ✅ **BigDecimal cho tiền**: Chính xác không bị làm tròn

### **Về hiển thị Frontend:**
- 📊 Dùng Chart.js hoặc Recharts để vẽ biểu đồ cột
- 📅 Mặc định hiển thị năm hiện tại
- 🔄 Có thể filter theo năm khác (dropdown)
- 💰 Format số tiền: `25.750.000 đ`

### **Về performance:**
- ⚡ Query đã optimize với GROUP BY
- 📈 Index trên `shop_id`, `status`, `created_at`
- 🚀 Cache kết quả nếu cần (Redis)

---

## 🔧 **Troubleshooting**

### **totalRevenue = 0 mặc dù có đơn hàng**
**Nguyên nhân:** Các đơn hàng chưa có status = COMPLETED  
**Giải pháp:**
```sql
-- Kiểm tra status của các đơn hàng
SELECT id, status, total_amount 
FROM orders 
WHERE shop_id = 1;

-- Cập nhật status thành COMPLETED (nếu cần)
UPDATE orders 
SET status = 'COMPLETED' 
WHERE id IN (1, 2, 3);
```

### **monthlyRevenue trống**
**Nguyên nhân:** Không có đơn hàng COMPLETED trong năm đó  
**Giải pháp:** Vẫn trả về 12 tháng với revenue = 0

### **Lỗi: "Seller chưa có shop"**
**Nguyên nhân:** User chưa có shop trong bảng `shops`  
**Giải pháp:**
```sql
INSERT INTO shops (owner_id, name, address, description, status, created_at) 
VALUES (1, 'My Shop', '123 Address', 'Description', 'ACTIVE', NOW());
```

---

## ✅ **Kết luận**

API đã sẵn sàng với các tính năng:
- ✅ Thống kê tổng doanh thu (chỉ đơn COMPLETED)
- ✅ Thống kê theo tháng (12 tháng đầy đủ)
- ✅ Filter theo năm (query parameter)
- ✅ Tự động fill 0 cho tháng không có dữ liệu
- ✅ Format chuẩn cho Frontend vẽ biểu đồ

**Sẵn sàng tích hợp với Frontend để hiển thị biểu đồ cột!** 📊🚀
