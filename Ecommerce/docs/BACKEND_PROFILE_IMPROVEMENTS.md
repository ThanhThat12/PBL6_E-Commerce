# Backend Profile Improvements - Implementation Summary

## Overview
Đã hoàn thành việc cải thiện backend cho module Profile, bao gồm việc thêm các API endpoint mới và nâng cấp cấu trúc dữ liệu User.

## Changes Implemented

### 1. Database Schema Enhancement
**File:** `User.java` (Entity)

**Thêm 4 trường mới:**
- `fullName` (String, length=100): Tên đầy đủ của người dùng
- `avatarUrl` (String, length=500): URL ảnh đại diện
- `createdAt` (LocalDateTime): Timestamp tự động khi tạo user (sử dụng @CreationTimestamp)
- `updatedAt` (LocalDateTime): Timestamp tự động khi cập nhật user (sử dụng @UpdateTimestamp)

**Migration Required:**
Cần chạy SQL migration để thêm các column mới vào bảng `users`:

```sql
ALTER TABLE users
ADD COLUMN full_name VARCHAR(100),
ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
```

---

### 2. New DTOs Created

#### a) **UpdateProfileDTO**
**Location:** `com.PBL6.Ecommerce.domain.dto.UpdateProfileDTO`

**Purpose:** Request DTO để cập nhật thông tin profile

**Fields:**
- `username` (String, 3-50 chars)
- `email` (String, @Email validation)
- `phoneNumber` (String, 10-15 chars)
- `fullName` (String)

**Validation:**
- @Size annotations cho username và phoneNumber
- @Email annotation cho email
- Tất cả fields là optional (có thể null)

#### b) **ChangePasswordDTO**
**Location:** `com.PBL6.Ecommerce.domain.dto.ChangePasswordDTO`

**Purpose:** Request DTO để đổi mật khẩu

**Fields:**
- `oldPassword` (String, @NotBlank)
- `newPassword` (String, @NotBlank, min=6 chars)
- `confirmPassword` (String, @NotBlank)

**Validation:**
- Tất cả fields đều required (@NotBlank)
- newPassword phải ít nhất 6 ký tự

#### c) **UserProfileDTO**
**Location:** `com.PBL6.Ecommerce.domain.dto.UserProfileDTO`

**Purpose:** Response DTO trả về đầy đủ thông tin profile

**Fields:**
- `id` (Long)
- `username` (String)
- `email` (String)
- `phoneNumber` (String)
- `fullName` (String)
- `role` (String)
- `avatarUrl` (String)
- `activated` (Boolean)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Khác biệt với UserInfoDTO:** Thêm fullName, avatarUrl, activated, createdAt, updatedAt

---

### 3. UserService - New Methods

**Location:** `com.PBL6.Ecommerce.service.UserService`

#### a) **getUserProfile()**
```java
public UserProfileDTO getUserProfile()
```
- Lấy thông tin profile đầy đủ của user hiện tại
- Sử dụng SecurityContext để lấy Authentication
- Trả về UserProfileDTO với tất cả fields
- Throws: UnauthenticatedException nếu chưa đăng nhập

#### b) **updateProfile(UpdateProfileDTO dto)**
```java
@Transactional
public UserProfileDTO updateProfile(UpdateProfileDTO dto)
```
- Cập nhật thông tin profile của user hiện tại
- Kiểm tra duplicate username/email/phoneNumber trước khi update
- Chỉ cập nhật các field không null trong DTO
- Trả về UserProfileDTO sau khi update
- Throws: 
  - UnauthenticatedException nếu chưa đăng nhập
  - DuplicateEmailException nếu email đã tồn tại
  - DuplicatePhoneException nếu số điện thoại đã tồn tại
  - RuntimeException nếu username đã tồn tại

#### c) **changePassword(ChangePasswordDTO dto)**
```java
@Transactional
public void changePassword(ChangePasswordDTO dto)
```
- Đổi mật khẩu cho user hiện tại
- Xác thực oldPassword với passwordEncoder
- So sánh newPassword và confirmPassword
- Mã hóa và lưu newPassword
- Throws:
  - UnauthenticatedException nếu chưa đăng nhập
  - PasswordMismatchException nếu oldPassword sai hoặc newPassword != confirmPassword

#### d) **updateAvatar(String avatarUrl)**
```java
@Transactional
public UserProfileDTO updateAvatar(String avatarUrl)
```
- Cập nhật avatar URL cho user hiện tại
- Trả về UserProfileDTO sau khi update
- Throws: UnauthenticatedException nếu chưa đăng nhập

---

### 4. ProfileController - New REST Endpoints

**Location:** `com.PBL6.Ecommerce.controller.ProfileController`

**Base URL:** `/api/user`

