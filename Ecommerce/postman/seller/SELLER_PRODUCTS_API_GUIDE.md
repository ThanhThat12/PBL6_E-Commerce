# Seller Products API - Testing Guide

## 📋 Overview
Complete Postman collection for testing Seller Product Management features with new image structure:
- **1 Main Image** - Ảnh chính sản phẩm
- **Max 9 Product Images** - Ảnh sản phẩm bổ sung
- **1 Image per Variant** - Ảnh riêng cho mỗi biến thể

## 🚀 Quick Start

### 1. Import Collection
```bash
File > Import > Choose file:
PBL6_E-Commerce/Ecommerce/postman/seller/Seller_Products_API.postman_collection.json
```

### 2. Import Environment
```bash
File > Import > Choose file:
PBL6_E-Commerce/Ecommerce/postman/seller/Seller_API_Local.postman_environment.json
```

### 3. Setup Environment Variables
- `base_url`: `http://localhost:8080`
- `token`: (Auto-set sau khi login)

## 🔐 Authentication Flow

### Step 1: Login as Seller
```
POST {{base_url}}/api/auth/login

Body:
{
  "username": "seller1",
  "password": "password123"
}
```

**Response:** Token tự động lưu vào environment variable `{{token}}`

## 📸 Image Upload Workflow

### Complete Image Upload Flow for Product Creation

#### Step 1: Upload Main Image (Required)
```
POST {{base_url}}/api/upload/product-image
Content-Type: multipart/form-data
Authorization: Bearer {{token}}

FormData:
- file: [chọn 1 file ảnh chính]
```

**Response:**
```json
{
  "code": 200,
  "message": "Upload ảnh thành công",
  "data": "https://res.cloudinary.com/xxx/image/main-abc123.jpg"
}
```

**Lưu URL này vào `mainImage`**

---

#### Step 2: Upload Product Images (Max 9)
```
POST {{base_url}}/api/upload/product-images
Content-Type: multipart/form-data
Authorization: Bearer {{token}}

FormData:
- files: [file1.jpg]
- files: [file2.jpg]
- files: [file3.jpg]
... (tối đa 9 files)
```

**Response:**
```json
{
  "code": 200,
  "message": "Upload 3 ảnh thành công",
  "data": [
    "https://res.cloudinary.com/xxx/image/prod1.jpg",
    "https://res.cloudinary.com/xxx/image/prod2.jpg",
    "https://res.cloudinary.com/xxx/image/prod3.jpg"
  ]
}
```

**Lưu mảng URLs này vào `imageUrls`**

---

#### Step 3: Upload Variant Images (1 per variant)
Repeat for each variant:
```
POST {{base_url}}/api/upload/product-image
Content-Type: multipart/form-data
Authorization: Bearer {{token}}

FormData:
- file: [variant1-image.jpg]
```

**Response:**
```json
{
  "code": 200,
  "data": "https://res.cloudinary.com/xxx/image/variant1.jpg"
}
```

**Lưu URL này vào `variants[0].imageUrl`**

---

#### Step 4: Create Product with All Images
```
POST {{base_url}}/api/seller/products
Content-Type: application/json
Authorization: Bearer {{token}}

Body:
{
  "categoryId": 1,
  "shopId": 1,
  "name": "Áo Thun Nam Premium",
  "description": "Áo thun cotton 100% cao cấp",
  "basePrice": 200000,
  "isActive": true,
  
  // ✅ 1 ảnh chính
  "mainImage": "https://res.cloudinary.com/.../main-abc123.jpg",
  
  // ✅ Mảng ảnh sản phẩm (max 9)
  "imageUrls": [
    "https://res.cloudinary.com/.../prod1.jpg",
    "https://res.cloudinary.com/.../prod2.jpg",
    "https://res.cloudinary.com/.../prod3.jpg"
  ],
  
  // ✅ Variants với imageUrl riêng
  "variants": [
    {
      "sku": "ATN-M-BLACK",
      "price": 200000,
      "stock": 50,
      "imageUrl": "https://res.cloudinary.com/.../variant-black.jpg",
      "variantValues": [
        {
          "productAttributeId": 1,
          "value": "M",
          "productAttribute": { "name": "Size" }
        },
        {
          "productAttributeId": 2,
          "value": "Đen",
          "productAttribute": { "name": "Color" }
        }
      ]
    },
    {
      "sku": "ATN-L-WHITE",
      "price": 200000,
      "stock": 30,
      "imageUrl": "https://res.cloudinary.com/.../variant-white.jpg",
      "variantValues": [
        {
          "productAttributeId": 1,
          "value": "L",
          "productAttribute": { "name": "Size" }
        },
        {
          "productAttributeId": 2,
          "value": "Trắng",
          "productAttribute": { "name": "Color" }
        }
      ]
    }
  ],
  
  "weightGrams": 200,
  "lengthCm": 30,
  "widthCm": 25,
  "heightCm": 2
}
```

**Expected Response:**
```json
{
  "code": 201,
  "message": "Tạo sản phẩm thành công (tự động duyệt)",
  "data": {
    "id": 123,
    "name": "Áo Thun Nam Premium",
    "mainImage": "...",
    "images": [...],
    "variants": [...],
    "isActive": true
  }
}
```

## 🏪 Shop Management APIs

### Get Shop Info
```
GET {{base_url}}/api/seller/shop
Authorization: Bearer {{token}}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "My Fashion Shop",
    "description": "...",
    "address": "...",
    "phone": "0901234567",
    "email": "contact@shop.com",
    "logo": "https://res.cloudinary.com/.../logo.jpg",
    "banner": "https://res.cloudinary.com/.../banner.jpg",
    "rating": 4.5,
    "totalSales": 1500000,
    "status": "ACTIVE",
    "created_at": "2024-01-01T00:00:00",
    "updated_at": "2024-01-15T10:30:00"
  }
}
```

