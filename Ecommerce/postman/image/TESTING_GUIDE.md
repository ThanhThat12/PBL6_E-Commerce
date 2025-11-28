# Product Image Upload API - Testing Guide

**Feature**: 005-product-image-upload  
**Version**: 1.0.0  
**Last Updated**: 2025-11-22

## Overview

Complete API collection for uploading and managing product images (main, gallery, and variant images).

## Authentication

All endpoints (except public GET) require JWT Bearer token:
```
Authorization: Bearer <your_jwt_token>
```

## Base URL

- Development: `http://localhost:8080/api`
- Production: `https://api.pbl6-ecommerce.com/api`

## API Endpoints

### 1. Main Image Operations

#### Upload/Replace Main Image
```http
POST /products/{productId}/images/main
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- file: (binary) Image file (JPEG/PNG/WEBP, max 5MB, min 500x500px)
```

**Response 200**:
```json
{
  "status": "SUCCESS",
  "data": {
    "imageUrl": "https://res.cloudinary.com/.../main_image.jpg",
    "publicId": "products/123/main/main_image",
    "message": "Main image uploaded successfully"
  }
}
```

#### Delete Main Image
```http
DELETE /products/{productId}/images/main
Authorization: Bearer {token}
```

---

### 2. Gallery Images Operations

#### Upload Gallery Images
```http
POST /products/{productId}/images/gallery
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- files: (binary array) Up to 10 images total per product
```

**Response 200**:
```json
{
  "status": "SUCCESS",
  "data": {
    "uploadedImages": [
      {
        "imageId": 501,
        "imageUrl": "https://res.cloudinary.com/.../gallery_0.jpg",
        "publicId": "products/123/gallery/gallery_0",
        "displayOrder": 0
      }
    ],
    "message": "3 gallery images uploaded successfully"
  }
}
```

#### Get Gallery Images
```http
GET /products/{productId}/images/gallery
Authorization: Bearer {token}
```

**Response 200**:
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "imageId": 501,
      "imageUrl": "https://res.cloudinary.com/.../gallery_0.jpg",
      "publicId": "products/123/gallery/gallery_0",
      "displayOrder": 0
    }
  ]
}
```

#### Delete Gallery Image
```http
DELETE /products/{productId}/images/gallery/{imageId}
Authorization: Bearer {token}
```

#### Reorder Gallery Images
```http
PUT /products/{productId}/images/gallery/reorder
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "imageOrders": [
    {"imageId": 501, "displayOrder": 2},
    {"imageId": 502, "displayOrder": 0},
    {"imageId": 503, "displayOrder": 1}
  ]
}
```

---

### 3. Variant Images Operations

#### Upload Variant Image
```http
POST /products/{productId}/images/variant
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- file: (binary) Image file
- attributeValue: (text) e.g., "Red", "Blue", "Large"
```

**Prerequisites**:
- Product MUST have a primary attribute set in `product_primary_attributes` table
- Attribute value MUST exist in `product_variant_values` table

**Response 200**:
```json
{
  "status": "SUCCESS",
  "data": {
    "id": 601,
    "attributeValue": "Red",
    "imageUrl": "https://res.cloudinary.com/.../variant_red.jpg",
    "publicId": "products/123/variants/variant_red"
  },
  "message": "Variant image uploaded for Red"
}
```

#### Delete Variant Image
```http
DELETE /products/{productId}/images/variant?attributeValue={value}
Authorization: Bearer {token}
```

---

### 4. Get All Product Images (Public)

#### Get All Images
```http
GET /products/{productId}/images
(No authentication required)
```

**Response 200**:
```json
{
  "status": "SUCCESS",
  "data": {
    "mainImage": "https://res.cloudinary.com/.../main.jpg",
    "galleryImages": [
      {
        "imageId": 501,
        "imageUrl": "https://res.cloudinary.com/.../gallery_0.jpg",
        "displayOrder": 0
      }
    ],
    "primaryAttribute": {
      "id": 10,
      "name": "Color",
      "values": ["Red", "Blue", "Green"]
    },
    "variantImages": {
      "Red": {
        "id": 601,
        "attributeValue": "Red",
        "imageUrl": "https://res.cloudinary.com/.../variant_red.jpg",
        "publicId": "products/123/variants/variant_red"
      },
      "Blue": {
        "id": 602,
        "attributeValue": "Blue",
        "imageUrl": "https://res.cloudinary.com/.../variant_blue.jpg",
        "publicId": "products/123/variants/variant_blue"
      }
    }
  }
}
```

---

## Testing Workflow

### Step 1: Login as Seller
```http
POST /auth/login
Content-Type: application/json

