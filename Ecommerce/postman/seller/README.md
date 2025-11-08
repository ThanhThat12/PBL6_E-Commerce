# Seller Management API - Postman Collection

## 📦 Collections Available

### 1. **Seller_API.postman_collection.json** (Original)
Basic seller registration and shop management APIs

### 2. **Seller_Products_API.postman_collection.json** (NEW - Phase 2)
Complete product management with new image structure:
- **1 Main Image** - Required product main image
- **Max 9 Product Images** - Additional product photos
- **1 Image per Variant** - Specific image for each variant

## 🚀 Quick Start

### Import Collections
1. Open Postman
2. File > Import
3. Select both collection files:
   - `Seller_API.postman_collection.json`
   - `Seller_Products_API.postman_collection.json`

### Import Environment
```
File > Import > Seller_API_Local.postman_environment.json
```

### Environment Variables
- `base_url`: `http://localhost:8080`
- `token`: (Auto-set after login)
- `username`: (Auto-set after login)

## 📋 API Categories

### 🔐 Authentication
- Login as Buyer
- Login as Seller

### 🏪 Seller Registration & Shop Management
- **POST** `/api/seller/register` - Buyer upgrades to seller
- **GET** `/api/seller/shop` - Get shop info (with new fields)
- **PUT** `/api/seller/shop` - Update shop (logo, banner, phone, email)

### 📦 Product Management (Phase 2)
- **POST** `/api/seller/products` - Create product with images
- **GET** `/api/seller/products` - List all products
- **GET** `/api/seller/products/:id` - Get product details
- **PUT** `/api/seller/products/:id` - Update product
- **DELETE** `/api/seller/products/:id` - Delete product
- **PATCH** `/api/seller/products/:id/status` - Toggle active status

### 📸 Image Upload (NEW)
- **POST** `/api/upload/product-image` - Upload single image
- **POST** `/api/upload/product-images` - Upload multiple images (max 9)
- **DELETE** `/api/upload/image` - Delete image from Cloudinary

### 🗂️ Categories & Attributes
- **GET** `/api/categories` - List all categories with attributes
- **GET** `/api/categories/:id` - Get category details

### 📊 Orders & Analytics
- **GET** `/api/seller/orders` - Get shop orders
- **GET** `/api/seller/analytics/overview` - Shop statistics

## 🎯 New Features - Phase 2

### Image Structure
```json
{
  "mainImage": "https://cloudinary.com/.../main.jpg",  // 1 required
  "imageUrls": [                                        // 0-9 optional
    "https://cloudinary.com/.../img1.jpg",
    "https://cloudinary.com/.../img2.jpg"
  ],
  "variants": [
    {
      "sku": "PROD-M-BLACK",
      "imageUrl": "https://cloudinary.com/.../variant-black.jpg",  // 0-1 per variant
      "variantValues": [...]
    }
  ]
}
```

### Shop Info Extended
```json
{
  "name": "Shop Name",
  "description": "...",
  "address": "...",
  "phone": "0901234567",           // NEW
  "email": "shop@email.com",       // NEW
  "logo": "https://.../logo.jpg",  // NEW
  "banner": "https://.../banner.jpg", // NEW
  "rating": 4.5,                   // NEW
  "totalSales": 1500000,           // NEW
  "status": "ACTIVE"
}
```

### Auto-Approval Enabled ✅
- **Products**: Auto-approved on creation (`isActive = true`)
- **Shops**: Auto-active on registration (`status = ACTIVE`)
- **No admin review** required temporarily

## 📖 Documentation

### Detailed Guides
- **SELLER_PRODUCTS_API_GUIDE.md** - Complete testing guide with examples
- Includes image upload workflow
- Product creation/update scenarios
- Shop management with logo/banner

## 🧪 Testing Flow

### Complete Product Creation Test

1. **Login as Seller**
   ```
   POST /api/auth/login
   Body: {"username": "seller1", "password": "password123"}
   ```

2. **Upload Main Image**
   ```
   POST /api/upload/product-image
   FormData: file = main-image.jpg
   → Save URL to mainImage
   ```

3. **Upload Product Images (max 9)**
   ```
   POST /api/upload/product-images
   FormData: files[] = [img1.jpg, img2.jpg, img3.jpg]
   → Save URLs array to imageUrls
   ```

