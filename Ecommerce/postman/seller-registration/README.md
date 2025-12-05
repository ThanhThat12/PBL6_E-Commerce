# Seller Registration API - Testing Guide

## 📋 Overview
Complete Postman collection để test tính năng đăng ký seller với **upload ảnh thực tế** (không phải mock URL).

## ✨ Features
- ✅ **Real file upload** - Upload ảnh CMND/CCCD thực tế
- ✅ **Auto token management** - Tự động lưu và sử dụng JWT token
- ✅ **Environment variables** - Tự động lưu image URLs sau upload
- ✅ **Complete workflow** - Từ upload ảnh → submit → admin approve/reject
- ✅ **Test scripts** - Validation và logging cho mỗi request

---

## 🚀 Setup

### 1. Import vào Postman

**Import Collection:**
```
File > Import > Select file:
📁 Seller_Registration_API.postman_collection.json
```

**Import Environment:**
```
File > Import > Select file:
📁 Seller_Registration_Local.postman_environment.json
```

### 2. Cấu hình Environment

Click vào **Seller Registration Local** environment và cập nhật:

```json
{
  "base_url": "https://localhost:8081/api",
  "buyer_username": "buyer123",          // ← Đổi thành username BUYER của bạn
  "buyer_password": "password123",       // ← Đổi thành password
  "admin_username": "admin",             // ← Đổi thành username ADMIN
  "admin_password": "admin123"           // ← Đổi thành password
}
```

### 3. Select Environment

Chọn **Seller Registration Local** từ dropdown ở góc trên bên phải Postman.

---

## 🧪 Testing Workflow

### **PHASE 1: Authentication & Image Upload**

#### Step 1: Login as BUYER
```
POST /auth/login
```
- ✅ Tự động lưu `buyer_access_token`
- ✅ Token dùng cho các request tiếp theo

#### Step 2: Upload KYC Images (QUAN TRỌNG!)

**2.1. Upload ID Card Front** 
```
POST /images/upload
Body: form-data
  - file: [SELECT IMAGE FILE] ← Click và chọn ảnh mặt trước CMND
  - folder: kyc
```
- ⚠️ **PHẢI chọn file ảnh thực tế**, không phải nhập URL
- ✅ Tự động lưu `id_card_front_url` và `id_card_front_public_id`

**2.2. Upload ID Card Back**
```
POST /images/upload
Body: form-data
  - file: [SELECT IMAGE FILE] ← Click và chọn ảnh mặt sau CMND
  - folder: kyc
```
- ✅ Tự động lưu `id_card_back_url` và `id_card_back_public_id`

**2.3. Upload Selfie with ID (Optional)**
```
POST /images/upload
Body: form-data
  - file: [SELECT IMAGE FILE] ← Chọn ảnh selfie cầm CMND
  - folder: kyc
```
- ✅ Tự động lưu `selfie_with_id_url`

**2.4. Upload Shop Logo (Optional)**
```
POST /images/upload
Body: form-data
  - file: [SELECT IMAGE FILE] ← Chọn logo shop
  - folder: kyc
```
- ✅ Tự động lưu `shop_logo_url`

---

### **PHASE 2: Submit Registration**

#### Step 3: Check Can Submit
```
GET /seller/registration/can-submit
```
- Kiểm tra xem buyer có thể submit đơn mới không

#### Step 4: Submit Registration
```
POST /seller/register
```
- ✅ Tự động sử dụng image URLs đã upload
- ✅ Tự động lưu `pending_shop_id`
- ✅ Tạo shop với status PENDING

**Request body tự động populate từ uploaded images:**
```json
{
  "idCardFrontUrl": "{{id_card_front_url}}",      // ← Từ Step 2.1
  "idCardBackUrl": "{{id_card_back_url}}",        // ← Từ Step 2.2
  "selfieWithIdUrl": "{{selfie_with_id_url}}",    // ← Từ Step 2.3
  "logoUrl": "{{shop_logo_url}}"                  // ← Từ Step 2.4
}
```

#### Step 5: Get Registration Status
```
GET /seller/registration/status
```
- Kiểm tra trạng thái đơn đăng ký (PENDING/ACTIVE/REJECTED)

---

### **PHASE 3: Admin Review**

#### Step 6: Login as ADMIN
```
POST /auth/login
```
- ✅ Tự động lưu `admin_access_token`

#### Step 7: Get Pending Applications
```
GET /admin/seller-registrations/pending?page=0&size=10
```
- Xem danh sách đơn đăng ký chờ duyệt

#### Step 8: Get Application Detail
```
GET /admin/seller-registrations/{{pending_shop_id}}
```
- Xem chi tiết KYC của đơn cụ thể
- Bao gồm URLs của ảnh CMND đã upload

#### Step 9A: APPROVE Registration ✅
```
POST /admin/seller-registrations/approve
Body:
{
  "shopId": {{pending_shop_id}},
  "note": "KYC verified successfully"
}
```
- ✅ Shop status → ACTIVE
- ✅ User role → SELLER

**HOẶC**

#### Step 9B: REJECT Registration ❌
```
POST /admin/seller-registrations/reject
Body:
{
  "shopId": {{pending_shop_id}},
  "rejectionReason": "Ảnh CMND không rõ"
}
```
- ❌ Shop status → REJECTED
- User có thể cancel và resubmit

---

## 📸 Cách Upload File Đúng