{
  "email": "seller@example.com",
  "password": "password123"
}
```

Save the JWT token from response.

### Step 2: Create/Select Product
```http
POST /products
Authorization: Bearer {token}

{
  "name": "Test Product",
  "description": "Product for image testing",
  "basePrice": 100.00,
  "categoryId": 1
}
```

Save the `productId` from response.

### Step 3: Upload Main Image
```bash
curl -X POST \
  http://localhost:8080/api/products/{productId}/images/main \
  -H 'Authorization: Bearer {token}' \
  -F 'file=@/path/to/main_image.jpg'
```

### Step 4: Upload Gallery Images
```bash
curl -X POST \
  http://localhost:8080/api/products/{productId}/images/gallery \
  -H 'Authorization: Bearer {token}' \
  -F 'files=@/path/to/gallery1.jpg' \
  -F 'files=@/path/to/gallery2.jpg' \
  -F 'files=@/path/to/gallery3.jpg'
```

### Step 5: Set Primary Attribute (Database)
```sql
-- Check if product has primary attribute
SELECT * FROM product_primary_attributes WHERE product_id = {productId};

-- If not exists, insert one
INSERT INTO product_primary_attributes (product_id, attribute_id)
VALUES ({productId}, {attributeId});

-- Get attribute values
SELECT DISTINCT pvv.value 
FROM product_variant_values pvv
WHERE pvv.product_id = {productId} AND pvv.attribute_id = {attributeId};
```

### Step 6: Upload Variant Images
```bash
# Upload for "Red"
curl -X POST \
  http://localhost:8080/api/products/{productId}/images/variant \
  -H 'Authorization: Bearer {token}' \
  -F 'file=@/path/to/red_variant.jpg' \
  -F 'attributeValue=Red'

# Upload for "Blue"
curl -X POST \
  http://localhost:8080/api/products/{productId}/images/variant \
  -H 'Authorization: Bearer {token}' \
  -F 'file=@/path/to/blue_variant.jpg' \
  -F 'attributeValue=Blue'
```

### Step 7: Get All Images (Public)
```bash
curl -X GET http://localhost:8080/api/products/{productId}/images
```

### Step 8: Reorder Gallery Images
```bash
curl -X PUT \
  http://localhost:8080/api/products/{productId}/images/gallery/reorder \
  -H 'Authorization: Bearer {token}' \
  -H 'Content-Type: application/json' \
  -d '{
    "imageOrders": [
      {"imageId": 501, "displayOrder": 2},
      {"imageId": 502, "displayOrder": 0},
      {"imageId": 503, "displayOrder": 1}
    ]
  }'
```

### Step 9: Delete Images
```bash
# Delete gallery image
curl -X DELETE \
  http://localhost:8080/api/products/{productId}/images/gallery/{imageId} \
  -H 'Authorization: Bearer {token}'

# Delete variant image
curl -X DELETE \
  'http://localhost:8080/api/products/{productId}/images/variant?attributeValue=Red' \
  -H 'Authorization: Bearer {token}'

# Delete main image
curl -X DELETE \
  http://localhost:8080/api/products/{productId}/images/main \
  -H 'Authorization: Bearer {token}'
```

---

## Error Scenarios

### 1. Invalid Format (400)
Upload a PDF or TXT file:
```bash
curl -X POST \
  http://localhost:8080/api/products/{productId}/images/main \
  -H 'Authorization: Bearer {token}' \
  -F 'file=@/path/to/document.pdf'