#### Endpoint 1: Get Profile
```
GET /api/user/profile
```
**Auth:** Required (JWT token)

**Response:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "phoneNumber": "0123456789",
    "fullName": "John Doe",
    "role": "CUSTOMER",
    "avatarUrl": "https://example.com/avatar.jpg",
    "activated": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-15T14:30:00"
  }
}
```

#### Endpoint 2: Update Profile
```
PUT /api/user/profile
```
**Auth:** Required (JWT token)

**Request Body:**
```json
{
  "username": "new_username",
  "email": "new_email@example.com",
  "phoneNumber": "0987654321",
  "fullName": "John Updated Doe"
}
```

**Validation:**
- username: 3-50 ký tự (optional)
- email: phải là email hợp lệ (optional)
- phoneNumber: 10-15 ký tự (optional)
- fullName: (optional)

**Response:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Profile updated successfully",
  "data": { /* UserProfileDTO */ }
}
```

**Error Cases:**
- 400: Validation error (invalid email, username too short, etc.)
- 401: Unauthenticated
- 409: Duplicate username/email/phoneNumber

#### Endpoint 3: Change Password
```
PUT /api/user/change-password
```
**Auth:** Required (JWT token)

**Request Body:**
```json
{
  "oldPassword": "current_password",
  "newPassword": "new_password_123",
  "confirmPassword": "new_password_123"
}
```

**Validation:**
- oldPassword: required
- newPassword: required, min 6 ký tự
- confirmPassword: required, phải giống newPassword

**Response:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Password changed successfully",
  "data": "Password has been updated"
}
```

**Error Cases:**
- 400: Validation error (newPassword too short, passwords don't match)
- 401: Unauthenticated
- 403: Old password incorrect

#### Endpoint 4: Update Avatar
```
PUT /api/user/avatar?avatarUrl={url}
```
**Auth:** Required (JWT token)

**Query Parameter:**
- `avatarUrl` (String, required): URL của ảnh đại diện mới

**Response:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Avatar updated successfully",
  "data": { /* UserProfileDTO */ }
}
```

---

## Testing Recommendations

### 1. Database Migration Test
1. Chạy SQL migration script
2. Verify các column mới đã được tạo
3. Test với existing users (các column mới nên là NULL)

### 2. API Testing với Postman/Thunder Client

**Test Case 1: Get Profile**
```
GET http://localhost:8080/api/user/profile
Headers:
  Authorization: Bearer {your_jwt_token}
```

**Test Case 2: Update Profile - Success**
```
PUT http://localhost:8080/api/user/profile
Headers:
  Authorization: Bearer {your_jwt_token}
  Content-Type: application/json
Body:
{
  "fullName": "Test User Full Name",
  "phoneNumber": "0123456789"
}
```

**Test Case 3: Update Profile - Duplicate Email**
```
PUT http://localhost:8080/api/user/profile
Body:
{
  "email": "existing_user@example.com"  // Email đã tồn tại
}
Expected: 409 Conflict hoặc error message
```

**Test Case 4: Change Password - Success**
```
PUT http://localhost:8080/api/user/change-password
Headers:
  Authorization: Bearer {your_jwt_token}
Body:
{
  "oldPassword": "correct_old_password",
  "newPassword": "new_secure_password",
  "confirmPassword": "new_secure_password"
}
```

**Test Case 5: Change Password - Wrong Old Password**
```
PUT http://localhost:8080/api/user/change-password
Body:
{
  "oldPassword": "wrong_password",
  "newPassword": "new_password",
  "confirmPassword": "new_password"
}
Expected: 403 Forbidden hoặc PasswordMismatchException
```

**Test Case 6: Change Password - Passwords Don't Match**
```
PUT http://localhost:8080/api/user/change-password
Body:
{
  "oldPassword": "correct_password",
  "newPassword": "password1",
  "confirmPassword": "password2"
}
Expected: 400 Bad Request hoặc PasswordMismatchException
```

**Test Case 7: Update Avatar**
```
PUT http://localhost:8080/api/user/avatar?avatarUrl=https://example.com/new-avatar.jpg
Headers:
  Authorization: Bearer {your_jwt_token}
```

---

## Frontend Integration Guide

### 1. Update API Endpoints Configuration

Thêm vào file `src/services/api/endpoints.js`:

```javascript
export const API_ENDPOINTS = {
  // ... existing endpoints
  PROFILE: {
    GET: '/api/user/profile',
    UPDATE: '/api/user/profile',
    CHANGE_PASSWORD: '/api/user/change-password',
    UPDATE_AVATAR: '/api/user/avatar',
  },
};
```

### 2. Create/Update userService.js

