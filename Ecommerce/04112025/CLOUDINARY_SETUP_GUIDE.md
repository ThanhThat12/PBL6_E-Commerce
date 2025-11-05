# 📸 CLOUDINARY INTEGRATION GUIDE

> **Status**: ✅ **HOÀN CHỈNH** - Backend & Frontend đã được implement  
> **Last Updated**: November 4, 2025

---

## 📋 TABLE OF CONTENTS

1. [Setup Cloudinary Account](#setup-cloudinary-account)
2. [Backend Configuration](#backend-configuration)
3. [Frontend Integration](#frontend-integration)
4. [API Endpoints](#api-endpoints)
5. [Usage Examples](#usage-examples)
6. [Troubleshooting](#troubleshooting)

---

## 🔑 SETUP CLOUDINARY ACCOUNT

### Step 1: Create Account
1. Truy cập: https://cloudinary.com/
2. Click **Sign Up** (hoặc Sign Up Free)
3. Đăng ký bằng email hoặc Google
4. Verify email

### Step 2: Get API Credentials
1. Đăng nhập vào Cloudinary Dashboard
2. Vào **Dashboard** > **Account Details**
3. Copy 3 thông tin sau:
   ```
   Cloud Name: your_cloud_name
   API Key: 123456789012345
   API Secret: your_api_secret_here
   ```

### Step 3: Configure Upload Presets (Optional)
1. Vào **Settings** > **Upload**
2. Scroll xuống **Upload presets**
3. Click **Add upload preset**
4. Thiết lập:
   - **Preset name**: `ecommerce_uploads`
   - **Signing Mode**: `Unsigned`
   - **Folder**: `ecommerce` (hoặc để trống)
   - **Unique filename**: ✅ Enabled
   - **Overwrite**: ❌ Disabled

---

## ⚙️ BACKEND CONFIGURATION

### Step 1: Update `application.properties`

File: `Ecommerce/src/main/resources/application.properties`

```properties
# Cloudinary credentials
cloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.api_secret=YOUR_API_SECRET
```

**⚠️ QUAN TRỌNG**: 
- Thay `YOUR_CLOUD_NAME`, `YOUR_API_KEY`, `YOUR_API_SECRET` bằng credentials từ Cloudinary Dashboard
- **KHÔNG** commit file này lên Git với credentials thật
- Sử dụng environment variables cho production

### Step 2: Environment Variables (Recommended for Production)

Tạo file `.env` trong thư mục `Ecommerce/`:

```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=your_api_secret_here
```

Update `application.properties`:

```properties
cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api_key=${CLOUDINARY_API_KEY}
cloudinary.api_secret=${CLOUDINARY_API_SECRET}
```

### Step 3: Verify Backend Implementation

Backend đã implement các files sau:

✅ **Config**:
- `com.PBL6.Ecommerce.config.CloudinaryConfig` - Cloudinary Bean configuration

✅ **Service**:
- `com.PBL6.Ecommerce.service.CloudinaryService` - Upload & delete logic

✅ **Controller**:
- `com.PBL6.Ecommerce.controller.ImageUploadController` - REST API endpoints

---

## 🎨 FRONTEND INTEGRATION

### Files Created:

✅ `src/services/cloudinaryService.js` - API service functions
✅ `src/components/common/ImageUploader.jsx` - Reusable upload component
✅ `src/components/common/ImageUploader.css` - Component styles

### Import & Use:

```javascript
import ImageUploader from '../../components/common/ImageUploader';
import { uploadReviewImages } from '../../services/cloudinaryService';
```

---

## 📡 API ENDPOINTS

### 1. **Upload Avatar** (BUYER/SELLER/ADMIN)

```http
POST /api/upload/avatar
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Request:**
```
Field name: file
Type: Image file (JPEG, PNG, GIF, WEBP)
Max size: 5MB
```

**Response:**
```json
{
  "url": "https://res.cloudinary.com/your_cloud/image/upload/v123/ecommerce/avatars/user_123_uuid.jpg"
}
```

**Frontend Usage:**
```javascript
import { uploadAvatar } from '../services/cloudinaryService';

const handleAvatarUpload = async (file) => {
  try {
    const url = await uploadAvatar(file);
    console.log('Avatar URL:', url);
    // Update user profile with url
  } catch (error) {
    console.error('Upload failed:', error);
  }
};
```

---

### 2. **Upload Product Image** (SELLER only)

```http
POST /api/upload/product?productId=123
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Request:**
```
Field name: file
Optional query param: productId (default: 0)
```

**Response:**
```json
{
  "url": "https://res.cloudinary.com/.../ecommerce/products/product_123_uuid.jpg"
}
```

**Frontend Usage:**
```javascript
import { uploadProductImage } from '../services/cloudinaryService';

const handleProductImageUpload = async (file, productId) => {
  const url = await uploadProductImage(file, productId);
  // Add to product images array
};
```

---

### 3. **Upload Single Review Image** (BUYER only)

```http
POST /api/upload/review
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Response:**
```json
{
  "url": "https://res.cloudinary.com/.../ecommerce/reviews/user_123_uuid.jpg"
}
```

---

### 4. **Upload Multiple Review Images** (BUYER only)

```http
POST /api/upload/reviews
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data
```

**Request:**
```
Field name: files (array)
Max files: 5
```

**Response:**
```json
{
  "urls": [
    "https://res.cloudinary.com/.../image1.jpg",
    "https://res.cloudinary.com/.../image2.jpg"
  ]
}
```

**Frontend Usage:**
```javascript
import { uploadReviewImages } from '../services/cloudinaryService';

const handleReviewImagesUpload = async (files) => {
  const urls = await uploadReviewImages(files);
  console.log('Uploaded URLs:', urls);
  // Use urls in review creation
};
```

---

### 5. **Delete Image** (All authenticated users)

```http
DELETE /api/upload/image
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "imageUrl": "https://res.cloudinary.com/.../image.jpg"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Xóa ảnh thành công"
}
```

---

## 💻 USAGE EXAMPLES

### Example 1: Upload Avatar in Profile Page

```jsx
import React, { useState } from 'react';
import { uploadAvatar } from '../../services/cloudinaryService';

const ProfilePage = () => {
  const [avatarUrl, setAvatarUrl] = useState('');
  const [uploading, setUploading] = useState(false);

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);
    try {
      const url = await uploadAvatar(file);
      setAvatarUrl(url);
      
      // Update user profile via API
      await updateUserProfile({ avatarUrl: url });
      
      alert('Cập nhật avatar thành công!');
    } catch (error) {
      alert('Upload thất bại: ' + error.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <input 
        type="file" 
        accept="image/*" 
        onChange={handleFileChange}
        disabled={uploading}
      />
      {uploading && <p>Đang tải lên...</p>}
      {avatarUrl && <img src={avatarUrl} alt="Avatar" width="200" />}
    </div>
  );
};
```

---

### Example 2: Use ImageUploader Component in Review Form

```jsx
import React, { useState } from 'react';
import ImageUploader from '../../components/common/ImageUploader';
import { createReview } from '../../services/reviewService';

const CreateReviewForm = ({ productId, orderId }) => {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [imageUrls, setImageUrls] = useState([]);

  const handleImagesUploaded = (urls) => {
    setImageUrls(urls);
    alert(`Upload thành công ${urls.length} ảnh!`);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      await createReview(productId, {
        orderId,
        rating,
        comment,
        images: imageUrls
      });
      
      alert('Đánh giá thành công!');
    } catch (error) {
      alert('Lỗi: ' + error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Đánh giá sản phẩm</h2>
      
      {/* Rating selector */}
      <div>
        <label>Đánh giá:</label>
        <select value={rating} onChange={(e) => setRating(e.target.value)}>
          <option value="5">5 ⭐⭐⭐⭐⭐</option>
          <option value="4">4 ⭐⭐⭐⭐</option>
          <option value="3">3 ⭐⭐⭐</option>
          <option value="2">2 ⭐⭐</option>
          <option value="1">1 ⭐</option>
        </select>
      </div>

      {/* Comment */}
      <div>
        <label>Nhận xét:</label>
        <textarea 
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          maxLength={1000}
          rows={5}
        />
      </div>

      {/* Image Uploader */}
      <ImageUploader
        maxImages={5}
        onUploadSuccess={handleImagesUploaded}
        onUploadError={(error) => alert(error.message)}
        uploadType="review"
      />

      {/* Submit */}
      <button type="submit" disabled={!imageUrls.length}>
        Gửi đánh giá
      </button>
    </form>
  );
};
```

---

### Example 3: Manual Upload with Progress

```jsx
import React, { useState } from 'react';
import { uploadReviewImages, validateImageFile } from '../../services/cloudinaryService';

const ManualUpload = () => {
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [urls, setUrls] = useState([]);

  const handleFileSelect = (e) => {
    const selectedFiles = Array.from(e.target.files);
    
    // Validate each file
    try {
      selectedFiles.forEach(file => validateImageFile(file));
      setFiles(selectedFiles);
    } catch (error) {
      alert(error.message);
    }
  };

  const handleUpload = async () => {
    if (files.length === 0) return;

    setUploading(true);
    try {
      const uploadedUrls = await uploadReviewImages(files);
      setUrls(uploadedUrls);
      alert('Upload thành công!');
    } catch (error) {
      alert('Upload thất bại: ' + error.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <input 
        type="file" 
        multiple 
        accept="image/*" 
        onChange={handleFileSelect}
      />
      <button onClick={handleUpload} disabled={uploading || files.length === 0}>
        {uploading ? 'Đang upload...' : 'Upload'}
      </button>
      
      {urls.length > 0 && (
        <div>
          <h3>Uploaded Images:</h3>
          {urls.map((url, index) => (
            <img key={index} src={url} alt={`Upload ${index}`} width="200" />
          ))}
        </div>
      )}
    </div>
  );
};
```

---

## 🐛 TROUBLESHOOTING

### Issue 1: "Invalid API credentials"

**Cause**: Sai Cloud Name, API Key hoặc API Secret

**Solution**:
1. Kiểm tra lại `application.properties`
2. Verify credentials từ Cloudinary Dashboard
3. Restart Spring Boot application sau khi update

---

### Issue 2: "File size exceeds limit"

**Cause**: File lớn hơn 5MB

**Solution**:
```javascript
// Compress image before upload
import { compressImage } from '../services/cloudinaryService';

const file = e.target.files[0];
const compressed = await compressImage(file, 1920, 0.8);
const url = await uploadAvatar(compressed);
```

---

### Issue 3: "CORS Error"

**Cause**: Frontend gọi API từ domain khác

**Solution**: Backend đã config CORS trong `SecurityConfig`, nhưng nếu vẫn lỗi:

```java
// Add to SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000");
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### Issue 4: "Upload timeout"

**Cause**: Network slow hoặc file quá lớn

**Solution**:
1. Compress image trước khi upload
2. Tăng timeout trong `api.js`:

```javascript
// src/services/api.js
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000, // 60 seconds
});
```

---

### Issue 5: "401 Unauthorized"

**Cause**: JWT token expired hoặc không có token

**Solution**:
```javascript
// Check if token exists and is valid
const token = localStorage.getItem('access_token');
if (!token) {
  // Redirect to login
  window.location.href = '/login';
}
```

---

## 📊 CLOUDINARY FOLDER STRUCTURE

Ảnh được organize theo folder:

```
ecommerce/
├── avatars/
│   └── user_123_uuid.jpg
├── products/
│   └── product_456_uuid.jpg
└── reviews/
    └── user_789_uuid.jpg
```

---

## 🔒 SECURITY BEST PRACTICES

1. **Never commit credentials**:
   - Add `application.properties` to `.gitignore`
   - Use environment variables

2. **Validate files on both frontend & backend**:
   - Max size: 5MB
   - Allowed types: JPEG, PNG, GIF, WEBP

3. **Use signed uploads for sensitive images**:
   - Avatar: Public
   - Product: Public
   - Review: Public (but can be moderated)

4. **Delete unused images**:
   ```javascript
   import { deleteImage } from '../services/cloudinaryService';
   await deleteImage(oldImageUrl);
   ```

---

## 📈 MONITORING & ANALYTICS

1. **Check upload statistics**:
   - Cloudinary Dashboard > Analytics

2. **Monitor storage usage**:
   - Dashboard > Media Library

3. **Set up transformation quotas**:
   - Settings > Upload > Transformation quotas

---

## ✅ CHECKLIST

### Backend Setup:
- [x] Add Cloudinary credentials to `application.properties`
- [x] `CloudinaryConfig.java` implemented
- [x] `CloudinaryService.java` implemented
- [x] `ImageUploadController.java` created
- [ ] Test upload endpoints with Postman
- [ ] Configure environment variables for production

### Frontend Setup:
- [x] `cloudinaryService.js` created
- [x] `ImageUploader.jsx` component created
- [x] `ImageUploader.css` styles added
- [ ] Import and use in review form
- [ ] Import and use in profile page
- [ ] Import and use in product creation

### Testing:
- [ ] Test avatar upload
- [ ] Test product image upload
- [ ] Test review images upload (multiple)
- [ ] Test image deletion
- [ ] Test error handling (large file, wrong format)

---

**Last Updated**: November 4, 2025  
**Status**: ✅ Ready for Integration
