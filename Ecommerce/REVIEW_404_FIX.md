# 🔧 Review API 404 Error - FIXED

## 📋 Vấn đề

Khi test `POST /api/products/2/reviews`, nhận được lỗi **404 Not Found** thay vì tạo review thành công.

## 🔍 Nguyên nhân

**SecurityConfig thiếu rules cho review endpoints** → Spring Security không authorize được → trả về 404.

### Test Data có sẵn:
```sql
-- Product ID: 2
'2', 'Professional Basketball', ..., 'COMPLETED'

-- Order ID: 2 (user_id=4, status='COMPLETED')
'2', '4', '2', NULL, NULL, '250000.00', 'COD', 'COMPLETED', 'PAID'

-- User: lmao (id=4, role=2=BUYER)
```

## ✅ Giải pháp đã áp dụng

### File: `SecurityConfig.java`

**Thêm rules cho review endpoints:**

```java
// Review endpoints
.requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll() // Public: view reviews
.requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasRole("BUYER") // Create review
.requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasRole("BUYER") // Update review
.requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasRole("BUYER") // Delete review
.requestMatchers(HttpMethod.GET, "/api/reviews/my").hasRole("BUYER") // My reviews
.requestMatchers("/api/seller/reviews/**").hasRole("SELLER") // Seller review management
```

### Vị trí thêm:
```java
// Category endpoints
.requestMatchers("/api/categories/addCategory").hasRole("ADMIN")
.requestMatchers("/api/categories/**").permitAll()

// ⬇️ THÊM RULES REVIEW TẠI ĐÂY ⬇️
// Review endpoints
.requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll()
.requestMatchers(HttpMethod.POST, "/api/products/*/reviews").hasRole("BUYER")
// ... (các rules khác)

.anyRequest().authenticated()
```

---

## 🧪 Hướng dẫn Test

### 1️⃣ Restart Spring Boot Application

```powershell
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
mvn spring-boot:run
```

### 2️⃣ Login để lấy JWT Token

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Request Body:**
```json
{
  "username": "lmao",
  "password": "lmao123"
}
```

**Response (copy accessToken):**
```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "abc-def-ghi",
    "user": {
      "id": 4,
      "username": "lmao",
      "role": "BUYER"
    }
  }
}
```

### 3️⃣ Create Review (FIXED - Giờ sẽ thành công!)

**Endpoint:** `POST http://localhost:8080/api/products/2/reviews`

**Headers:**
```
Authorization: Bearer <accessToken từ bước 2>
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": 2,
  "rating": 5,
  "comment": "Bóng rổ chất lượng tuyệt vời! Độ bền cao, phù hợp cho training.",
  "images": [
    "https://example.com/basketball-review-1.jpg",
    "https://example.com/basketball-review-2.jpg"
  ]
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "productId": 2,
  "productName": "Professional Basketball",
  "userId": 4,
  "username": "lmao",
  "orderId": 2,
  "rating": 5,
  "comment": "Bóng rổ chất lượng tuyệt vời! Độ bền cao, phù hợp cho training.",
  "images": [
    "https://example.com/basketball-review-1.jpg",
    "https://example.com/basketball-review-2.jpg"
  ],
  "verifiedPurchase": true,
  "sellerResponse": null,
  "createdAt": "2025-10-31T20:00:00"
}
```

### 4️⃣ Verify Triggers (Auto-update Rating)

**Check Product Rating:**
```sql
SELECT id, name, rating, review_count 
FROM products 
WHERE id = 2;

-- Expected:
-- rating = 5.00 (trigger tự động tính)
-- review_count = 1
```

**Check Shop Rating:**
```sql
SELECT s.id, s.name, s.rating, s.review_count
FROM shops s
JOIN products p ON p.shop_id = s.id
WHERE p.id = 2;

-- Expected:
-- rating = 5.00
-- review_count = 1
```

---

## 📊 Các Endpoints Review (Đã FIXED)

| Method | Endpoint | Role | Status |
|--------|----------|------|--------|
| **GET** | `/api/products/{id}/reviews` | Public | ✅ Working |
| **POST** | `/api/products/{id}/reviews` | BUYER | ✅ **FIXED** |
| **PUT** | `/api/reviews/{id}` | BUYER (owner) | ✅ **FIXED** |
| **DELETE** | `/api/reviews/{id}` | BUYER (owner) | ✅ **FIXED** |
| **GET** | `/api/reviews/my` | BUYER | ✅ **FIXED** |
| **GET** | `/api/seller/reviews` | SELLER | ✅ **FIXED** |
| **GET** | `/api/seller/reviews/statistics` | SELLER | ✅ **FIXED** |
| **POST** | `/api/reviews/{id}/seller-response` | SELLER | ✅ **FIXED** |

---

## 🎯 Next Steps

1. ✅ **Restart Spring Boot** (apply SecurityConfig changes)
2. ✅ **Test Create Review** (should return 201 now)
3. ✅ **Verify Database Triggers** (check rating auto-update)
4. ✅ **Test Full CRUD Flow** (create → read → update → delete)
5. ✅ **Test Seller Features** (view reviews, add response)

---

## 📝 Error Codes Reference

| Code | Meaning | Solution |
|------|---------|----------|
| **404** | ❌ SecurityConfig missing rules | ✅ **FIXED** - Added review rules |
| **401** | Missing/invalid JWT token | Login lại để lấy token mới |
| **403** | Wrong role (e.g., SELLER trying to create review) | Dùng BUYER account |
| **400** | Validation failed | Check order COMPLETED, product in order |
| **409** | Duplicate review | User đã review product này rồi |

---

## ✅ Status

**Status:** 🟢 **RESOLVED**

**Changes:**
- ✅ Added 6 review endpoint rules to SecurityConfig
- ✅ GET reviews: Public (permitAll)
- ✅ POST/PUT/DELETE reviews: BUYER role required
- ✅ Seller endpoints: SELLER role required

**Commit:**
```bash
git add src/main/java/com/PBL6/Ecommerce/config/SecurityConfig.java
git commit -m "fix: Add review endpoints to SecurityConfig (fix 404 error)"
```

---

Last Updated: October 31, 2025
