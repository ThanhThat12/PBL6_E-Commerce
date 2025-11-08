# Variant Image Management - Implementation Guide

## Overview
Per-variant image gallery system cho phép mỗi product variant (size, color, etc.) có những hình ảnh riêng biệt, được lưu trữ trong Cloudinary.

## Database Structure
Sử dụng bảng `product_images` hiện tại với các cột mở rộng:

| Column | Type | Purpose |
|--------|------|---------|
| `id` | bigint | Primary key |
| `product_id` | bigint (FK) | Reference to product |
| **`variant_id`** | bigint (FK) | **NEW: Reference to product variant** |
| `image_url` | varchar(500) | Cloudinary secure URL |
| `color` | varchar(50) | Color reference (optional) |
| **`display_order`** | int | **NEW: Gallery ordering (0=main image)** |
| **`alt_text`** | varchar(255) | **NEW: SEO alt text** |
| **`is_active`** | bit | **NEW: Active/inactive flag** |
| **`created_at`** | datetime(6) | **NEW: Creation timestamp** |

## Architecture

### Entities
- **VariantImage**: JPA entity mapping to `product_images` table with variant-specific fields

### DTOs  
- **VariantImageDTO** (dto.seller package): Transfer object used by API responses

### Repositories
- **VariantImageRepository**: Provides query methods:
  - `findByVariantIdOrderByDisplayOrder()` - Get all images for variant sorted by order
  - `findByVariantIdAndDisplayOrder(variantId, 0)` - Get main image
  - `findByVariantIdAndIsActiveTrue()` - Get only active images
  - `findByProductIdAndColorOrderByDisplayOrder()` - Get images by product color

### Services
- **VariantImageService**: Business logic for image management
  - Cloudinary integration for upload/delete
  - Display order management for gallery
  - Active/inactive status control

### Controllers
- **ProductController**: REST API endpoints for variant image operations

## REST API Endpoints

### Upload Image
```http
POST /api/seller/products/{productId}/variants/{variantId}/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

Parameters:
- image (file, required): Image file (max 10MB, image/* only)
- displayOrder (int, optional): Display position (default: 0)
- altText (string, optional): SEO alt text

Response:
{
  "code": 200,
  "message": "Image uploaded successfully",
  "data": {
    "id": 1,
    "variantId": 5,
    "imageUrl": "https://res.cloudinary.com/...",
    "displayOrder": 0,
    "altText": "Product variant image",
    "isActive": true,
    "createdAt": "2025-11-07T10:30:00"
  }
}
```

### Get All Variant Images
```http
GET /api/seller/products/{productId}/variants/{variantId}/images

Response:
{
  "code": 200,
  "message": "Retrieved successfully",
  "data": [
    { image1_data },
    { image2_data },
    { image3_data }
  ]
}
```

### Get Main Image (displayOrder=0)
```http
GET /api/seller/products/{productId}/variants/{variantId}/images/main

Response:
{
  "code": 200,
  "message": "Retrieved successfully",
  "data": { main_image_data }
}
```

### Get Active Images Only
```http
GET /api/seller/products/{productId}/variants/{variantId}/images/active
```

### Get Images by Color
```http
GET /api/products/{productId}/images/color/{color}

Examples:
- GET /api/products/1/images/color/Red
- GET /api/products/1/images/color/Blue
```

### Update Image Metadata
```http
PUT /api/seller/products/{productId}/variants/{variantId}/images/{imageId}
Authorization: Bearer {token}

Parameters:
- displayOrder (int, optional): New display order
- altText (string, optional): New alt text

Response: Updated VariantImageDTO
```

### Delete Image
```http
DELETE /api/seller/products/{productId}/variants/{variantId}/images/{imageId}
Authorization: Bearer {token}

Behavior:
1. Deletes from Cloudinary (attempts gracefully)
2. Deletes from database
3. Returns 200 on success even if Cloudinary delete fails
```

### Reorder Images
```http
POST /api/seller/products/{productId}/variants/{variantId}/images/reorder
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "imageIds": [3, 1, 2]  // New display order
}

Response: List of reordered VariantImageDTOs with updated displayOrder values
```

### Delete All Variant Images
```http
DELETE /api/seller/products/{productId}/variants/{variantId}/images
Authorization: Bearer {token}

Behavior:
1. Deletes all images from Cloudinary
2. Deletes all records from database
3. Returns 200 on success
```

## Implementation Details

