# Tổng Hợp Các File Tạo/Thay Đổi - Quản Lý Đơn Hàng Admin

## 📋 Danh Sách Các File Tạo Mới

### 1. **OrderDTO.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/dto/OrderDTO.java`
- **Mô tả:** Data Transfer Object cho Order, chứa các trường:
  - `id, userId, userName, shopId, shopName, status, totalAmount, method, createdAt, updatedAt, orderItems`
- **Thay đổi:** Tách `OrderItemDTO` ra file riêng (trước đó là inner class)

### 2. **OrderItemDTO.java** ✨ (Mới)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/dto/OrderItemDTO.java`
- **Mô tả:** Data Transfer Object cho OrderItem, chứa các trường:
  - `id, productId, productName, quantity, price`
- **Ghi chú:** Tách riêng từ `OrderDTO` để dễ quản lý

### 3. **OrderRepository.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/repository/OrderRepository.java`
- **Mô tả:** JPA Repository interface cho Order
- **Methods:**
  - `findByShopId(Long shopId)` - Tìm đơn hàng theo shop
  - `findByUserId(Long userId)` - Tìm đơn hàng theo user

### 4. **OrderService.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/service/OrderService.java`
- **Mô tả:** Interface OrderService định nghĩa các phương thức
- **Methods:**
  - `getAllOrders()` - Lấy tất cả đơn hàng
  - `getOrderById(Long id)` - Lấy chi tiết đơn hàng
  - `updateOrderStatus(Long id, String status)` - Cập nhật trạng thái
  - `refundOrder(Long id)` - Hoàn tiền đơn hàng

### 5. **OrderServiceImpl.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/service/impl/OrderServiceImpl.java`
- **Mô tả:** Triển khai OrderService
- **Thay đổi:**
  - Thêm import `OrderItemDTO`
  - Sửa mapping `OrderDTO.OrderItemDTO` → `OrderItemDTO`
  - Hàm `toDTO()` chuyển đổi entity sang DTO đầy đủ

### 6. **AdminOrderController.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/controller/AdminOrderController.java`
- **Mô tả:** REST Controller cho admin quản lý đơn hàng
- **Cấu trúc:** Có try-catch xử lý exception, trả về `ResponseDTO` với status code
- **Endpoints:**
  - `GET /api/admin/orders` - Lấy tất cả đơn hàng
  - `GET /api/admin/orders/{id}` - Lấy chi tiết đơn hàng
  - `PATCH /api/admin/orders/{id}/status?status=...` - Cập nhật trạng thái
  - `POST /api/admin/orders/{id}/refund` - Hoàn tiền

---

## 🔄 Danh Sách Các File Thay Đổi

### 1. **Order.java** (Thêm Getter/Setter)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/Order.java`
- **Thay đổi:**
  - Thêm getter/setter cho: `id, user, shop, status, totalAmount, method, createdAt, updatedAt, orderItems`

### 2. **OrderItem.java** (Thêm Getter/Setter)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/OrderItem.java`
- **Thay đổi:**
  - Thêm getter/setter cho: `id, order, product, quantity, price`

### 3. **SecurityConfig.java** (Cập nhật JWT)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/config/SecurityConfig.java`
- **Thay đổi:**
  - Thêm `JwtAuthenticationConverter` để ánh xạ claim `"role"` thành authority `"ROLE_ADMIN"`, `"ROLE_SELLER"`, `"ROLE_BUYER"`
  - Thêm authorize rule cho `/api/admin/**` chỉ cho ADMIN

### 4. **ShopServiceImpl.java** (Sửa logic)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/service/impl/ShopServiceImpl.java`
- **Thay đổi:**
  - Sửa `createShop()`: Luôn set status = `PENDING` khi tạo mới
  - Sửa `toEntity()`: Không set status từ DTO khi tạo, để mặc định `PENDING`
  - Sửa `verifyShop()`: Khi verify set `verified=true` và `status=APPROVED`

---

## 📊 OrderStatus Enum

```
PENDING      - Chờ duyệt
PROCESSING   - Đang xử lý
COMPLETED    - Hoàn thành
CANCELLED    - Bị hủy
```

---

## 🔐 Quyền Truy Cập (Role-Based)

- **Admin endpoints:** `/api/admin/**` - Chỉ ADMIN
- **Seller endpoints:** `/api/shops/**` - SELLER hoặc ADMIN
- **Public endpoints:** `/api/register/*`, `/api/authenticate`, `/api/forgot-password/**`

---

## 🧪 Cách Test Trên Postman

### 1. Đăng nhập (lấy token)

- POST `http://localhost:8081/api/authenticate`
- Body: `{"username": "admin_user", "password": "password"}`
- **Script Tests:** Lưu token vào biến môi trường `{{token}}`
  ```javascript
  try {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.token) {
      pm.environment.set("token", jsonData.data.token);
    }
  } catch (e) {
    console.log("Response is not JSON or wrong format");
  }
  ```

### 2. Lấy tất cả đơn hàng

- GET `http://localhost:8081/api/admin/orders`
- Header: `Authorization: Bearer {{token}}`
- **Response:** Danh sách OrderDTO với try-catch handling

### 3. Lấy chi tiết đơn hàng

- GET `http://localhost:8081/api/admin/orders/1`
- Header: `Authorization: Bearer {{token}}`
- **Response:** Một OrderDTO hoặc lỗi 404 nếu không tìm thấy

### 4. Cập nhật trạng thái

- PATCH `http://localhost:8081/api/admin/orders/1/status?status=COMPLETED`
- Header: `Authorization: Bearer {{token}}`
- **Status hợp lệ:** `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELLED`

### 5. Hoàn tiền

- POST `http://localhost:8081/api/admin/orders/1/refund`
- Header: `Authorization: Bearer {{token}}`
- **Kết quả:** Status được set thành `CANCELLED`

---

## 📋 Response Format

Tất cả endpoint đều trả về format `ResponseDTO`:

```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách đơn hàng thành công",
  "data": [ ... ]
}
```

Khi lỗi:

```json
{
  "status": 400,
  "error": "Error message",
  "message": "Thất bại",
  "data": null
}
```

---

## ✅ Trạng Thái Hoàn Thành

- [x] Tạo OrderDTO, OrderItemDTO
- [x] Tạo OrderRepository, OrderService, OrderServiceImpl
- [x] Tạo AdminOrderController với 4 endpoints
- [x] Thêm getter/setter cho Order, OrderItem
- [x] Cập nhật JWT Authority Mapping
- [x] Sửa logic Shop: PENDING mặc định, verify → APPROVED
- [x] Tách OrderItemDTO ra file riêng
- [x] **Thêm try-catch error handling cho AdminOrderController**
- [x] Trả về ResponseDTO với message tiếng Việt

---

**Ngày cập nhật:** 16/10/2025