4. **Upload Variant Images**
   ```
   POST /api/upload/product-image (for each variant)
   → Save URL to variants[i].imageUrl
   ```

5. **Create Product**
   ```
   POST /api/seller/products
   Body: {
     mainImage: "...",
     imageUrls: [...],
     variants: [{imageUrl: "..."}]
   }
   ```

6. **Verify Product**
   ```
   GET /api/seller/products/:id
   → Check isActive = true (auto-approved)
   → Check all images present
   ```

## 🔧 Business Rules

### Product Rules
- **isActive** defaults to `true` (auto-approval)
- **Main image** is required
- **Product images** max 9
- **Variant image** max 1 per variant
- Sellers can only manage their own products

### Shop Rules
- **Phone** must be unique and 10-11 digits
- **Email** must be valid format
- **Logo & Banner** stored as Cloudinary URLs
- **Status** can be ACTIVE/INACTIVE
- Auto-activated on registration

### Image Upload Rules
- **File types**: JPG, PNG only
- **Max file size**: 5MB per image
- **Storage**: Cloudinary with auto-optimization
- **URLs**: Persistent (VARCHAR 500 in DB)

## 🐛 Troubleshooting

### Common Issues

#### 1. 401 Unauthorized
- **Cause**: Token expired or missing
- **Fix**: Login again to get new token

#### 2. Image Upload Failed (500)
- **Check**: Cloudinary credentials in application.properties
- **Check**: File size < 5MB and type is image

#### 3. Product Create Failed - Missing mainImage
- **Cause**: mainImage is required
- **Fix**: Upload main image first, then create product

#### 4. Shop Update Failed - Phone/Email Invalid
- **Check**: Phone 10-11 digits, Email valid format
- **Check**: Phone/Email not already used by another shop

## 📊 Response Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Request successful |
| 201 | Created | Resource created (auto-approved) |
| 400 | Bad Request | Check request body/params |
| 401 | Unauthorized | Re-login to get token |
| 403 | Forbidden | Check user role (need SELLER) |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Check server logs |

## 🎯 Test Checklist

### Image Upload
- [ ] Upload single image successfully
- [ ] Upload multiple images (1-9) successfully
- [ ] Upload fails for invalid file type
- [ ] Upload fails for file > 5MB
- [ ] Delete image from Cloudinary

### Product Management
- [ ] Create product with all image types
- [ ] Product auto-approved (isActive = true)
- [ ] Update product main image
- [ ] Update product images array
- [ ] Update variant images
- [ ] Delete product
- [ ] Toggle product status

### Shop Management
- [ ] Get shop info with new fields
- [ ] Upload shop logo
- [ ] Upload shop banner
- [ ] Update shop with logo/banner/phone/email
- [ ] Phone/Email validation working

## 📚 Related Documentation

- [Backend API Documentation](../../docs/)
- [Frontend Integration Guide](../../../PBL6_SellerFE/README.md)
- [Seller FE API Sync](../../../PBL6_SellerFE/SELLER_FE_API_SYNC.md)

## 📝 Notes

- All endpoints require `Authorization: Bearer {{token}}`
- Token auto-saved to environment after login
- Cloudinary URLs are persistent
- Image upload must happen before product create/update
- Auto-approval temporarily enabled for testing

---

**Last Updated**: 2024-01-15  
**Version**: 2.0 (Phase 2 - Image Structure Update)  
**Status**: ✅ Ready for Testing

4. **Manage Shop**
   - Get shop info: `GET /api/seller/shop`
   - Update shop: `PUT /api/seller/shop`

## Error Scenarios to Test

- Register with existing seller phone number
- Register with existing shop name
- Register with non-buyer account
- Access shop APIs without SELLER role
- Update non-existent shop

## Sample Data

### Buyer Credentials
```json
{
  "username": "buyer1",
  "password": "buyer123"
}
```

### Seller Registration Request
```json
{
  "shopName": "My Awesome Shop",
  "shopDescription": "Best products in town",
  "shopPhone": "0987654321",
  "shopAddress": "123 Main Street, City"
}
```

### Shop Update Request
```json
{
  "name": "Updated Shop Name",
  "address": "456 New Address",
  "description": "Updated description",
  "status": "ACTIVE"
}
```