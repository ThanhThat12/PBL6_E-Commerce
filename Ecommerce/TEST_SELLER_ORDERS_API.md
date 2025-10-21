# 🧪 Test API Seller Orders - Postman Guide

## Chuẩn bị dữ liệu test

### 1. Tạo seller account và shop

```sql
-- Tạo user với role SELLER (role = 1)
INSERT INTO users (username, password, email, role, activated) 
VALUES ('seller_test', '$2a$10$hXY8IEU9oHfWOzT3TQntTeDdsumQ.dkXaYNhLZjAhBOdxzI9uhz3u', 'seller@test.com', 1, 1);

-- Lấy ID của user vừa tạo (giả sử id = 100)
SET @seller_id = LAST_INSERT_ID();

-- Tạo shop cho seller
INSERT INTO shops (owner_id, name, address, description, status, created_at) 
VALUES (@seller_id, 'Shop Test', '123 Test Street', 'Shop bán phụ kiện thể thao', 'ACTIVE', NOW());

-- Lấy ID của shop vừa tạo
SET @shop_id = LAST_INSERT_ID();

-- Tạo user buyer để test
INSERT INTO users (username, password, email, role, activated) 
VALUES ('buyer1', '$2a$10$hXY8IEU9oHfWOzT3TQntTeDdsumQ.dkXaYNhLZjAhBOdxzI9uhz3u', 'buyer1@test.com', 2, 1);

SET @buyer_id = LAST_INSERT_ID();

-- Tạo orders mẫu cho shop
INSERT INTO orders (created_at, method, status, total_amount, updated_at, user_id, shop_id) 
VALUES 
('2025-10-16 10:30:00', 'COD', 'PENDING', 299000.00, '2025-10-16 10:30:00', @buyer_id, @shop_id),
('2025-10-15 14:20:00', 'VNPAY', 'COMPLETED', 550000.00, '2025-10-15 16:45:00', @buyer_id, @shop_id),
('2025-10-14 09:15:00', 'MOMO', 'PROCESSING', 1200000.00, '2025-10-14 09:15:00', @buyer_id, @shop_id),
('2025-10-13 16:00:00', 'COD', 'CANCELLED', 450000.00, '2025-10-13 18:30:00', @buyer_id, @shop_id);
```

---

## Test Cases

### Test Case 1: Đăng nhập lấy JWT Token

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

Body:
{
  "username": "seller_test",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "seller_test",
    "role": "SELLER"
  }
}
```

**Action:** Copy token từ `data.token`

---

### Test Case 2: Lấy danh sách orders của shop

**Request:**
```
GET http://localhost:8080/api/seller/orders
Authorization: Bearer <paste_token_here>
```

**Expected Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": [
    {
      "id": 1,
      "createdAt": "2025-10-16T10:30:00",
      "method": "COD",
      "status": "PENDING",
      "totalAmount": 299000.00,
      "userId": 5
    },
    {
      "id": 2,
      "createdAt": "2025-10-15T14:20:00",
      "method": "VNPAY",
      "status": "COMPLETED",
      "totalAmount": 550000.00,
      "userId": 5
    },
    {
      "id": 3,
      "createdAt": "2025-10-14T09:15:00",
      "method": "MOMO",
      "status": "PROCESSING",
      "totalAmount": 1200000.00,
      "userId": 5
    },
    {
      "id": 4,
      "createdAt": "2025-10-13T16:00:00",
      "method": "COD",
      "status": "CANCELLED",
      "totalAmount": 450000.00,
      "userId": 5
    }
  ]
}
```

**Verify:**
- ✅ Status = 200
- ✅ Data là array of OrderDTO
- ✅ Chỉ có orders của shop thuộc seller
- ✅ Sắp xếp theo createdAt DESC

---

### Test Case 3: Lấy chi tiết 1 order

**Request:**
```
GET http://localhost:8080/api/seller/orders/1
Authorization: Bearer <paste_token_here>
```

