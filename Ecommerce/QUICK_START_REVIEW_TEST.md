# 🚀 Quick Start - Test Review API sau khi Fix 404

## ⚡ TL;DR

```powershell
# 1. Restart Spring Boot
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
mvn spring-boot:run

# 2. Run test script (in new terminal)
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
.\test_review_api.ps1

# Expected: ✅ All tests pass, review created successfully!
```

---

## 📋 Vấn đề đã fix

❌ **Before:** `POST /api/products/2/reviews` → 404 Not Found  
✅ **After:** `POST /api/products/2/reviews` → 201 Created

**Root cause:** SecurityConfig thiếu rules cho review endpoints

**Solution:** Đã thêm 6 rules vào `SecurityConfig.java`

---

## 🧪 Testing Options

### Option 1: PowerShell Script (Fastest) ⚡

```powershell
.\test_review_api.ps1
```

**What it does:**
1. Login as user 'lmao'
2. Create review (5 stars)
3. Get public reviews
4. Get my reviews
5. Update review (4 stars)
6. Display summary

**Expected output:**
```
=== 1. Login as user 'lmao' ===
✅ Login successful!

=== 2. Create Review for Product ID 2 ===
✅ Review created successfully!
Review ID: 1
Rating: 5 stars

✅ TEST COMPLETED SUCCESSFULLY!
```

---

### Option 2: Postman Collection 📮

```powershell
# Import these files to Postman:
postman/Product_Reviews_API.postman_collection.json
postman/Review_API_Local.postman_environment.json

# Follow guide:
postman/README.md
```

---

### Option 3: Manual cURL (Windows) 🔧

```powershell
# Step 1: Login
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST -ContentType "application/json" `
    -Body '{"username":"lmao","password":"lmao123"}'

$token = $response.data.accessToken
Write-Host "Token: $token"

# Step 2: Create Review (THIS NOW WORKS!)
Invoke-RestMethod -Uri "http://localhost:8080/api/products/2/reviews" `
    -Method POST -ContentType "application/json" `
    -Headers @{ "Authorization" = "Bearer $token" } `
    -Body '{"orderId":2,"rating":5,"comment":"Great product!","images":[]}'

# Expected: 201 Created with review data
```

---

## ✅ Verification Checklist

After running tests:

- [ ] Spring Boot running without errors
- [ ] Login successful (JWT token received)
- [ ] **Create review returns 201** (not 404)
- [ ] Review visible in GET /api/products/2/reviews
- [ ] Database has new review record
- [ ] Product rating updated automatically (trigger)
- [ ] Shop rating updated automatically (trigger)

---

## 🗂️ Test Data

**Product ID:** 2 (Professional Basketball, 250,000 VND)  
**Order ID:** 2 (user_id=4, status='COMPLETED', created 3 days ago)  
**User:** lmao (id=4, role=2=BUYER, password: lmao123)

✅ Order eligible for review (COMPLETED status, within 7-day window)

---

## 🐛 Troubleshooting

### Still getting 404?

```powershell
# Check if Spring Boot restarted with new SecurityConfig
# Look for this in logs:
# "Mapping ... POST /api/products/{productId}/reviews ... BUYER"
```

### Getting 401 Unauthorized?

```powershell
# Token expired, login again:
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST -ContentType "application/json" `
    -Body '{"username":"lmao","password":"lmao123"}'

$token = $response.data.accessToken
```

### Getting 400 Bad Request?

Check:
- Order must be COMPLETED
- Order must contain the product
- User can only review once per product

---

## 📊 All Review Endpoints (Fixed)

| Endpoint | Role | Status |
|----------|------|--------|
| `GET /api/products/{id}/reviews` | Public | ✅ |
| `POST /api/products/{id}/reviews` | BUYER | ✅ **FIXED** |
| `PUT /api/reviews/{id}` | BUYER | ✅ **FIXED** |
| `DELETE /api/reviews/{id}` | BUYER | ✅ **FIXED** |
| `GET /api/reviews/my` | BUYER | ✅ **FIXED** |
| `GET /api/seller/reviews` | SELLER | ✅ **FIXED** |

---

## 📚 Full Documentation

- `REVIEW_FIX_SUMMARY.md` - Complete fix details
- `REVIEW_404_FIX.md` - Detailed troubleshooting guide
- `postman/README.md` - Postman collection guide
- `sql/RUN_TEST_SCRIPT.md` - SQL test data guide

---

## 🎯 Next Steps

1. ✅ Run `.\test_review_api.ps1` 
2. ✅ Verify all tests pass
3. ✅ Check database for rating updates
4. 📝 (Optional) Test with Postman collection
5. 📝 (Optional) Test seller endpoints
6. 📝 (Optional) Test error cases

---

**Status:** ✅ Ready to test  
**Last Updated:** October 31, 2025
