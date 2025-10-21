# 📋 Báo Cáo Kiểm Tra Project & Tổng Hợp API Endpoints

**Ngày:** 21/10/2025  
**Project:** PBL6 E-Commerce  
**Repository:** ThanhThat12/PBL6_E-Commerce  
**Nhánh:** main (sau merge feature/auth-improvements)

---

## 🚨 PHẦN 1: CÁC VẤN ĐỀ CẦN SỬA NGAY

### ❌ Lỗi Critical (Cần sửa trước khi deploy)

#### 1. **CartService.java - Product.getStock() không tồn tại** ⚠️ NGHIÊM TRỌNG

**Vị trí:** `CartService.java` lines 57, 75, 76, 102, 103

**Vấn đề:**

```java
// ❌ SAI - Product không có field stock
if (product.getStock() < quantity) {
    throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + product.getStock());
}
```

**Nguyên nhân:**

- `Product` entity không có field `stock`
- Stock được lưu trong `ProductVariant` entity (line 36: `private Integer stock`)
- CartService đang sử dụng `Product` thay vì `ProductVariant`

**Giải pháp:**

**Cách 1: Thêm stock vào Product** (Đơn giản nhất)

```java
// Thêm vào Product.java
@Column(nullable = false)
private Integer stock = 0;

public Integer getStock() {
    return stock;
}

public void setStock(Integer stock) {
    this.stock = stock;
}
```

**Cách 2: Refactor CartService sử dụng ProductVariant** (Chuẩn hơn)

```java
// Thay đổi CartItem relation từ Product sang ProductVariant
@ManyToOne
@JoinColumn(name = "product_variant_id")
private ProductVariant productVariant;

// CartService check stock từ variant
if (productVariant.getStock() < quantity) {
    throw new RuntimeException("Số lượng vượt quá tồn kho: " + productVariant.getStock());
}
```

**Khuyến nghị:** Dùng Cách 2 vì đúng với thiết kế database (product có nhiều variants, mỗi variant có stock riêng)

---

#### 2. **application.properties - Lỗi cú pháp JWT secret** ⚠️

**Vị trí:** `application.properties` line 37

**Vấn đề:**

```properties
# ❌ SAI - Dấu backtick không đúng
jwt.secret=`${JWT_SECRET_KEY:my-secret-key-which-should-be-long-and-secure-at-least-32-chars}
```

**Sửa:**

```properties
# ✅ ĐÚNG
jwt.secret=${JWT_SECRET_KEY:my-secret-key-which-should-be-long-and-secure-at-least-32-chars}
```

---

### ⚠️ Lỗi Trung Bình (Nên sửa)

#### 3. **JwtUtil.java - Deprecated methods** (Minor)

**Vị trí:** `JwtUtil.java` lines 26, 31

**Vấn đề:**

```java
// ❌ Deprecated methods
.signWith(SignatureAlgorithm.HS256, SECRET_KEY)
Jwts.parser().setSigningKey(SECRET_KEY)
```

**Giải pháp:** Dùng TokenProvider.java (đã có sẵn, hiện đại hơn)

- ✅ Đã có `TokenProvider.java` sử dụng modern JJWT API
- Xóa hoặc deprecate `JwtUtil.java`

---

#### 4. **Unused fields & variables**

**4.1. CartService.java**

```java
// ❌ Không sử dụng
private final UserRepository userRepository; // Line 19
```

**Sửa:** Xóa hoặc sử dụng nó

**4.2. FacebookAuthController.java**

```java
// ❌ Không sử dụng
private final UserRepository userRepository; // Line 30
```

**Sửa:** Xóa vì AuthService đã có UserRepository

**4.3. GoogleAuthService.java**

```java
// ❌ Các biến không sử dụng
String name = (String) payload.get("name");
String givenName = (String) payload.get("given_name");
String familyName = (String) payload.get("family_name");
```

**Sửa:** Xóa hoặc sử dụng để tạo username

**4.4. Order & OrderItem entities**

- Nhiều fields không được sử dụng (totalAmount, method, createdAt, updatedAt, quantity, price)
- Có thể bỏ qua vì đây là entities (sẽ dùng sau)

---

### 🔧 Lỗi Code Quality (Optional)

#### 5. **FacebookAuthService.java - Generic warnings**

**Vấn đề:**

```java
// ❌ Raw type usage
ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
Map<String, Object> fbUser = response.getBody();
```

**Sửa:**

```java
// ✅ Type-safe
ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    url,
    HttpMethod.GET,
    null,
    new ParameterizedTypeReference<Map<String, Object>>() {}
);
Map<String, Object> fbUser = response.getBody();
```

---

#### 6. **UserService.java - Null assignment**

**Vấy đề:**

```java
User user = null; // Line 252 - assigned but never used
```

**Sửa:**

```java
User user; // Không cần khởi tạo null
// hoặc
User user = contact.contains("@")
    ? userRepository.findOneByEmail(contact).orElseThrow(...)
    : userRepository.findOneByPhoneNumber(contact).orElseThrow(...);
