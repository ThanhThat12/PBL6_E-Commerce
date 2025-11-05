# 📸 Cloudinary Image Upload API - Postman Collection

**Version**: 1.0.0
**Date**: November 4, 2025
**Test Coverage**: ✅ Complete (25+ test cases)

---

## 📋 TABLE OF CONTENTS

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Setup Instructions](#setup-instructions)
4. [API Endpoints Overview](#api-endpoints-overview)
5. [Test Cases Summary](#test-cases-summary)
6. [Running Tests](#running-tests)
7. [Test Results Interpretation](#test-results-interpretation)
8. [Troubleshooting](#troubleshooting)

---

## 🚀 QUICK START

### 1. Import Collection & Environment
```bash
# Files to import in Postman:
# - Cloudinary_API.postman_collection.json
# - Cloudinary_API_Local.postman_environment.json
```

### 2. Setup Cloudinary Credentials
- Update `application.properties` with your Cloudinary credentials
- Restart Spring Boot application

### 3. Run Authentication Tests
- Execute "Login as BUYER (lmao)" or "Login as SELLER (Test Sports Shop)"
- JWT token will be automatically saved

### 4. Run Upload Tests
- Start with ✅ positive test cases
- Then test ❌ negative/error cases

---

## 📋 PREREQUISITES

### Backend Setup
- ✅ Spring Boot application running on `http://localhost:8080`
- ✅ Cloudinary credentials configured in `application.properties`
- ✅ Database with test users (lmao, testsportsshop)

### Frontend Setup (Optional)
- React application for integration testing
- CORS configured for localhost:3000

### Test Files
Prepare these test image files:
- `avatar.jpg` - Small JPEG image (< 5MB)
- `product.png` - Product image (< 5MB)
- `review1.jpg`, `review2.jpg`, `review3.jpg` - Review images (< 5MB each)
- `large_image.jpg` - Large image (> 5MB) for error testing
- `text_file.txt` - Non-image file for error testing

---

## ⚙️ SETUP INSTRUCTIONS

### Step 1: Import to Postman

1. **Open Postman**
2. **Import Collection**:
   - File → Import
   - Select `Cloudinary_API.postman_collection.json`
3. **Import Environment**:
   - File → Import
   - Select `Cloudinary_API_Local.postman_environment.json`
4. **Select Environment**:
   - Top-right dropdown → "Cloudinary API - Local Environment"

### Step 2: Configure Test Files

For each file upload request:
1. Click on the request
2. Go to **Body** tab
3. For `form-data` fields with `type: "file"`
4. Click **Select File** and choose your test image

### Step 3: Backend Verification

Ensure your `application.properties` has:
```properties
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.api_secret=YOUR_API_SECRET
```

### Step 4: Start Testing

1. **Login First**: Run authentication request
2. **Upload Tests**: Start with avatar upload
3. **Delete Tests**: Use uploaded URLs for deletion testing

---

## 📡 API ENDPOINTS OVERVIEW

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/api/upload/avatar` | POST | BUYER/SELLER/ADMIN | Upload avatar image |
| `/api/upload/product` | POST | SELLER | Upload product image |
| `/api/upload/review` | POST | BUYER | Upload single review image |
| `/api/upload/reviews` | POST | BUYER | Upload multiple review images (max 5) |
| `/api/upload/image` | DELETE | All | Delete image by URL |

### Authentication
- **Type**: JWT Bearer Token
- **Header**: `Authorization: Bearer <token>`
- **Login Endpoint**: `POST /api/auth/login`

### File Upload
- **Content-Type**: `multipart/form-data`
- **Max Size**: 5MB per file
- **Allowed Types**: JPEG, PNG, GIF, WEBP
- **Multiple Files**: Only for `/api/upload/reviews` (max 5 files)

---

## 🧪 TEST CASES SUMMARY

### ✅ POSITIVE TEST CASES (15 tests)

#### Authentication (2 tests)
- ✅ Login as BUYER (lmao)
- ✅ Login as SELLER (Test Sports Shop)

#### Avatar Upload (1 test)
- ✅ Upload Avatar - Valid Image (JPEG)

#### Product Upload (2 tests)
- ✅ Upload Product Image - Valid Image
- ✅ Upload Product Image - No Product ID

#### Review Upload (4 tests)
- ✅ Upload Single Review Image - Valid Image
- ✅ Upload Multiple Review Images (2 files)
- ✅ Upload Multiple Review Images (5 files - Max)
- ✅ Upload Multiple Review Images (3 files) - Integration

#### Delete Image (3 tests)
- ✅ Delete Uploaded Avatar
- ✅ Delete Uploaded Product Image
- ✅ Delete Uploaded Review Image

#### Integration Tests (2 tests)
- ✅ Full Upload Flow - Avatar (Upload + Delete)
- ✅ Full Upload Flow - Multiple Review Images (Upload + Delete)

### ❌ NEGATIVE TEST CASES (10 tests)

#### Avatar Upload (4 tests)
- ❌ Upload Avatar - No File
- ❌ Upload Avatar - Invalid File Type (TXT)
- ❌ Upload Avatar - File Too Large (10MB+)
- ❌ Upload Avatar - No Authentication

#### Product Upload (1 test)
- ❌ Upload Product Image - BUYER Access Denied

#### Review Upload (2 tests)
- ❌ Upload Multiple Review Images - Too Many (6 files)
- ❌ Upload Review Image - SELLER Access Denied

#### Delete Image (3 tests)
- ❌ Delete Image - Invalid URL
- ❌ Delete Image - Empty URL
- ❌ Delete Image - No Authentication

**Total**: 25 test cases covering all scenarios

---

## ▶️ RUNNING TESTS

### Method 1: Run Individual Tests

1. **Select Request** from collection
2. **Attach Test File** (for upload requests)
3. **Click Send**
4. **Check Response** and **Test Results** tab

### Method 2: Run Collection Tests

1. **Click Runner** button in Postman
2. **Select Collection**: "Cloudinary Image Upload API"
3. **Select Environment**: "Cloudinary API - Local Environment"
4. **Configure**:
   - Delay: 1000ms between requests
   - Save responses: Yes
5. **Click Run Collection**

### Method 3: Automated Testing

```javascript
// Run in Postman Console
pm.test("Collection run complete", function () {
    // Custom validation logic
});
```

---

## 📊 TEST RESULTS INTERPRETATION

### ✅ Successful Test Indicators

#### Avatar Upload Success
```json
{
  "url": "https://res.cloudinary.com/your_cloud/image/upload/v123/ecommerce/avatars/user_123_uuid.jpg"
}
```

#### Product Upload Success
```json
{
  "url": "https://res.cloudinary.com/your_cloud/image/upload/v123/ecommerce/products/product_1_uuid.jpg"
}
```

#### Review Upload Success (Single)
```json
{
  "url": "https://res.cloudinary.com/your_cloud/image/upload/v123/ecommerce/reviews/user_123_uuid.jpg"
}
```

#### Review Upload Success (Multiple)
```json
{
  "urls": [
    "https://res.cloudinary.com/.../image1.jpg",
    "https://res.cloudinary.com/.../image2.jpg",
    "https://res.cloudinary.com/.../image3.jpg"
  ]
}
```

#### Delete Success
```json
{
  "success": true,
  "message": "Xóa ảnh thành công"
}
```

### ❌ Expected Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2025-11-04T16:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "File size exceeds maximum allowed size",
  "path": "/api/upload/avatar"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2025-11-04T16:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/upload/avatar"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2025-11-04T16:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/upload/product"
}
```

---

## 🔧 TROUBLESHOOTING

### Issue 1: "Invalid API credentials"

**Symptoms**: All upload requests return 500 error

**Cause**: Cloudinary credentials not configured or incorrect

**Solution**:
1. Check `application.properties`
2. Verify credentials in Cloudinary Dashboard
3. Restart Spring Boot application
4. Test with simple upload request

### Issue 2: "Connection refused"

**Symptoms**: All requests fail with network error

**Cause**: Spring Boot application not running

**Solution**:
```bash
cd Ecommerce/
./mvnw spring-boot:run
```

### Issue 3: "403 Forbidden" on Product Upload

**Symptoms**: Product upload fails for BUYER role

**Cause**: Insufficient permissions (only SELLER can upload products)

**Solution**:
- Login as SELLER (testsportsshop)
- Or test with BUYER role for avatar/review uploads only

### Issue 4: File Upload Not Working

**Symptoms**: "No file selected" error

**Cause**: Test file not attached in Postman

**Solution**:
1. Go to **Body** tab
2. For `form-data` fields
3. Click **Select File** for each file field
4. Choose appropriate test image

### Issue 5: JWT Token Expired

**Symptoms**: 401 errors after some time

**Cause**: JWT token expired

**Solution**:
- Re-run login request
- Token will be automatically updated

### Issue 6: Large File Upload Fails

**Symptoms**: Upload succeeds but image is corrupted

**Cause**: File compression needed

**Solution**:
- Compress image before upload
- Ensure file < 5MB
- Use JPEG format for better compression

---

## 📈 TEST COVERAGE METRICS

- **API Endpoints**: 5/5 (100%)
- **HTTP Methods**: 2/2 (POST, DELETE) (100%)
- **Authentication**: JWT Bearer Token ✅
- **Role-based Access**: BUYER, SELLER, ADMIN ✅
- **File Validation**: Size, Type, Count ✅
- **Error Handling**: 400, 401, 403, 500 ✅
- **Integration Tests**: Upload + Delete flows ✅

---

## 🎯 TESTING BEST PRACTICES

### 1. Test Order
1. **Authentication** first
2. **Positive tests** (✅)
3. **Negative tests** (❌)
4. **Integration tests** (🏃‍♂️)

### 2. File Selection
- Use small files (< 1MB) for quick testing
- Test with different formats (JPEG, PNG, GIF, WEBP)
- Prepare large file (> 5MB) for error testing
- Use non-image file for validation testing

### 3. Environment Variables
- Keep sensitive data in environment variables
- Use different environments for dev/staging/prod
- Document all required variables

### 4. Response Validation
- Check HTTP status codes
- Validate response JSON structure
- Verify Cloudinary URLs contain correct folders
- Test error messages are user-friendly

---

## 📞 SUPPORT

### Documentation
- 📖 **Setup Guide**: `CLOUDINARY_SETUP_GUIDE.md`
- 📖 **Implementation**: `CLOUDINARY_IMPLEMENTATION.md`
- 📖 **Frontend Integration**: `src/services/cloudinaryService.js`

### Common Issues
- Check Spring Boot logs for detailed errors
- Verify Cloudinary dashboard for upload statistics
- Test with simple curl commands if Postman fails

### Test Data
- **BUYER**: username: `lmao`, password: `123456`
- **SELLER**: username: `testsportsshop`, password: `123456`

---

**Last Updated**: November 4, 2025
**Test Suite Version**: 1.0.0
**Coverage**: 100% (25/25 test cases)