### Cloudinary Integration
- Uploads: `uploadProductImage(file, productId)` - 800x800px with fit crop
- Quality: `quality=auto:good, fetch_format=auto` - WebP with fallback
- Organization: `products/` folder in Cloudinary
- Deletion: Graceful failure handling (logs but doesn't fail)

### Display Order Management
- `displayOrder = 0`: Main image for variant
- `displayOrder > 0`: Gallery images in sequence
- Reordering: Updates displayOrder field for all images

### Validation
- File size: Max 10MB
- Content type: Must be image/*
- Variant existence: Must exist before upload
- Permission: Seller-only (@PreAuthorize)

### Error Handling
- Invalid variants: `400 BAD_REQUEST`
- Missing images: `404 NOT_FOUND`
- Upload failures: `500 INTERNAL_SERVER_ERROR`
- Cloudinary failures: Logged but database operations continue

## Example Usage Flow

### 1. Seller uploads first image (main image)
```bash
curl -X POST \
  "http://localhost:8080/api/seller/products/1/variants/5/images" \
  -H "Authorization: Bearer {token}" \
  -F "image=@product_red_front.jpg" \
  -F "displayOrder=0" \
  -F "altText=Red product front view"
```
Response: Image saved with `displayOrder=0` (main image)

### 2. Seller uploads additional images
```bash
curl -X POST \
  "http://localhost:8080/api/seller/products/1/variants/5/images" \
  -H "Authorization: Bearer {token}" \
  -F "image=@product_red_side.jpg" \
  -F "displayOrder=1" \
  -F "altText=Red product side view"

curl -X POST \
  "http://localhost:8080/api/seller/products/1/variants/5/images" \
  -H "Authorization: Bearer {token}" \
  -F "image=@product_red_back.jpg" \
  -F "displayOrder=2" \
  -F "altText=Red product back view"
```

### 3. Buyer views variant images (sorted by displayOrder)
```bash
curl "http://localhost:8080/api/seller/products/1/variants/5/images"
```
Response: 3 images ordered 0, 1, 2

### 4. Seller reorders gallery
```bash
curl -X POST \
  "http://localhost:8080/api/seller/products/1/variants/5/images/reorder" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"imageIds": [2, 1, 3]}'
```
Result: Back view becomes main, side becomes 2nd, front becomes 3rd

## Query Examples

### Get all images for variant with color=Red
```java
List<VariantImage> images = variantImageRepository
  .findByProductIdAndColorOrderByDisplayOrder(productId, "Red");
```

### Get main image
```java
Optional<VariantImage> mainImage = variantImageRepository
  .findByVariantIdAndDisplayOrder(variantId, 0);
```

### Get active images only
```java
List<VariantImage> activeImages = variantImageRepository
  .findByVariantIdAndIsActiveTrue(variantId);
```

## Database Migration (Manual - No Script)
Since we're using existing `product_images` table, ensure these columns exist:
- `variant_id` (nullable bigint FK to product_variants.id)
- `display_order` (int DEFAULT 0)
- `alt_text` (varchar(255) nullable)
- `is_active` (bit DEFAULT 1)
- `created_at` (datetime(6) DEFAULT CURRENT_TIMESTAMP(6))

If columns don't exist, add them with:
```sql
ALTER TABLE product_images
ADD COLUMN variant_id BIGINT,
ADD COLUMN display_order INT DEFAULT 0,
ADD COLUMN alt_text VARCHAR(255),
ADD COLUMN is_active BIT DEFAULT 1,
ADD COLUMN created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
ADD FOREIGN KEY (variant_id) REFERENCES product_variants(id);
```

## Security Notes
- All upload endpoints require `@PreAuthorize("hasRole('SELLER')")`
- Sellers can only upload/modify images for their own products
- Product ownership verification should be added (optional enhancement)
- File size and type validation prevents abuse
- Cloudinary API key never exposed in frontend

## Performance Optimization (Optional)
- Add `@Cacheable` on `getMainImage()` method
- Index `variant_id` and `display_order` columns
- Lazy-load images in ProductVariant entity
- CDN delivery via Cloudinary's global network

## Frontend Integration Points
- Product edit form: File input for uploading images
- Variant selector: Show gallery for selected variant
- Image carousel: Display images sorted by displayOrder
- Drag-and-drop: Reorder gallery using reorder endpoint
- Thumbnail strips: Show all images with main image highlighted

## Troubleshooting

### Images not appearing
- Check Cloudinary credentials in application.properties
- Verify secure_url returned from upload (not url)
- Check database for orphaned records (variant_id=null)

### Display order not updating
- Ensure displayOrder parameter is provided as Integer
- Check POST body format for reorder endpoint: `{"imageIds": [...]}`

### Cloudinary deletion fails silently
- This is intentional behavior (graceful degradation)
- Check Cloudinary API logs
- Database records are deleted regardless

### Images visible but not associated to variant
- Check product_images.variant_id is populated
- Verify ProductVariant entity relationships