```

---

### ✅ Đã Sửa (Trong Session Này)

- ✅ **UserInfoDTO constructor** - Fixed 3 chỗ thiếu phoneNumber parameter
- ✅ **JwtFilter.java** - Đã xóa (replaced by OAuth2 resource server)
- ✅ **SecurityConfig.java** - Đã merge với modern config
- ✅ **UserController.java** - Đã merge admin APIs + rate limiting
- ✅ **UserService.java** - Đã merge admin services + OTP security
- ✅ **VerificationRepository.java** - Đã gộp duplicate imports

---

## 📚 PHẦN 2: TỔNG HỢP API ENDPOINTS

### 🔐 Authentication & Authorization

#### **AuthController** (`/api`)

| Method | Endpoint            | Auth   | Mô tả                            |
| ------ | ------------------- | ------ | -------------------------------- |
| POST   | `/api/authenticate` | Public | Đăng nhập bằng username/password |

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**

```json
{
  "code": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600000,
    "userInfo": {
      "id": 1,
      "email": "user@example.com",
      "username": "username",
      "phoneNumber": "0123456789",
      "role": "BUYER"
    }
  }
}
```

---

#### **RefreshTokenController** (`/api/auth`)

| Method | Endpoint                                | Auth          | Mô tả                                   |
| ------ | --------------------------------------- | ------------- | --------------------------------------- |
| POST   | `/api/auth/refresh`                     | Public        | Làm mới access token                    |
| GET    | `/api/auth/refresh/remaining/{tokenId}` | Authenticated | Xem thời gian còn lại của refresh token |

**Refresh Token Request:**

```json
{
  "refreshToken": "refresh_token_here"
}
```

**Refresh Token Response:**

```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new_jwt_token",
    "refreshToken": "new_refresh_token",
    "expiresIn": 3600000,
    "userInfo": {
      /* user info */
    }
  }
}
```

---

#### **LogoutController** (`/api/auth`)

| Method | Endpoint               | Auth          | Mô tả                       |
| ------ | ---------------------- | ------------- | --------------------------- |
| POST   | `/api/auth/logout`     | Authenticated | Đăng xuất (blacklist token) |
| POST   | `/api/auth/logout-all` | Authenticated | Đăng xuất tất cả thiết bị   |

**Logout Request:**

```json
{
  "refreshToken": "refresh_token_here"
}
```

---

#### **GoogleAuthController** (`/api`)

| Method | Endpoint                   | Auth   | Mô tả                 |
| ------ | -------------------------- | ------ | --------------------- |
| POST   | `/api/authenticate/google` | Public | Đăng nhập bằng Google |

**Request Body:**

```json
{
  "idToken": "google_id_token_here"
}
```

---

#### **FacebookAuthController** (`/api`)

| Method | Endpoint                     | Auth   | Mô tả                   |
| ------ | ---------------------------- | ------ | ----------------------- |
| POST   | `/api/authenticate/facebook` | Public | Đăng nhập bằng Facebook |

**Request Body:**

```json
{
  "accessToken": "facebook_access_token_here"
}
```

---

### 👥 User Management

#### **UserController** (`/api`)

| Method | Endpoint                           | Auth          | Mô tả                                 |
| ------ | ---------------------------------- | ------------- | ------------------------------------- |
| POST   | `/api/register/check-contact`      | Public        | Kiểm tra email/phone và gửi OTP       |
| POST   | `/api/register/verify-otp`         | Public        | Xác thực OTP                          |
| POST   | `/api/register/register`           | Public        | Đăng ký tài khoản mới                 |
| GET    | `/api/user/me`                     | Authenticated | Lấy thông tin user hiện tại           |
| GET    | `/api/admin/users/admin`           | ADMIN         | Lấy danh sách admin users             |
| GET    | `/api/admin/users/sellers`         | ADMIN         | Lấy danh sách seller users            |
| GET    | `/api/admin/users/customers`       | ADMIN         | Lấy danh sách customer users          |
| GET    | `/api/admin/users/detail/{userId}` | ADMIN         | Lấy chi tiết user theo ID             |
| PATCH  | `/api/admin/users/{userId}/role`   | ADMIN         | Cập nhật role của user                |
| PATCH  | `/api/admin/users/{userId}/status` | ADMIN         | Cập nhật trạng thái (active/inactive) |
| DELETE | `/api/admin/users/{userId}`        | ADMIN         | Xóa user                              |

**Check Contact Request:**

```json
{
  "contact": "email@example.com" // hoặc "0123456789"
}
```

**Verify OTP Request:**

```json
{
  "contact": "email@example.com",
  "otp": "123456"
}
```

**Register Request:**

```json
{
  "contact": "email@example.com",
  "username": "username",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Update Role Request:**

```json
{
  "role": "SELLER" // ADMIN, SELLER, BUYER
}
```

**Update Status Request:**

```json
{
  "activated": true // hoặc false
}
```

---

#### **ForgotPasswordController** (`/api/forgot-password`)

| Method | Endpoint                          | Auth   | Mô tả                       |
| ------ | --------------------------------- | ------ | --------------------------- |
| POST   | `/api/forgot-password/send-otp`   | Public | Gửi OTP để reset password   |
| POST   | `/api/forgot-password/verify-otp` | Public | Xác thực OTP reset password |
| POST   | `/api/forgot-password/reset`      | Public | Reset password mới          |

**Send OTP Request:**

```json
{
  "contact": "email@example.com"
}
```

**Verify OTP Request:**

```json
{
  "contact": "email@example.com",
  "otp": "123456"
}
```

**Reset Password Request:**

```json
{
  "contact": "email@example.com",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

---

### 🏪 Shop Management

#### **ShopController** (`/api/shops`)

| Method | Endpoint                    | Auth          | Mô tả                           |
| ------ | --------------------------- | ------------- | ------------------------------- |
| POST   | `/api/shops/register`       | SELLER        | Đăng ký shop mới cho seller     |
| GET    | `/api/shops/user/{userId}`  | Public        | Lấy thông tin shop theo user ID |
| GET    | `/api/shops/check/{userId}` | Authenticated | Kiểm tra user có shop chưa      |

**Register Shop Request:**

```json
{
  "name": "Tên Shop",
  "address": "Địa chỉ shop",
  "description": "Mô tả shop"
}
```

---

### 📦 Product Management

#### **ProductController** (`/api/products`)

| Method | Endpoint                                  | Auth         | Mô tả                                         |
| ------ | ----------------------------------------- | ------------ | --------------------------------------------- |
| GET    | `/api/products/all`                       | Public       | Lấy tất cả sản phẩm (không phân trang)        |
| POST   | `/api/products`                           | ADMIN/SELLER | Tạo sản phẩm mới                              |
| POST   | `/api/products/add`                       | SELLER       | Thêm sản phẩm mới (dành cho seller)           |
| GET    | `/api/products/manage`                    | SELLER       | Quản lý sản phẩm của seller (có phân trang)   |
| GET    | `/api/products`                           | Public       | Lấy danh sách sản phẩm (có phân trang)        |
| GET    | `/api/products/{id}`                      | Public       | Lấy chi tiết sản phẩm theo ID                 |
| GET    | `/api/products/search`                    | Public       | Tìm kiếm sản phẩm theo tên                    |
| GET    | `/api/products/category/{categoryId}`     | Public       | Lấy sản phẩm theo category (có phân trang)    |
| GET    | `/api/products/category/{categoryId}/all` | Public       | Lấy tất cả sản phẩm theo category             |
| PUT    | `/api/products/{id}`                      | ADMIN/SELLER | Cập nhật sản phẩm                             |
| DELETE | `/api/products/{id}`                      | ADMIN/SELLER | Xóa sản phẩm                                  |
| GET    | `/api/products/my-products`               | SELLER       | Lấy sản phẩm của seller hiện tại (phân trang) |
| GET    | `/api/products/my-products/all`           | SELLER       | Lấy tất cả sản phẩm của seller                |
| PATCH  | `/api/products/{id}/status`               | SELLER       | Cập nhật trạng thái active/inactive           |

**Query Parameters (Phân trang):**

- `page` (default: 0)
- `size` (default: 10)
- `keyword` (cho search)

**Create Product Request:**

```json
{
  "categoryId": 1,
  "name": "Tên sản phẩm",
  "description": "Mô tả sản phẩm",
  "basePrice": 100000,
  "mainImage": "url_to_image"
}
```

**Update Status Request:**

```json
{
  "isActive": true // hoặc false
}
```

---

### 📁 Category Management

#### **CategoryController** (`/api/categories`)

| Method | Endpoint               | Auth   | Mô tả                 |
| ------ | ---------------------- | ------ | --------------------- |
| GET    | `/api/categories`      | Public | Lấy tất cả categories |
| POST   | `/api/categories`      | ADMIN  | Tạo category mới      |
| DELETE | `/api/categories/{id}` | ADMIN  | Xóa category          |

**Create Category Request:**

```json
{
  "name": "Tên danh mục",
  "description": "Mô tả danh mục"
}
```

---

### 🛒 Cart Management

#### **CartController** (⚠️ ĐANG COMMENT - CHƯA HOẠT ĐỘNG)

**Lưu ý:** Cart API đang bị comment trong code. Cần uncomment và test lại.

```java
// Các endpoints dự kiến:
// POST /api/carts - Thêm sản phẩm vào giỏ
// GET /api/carts - Xem giỏ hàng
// PUT /api/carts/{productId} - Cập nhật số lượng
// DELETE /api/carts/{productId} - Xóa khỏi giỏ
```

---

## 📊 PHẦN 3: THỐNG KÊ PROJECT

### Tổng Quan Số Liệu

| Loại                | Số lượng |
| ------------------- | -------- |
| **Controllers**     | 12       |
| **API Endpoints**   | 43+      |
| **Services**        | 15+      |
| **Repositories**    | 11       |
| **Domain Entities** | 14       |
| **DTOs**            | 20+      |

### Phân Loại API Endpoints

| Nhóm                    | Số lượng | Public | Authenticated | Admin Only | Seller |
| ----------------------- | -------- | ------ | ------------- | ---------- | ------ |
| **Authentication**      | 7        | 5      | 2             | 0          | 0      |
| **User Management**     | 11       | 3      | 1             | 7          | 0      |
| **Shop Management**     | 3        | 1      | 1             | 0          | 1      |
| **Product Management**  | 15       | 8      | 0             | 2          | 5      |
| **Category Management** | 3        | 1      | 0             | 2          | 0      |
| **Forgot Password**     | 3        | 3      | 0             | 0          | 0      |
| **Cart**                | 0        | 0      | 0             | 0          | 0      |
| **TỔNG**                | **42**   | **21** | **4**         | **11**     | **6**  |

---

## 🎯 PHẦN 4: KHUYẾN NGHỊ

### Ưu tiên 1 - SỬA NGAY ⚠️

1. **Fix CartService.java** - Product.getStock() issue

   - Thêm stock field vào Product entity
   - Hoặc refactor CartService sử dụng ProductVariant

2. **Fix application.properties** - JWT secret syntax error

   - Xóa backtick thừa

3. **Test Cart APIs**
   - Uncomment CartController
   - Fix cart logic với ProductVariant
   - Test add/update/remove cart

### Ưu tiên 2 - CẢI THIỆN 📈

4. **Thay thế JwtUtil bằng TokenProvider**

   - Xóa hoặc deprecate JwtUtil.java
   - Sử dụng TokenProvider.java (modern)

5. **Clean up unused code**

   - Xóa unused fields/variables
   - Xóa unused imports

6. **Type safety improvements**
   - Fix raw type warnings trong FacebookAuthService

### Ưu tiên 3 - BỔ SUNG TÍNH NĂNG 🚀

7. **Order Management APIs** (Chưa có)

   - Create order
   - Get orders
   - Update order status
   - Cancel order

8. **Payment Integration** (Chưa có)

   - VNPay/MoMo integration
   - Payment callback handling

9. **Review & Rating** (Chưa có)

   - Product reviews
   - Seller ratings

10. **Admin Dashboard APIs** (Chưa có)
    - Statistics
    - Reports
    - Analytics

---

## 🔒 PHẦN 5: BẢO MẬT

### ✅ Điểm mạnh

1. **JWT với Refresh Token Rotation** ✅
2. **Rate Limiting** cho login, OTP, registration ✅
3. **OTP Security** với failed attempts, locked, used status ✅
4. **Token Blacklist** cho logout ✅
5. **OAuth2 Resource Server** modern config ✅
6. **Password Encoding** với BCrypt ✅

### ⚠️ Cần cải thiện

1. **JWT Secret** trong production

   - Hiện tại: `my-secret-key-which-should-be-long-and-secure-at-least-32-chars`
   - Cần: Generate random secret 256-bit và lưu trong environment variable

2. **HTTPS Only** (Production)

   - Force HTTPS trong production
   - Secure cookies

3. **Input Validation**

   - Thêm `@Valid` annotation
   - Custom validators cho email, phone, password strength

4. **CORS Configuration**
   - Hiện tại: `cors.configure(http)` - mở tất cả origins
   - Cần: Chỉ định cụ thể allowed origins

---

## 📝 PHẦN 6: TESTING CHECKLIST

### API Testing

- [ ] Authentication flow (login, refresh, logout)
- [ ] Registration flow (check-contact, verify-otp, register)
- [ ] Forgot password flow
- [ ] Google/Facebook OAuth
- [ ] Admin user management
- [ ] Product CRUD operations
- [ ] Category management
- [ ] Shop registration
- [ ] Rate limiting thresholds
- [ ] Token expiration & refresh
- [ ] Role-based access control

### Security Testing

- [ ] JWT token validation
- [ ] Expired token handling
- [ ] Invalid token handling
- [ ] Rate limiting bypass attempts
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection

---

## 🚀 NEXT STEPS

### Immediate (Trong 1-2 ngày)

1. ✅ Fix CartService.java - Product.getStock()
2. ✅ Fix application.properties syntax
3. ✅ Test all authentication flows
4. ✅ Test admin APIs

### Short-term (Trong 1 tuần)

5. ✅ Implement Cart APIs hoàn chỉnh
6. ✅ Implement Order Management
7. ✅ Add API documentation (Swagger/OpenAPI)
8. ✅ Write integration tests

### Long-term (1-2 tuần)

9. ✅ Payment integration
10. ✅ Review & Rating system
11. ✅ Admin dashboard
12. ✅ Performance optimization
13. ✅ Deploy to staging environment

---

## 📞 HỖ TRỢ

Nếu cần hỗ trợ thêm về:

- Chi tiết implement từng API
- Fix các issues cụ thể
- Thêm tính năng mới
- Testing strategies
- Deployment guides

Hãy hỏi mình! 😊

---

**Cập nhật lần cuối:** 21/10/2025  
**Tình trạng:** 🟡 Code working, có issues cần fix  
**Build status:** ⚠️ Có compile errors cần fix (CartService)