**Expected Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": {
    "id": 1,
    "createdAt": "2025-10-16T10:30:00",
    "method": "COD",
    "status": "PENDING",
    "totalAmount": 299000.00,
    "updatedAt": "2025-10-16T10:30:00",
    "shopId": 1,
    "userId": 5
  }
}
```

**Verify:**
- ✅ Status = 200
- ✅ Data là OrderDetailDTO
- ✅ Có đầy đủ 8 fields
- ✅ shopId = shop của seller

---

### Test Case 4: Seller không có shop

**Setup:**
```sql
-- Tạo seller mới chưa có shop
INSERT INTO users (username, password, email, role, activated) 
VALUES ('seller_noshop', '$2a$10$hXY8IEU9oHfWOzT3TQntTeDdsumQ.dkXaYNhLZjAhBOdxzI9uhz3u', 'noshop@test.com', 1, 1);
```

**Request:**
```
POST /api/auth/login
{
  "username": "seller_noshop",
  "password": "password123"
}

GET /api/seller/orders
Authorization: Bearer <new_token>
```

**Expected Response:**
```json
{
  "status": 400,
  "error": "Seller chưa có shop",
  "message": "Lấy danh sách đơn hàng thất bại",
  "data": null
}
```

---

### Test Case 5: Xem order không thuộc shop của mình

**Setup:**
```sql
-- Tạo shop khác và order
INSERT INTO shops (owner_id, name, address, status) 
VALUES (1, 'Other Shop', '456 Other St', 'ACTIVE');

SET @other_shop_id = LAST_INSERT_ID();

INSERT INTO orders (created_at, method, status, total_amount, user_id, shop_id) 
VALUES ('2025-10-16 10:00:00', 'COD', 'PENDING', 100000, 5, @other_shop_id);

SET @other_order_id = LAST_INSERT_ID();
```

**Request:**
```
GET /api/seller/orders/<other_order_id>
Authorization: Bearer <seller_test_token>
```

**Expected Response:**
```json
{
  "status": 400,
  "error": "Bạn không có quyền xem đơn hàng này",
  "message": "Lấy chi tiết đơn hàng thất bại",
  "data": null
}
```

---

### Test Case 6: Buyer cố gắng truy cập API seller

**Request:**
```
POST /api/auth/login
{
  "username": "buyer1",
  "password": "password123"
}

GET /api/seller/orders
Authorization: Bearer <buyer_token>
```

**Expected Response:**
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "Forbidden"
}
```

---

### Test Case 7: Không có JWT token

**Request:**
```
GET /api/seller/orders
(Không có header Authorization)
```

**Expected Response:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

---

## Postman Collection

### Environment Variables:
```
baseUrl: http://localhost:8080
token: (auto-set from login)
orderId: 1
```

### Pre-request Script (Login):
```javascript
pm.sendRequest({
    url: pm.environment.get("baseUrl") + "/api/auth/login",
    method: 'POST',
    header: {
        'Content-Type': 'application/json'
    },
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            username: "seller_test",
            password: "password123"
        })
    }
}, function (err, res) {
    var jsonData = res.json();
    pm.environment.set("token", jsonData.data.token);
});
```

### Test Script (Verify Response):
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has correct structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('status');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

pm.test("Data is array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.be.an('array');
});
```

---

## Checklist ✅

- [ ] Database có seller với shop
- [ ] Database có orders cho shop đó
- [ ] Backend đang chạy (port 8080)
- [ ] Đã login và lấy JWT token
- [ ] Token được set trong Authorization header
- [ ] Test Case 1: Login - PASS
- [ ] Test Case 2: List orders - PASS
- [ ] Test Case 3: Order detail - PASS
- [ ] Test Case 4: No shop error - PASS
- [ ] Test Case 5: Unauthorized order - PASS
- [ ] Test Case 6: Buyer forbidden - PASS
- [ ] Test Case 7: No token - PASS

---

## Expected Results Summary

| Scenario | Expected Status | Expected Message |
|----------|----------------|------------------|
| Login seller | 200 | Đăng nhập thành công |
| Get orders list | 200 | Lấy danh sách đơn hàng thành công |
| Get order detail | 200 | Lấy chi tiết đơn hàng thành công |
| Seller no shop | 400 | Seller chưa có shop |
| Wrong order | 400 | Bạn không có quyền xem... |
| Buyer access | 403 | Forbidden |
| No token | 401 | Unauthorized |