```javascript
// src/services/userService.js
import api from './api/axiosConfig';
import { API_ENDPOINTS } from './api/endpoints';

export const userService = {
  // Get full profile
  getProfile: async () => {
    const response = await api.get(API_ENDPOINTS.PROFILE.GET);
    return response.data;
  },

  // Update profile
  updateProfile: async (profileData) => {
    const response = await api.put(API_ENDPOINTS.PROFILE.UPDATE, profileData);
    return response.data;
  },

  // Change password
  changePassword: async (passwordData) => {
    const response = await api.put(API_ENDPOINTS.PROFILE.CHANGE_PASSWORD, passwordData);
    return response.data;
  },

  // Update avatar
  updateAvatar: async (avatarUrl) => {
    const response = await api.put(
      `${API_ENDPOINTS.PROFILE.UPDATE_AVATAR}?avatarUrl=${encodeURIComponent(avatarUrl)}`
    );
    return response.data;
  },
};
```

### 3. Fix ProfilePage.jsx Bugs

```javascript
// Line 24-30 - Fix response handling
useEffect(() => {
  const fetchUserProfile = async () => {
    try {
      const response = await userService.getProfile();
      // FIX: Change response.data.code to response.statusCode
      if (response.statusCode === 200) {
        // FIX: Change response.data.data to response.data
        setUserInfo(response.data);
      }
    } catch (error) {
      console.error('Error fetching user info:', error);
    }
  };

  // Remove duplicate data loading - get from AuthContext OR API, not both
  if (user) {
    setUserInfo(user);
  } else {
    fetchUserProfile();
  }
}, [user]);
```

### 4. Create ProfileEditModal Component

```javascript
// src/components/modals/ProfileEditModal.jsx
import React, { useState } from 'react';
import { userService } from '../../services/userService';

const ProfileEditModal = ({ isOpen, onClose, currentProfile, onSuccess }) => {
  const [formData, setFormData] = useState({
    username: currentProfile?.username || '',
    email: currentProfile?.email || '',
    phoneNumber: currentProfile?.phoneNumber || '',
    fullName: currentProfile?.fullName || '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await userService.updateProfile(formData);
      if (response.statusCode === 200) {
        onSuccess(response.data);
        onClose();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2>Edit Profile</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              minLength={3}
              maxLength={50}
            />
          </div>
          
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="text"
              value={formData.phoneNumber}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              minLength={10}
              maxLength={15}
            />
          </div>

          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="modal-actions">
            <button type="button" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" disabled={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileEditModal;
```

---

## Next Steps

### Immediate Actions Required:
1. **Database Migration**: Chạy SQL script để thêm 4 columns mới vào bảng users
2. **Test APIs**: Dùng Postman/Thunder Client test tất cả 4 endpoints
3. **Update Frontend**: 
   - Fix ProfilePage.jsx bugs (response handling)
   - Add API endpoints configuration
   - Create ProfileEditModal component
   - Create ChangePasswordModal component
   - Add avatar upload UI (optional, có thể dùng external service như Cloudinary)

### Optional Enhancements:
1. **Avatar Upload**: Implement file upload endpoint với MultipartFile
2. **Email Verification**: Khi đổi email, gửi verification code
3. **Phone Verification**: Khi đổi số điện thoại, gửi OTP
4. **Profile Image Storage**: Integrate với cloud storage (AWS S3, Cloudinary, etc.)
5. **Audit Log**: Log tất cả profile changes vào audit table
6. **Rate Limiting**: Giới hạn số lần đổi password/profile trong 1 khoảng thời gian

---

## Files Created/Modified

### Created:
1. `UpdateProfileDTO.java` - Request DTO cho update profile
2. `ChangePasswordDTO.java` - Request DTO cho change password
3. `UserProfileDTO.java` - Response DTO cho profile data
4. `ProfileController.java` - REST controller cho profile endpoints

### Modified:
1. `User.java` - Added 4 new fields (fullName, avatarUrl, createdAt, updatedAt)
2. `UserService.java` - Added 4 new methods (getUserProfile, updateProfile, changePassword, updateAvatar)

---

## Security Considerations

1. **Authentication**: Tất cả endpoints đều require JWT authentication
2. **Authorization**: User chỉ có thể update profile của chính họ (checked trong service layer)
3. **Password Validation**: 
   - Old password phải correct
   - New password min 6 chars
   - Passwords được mã hóa với BCrypt
4. **Duplicate Prevention**: Check username/email/phone uniqueness trước khi update
5. **Input Validation**: Jakarta Bean Validation (@Valid, @NotBlank, @Email, @Size)

---

## Summary
✅ Database schema enhanced với 4 fields mới
✅ 3 DTOs created với full validation
✅ 4 service methods implemented với security checks
✅ 4 REST endpoints created với proper error handling
✅ Documentation complete với testing guide và frontend integration guide

Backend profile module đã hoàn thành! Bước tiếp theo là chạy database migration và test các APIs.
