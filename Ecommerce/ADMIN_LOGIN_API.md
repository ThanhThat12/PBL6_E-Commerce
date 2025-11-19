# 🔐 ADMIN LOGIN API - HƯỚNG DẪN SỬ DỤNG

## 📌 Overview

API endpoint riêng cho admin login, tách biệt với login thường để:
- ✅ Bảo mật cao hơn
- ✅ Kiểm tra role ADMIN ngay tại backend
- ✅ Sẵn sàng cho mobile app tương lai
- ✅ Logging và monitoring riêng cho admin access

---

## 🎯 API Endpoint

### **POST** `/api/auth/admin/login`

**Base URL:** `http://localhost:8081`

**Full URL:** `http://localhost:8081/api/auth/admin/login`

---

## 📥 Request

### Headers
```
Content-Type: application/json
```

### Body (JSON)
```json
{
  "username": "admin",
  "password": "your_password"
}
```

### Example with cURL
```bash
curl -X POST http://localhost:8081/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

---

## 📤 Response

### ✅ Success Response (200 OK)

```json
{
  "statusCode": 200,
  "error": null,
  "message": "Admin login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@dealport.com",
      "role": "ADMIN"
    }
  }
}
```

### ❌ Error Responses

#### 401 Unauthorized (Sai username/password)
```json
{
  "statusCode": 401,
  "error": "Unauthorized",
  "message": "Tên đăng nhập hoặc mật khẩu không đúng",
  "data": null
}
```

#### 403 Forbidden (Không phải ADMIN)
```json
{
  "statusCode": 403,
  "error": "Forbidden",
  "message": "Access denied. Only administrators can access this endpoint.",
  "data": null
}
```

#### 403 Forbidden (Account chưa activated)
```json
{
  "statusCode": 403,
  "error": "Forbidden",
  "message": "Your admin account is not activated. Please contact the system administrator.",
  "data": null
}
```

---

## 🔒 Security Checks

API này thực hiện các kiểm tra bảo mật sau:

1. ✅ **Authentication Check**
   - Validate username và password qua Spring Security
   - Sử dụng BCrypt password encoder

2. ✅ **Role Check**
   - User PHẢI có role = "ADMIN"
   - Reject nếu là BUYER hoặc SELLER

3. ✅ **Activation Check**
   - Account PHẢI được activated (activated = true)
   - Reject nếu account bị vô hiệu hóa

4. ✅ **Logging**
   - Log mọi admin login attempt
   - Log cả thành công và thất bại
   - Track username trong logs

---

## 🔑 Token Management

### Token trong Response
```javascript
{
  "token": "JWT_ACCESS_TOKEN",      // Dùng cho API calls (expires in 24h)
  "refreshToken": "REFRESH_TOKEN"   // Dùng để lấy token mới khi hết hạn
}
```

### Sử dụng Token
```javascript
// Frontend: Save to localStorage
localStorage.setItem('adminToken', data.token);
localStorage.setItem('adminRefreshToken', data.refreshToken);
localStorage.setItem('adminUser', JSON.stringify(data.user));

// API calls: Add to Authorization header
headers: {
  'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
}
```

---

## 📱 Frontend Integration

### adminAuthService.js
```javascript
import axios from 'axios';

const API_URL = 'http://localhost:8081/api';

export const loginAdmin = async (username, password) => {
  try {
    const response = await axios.post(`${API_URL}/auth/admin/login`, {
      username,
      password
    });

    if (response.data.statusCode === 200 && response.data.data) {
      const { token, refreshToken, user } = response.data.data;
      
      // Save to localStorage
      localStorage.setItem('adminToken', token);
      localStorage.setItem('adminRefreshToken', refreshToken);
      localStorage.setItem('adminUser', JSON.stringify(user));
      
      return { success: true, data: response.data.data };
    }
    
    return { success: false, message: response.data.message };
  } catch (error) {
    return {
      success: false,
      message: error.response?.data?.message || 'Đăng nhập thất bại'
    };
  }
};
```

### LoginAdmin Component
```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setIsLoading(true);

  try {
    const result = await loginAdmin(formData.username, formData.password);
    
    if (result.success) {
      navigate('/admin/dashboard');
    } else {
      setError(result.message);
    }
  } catch (err) {
    setError('Đã xảy ra lỗi. Vui lòng thử lại.');
  } finally {
    setIsLoading(false);
  }
};
```

---

## 🧪 Testing

### 1. Tạo Admin Account trong Database

```sql
-- Tạo role ADMIN (nếu chưa có)
INSERT INTO roles (id, name) VALUES (1, 'ADMIN');

