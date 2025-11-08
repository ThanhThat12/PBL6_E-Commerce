# Variant Image Implementation - Quick Reference

## 📍 Database Schema (No Changes Required)

Table `product_images` now supports variant-specific images:

```sql
-- Existing columns used:
- id (PK)
- product_id (FK) → products
- image_url
- display_order (0 = main image)
- alt_text (SEO)
- is_active (visibility flag)
- created_at (timestamp)

-- NEW usage:
- variant_id (FK) → product_variants (can be NULL for product-level images)
```

## 🎯 Core Responsibilities

| Component | Responsibility |
|-----------|-----------------|
| **VariantImage** | Maps product_images table with variant-specific fields |
| **VariantImageRepository** | Database queries for variant images |
| **VariantImageService** | Business logic, Cloudinary integration, gallery management |
| **ProductController** | REST API endpoints for buyers/sellers |
| **CloudinaryService** | Image upload/delete, file validation |

## 🔗 Request Flow

```
Client Request
    ↓
ProductController (validates input)
    ↓
VariantImageService (business logic)
    ↓
CloudinaryService (upload/delete)
    ↓
VariantImageRepository (database ops)
    ↓
product_images table
```

## 💡 API Usage Patterns

### Upload New Variant Images
```bash
# Image 1 (main)
POST /api/seller/products/1/variants/5/images
  image: red_main.jpg
  displayOrder: 0
  altText: Red product main view

# Image 2 (gallery)
POST /api/seller/products/1/variants/5/images
  image: red_gallery1.jpg
  displayOrder: 1
  
# Image 3 (gallery)
POST /api/seller/products/1/variants/5/images
  image: red_gallery2.jpg
  displayOrder: 2
```

### View Variant Images
```bash
# Get all images (sorted by displayOrder: 0, 1, 2, ...)
GET /api/seller/products/1/variants/5/images

# Get only active images
GET /api/seller/products/1/variants/5/images/active

# Get main image (displayOrder=0)
GET /api/seller/products/1/variants/5/images/main

# Get images by color
GET /api/products/1/images/color/Red
```

### Reorder Gallery
```bash
# Change order: image_id 2 becomes main, then 1, then 3
POST /api/seller/products/1/variants/5/images/reorder
  body: {"imageIds": [2, 1, 3]}
  
# Result: displayOrder updated to 0, 1, 2
```

### Update/Delete
```bash
# Update metadata
PUT /api/seller/products/1/variants/5/images/1
  displayOrder: 2  (move to position 2)
  altText: Updated text

# Delete single image
DELETE /api/seller/products/1/variants/5/images/1

# Delete all images for variant
DELETE /api/seller/products/1/variants/5/images
```

## 🔐 Security Rules

- ✅ All POST/PUT/DELETE require `@PreAuthorize("hasRole('SELLER')")`
- ✅ GET endpoints public (images are public product info)
- ✅ File validation: size ≤10MB, type image/*
- ✅ Variant must exist to upload images

## 🌩️ Cloudinary Integration

| Operation | Method | Result |
|-----------|--------|--------|
| Upload | `uploadProductImage(file, productId)` | Resized 800x800, quality=auto:good |
| Delete | `deleteImage(publicId)` | Silent fail if error (graceful) |
| Validate | `validateImageFile(file)` | Throws exception if invalid |

## 📊 Data Flow Example

**User uploads image:**
```
1. Client: POST /api/seller/products/1/variants/5/images + file
2. ProductController: Validates @PreAuthorize("hasRole('SELLER')")
3. VariantImageService.uploadVariantImage():
   - Get variant from DB
   - Call CloudinaryService.uploadProductImage() → get URL
   - Create VariantImage object
   - Save to product_images table
   - Return VariantImageDTO
4. Client: Receives { id: 1, imageUrl: "...", displayOrder: 0, ... }
```

**Buyer views images:**
```
1. Client: GET /api/seller/products/1/variants/5/images
2. VariantImageRepository: SELECT * WHERE variant_id=5 ORDER BY display_order
3. VariantImageService: Convert to DTOs
4. Client: Receives [{ image1_data }, { image2_data }, ...]
```

## 🎨 Variant Color/Attribute Relationship

```
ProductVariant (e.g., Size=M, Color=Red)
  ↓
ProductVariantValues
  ├─ Size=M
  └─ Color=Red
  
VariantImage (variant-specific images)
  ├─ image_url: "red_front.jpg"
  ├─ color: "Red" (optional reference)
  └─ displayOrder: 0

Query: Get all Red images
  → findByProductIdAndColorOrderByDisplayOrder(productId, "Red")
```

## 🧠 Service Methods Cheat Sheet

```java
// Upload
VariantImageDTO uploadVariantImage(variantId, file, displayOrder, altText)

// Query
List<VariantImageDTO> getVariantImages(variantId)
List<VariantImageDTO> getActiveVariantImages(variantId)
VariantImageDTO getMainImage(variantId)
List<VariantImageDTO> getImagesByColor(productId, color)

// Update
VariantImageDTO updateVariantImage(variantId, imageId, displayOrder, altText)

// Delete
void deleteVariantImage(variantId, imageId)
List<VariantImageDTO> reorderVariantImages(variantId, imageIds)
void deleteAllVariantImages(variantId)
```

## 🚨 Error Scenarios

| Scenario | Response | Status |
|----------|----------|--------|
| Invalid variant | Variant not found | 400 |
| Image not found | Image not found | 404 |
| File too large | Size > 10MB | 400 |
| Wrong file type | Not image/* | 400 |
| Cloudinary error | Upload failed | 500 |
| Not authorized | Not SELLER | 403 |

## 🧪 Quick Test Commands

```bash
# 1. Upload image
curl -X POST http://localhost:8080/api/seller/products/1/variants/1/images \
  -H "Authorization: Bearer TOKEN" \
  -F "image=@test.jpg" \
  -F "displayOrder=0"

# 2. Get images
curl http://localhost:8080/api/seller/products/1/variants/1/images

# 3. Get main image
curl http://localhost:8080/api/seller/products/1/variants/1/images/main

# 4. Reorder
curl -X POST http://localhost:8080/api/seller/products/1/variants/1/images/reorder \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"imageIds":[2,1,3]}'

# 5. Delete
curl -X DELETE http://localhost:8080/api/seller/products/1/variants/1/images/1 \
  -H "Authorization: Bearer TOKEN"
```

## 📝 Configuration Required

In `application.properties`:
```properties
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
cloudinary.secure=true
```

## ✨ Features Enabled

✅ Upload images per variant  
✅ Gallery ordering with displayOrder  
✅ Main image designation (displayOrder=0)  
✅ Active/inactive status  
✅ Bulk reordering  
✅ Graceful Cloudinary error handling  
✅ Color-based image query  
✅ Seller-only write operations  
✅ Automatic Cloudinary cleanup  
✅ File validation (size, type)

## 🔄 Relationship Diagram

```
Product (1) ──→ (M) ProductVariant
                        ↓
                  VariantImage (variant-specific gallery)
                        ↓
                  Cloudinary (cloud storage)
```

## 📌 Important Notes

1. **displayOrder=0 is main image** - Essential for gallery
2. **Null variant_id** - Can be used for product-level images (future)
3. **Graceful failure** - Cloudinary errors don't block deletion
4. **Lazy loading** - Use ProductVariant with @OneToMany
5. **Cascade delete** - Deleting variant deletes all images

---

**Status**: ✅ Complete & Ready for Testing  
**Last Updated**: November 7, 2025
