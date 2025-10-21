# Tổng Hợp Các File Tạo/Thay Đổi - Quản Lý Shop (Seller & Admin)

## 📋 Danh Sách Các File Tạo Mới

### 1. **ShopDTO.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/dto/ShopDTO.java`
- **Mô tả:** Data Transfer Object cho Shop
- **Trường:** `id, ownerId, ownerName, name, address, description, status, createdAt`
- **Phương thức:** `fromEntity()` - Chuyển từ entity sang DTO

### 2. **ShopRepository.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/repository/ShopRepository.java`
- **Mô tả:** JPA Repository interface cho Shop
- **Methods:**
  - `findByOwnerId(Long ownerId)` - Tìm shop của seller
  - `findAll()` - Lấy tất cả shop (cho admin)

### 3. **ShopService.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/service/ShopService.java`
- **Mô tả:** Interface ShopService định nghĩa các phương thức
- **Methods (Seller):**
  - `createShop(ShopDTO)` - Tạo shop mới
  - `updateShop(Long id, ShopDTO)` - Cập nhật shop
  - `deleteShop(Long id)` - Xóa shop
  - `getShopsByOwnerId(Long ownerId)` - Lấy shop của seller
- **Methods (Admin):**
  - `listAllShops()` - Lấy tất cả shop
  - `approveShop(Long id)` - Duyệt shop
  - `rejectShop(Long id)` - Từ chối shop
  - `suspendShop(Long id)` - Tạm ngưng shop
  - `verifyShop(Long id)` - Xác minh shop

### 4. **ShopServiceImpl.java**

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/service/impl/ShopServiceImpl.java`
- **Mô tả:** Triển khai ShopService
- **Thay đổi:**
  - `createShop()`: Luôn set status = `PENDING` khi tạo mới
  - `toEntity()`: Không set status từ DTO
  - `verifyShop()`: Set `verified=true` và `status=APPROVED`

### 5. **ShopController.java** (Seller Endpoints)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/controller/ShopController.java`
- **Mô tả:** REST Controller cho seller quản lý shop
- **Endpoints:**
  - `POST /api/shops` - Tạo shop mới
  - `PUT /api/shops/{id}` - Cập nhật shop
  - `DELETE /api/shops/{id}` - Xóa shop
  - `GET /api/shops` - Lấy shop của seller hiện tại
  - `GET /api/shops/{id}` - Lấy chi tiết shop

### 6. **AdminShopController.java** (Admin Endpoints)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/controller/AdminShopController.java`
- **Mô tả:** REST Controller cho admin quản lý shop
- **Endpoints:**
  - `GET /api/admin/shops` - Lấy tất cả shop
  - `GET /api/admin/shops/{id}` - Lấy chi tiết shop
  - `PATCH /api/admin/shops/{id}/approve` - Duyệt shop
  - `PATCH /api/admin/shops/{id}/reject` - Từ chối shop
  - `PATCH /api/admin/shops/{id}/suspend` - Tạm ngưng shop
  - `PATCH /api/admin/shops/{id}/verify` - Xác minh shop

---

## 🔄 Danh Sách Các File Thay Đổi

### 1. **Shop.java** (Entity - Thêm Fields & Getter/Setter)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/domain/Shop.java`
- **Thay đổi:**
  - Enum `ShopStatus`: `PENDING, APPROVED, REJECTED, SUSPENDED`
  - Trường: `owner, name, address, description, status, verified, createdAt, products`
  - Thêm getter/setter cho tất cả trường

### 2. **SecurityConfig.java** (Cập nhật JWT & Quyền Truy Cập)

- **Đường dẫn:** `src/main/java/com/PBL6/Ecommerce/config/SecurityConfig.java`
- **Thay đổi:**
  - Thêm `JwtAuthenticationConverter` để ánh xạ claim `"role"` → authority `"ROLE_ADMIN"`, `"ROLE_SELLER"`, `"ROLE_BUYER"`
  - Authorize rules:
    - `/api/admin/shops/**` → `hasRole("ADMIN")`
    - `/api/shops/**` → `hasAnyRole("SELLER", "ADMIN")`
    - `/api/register/*`, `/api/authenticate`, `/api/forgot-password/**` → `permitAll()`

---

## 📊 ShopStatus Enum

```
PENDING      - Chờ duyệt (mặc định khi tạo)
APPROVED     - Được duyệt
REJECTED     - Bị từ chối
SUSPENDED    - Bị tạm ngưng
```

