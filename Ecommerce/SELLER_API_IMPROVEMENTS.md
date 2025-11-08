# 📊 Phân tích và Cải thiện Seller APIs

## 🔍 So sánh Frontend Services vs Backend APIs

### ✅ **Đã có và hoạt động tốt:**

#### 1. Shop Management (ShopController)
- ✅ `GET /api/seller/shop` - Lấy thông tin shop
- ✅ `PUT /api/seller/shop` - Cập nhật shop
- ✅ `GET /api/seller/shop/analytics` - Thống kê shop (có sẵn)
- ✅ `POST /api/seller/register` - Đăng ký seller

#### 2. Order Management (OrdersController) 
- ✅ `GET /api/seller/orders` - Danh sách đơn hàng
- ✅ `GET /api/seller/orders/{id}` - Chi tiết đơn hàng
- ✅ `PATCH /api/seller/orders/{id}/status` - Cập nhật trạng thái

#### 3. Product Management (ProductController)
- ✅ `GET /api/products/my-shop/all` - Lấy sản phẩm shop
- ✅ `POST /api/products` - Tạo sản phẩm
- ✅ `DELETE /api/products/{id}` - Xóa sản phẩm

---

## ❌ **Thiếu và cần bổ sung:**

### 1. Dashboard APIs (THIẾU HOÀN TOÀN)

Frontend cần:
```javascript
// dashboardService.js
GET /seller/dashboard/stats           // Tổng quan: revenue, orders, products, customers
GET /seller/dashboard/revenue         // Doanh thu theo time range
GET /seller/dashboard/recent-orders   // Đơn hàng gần đây
GET /seller/dashboard/top-products    // Sản phẩm bán chạy
```

**Backend cần tạo:** `SellerDashboardController.java`

---

### 2. Product APIs (THIẾU MỘT SỐ)

Frontend cần:
```javascript
// productService.js
GET  /seller/products                 // ❌ THIẾU (frontend dùng /seller/products)
GET  /seller/products/{id}            // ❌ THIẾU
PUT  /seller/products/{id}            // ❌ THIẾU (có PATCH status nhưng thiếu PUT full update)
PATCH /seller/products/{id}/status    // ❌ THIẾU
```

**Hiện tại backend có:**
- `GET /api/products/my-shop/all` (không khớp với frontend `/seller/products`)
- `POST /api/products`
- `DELETE /api/products/{id}`

**Vấn đề:** 
- Frontend gọi `/seller/products` nhưng backend có `/api/products/my-shop/all`
- Thiếu API update sản phẩm
- Thiếu API toggle status riêng cho seller

---

### 3. Order APIs (THIẾU MỘT SỐ)

Frontend cần:
```javascript
// orderService.js
GET /seller/orders/stats              // ❌ THIẾU - Thống kê đơn hàng theo status
POST /seller/orders/{id}/cancel       // ❌ THIẾU - Cancel order với lý do
```

**Backend hiện có:** 
- `GET /api/seller/orders`
- `GET /api/seller/orders/{id}`
- `PATCH /api/seller/orders/{id}/status`

**Vấn đề:** Thiếu API stats và cancel với reason

---

### 4. Shop APIs (THIẾU MỘT SỐ)

Frontend cần:
```javascript
// shopService.js
GET  /seller/shop/stats               // ❌ THIẾU - Stats riêng (rating, followers, products)
POST /seller/shop/logo                // ❌ THIẾU - Upload logo
POST /seller/shop/banner              // ❌ THIẾU - Upload banner
```

**Backend hiện có:**
- `GET /api/seller/shop/analytics` (không giống `/stats`)

**Vấn đề:** Thiếu upload logo/banner, thiếu stats cơ bản

---

### 5. Statistical APIs (THIẾU HOÀN TOÀN)

Frontend cần:
```javascript
// statisticalService.js
GET /seller/statistical/revenue       // Doanh thu theo time
GET /seller/statistical/sales         // Số lượng bán
GET /seller/statistical/top-products  // Top products
GET /seller/statistical/customers     // Thống kê khách hàng
GET /seller/statistical/order-status  // Phân bố trạng thái
GET /seller/statistical/export        // Export báo cáo
```

**Backend:** ❌ **CHƯA CÓ CONTROLLER NÀO**

---

### 6. Customer APIs (THIẾU HOÀN TOÀN)

Frontend cần:
```javascript
// customerService.js
GET /seller/customers                 // Danh sách khách hàng
GET /seller/customers/{id}            // Chi tiết khách hàng
GET /seller/customers/{id}/orders     // Lịch sử mua hàng
GET /seller/customers/stats           // Thống kê khách hàng
```

**Backend:** ❌ **CHƯA CÓ CONTROLLER NÀO**

---

## 🎯 Kế hoạch cải thiện (KHÔNG SỬA DB)