```

Expected response:
```json
{
  "status": "ERROR",
  "error": "InvalidImageFormatException",
  "message": "Invalid format. Only JPEG, PNG, WEBP allowed",
  "timestamp": "2025-11-22T..."
}
```

### 2. File Too Large (400)
Upload image > 5MB.

Expected response:
```json
{
  "status": "ERROR",
  "error": "ImageSizeLimitExceededException",
  "message": "File size 6291456 bytes exceeds 5MB limit",
  "timestamp": "2025-11-22T..."
}
```

### 3. No Primary Attribute (400)
Try uploading variant image for product without primary attribute.

Expected response:
```json
{
  "status": "ERROR",
  "error": "VariantImageNotAllowedException",
  "message": "Product must have a primary attribute before uploading variant images",
  "timestamp": "2025-11-22T..."
}
```

### 4. Invalid Attribute Value (400)
Upload variant image with non-existent attribute value.

Expected response:
```json
{
  "status": "ERROR",
  "error": "InvalidColorValueException",
  "message": "Attribute value 'InvalidColorXYZ' not found in product variants",
  "timestamp": "2025-11-22T..."
}
```

### 5. Gallery Limit Exceeded (400)
Try uploading more than 10 gallery images total.

Expected response:
```json
{
  "status": "ERROR",
  "error": "GalleryImageLimitExceededException",
  "message": "Maximum 10 gallery images allowed. Current: 8, Attempting to add: 3",
  "timestamp": "2025-11-22T..."
}
```

### 6. Unauthorized (401)
Make request without JWT token.

Expected response: 401 Unauthorized

### 7. Forbidden (403)
Try uploading to another seller's product.

Expected response:
```json
{
  "status": "ERROR",
  "error": "ForbiddenException",
  "message": "You are not authorized to modify this product",
  "timestamp": "2025-11-22T..."
}
```

### 8. Not Found (404)
Use non-existent productId or imageId.

Expected response:
```json
{
  "status": "ERROR",
  "error": "ProductNotFoundException",
  "message": "Product with ID 999 not found",
  "timestamp": "2025-11-22T..."
}
```

---

## Database Verification

### Check Main Image
```sql
SELECT id, name, main_image, main_image_public_id 
FROM products 
WHERE id = {productId};
```

### Check Gallery Images
```sql
SELECT id, product_id, image_type, image_url, public_id, display_order
FROM product_images
WHERE product_id = {productId} AND image_type = 'GALLERY'
ORDER BY display_order;
```

### Check Variant Images
```sql
SELECT pi.id, pi.product_id, pi.variant_attribute_value, pi.image_url, pi.public_id,
       ppa.attribute_id, pa.name as attribute_name
FROM product_images pi
JOIN product_primary_attributes ppa ON pi.product_id = ppa.product_id
JOIN product_attributes pa ON ppa.attribute_id = pa.id
WHERE pi.product_id = {productId} AND pi.image_type = 'VARIANT';
```

### Check Primary Attribute
```sql
SELECT ppa.product_id, ppa.attribute_id, pa.name as attribute_name,
       GROUP_CONCAT(DISTINCT pvv.value) as possible_values
FROM product_primary_attributes ppa
JOIN product_attributes pa ON ppa.attribute_id = pa.id
LEFT JOIN product_variant_values pvv ON pvv.product_id = ppa.product_id 
                                     AND pvv.attribute_id = ppa.attribute_id
WHERE ppa.product_id = {productId}
GROUP BY ppa.product_id, ppa.attribute_id, pa.name;
```

---

## Cloudinary Verification

Login to Cloudinary dashboard:
- https://cloudinary.com/console

Navigate to Media Library and check folders:
- `products/{productId}/main/` - Main image
- `products/{productId}/gallery/` - Gallery images (gallery_0, gallery_1, ...)
- `products/{productId}/variants/` - Variant images (variant_red, variant_blue, ...)

---

## Success Criteria

✅ **Main Image**: Uploaded successfully, old image deleted from Cloudinary  
✅ **Gallery Images**: Multiple uploads, correct display_order, max 10 enforced  
✅ **Variant Images**: Uploaded per attribute value, primary attribute validated  
✅ **Reorder**: Gallery images reordered correctly  
✅ **Delete**: Images deleted from both DB and Cloudinary  
✅ **Get All**: Public endpoint returns complete image structure  
✅ **Validation**: All format/size/dimension validations work  
✅ **Authorization**: JWT and ownership checks enforced  
✅ **Error Handling**: Proper exception responses with GlobalExceptionHandler  

---

## Phase 7 Tasks Completed

- [X] T071 - Created comprehensive API testing guide
- [X] T074 - Verified all endpoints return consistent error responses
- [ ] T068 - Add caching for variant image lookup (optional)
- [ ] T069 - Implement parallel bulk upload (optional)
- [ ] T070 - Add structured logging (optional)
- [ ] T072 - Add rate limiting (optional)
- [ ] T073 - Document Cloudinary folder structure (optional)
- [ ] T075 - Add database indexes (optional)

---

## Notes

1. **Image Format**: Use Apache Tika for magic byte validation (not just file extension)
2. **Optimistic Locking**: Product and ProductImage entities have `@Version` field
3. **Transaction Rollback**: On failure, uploaded Cloudinary images are deleted
4. **Primary Attribute**: Required for variant images, typically "Color"
5. **Display Order**: Gallery images auto-numbered 0-9, gaps preserved on reorder
6. **Cloudinary Cleanup**: Delete operations remove from both DB and cloud storage

---

## Support

For issues or questions:
- GitHub Issues: https://github.com/ThanhThat12/PBL6_E-Commerce
- Email: support@pbl6-ecommerce.com