---

## 🔐 Quyền Truy Cập (Role-Based)

### Seller Endpoints (`/api/shops/**`)

- Chỉ SELLER và ADMIN có quyền truy cập
- Seller chỉ có thể quản lý shop của mình

### Admin Endpoints (`/api/admin/shops/**`)

- Chỉ ADMIN có quyền truy cập
- Có thể xem, duyệt, từ chối, tạm ngưng, xác minh shop

---

## 🧪 Cách Test Trên Postman

### 1. Đăng nhập (lấy token)

- POST `http://localhost:8081/api/authenticate`
- Body: `{"username": "seller_user", "password": "password"}`
- Script Tests: Lưu token vào biến môi trường `{{token}}`

### 2. **SELLER ENDPOINTS**

#### Tạo shop mới

- POST `http://localhost:8081/api/shops`
- Header: `Authorization: Bearer {{token}}`
- Body:
  ```json
  {
    "name": "My Store",
    "address": "123 Main St",
    "description": "Electronics & gadgets"
  }
  ```
- **Kết quả:** Status sẽ là `PENDING`

#### Lấy shop của seller

- GET `http://localhost:8081/api/shops`
- Header: `Authorization: Bearer {{token}}`

#### Cập nhật shop

- PUT `http://localhost:8081/api/shops/1`
- Header: `Authorization: Bearer {{token}}`
- Body: `{ "name": "Updated Store", "address": "456 New St", "description": "New description" }`

#### Xóa shop

- DELETE `http://localhost:8081/api/shops/1`
- Header: `Authorization: Bearer {{token}}`

---

### 3. **ADMIN ENDPOINTS** (Đăng nhập với tài khoản ADMIN)

#### Lấy tất cả shop

- GET `http://localhost:8081/api/admin/shops`
- Header: `Authorization: Bearer {{admin_token}}`

#### Lấy chi tiết shop

- GET `http://localhost:8081/api/admin/shops/1`
- Header: `Authorization: Bearer {{admin_token}}`

#### Duyệt shop (PENDING → APPROVED)

- PATCH `http://localhost:8081/api/admin/shops/1/approve`
- Header: `Authorization: Bearer {{admin_token}}`
- **Response:** ShopDTO với try-catch handling

#### Từ chối shop (PENDING → REJECTED)

- PATCH `http://localhost:8081/api/admin/shops/1/reject`
- Header: `Authorization: Bearer {{admin_token}}`
- **Response:** ShopDTO với try-catch handling

#### Tạm ngưng shop (ANY → SUSPENDED)

- PATCH `http://localhost:8081/api/admin/shops/1/suspend`
- Header: `Authorization: Bearer {{admin_token}}`
- **Response:** ShopDTO với try-catch handling

#### Xác minh shop (verified=true, status=APPROVED)

- PATCH `http://localhost:8081/api/admin/shops/1/verify`
- Header: `Authorization: Bearer {{admin_token}}`
- **Response:** ShopDTO với try-catch handling

---

## 📈 Workflow Quản Lý Shop

```
1. SELLER tạo shop mới
   ↓
   Shop status = PENDING, verified = false
   ↓
2. ADMIN duyệt hoặc từ chối
   ├─ Duyệt (approve)
   │  └─ Status = APPROVED
   │
   └─ Từ chối (reject)
      └─ Status = REJECTED
   ↓
3. Sau duyệt, ADMIN có thể xác minh
   ├─ Verify
   │  └─ Status = APPROVED, verified = true
   │
   └─ Suspend
      └─ Status = SUSPENDED
```

---

## 📋 Response Format

Tất cả endpoint đều trả về format `ResponseDTO`:

```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách shop thành công",
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

## ✅ Trạng Thái Hoàn Thành - Shop Feature

- [x] Tạo ShopDTO, ShopRepository
- [x] Tạo ShopService, ShopServiceImpl
- [x] Tạo ShopController (Seller endpoints)
- [x] Tạo AdminShopController (Admin endpoints)
- [x] Cập nhật JWT Authority Mapping trong SecurityConfig
- [x] Phân chia quyền truy cập: Seller vs Admin
- [x] Logic: Shop mới luôn PENDING, verify → APPROVED
- [x] Test thành công trên Postman
- [x] **Thêm try-catch error handling cho AdminShopController**
- [x] Trả về ResponseDTO với message tiếng Việt

---

**Ngày cập nhật:** 16/10/2025
