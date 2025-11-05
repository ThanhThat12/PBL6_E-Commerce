# 🚀 QUICK START: Cloudinary API Testing

## ⚡ Run All Tests in 3 Steps

### Step 1: Setup Credentials
```bash
# Edit application.properties
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.api_secret=YOUR_API_SECRET
```

### Step 2: Start Backend
```bash
cd Ecommerce/
./mvnw spring-boot:run
```

### Step 3: Run Tests
```bash
cd postman/cloudinary/
chmod +x run_api_tests.sh
./run_api_tests.sh
```

---

## 📊 Expected Output

```
========================================
🧪 CLOUDINARY API TEST SUITE
========================================

🔐 Testing Authentication...
✅ PASS - BUYER Login
✅ PASS - SELLER Login

📸 Testing Avatar Upload...
✅ PASS - Avatar Upload Success
✅ PASS - Avatar Upload - No File (400)
✅ PASS - Avatar Upload - No Auth (401)

🛍️ Testing Product Image Upload...
✅ PASS - Product Upload Success
✅ PASS - Product Upload - BUYER Denied (403)

⭐ Testing Review Image Upload...
✅ PASS - Single Review Upload Success
✅ PASS - Multiple Review Upload Success
✅ PASS - Review Upload - Too Many Files (400)

🗑️ Testing Image Deletion...
✅ PASS - Image Deletion Success
✅ PASS - Image Deletion - Invalid URL (400)
✅ PASS - Image Deletion - No Auth (401)

========================================
📊 TEST SUMMARY
========================================
Total Tests: 13
Passed: 13
Failed: 0
🎉 ALL TESTS PASSED!
✅ Cloudinary API is working correctly
========================================
```

---

## 🔧 Manual Testing with Postman

### Import Files
1. `Cloudinary_API.postman_collection.json`
2. `Cloudinary_API_Local.postman_environment.json`

### Test Order
1. **Login** → Get JWT token
2. **Avatar Upload** → Test basic upload
3. **Product Upload** → Test with SELLER role
4. **Review Upload** → Test single & multiple
5. **Delete Image** → Test deletion

### Sample Test Data
- **BUYER**: `lmao` / `123456`
- **SELLER**: `testsportsshop` / `123456`
- **Test Images**: Use small JPEG/PNG files (< 5MB)

---

## 📋 API Endpoints Summary

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/upload/avatar` | BUYER/SELLER/ADMIN | Upload avatar |
| POST | `/api/upload/product` | SELLER | Upload product image |
| POST | `/api/upload/review` | BUYER | Single review image |
| POST | `/api/upload/reviews` | BUYER | Multiple review images (max 5) |
| DELETE | `/api/upload/image` | All | Delete by URL |

---

## 🐛 Common Issues

### ❌ "Connection refused"
**Fix**: Start Spring Boot server first
```bash
./mvnw spring-boot:run
```

### ❌ "Invalid API credentials"
**Fix**: Check `application.properties` credentials

### ❌ "403 Forbidden"
**Fix**: Use correct role (SELLER for products, BUYER for reviews)

### ❌ "File too large"
**Fix**: Use images < 5MB

---

## 📖 Full Documentation

- **Setup Guide**: `CLOUDINARY_SETUP_GUIDE.md`
- **Implementation**: `CLOUDINARY_IMPLEMENTATION.md`
- **Detailed README**: `README.md`

---

**Ready to test? Run `./run_api_tests.sh` now! 🎯**