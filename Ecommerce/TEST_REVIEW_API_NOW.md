# 🚀 Test Review API - Quick Guide

## ✅ **JWT FIX APPLIED - READY TO TEST**

---

## 🎯 **QUICK TEST (5 minutes)**

### **Step 1: Start Spring Boot**
```powershell
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
mvn spring-boot:run
```

Wait for: `Started EcommerceApplication`

---

### **Step 2: Login to get JWT token**

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"lmao\", \"password\": \"lmao123\"}'
```

**Copy the `accessToken` from response!**

Example:
```json
{
  "statusCode": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 4,
      "username": "lmao",
      "role": 2
    }
  }
}
```

---

### **Step 3: Save token to variable**

```powershell
$TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### **Step 4: Create Review (THE MOMENT OF TRUTH 🎯)**

```powershell
curl -X POST http://localhost:8080/api/products/2/reviews `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{
    \"orderId\": 4,
    \"rating\": 5,
    \"comment\": \"Bóng rổ chất lượng tuyệt vời! Phù hợp cho tập luyện và thi đấu.\",
    \"images\": [\"https://example.com/review1.jpg\"]
  }'
```

---

## ✅ **EXPECTED SUCCESS RESPONSE:**

```json
{
  "id": 1,
  "productId": 2,
  "productName": "Professional Basketball",
  "userId": 4,
  "username": "lmao",
  "orderId": 4,
  "rating": 5,
  "comment": "Bóng rổ chất lượng tuyệt vời! Phù hợp cho tập luyện và thi đấu.",
  "images": ["https://example.com/review1.jpg"],
  "verifiedPurchase": true,
  "sellerResponse": null,
  "sellerResponseDate": null,
  "createdAt": "2025-10-31T20:30:00",
  "updatedAt": "2025-10-31T20:30:00"
}
```

**Status Code: 201 Created ✅**

---

## ❌ **IF YOU SEE THIS - SOMETHING WRONG:**

### **404 User không tồn tại:**
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "User không tồn tại",
  "data": null
}
```
**Solution:** Check if JWT fix was applied (should be fixed now!)

### **401 Unauthorized:**
```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Token không hợp lệ"
}
```
**Solution:** Login again to get new token

### **403 Forbidden:**
```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Bạn không có quyền thực hiện hành động này"
}
```
**Solution:** Make sure user `lmao` has role BUYER (role=2)

### **400 Bad Request:**
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Đơn hàng không chứa sản phẩm này"
}
```
**Solution:** Run SQL script to create test order first:
```powershell
mysql -u root -p123456789 ecommerce_v2 < d:\AuthTest\PBL6_E-Commerce\Ecommerce\sql\create_test_data_lmao.sql
```

---

## 🎯 **COMPLETE TEST FLOW**

### **1. Create Review (as BUYER):**
```powershell
curl -X POST http://localhost:8080/api/products/2/reviews `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{\"orderId\": 4, \"rating\": 5, \"comment\": \"Great product!\"}'
```

### **2. Get Product Reviews (PUBLIC - no auth):**
```powershell
curl http://localhost:8080/api/products/2/reviews
```

### **3. Get My Reviews:**
```powershell
curl -H "Authorization: Bearer $TOKEN" `
  http://localhost:8080/api/reviews/my
```

### **4. Update Review:**
```powershell
curl -X PUT http://localhost:8080/api/reviews/1 `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{\"rating\": 4, \"comment\": \"Updated review - still good!\"}'
```

### **5. Delete Review:**
```powershell
curl -X DELETE http://localhost:8080/api/reviews/1 `
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔍 **VERIFY IN DATABASE**

```sql
-- Check review was created
SELECT * FROM product_reviews WHERE user_id = 4;

-- Check product rating was auto-updated (by trigger)
SELECT id, name, rating, review_count 
FROM products 
WHERE id = 2;

-- Check shop rating was auto-updated (by trigger)
SELECT id, name, rating, review_count 
FROM shops 
WHERE id = 2;
```

---

## 📊 **WHAT WAS FIXED?**

### **Before (ERROR):**
```java
Authentication authentication  // ❌ Wrong
String username = authentication.getName(); // ❌ Returns null
```

### **After (SUCCESS):**
```java
@AuthenticationPrincipal Jwt jwt  // ✅ Correct
String username = jwt.getSubject(); // ✅ Returns "lmao"
```

---

## ✅ **SUCCESS CHECKLIST**

- [ ] Spring Boot started successfully
- [ ] Login returns JWT token
- [ ] Create review returns 201 Created
- [ ] Review appears in database
- [ ] Product rating auto-updated
- [ ] Shop rating auto-updated
- [ ] Get reviews returns the review
- [ ] Update review works
- [ ] Delete review works

---

## 🎉 **IF ALL PASS:**

**🎊 CONGRATULATIONS! Review Module 100% Working! 🎊**

Next steps:
1. ✅ Test seller response endpoints
2. ✅ Import Postman collection for full testing
3. ✅ Run all 19 API requests
4. ✅ Commit to Git
5. ✅ Deploy to production

---

**Last Updated:** October 31, 2025, 8:30 PM
**Fix Applied:** JWT Authentication Pattern
**Status:** ✅ READY TO TEST