### Update Shop Info (with Logo & Banner)

#### Upload Shop Logo
```
POST {{base_url}}/api/upload/product-image
FormData:
- file: [shop-logo.jpg]
```

#### Upload Shop Banner
```
POST {{base_url}}/api/upload/product-image
FormData:
- file: [shop-banner.jpg]
```

#### Update Shop
```
PUT {{base_url}}/api/seller/shop
Authorization: Bearer {{token}}

Body:
{
  "name": "Updated Shop Name",
  "description": "...",
  "address": "...",
  "phone": "0901234567",
  "email": "shop@email.com",
  "logo": "https://res.cloudinary.com/.../new-logo.jpg",
  "banner": "https://res.cloudinary.com/.../new-banner.jpg",
  "status": "ACTIVE"
}
```

## 📦 Product Management APIs

### Get All Products
```
GET {{base_url}}/api/seller/products?page=0&size=10&sort=createdAt&direction=DESC
Authorization: Bearer {{token}}
```

### Get Product By ID
```
GET {{base_url}}/api/seller/products/{productId}
Authorization: Bearer {{token}}
```

### Update Product
```
PUT {{base_url}}/api/seller/products/{productId}
Authorization: Bearer {{token}}

Body: (Same structure as create, but keep variant.id to update existing)
{
  "categoryId": 1,
  "mainImage": "...",
  "imageUrls": [...],
  "variants": [
    {
      "id": 1,  // ← Keep ID to update
      "sku": "...",
      "imageUrl": "..."
    }
  ]
}
```

### Delete Product
```
DELETE {{base_url}}/api/seller/products/{productId}
Authorization: Bearer {{token}}
```

### Toggle Product Status
```
PATCH {{base_url}}/api/seller/products/{productId}/status?status=true
Authorization: Bearer {{token}}
```

## 🗂️ Categories & Attributes

### Get All Categories
```
GET {{base_url}}/api/categories
```

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "Fashion",
      "attributes": [
        {
          "id": 1,
          "name": "Size",
          "label": "Kích cỡ",
          "type": "select",
          "options": ["S", "M", "L", "XL"]
        },
        {
          "id": 2,
          "name": "Color",
          "label": "Màu sắc",
          "type": "select",
          "options": ["Đen", "Trắng", "Xanh", "Đỏ"]
        }
      ]
    }
  ]
}
```

## ✅ Testing Checklist

### Image Upload Structure
- [ ] Upload 1 main image successfully
- [ ] Upload 1-9 product images successfully
- [ ] Upload 1 image per variant successfully
- [ ] All image URLs valid and accessible
- [ ] Images displayed correctly in Cloudinary

### Product Creation
- [ ] Create product with all 3 types of images
- [ ] Product auto-approved (isActive = true)
- [ ] Main image saved correctly
- [ ] Product images array (max 9) saved
- [ ] Each variant has correct imageUrl

### Product Update
- [ ] Update main image only
- [ ] Update product images only
- [ ] Update variant images only
- [ ] Update all images together
- [ ] Keep existing images when not changed

### Shop Management
- [ ] Upload shop logo successfully
- [ ] Upload shop banner successfully
- [ ] Update shop with logo & banner
- [ ] Phone & email validation working
- [ ] Shop status updates correctly

## 🔧 Troubleshooting

### Common Issues

#### 1. Token Expired
**Error:** 401 Unauthorized
**Solution:** Re-login để lấy token mới

#### 2. Image Upload Failed
**Error:** 500 Internal Server Error
**Check:**
- File size < 5MB
- File type is image (JPG, PNG)
- Cloudinary credentials configured

#### 3. Product Create Failed - Missing Images
**Error:** 400 Bad Request
**Solution:** Đảm bảo đã upload và có URL cho:
- mainImage (required)
- imageUrls (optional, max 9)
- variant.imageUrl (optional, 1 per variant)

#### 4. Variant Without Image
**Note:** Variant.imageUrl là optional
- Nếu không upload ảnh variant, để empty string ""
- Frontend sẽ hiển thị placeholder

## 📊 Expected Behavior

### Auto-Approval Enabled ✅
- **Product Creation:** `isActive = true` (tự động active)
- **Shop Registration:** `status = ACTIVE` (tự động active)
- **No Admin Review:** Sản phẩm ngay lập tức hiển thị

### Image Limits
- Main Image: **1 required**
- Product Images: **0-9 optional**
- Variant Image: **0-1 per variant optional**

## 📝 Notes

- Tất cả APIs yêu cầu `Authorization: Bearer {{token}}`
- Token có thời hạn (JWT expiration)
- Upload images trước khi create/update product
- Lưu URLs từ response để dùng trong product payload
- Cloudinary URLs persistent (không mất khi restart server)

## 🎯 Test Scenarios

### Scenario 1: Create Product with Full Images
1. Login as seller
2. Upload main image → save URL
3. Upload 9 product images → save URLs array
4. Upload 2 variant images → save URLs
5. Create product with all URLs
6. Verify product created with isActive = true

### Scenario 2: Update Product - Change Main Image Only
1. Get existing product
2. Upload new main image
3. Update product with new mainImage URL
4. Keep existing imageUrls and variant images
5. Verify only main image changed

### Scenario 3: Shop Update with Logo & Banner
1. Get current shop info
2. Upload shop logo
3. Upload shop banner
4. Update shop with new logo & banner URLs
5. Update phone & email
6. Verify all fields updated correctly

---

**Collection Version:** 2.0  
**Last Updated:** 2024-01-15  
**Status:** ✅ Production Ready - Auto-Approval Enabled
