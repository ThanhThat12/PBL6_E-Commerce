# 🏪 API QUẢN LÝ SHOP - SELLER

## 📋 **Tổng quan**

API cho phép **SELLER** quản lý thông tin shop của mình:
- ✅ Xem thông tin shop (GET)
- ✅ Cập nhật thông tin shop (PUT)

**Đặc điểm:**
- 🔐 **Tự động nhận diện shop**: Dựa vào JWT token, không cần truyền shop_id
- 🔒 **Bảo mật**: Seller chỉ xem/sửa được shop của mình (owner_id)
- 🎯 **Chỉ hiển thị thông tin cần thiết**: name, address, description, status, created_at

---

## 🔍 **API 1: Lấy thông tin shop**

### **GET /api/seller/shop**

Lấy thông tin shop của seller đang đăng nhập.

#### **Request**

```http
GET http://localhost:8081/api/seller/shop
Authorization: Bearer <JWT_TOKEN>
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body:** Không cần

---

#### **Response Success (200)**

```json
{
  "status": 200,
  "error": null,
  "message": "Lấy thông tin shop thành công",
  "data": {
    "id": 1,
    "name": "Sport Accessories Pro",
    "address": "123 Nguyen Van Linh, Da Nang",
    "description": "Cửa hàng phụ kiện thể thao chất lượng cao",
    "status": "ACTIVE",
    "createdAt": "2025-10-01T10:30:00"
  }
}
```

**ShopDTO Fields:**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | ID của shop |
| name | String | Tên shop |
| address | String | Địa chỉ shop |
| description | String | Mô tả shop |
| status | String | Trạng thái: ACTIVE, INACTIVE |
| createdAt | DateTime | Ngày tạo shop |

---

#### **Response Error**

**1. Seller chưa có shop (404)**
```json
{
  "status": 404,
  "error": "Seller chưa có shop",
  "message": "Lấy thông tin shop thất bại",
  "data": null
}
```

**2. User không phải SELLER (403)**
```json
{
  "status": 403,
  "error": "Người dùng không phải là seller",
  "message": "Lấy thông tin shop thất bại",
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

## ✏️ **API 2: Cập nhật thông tin shop**

### **PUT /api/seller/shop**

Cập nhật thông tin shop của seller đang đăng nhập.

#### **Request**

```http
PUT http://localhost:8081/api/seller/shop
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "Sport Accessories Pro - Official Store",
  "address": "456 Le Duan, Da Nang",
  "description": "Chuyên cung cấp phụ kiện thể thao chính hãng",
  "status": "ACTIVE"
}
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (UpdateShopDTO):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | No | Tên shop (nếu muốn đổi) |
| address | String | No | Địa chỉ shop (nếu muốn đổi) |
| description | String | No | Mô tả shop (nếu muốn đổi) |
| status | String | No | Trạng thái: ACTIVE hoặc INACTIVE |

**Lưu ý:**
- ✅ Chỉ gửi các field muốn cập nhật
- ✅ Các field không gửi sẽ giữ nguyên giá trị cũ
- ✅ Không thể thay đổi `owner_id` và `created_at`

---

#### **Response Success (200)**

```json
{
  "status": 200,
  "error": null,
  "message": "Cập nhật thông tin shop thành công",
  "data": {
    "id": 1,
    "name": "Sport Accessories Pro - Official Store",
    "address": "456 Le Duan, Da Nang",
    "description": "Chuyên cung cấp phụ kiện thể thao chính hãng",
    "status": "ACTIVE",
    "createdAt": "2025-10-01T10:30:00"
  }
}
```

---

#### **Response Error**

**1. Status không hợp lệ (400)**
```json
{
  "status": 400,
  "error": "Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE hoặc INACTIVE",
  "message": "Cập nhật thông tin shop thất bại",
  "data": null
}
```

**2. Seller chưa có shop (404)**
```json
{
  "status": 404,
  "error": "Seller chưa có shop",
  "message": "Cập nhật thông tin shop thất bại",
  "data": null
}
```

**3. User không phải SELLER (403)**
```json
{
  "status": 403,
  "error": "Người dùng không phải là seller",
  "message": "Cập nhật thông tin shop thất bại",
  "data": null
}
```

---

## 🧪 **Test với Postman**

### **Test Case 1: GET thông tin shop**

**Step 1: Login**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "thanhthat120704",
  "password": "123456"
}
```

**Step 2: Copy token và GET shop**
```http
GET http://localhost:8081/api/seller/shop
Authorization: Bearer <token_từ_step_1>
```

**Expected:**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy thông tin shop thành công",
  "data": {
    "id": 1,
    "name": "Sport Accessories Pro",
    "address": "123 Nguyen Van Linh, Da Nang",
    "description": "Cửa hàng phụ kiện thể thao chất lượng cao",
    "status": "ACTIVE",
    "createdAt": "2025-10-01T10:30:00"
  }
}
```

---

### **Test Case 2: PUT cập nhật chỉ tên shop**

```http
PUT http://localhost:8081/api/seller/shop
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Sport Accessories Pro - Official"
}
```

**Expected:**
- Chỉ `name` thay đổi
- Các field khác (`address`, `description`, `status`) giữ nguyên

---

### **Test Case 3: PUT cập nhật tất cả thông tin**

```http
PUT http://localhost:8081/api/seller/shop
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Shop Name",
  "address": "789 Tran Hung Dao, Da Nang",
  "description": "New description for shop",
  "status": "INACTIVE"
}
```

**Expected:**
- Tất cả các field đều được cập nhật
- `createdAt` vẫn giữ nguyên (không thay đổi)

---

### **Test Case 4: PUT với status không hợp lệ**

```http
PUT http://localhost:8081/api/seller/shop
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "CLOSED"
}
```

**Expected (400):**
```json
{
  "status": 400,
  "error": "Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE hoặc INACTIVE",
  "message": "Cập nhật thông tin shop thất bại",
  "data": null
}
```

---

### **Test Case 5: Buyer cố gắng truy cập**

```http
GET http://localhost:8081/api/seller/shop
Authorization: Bearer <buyer_token>
```

**Expected (403):**
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "Bạn không có quyền truy cập",
  "data": null
}
```