-- Tạo admin user
INSERT INTO users (
  username, 
  email, 
  password, 
  role_id, 
  activated, 
  created_at
) VALUES (
  'admin', 
  'admin@dealport.com', 
  '$2a$10$YourBcryptHashedPasswordHere',  -- Use BCrypt
  1,  -- role_id = ADMIN
  true, 
  NOW()
);
```

**Generate BCrypt password:**
```bash
# Online: https://bcrypt-generator.com/
# Or use Spring Boot:
System.out.println(new BCryptPasswordEncoder().encode("admin123"));
```

### 2. Test với Postman

1. **Create new request:**
   - Method: `POST`
   - URL: `http://localhost:8081/api/auth/admin/login`

2. **Headers:**
   ```
   Content-Type: application/json
   ```

3. **Body (raw JSON):**
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

4. **Send** và check response

### 3. Test với Frontend

1. **Chạy backend:**
   ```bash
   cd PBL6_E-Commerce/Ecommerce
   ./mvnw spring-boot:run
   ```

2. **Chạy frontend:**
   ```bash
   cd PBL6_E-Commerce_FrontEnd
   npm start
   ```

3. **Truy cập:**
   ```
   http://localhost:3000/admin
   ```

4. **Login với admin credentials**

5. **Check browser console:**
   - 🔐 Attempting admin login...
   - 📍 API URL: ...
   - ✅ Login response: ...
   - 👤 Admin user: ...

---

## 🔄 Flow Diagram

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ POST /auth/admin/login
       │ {username, password}
       ↓
┌──────────────────┐
│ AuthController   │
│ @PostMapping     │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐     ✅ Credentials valid?
│   AuthService    │────────→ Spring Security
└──────┬───────────┘
       │
       ↓
┌──────────────────┐     ✅ Role = ADMIN?
│  UserRepository  │────────→ Check user.role
└──────┬───────────┘
       │
       ↓
┌──────────────────┐     ✅ Activated = true?
│  Validation      │────────→ Check user.activated
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Generate Tokens  │
│ - JWT Token      │
│ - Refresh Token  │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Return Response  │
│ - token          │
│ - refreshToken   │
│ - user info      │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│   Frontend       │
│ Save to          │
│ localStorage     │
│ → Dashboard      │
└──────────────────┘
```

---

## 🚀 Next Steps (Mobile App Ready)

Khi phát triển mobile app, chỉ cần:

1. **Gọi cùng endpoint** `/api/auth/admin/login`
2. **Gửi same format** `{username, password}`
3. **Nhận same response** với token
4. **Lưu token** vào secure storage (Keychain/KeyStore)
5. **Sử dụng token** cho API calls

**Không cần thay đổi backend!** ✅

---

## 📝 Notes

- Token expires sau **24 giờ** (có thể config trong `application.properties`)
- Refresh token dùng để lấy token mới khi hết hạn
- API có rate limiting để chống brute force (có thể thêm sau)
- Tất cả admin login attempts được log để audit

---

## ✅ Checklist

- [x] Backend: AdminLoginDTO created
- [x] Backend: Admin login endpoint implemented
- [x] Backend: Role check (ADMIN only)
- [x] Backend: Activation check
- [x] Backend: Exception handling
- [x] Backend: Logging
- [x] Frontend: adminAuthService.js updated
- [x] Frontend: LoginAdmin.jsx updated
- [x] Frontend: ProtectedRoute configured
- [x] CORS: Configured for localhost:3000
- [ ] Testing: Create admin account in DB
- [ ] Testing: Test login flow
- [ ] Testing: Test error cases

---

**🎉 Admin Login API đã sẵn sàng cho cả web và mobile app!**
