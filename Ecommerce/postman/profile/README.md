# 📮 **PROFILE API - POSTMAN COLLECTION**

## 🚀 **IMPORT COLLECTION**

### **Bước 1: Open Postman**
- Click "Collections" tab (left sidebar)
- Click "Import" button
- Select file: `Profile_API.postman_collection.json`

### **Bước 2: Import Environment**
- Click "Environments" tab (left sidebar)
- Click "Import" button
- Select file: `Profile_API_Local.postman_environment.json`

### **Bước 3: Select Environment**
- Click environment dropdown (top right)
- Select "Profile API - Local"

---

## 📋 **COLLECTION STRUCTURE**

### **1. Authentication**
```
├── Login - Buyer1       (POST /api/auth/login)
└── Login - Seller1      (POST /api/auth/login)
```
**Purpose:** Get JWT token để sử dụng cho các requests sau
**Auto-saves token** vào environment variable

### **2. Profile - Get**
```
└── Get My Profile       (GET /api/profile)
```
**Purpose:** Xem thông tin profile của mình
**Tests:** Kiểm tra status 200 + required fields

### **3. Profile - Update**
```
├── Update Profile - Valid          (PUT /api/profile)
├── Update Profile - Invalid Phone  (PUT /api/profile)
└── Update Profile - Empty FullName (PUT /api/profile)
```
**Purpose:** Update fullName và phoneNumber
**Tests:** Validation errors

### **4. Profile - Avatar**
```
├── Upload Avatar         (POST /api/profile/avatar)
└── Upload Avatar - No File (POST /api/profile/avatar)
```
**Purpose:** Upload avatar image
**Tests:** File validation

### **5. Profile - Password**
```
├── Change Password - Valid                (PUT /api/profile/password)
├── Change Password - Current Password Wrong (PUT /api/profile/password)
├── Change Password - Confirm Mismatch    (PUT /api/profile/password)
└── Change Password - Too Short           (PUT /api/profile/password)
```
**Purpose:** Đổi mật khẩu
**Tests:** Password validation

### **6. Security Tests**
```
├── Get Profile - No Token       (GET /api/profile)
└── Get Profile - Invalid Token  (GET /api/profile)
```
**Purpose:** Kiểm tra authentication
**Tests:** 401 Unauthorized errors

---

## 🎯 **QUICK START WORKFLOW**

### **Workflow 1: Test Profile Operations (Buyer)**

```
1. Login - Buyer1
   ✓ Lấy token và username

2. Get My Profile
   ✓ Xem thông tin hiện tại

3. Update Profile - Valid
   ✓ Update fullName và phoneNumber

4. Get My Profile (lại)
   ✓ Kiểm tra update thành công

5. Upload Avatar
   ✓ Upload ảnh (chọn file từ máy)

6. Get My Profile (lại)
   ✓ Kiểm tra avatarUrl đã update

7. Change Password - Valid
   ✓ Đổi mật khẩu
```

### **Workflow 2: Test Validations**

```
1. Login - Buyer1

2. Update Profile - Invalid Phone
   ✓ Kiểm tra error: số điện thoại sai format

3. Update Profile - Empty FullName
   ✓ Kiểm tra error: fullName bắt buộc

4. Change Password - Current Password Wrong
   ✓ Kiểm tra error: mật khẩu hiện tại sai

5. Change Password - Confirm Mismatch
   ✓ Kiểm tra error: mật khẩu confirm không khớp

6. Change Password - Too Short
   ✓ Kiểm tra error: mật khẩu quá ngắn
```

### **Workflow 3: Test Security**

```
1. Get Profile - No Token
   ✓ Kiểm tra 401: không có token

2. Get Profile - Invalid Token
   ✓ Kiểm tra 401: token không hợp lệ
```

---

## 📝 **VARIABLE SETUP**

### **Environment Variables:**
| Variable | Value | Purpose |
|----------|-------|---------|
| `base_url` | `http://localhost:8080` | Base URL của API |
| `token` | (auto-set) | JWT token từ login |
| `username` | (auto-set) | Username từ login |

**Auto-setup:** Khi bạn chạy Login request, token và username sẽ tự động lưu vào environment.

---

## 🔄 **RUNNING REQUESTS**

### **Individual Request:**
1. Chọn request bất kỳ
2. Click "Send" button
3. Xem response ở tab "Response"

### **Run Entire Folder:**
1. Right-click folder (e.g., "Profile - Get")
2. Click "Run"
3. Xem kết quả cho tất cả requests trong folder

### **Run Collection:**
1. Click "▶ Run" button (ở collection name)
2. Chọn environment "Profile API - Local"
3. Click "Run Profile API" button
4. Xem console output

---

## ✅ **TEST CASES INCLUDED**

Mỗi request có **test scripts** để:
- ✅ Kiểm tra HTTP status code
- ✅ Validate response structure
- ✅ Check error messages
- ✅ Verify field values

**View test results:**
1. Run request
2. Click "Tests" tab ở response panel
3. Xem passing/failing tests

---

## 📤 **UPLOAD AVATAR TEST**

### **Cách upload file:**
1. Chọn request "Upload Avatar"
2. Tab "Body" → select "form-data"
3. Key: `avatar` → Type: `File`
4. Click value → "Select Files" → chọn ảnh từ máy
5. Click "Send"

### **File Requirements:**
- ✅ Max size: 5MB
- ✅ Type: JPEG, PNG, WebP, GIF
- ✅ Bắt buộc: phải có file

**Test invalid file:**
- Chọn "Upload Avatar - No File" request
- Click "Send" (không chọn file)
- Kiểm tra error 400

---

## 🔐 **AUTHENTICATION NOTE**

Tất cả `/api/profile` endpoints yêu cầu **JWT token** trong header:
```
Authorization: Bearer {token}
```

**Auto-setup:** Chỉ cần chạy "Login" request trước, token sẽ tự động thêm vào headers.

---

## 🎯 **TESTING CHECKLIST**

- [ ] Import collection thành công
- [ ] Import environment thành công
- [ ] Login - Buyer1 thành công (token được set)
- [ ] Get Profile trả về 200 + valid data
- [ ] Update Profile thành công
- [ ] Upload Avatar thành công (nếu chọn file)
- [ ] Change Password thành công
- [ ] Validation tests fail đúng cách
- [ ] Security tests return 401
- [ ] Seller có thể login + access profile
- [ ] All tests pass khi run collection

---

## 🚀 **NEXT STEPS**

1. **Integrate với Review API:**
   - Import `Product_Reviews_API.postman_collection.json`
   - Test create review, update review

2. **Integrate với Order API:**
   - Import Order API collection
   - Test order creation, checkout

3. **Full Integration Test:**
   - Run tất cả collections theo sequence
   - Verify end-to-end flow

---

## 📞 **TROUBLESHOOTING**

### **Issue: 401 Unauthorized**
**Solution:** 
- Chạy "Login" request trước
- Kiểm tra token đã được set: `{{token}}`
- Nếu vẫn error, login lại

### **Issue: 400 Bad Request (khi upload avatar)**
**Solution:**
- Kiểm tra file size < 5MB
- Kiểm tra file là image (JPEG, PNG, WebP, GIF)
- Nếu test "Upload Avatar - No File", đó là expected error

### **Issue: Tests fail khi run collection**
**Solution:**
- Kiểm tra base_url đúng: `http://localhost:8080`
- Kiểm tra Spring Boot app đang chạy
- Kiểm tra database có dữ liệu test (buyer1, seller1)

---

**Profile API Postman Collection - Ready to use!** 🎉