---

## 📊 **Tổng hợp Test Cases**

| # | Test Case | Method | Body | Expected Status | Expected Message |
|---|-----------|--------|------|----------------|------------------|
| 1 | GET thông tin shop | GET | - | 200 | Lấy thông tin shop thành công |
| 2 | PUT cập nhật tên | PUT | `{"name":"New Name"}` | 200 | Cập nhật thành công |
| 3 | PUT cập nhật địa chỉ | PUT | `{"address":"New Address"}` | 200 | Cập nhật thành công |
| 4 | PUT cập nhật status ACTIVE | PUT | `{"status":"ACTIVE"}` | 200 | Cập nhật thành công |
| 5 | PUT cập nhật status INACTIVE | PUT | `{"status":"INACTIVE"}` | 200 | Cập nhật thành công |
| 6 | PUT tất cả fields | PUT | All fields | 200 | Cập nhật thành công |
| 7 | PUT status không hợp lệ | PUT | `{"status":"INVALID"}` | 400 | Trạng thái không hợp lệ |
| 8 | GET không có shop | GET | - | 404 | Seller chưa có shop |
| 9 | Buyer truy cập | GET | - | 403 | Access Denied |
| 10 | Không có token | GET | - | 401 | Unauthorized |

---

## 🔄 **Luồng hoạt động**

```
┌─────────────────────────────────────────────────────────┐
│  1. Seller đăng nhập                                    │
│     → JWT token chứa username                           │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  2. Client gọi GET /api/seller/shop                     │
│     → Gửi kèm JWT token trong header                    │
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
│  5. Kiểm tra role = SELLER                              │
│     if (user.role != SELLER) → throw error              │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  6. Tìm Shop theo owner_id                              │
│     SELECT * FROM shops WHERE owner_id = 1              │
│     → shop_id = 1                                       │
└────────────────┬────────────────────────────────────────┘
                 ▼
┌─────────────────────────────────────────────────────────┐
│  7. Convert sang ShopDTO và trả về                      │
│     Chỉ lấy: id, name, address, description,            │
│              status, created_at                         │
└─────────────────────────────────────────────────────────┘
```

---

## 💡 **Lưu ý quan trọng**

### **Về ShopDTO:**
- ✅ **Không hiển thị** `owner_id` (bảo mật)
- ✅ **Không hiển thị** danh sách products
- ✅ **Chỉ hiển thị** thông tin cơ bản: name, address, description, status, created_at

### **Về UpdateShopDTO:**
- ✅ **Cập nhật partial**: Chỉ gửi field muốn đổi
- ✅ **Validate status**: Chỉ chấp nhận ACTIVE hoặc INACTIVE
- ✅ **Không thể đổi** owner_id và created_at

### **Về bảo mật:**
- 🔒 Seller chỉ xem/sửa được shop của mình
- 🔒 Tự động nhận diện shop qua JWT token (owner_id)
- 🔒 Buyer/Admin không thể truy cập API này

---

## 🔧 **Troubleshooting**

### **Lỗi: "Seller chưa có shop"**
**Nguyên nhân:** User chưa có shop trong bảng `shops`  
**Giải pháp:**
```sql
INSERT INTO shops (owner_id, name, address, description, status, created_at) 
VALUES (1, 'My Shop', '123 Address', 'Description', 'ACTIVE', NOW());
```

### **Lỗi: "Người dùng không phải là seller"**
**Nguyên nhân:** User có role != SELLER  
**Giải pháp:**
```sql
UPDATE users SET role = 1 WHERE username = 'thanhthat120704';
```

### **Lỗi: "Trạng thái không hợp lệ"**
**Nguyên nhân:** Status không phải ACTIVE hoặc INACTIVE  
**Giải pháp:** Chỉ dùng `"status": "ACTIVE"` hoặc `"status": "INACTIVE"`

---

## ✅ **Kết luận**

API đã sẵn sàng với các tính năng:
- ✅ GET thông tin shop tự động theo seller
- ✅ PUT cập nhật thông tin shop (partial update)
- ✅ Validate status (ACTIVE/INACTIVE)
- ✅ Bảo mật với JWT và role-based access control
- ✅ Xử lý lỗi đầy đủ với status code chuẩn

**Sẵn sàng tích hợp với Frontend!** 🚀