### Phase 1: Dashboard APIs (Ưu tiên cao ⭐⭐⭐)
Tạo `SellerDashboardController.java`:
- Sử dụng repository queries trên bảng `orders`, `products`, `users` hiện có
- Không cần sửa database schema

### Phase 2: Product APIs Alignment (Ưu tiên cao ⭐⭐⭐)
Thêm vào `ProductController.java`:
- `GET /api/seller/products` - Alias cho `/my-shop/all`
- `GET /api/seller/products/{id}` - Lấy product của seller
- `PUT /api/seller/products/{id}` - Update product (reuse existing)
- `PATCH /api/seller/products/{id}/status` - Toggle status

### Phase 3: Order APIs Enhancement (Ưu tiên trung bình ⭐⭐)
Thêm vào `OrdersController.java`:
- `GET /api/seller/orders/stats` - Thống kê theo status
- `POST /api/seller/orders/{id}/cancel` - Cancel với reason

### Phase 4: Shop APIs Enhancement (Ưu tiên trung bình ⭐⭐)
Thêm vào `ShopController.java`:
- `GET /api/seller/shop/stats` - Stats cơ bản
- `POST /api/seller/shop/logo` - Upload logo
- `POST /api/seller/shop/banner` - Upload banner

### Phase 5: Statistical APIs (Ưu tiên thấp ⭐)
Tạo `SellerStatisticalController.java`:
- Queries trên bảng `orders`, `order_items`, `products` hiện có
- Group by date cho charts

### Phase 6: Customer APIs (Ưu tiên thấp ⭐)
Tạo `SellerCustomerController.java`:
- Queries trên bảng `users`, `orders` hiện có
- Lấy distinct users từ orders của shop

---

## 📝 DTOs cần tạo

### Dashboard
- `DashboardStatsDTO` - Tổng quan stats
- `RevenueStatsDTO` - Doanh thu theo thời gian
- `TopProductDTO` - Sản phẩm bán chạy

### Product
- `ProductUpdateDTO` - Update product data
- `ProductStatusDTO` - Status toggle

### Order
- `OrderStatsDTO` - Stats theo status
- `OrderCancelDTO` - Cancel reason

### Shop
- `ShopStatsDTO` - Shop statistics
- `ImageUploadResponseDTO` - Upload response

### Statistical
- `RevenueDataDTO` - Revenue data points
- `SalesDataDTO` - Sales data points
- `CustomerStatsDTO` - Customer analytics

### Customer
- `CustomerDTO` - Customer info
- `CustomerDetailDTO` - Customer với purchase history
- `CustomerStatsDTO` - Customer statistics

---

## 🔧 Thay đổi Database (Tối thiểu)

### Shop Table Enhancement (ĐÃ CÓ MIGRATION SCRIPT)
```sql
-- Đã có file: migration_add_pickup_address_to_shops.sql
ALTER TABLE shops 
  DROP COLUMN address,
  ADD COLUMN pickup_address_id bigint(20),
  ADD FOREIGN KEY (pickup_address_id) REFERENCES addresses(id);
```

**Bổ sung thêm (optional):**
```sql
-- Thêm cột logo và banner cho shop
ALTER TABLE shops
  ADD COLUMN logo_url VARCHAR(500) DEFAULT NULL,
  ADD COLUMN banner_url VARCHAR(500) DEFAULT NULL;
```

**LƯU Ý:** 
- Có thể không cần thêm `logo_url`, `banner_url` nếu lưu trong field `description` (dạng JSON)
- Hoặc tạo bảng `shop_images` riêng (không ảnh hưởng schema hiện có)

---

## 🚀 Thứ tự triển khai đề xuất

1. ✅ **Chạy migration script** (đã có sẵn)
2. 🔧 **Phase 1 + 2**: Dashboard + Product APIs (cần gấp cho frontend)
3. 🔧 **Phase 3 + 4**: Order + Shop enhancements
4. 🔧 **Phase 5 + 6**: Statistical + Customer (có thể làm sau)

---

## 📊 Tóm tắt

| Module | Frontend Calls | Backend APIs | Status | Priority |
|--------|----------------|--------------|---------|----------|
| Dashboard | 4 APIs | 0 APIs | ❌ Thiếu hoàn toàn | ⭐⭐⭐ Cao |
| Product | 5 APIs | 3 APIs | ⚠️ Thiếu 2 APIs | ⭐⭐⭐ Cao |
| Order | 5 APIs | 3 APIs | ⚠️ Thiếu 2 APIs | ⭐⭐ Trung bình |
| Shop | 5 APIs | 3 APIs | ⚠️ Thiếu 3 APIs | ⭐⭐ Trung bình |
| Statistical | 6 APIs | 0 APIs | ❌ Thiếu hoàn toàn | ⭐ Thấp |
| Customer | 4 APIs | 0 APIs | ❌ Thiếu hoàn toàn | ⭐ Thấp |

**Tổng cộng:** 29 API calls từ frontend, hiện có 9 APIs backend → **Thiếu 20 APIs** (69%)