### ❌ SAI - Không làm thế này:
```
Body: raw JSON
{
  "file": "path/to/image.jpg"  // ← SAI!
}
```

### ✅ ĐÚNG - Làm như này:

1. **Chọn Body tab** → **form-data**
2. **Key**: `file`, **Type**: `File` (dropdown)
3. **Click "Select Files"** → Chọn ảnh từ máy tính
4. **Key**: `folder`, **Type**: `Text`, **Value**: `kyc`
5. **Send**

![Upload Example](https://i.imgur.com/example.png)

---

## 🔍 Validation Logic

### Frontend Logic Matching
Collection này test đúng logic frontend:

**Frontend Code (SellerRegistrationPage.jsx):**
```javascript
const handleImageUpload = async (e, type) => {
  const file = e.target.files?.[0];
  
  // Validate
  if (!file.type.startsWith('image/')) {
    toast.error('Vui lòng chọn file ảnh');
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    toast.error('Kích thước ảnh tối đa 5MB');
    return;
  }

  // Upload
  const formDataUpload = new FormData();
  formDataUpload.append('file', file);
  formDataUpload.append('folder', 'kyc');

  const response = await fetch('https://localhost:8081/api/images/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formDataUpload
  });
}
```

**Postman Collection:**
- ✅ Sử dụng `form-data` (giống FormData)
- ✅ Key `file` + `folder` (giống append)
- ✅ Bearer token authentication
- ✅ Endpoint `/images/upload`

---

## 🎯 Expected Results

### Upload Image Response:
```json
{
  "statusCode": 200,
  "data": {
    "url": "https://res.cloudinary.com/xxx/image/upload/v1/kyc/abc123.jpg",
    "publicId": "kyc/abc123",
    "imageUrl": "https://res.cloudinary.com/xxx/..."
  }
}
```

### Submit Registration Response:
```json
{
  "statusCode": 201,
  "message": "Đăng ký bán hàng thành công! Đơn của bạn đang chờ xét duyệt.",
  "data": {
    "success": true,
    "shopId": 15,
    "shopName": "Sport Pro Shop",
    "status": "PENDING"
  }
}
```

### Approve Response:
```json
{
  "statusCode": 200,
  "data": {
    "success": true,
    "shopId": 15,
    "status": "ACTIVE"
  }
}
```

---

## 🐛 Troubleshooting

### Error: "Missing required image uploads"
- **Cause:** Chưa upload ảnh CMND
- **Fix:** Chạy folder **"Image Upload (KYC)"** trước

### Error: "Upload failed"
- **Cause:** File không phải ảnh hoặc quá 5MB
- **Fix:** Chọn file JPG/PNG < 5MB

### Error: "401 Unauthorized"
- **Cause:** Token hết hạn hoặc chưa login
- **Fix:** Chạy lại "Login as BUYER" hoặc "Login as ADMIN"

### Error: "Không tìm thấy đơn đăng ký"
- **Cause:** Chưa submit registration
- **Fix:** Chạy "Submit Seller Registration" trước

### Error: "Buyer đã có đơn đăng ký"
- **Cause:** User đã submit rồi
- **Fix:** 
  - Nếu PENDING: Đợi admin approve/reject
  - Nếu REJECTED: Chạy "Cancel Rejected Application"
  - Nếu ACTIVE: User đã là SELLER

---

## 📊 Test Cases Coverage

✅ **Image Upload Flow:**
- Upload ID card front
- Upload ID card back
- Upload selfie with ID (optional)
- Upload shop logo (optional)
- Auto-save URLs to environment

✅ **Buyer Registration Flow:**
- Check eligibility
- Submit registration with uploaded images
- Get registration status
- Cancel rejected application

✅ **Admin Review Flow:**
- Get pending applications list
- Search applications
- Get pending count
- View application detail with KYC images
- Approve registration
- Reject registration with reason

✅ **Security:**
- BUYER role required for registration
- ADMIN role required for approval
- Bearer token authentication

---

## 📝 Notes

1. **Upload ảnh TRƯỚC** khi submit registration
2. Ít nhất phải upload **ID card front và back**
3. Selfie và logo là **optional**
4. Mỗi lần test mới nên upload ảnh mới để tránh duplicate
5. Shop name phải unique - collection tự động thêm random number
6. Test scripts tự động validate responses

---

## 🎓 Complete Test Scenario

```
1. Login as BUYER ✅
2. Upload ID Card Front ✅ → Lưu URL
3. Upload ID Card Back ✅ → Lưu URL
4. Upload Selfie (optional) ✅ → Lưu URL
5. Upload Logo (optional) ✅ → Lưu URL
6. Check Can Submit ✅
7. Submit Registration ✅ → Sử dụng URLs từ step 2-5
8. Get Status ✅ → PENDING
9. Login as ADMIN ✅
10. Get Pending List ✅
11. Get Application Detail ✅ → Xem KYC images
12. Approve ✅ → Shop ACTIVE, User → SELLER
    hoặc
    Reject ❌ → Shop REJECTED
```

---

## 🔗 Related Documents

- Backend API: `SELLER_REGISTRATION_PHASE1.md`
- Frontend Component: `src/pages/user/SellerRegistrationPage.jsx`
- Frontend Service: `src/services/sellerRegistrationService.js`
- Backend Controller: `ShopController.java`, `AdminSellerRegistrationController.java`

---

**Created:** 2025-12-02  
**Last Updated:** 2025-12-02  
**Version:** 1.0.0
