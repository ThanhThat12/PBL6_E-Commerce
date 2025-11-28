# Đặc Tả Yêu Cầu Xử Lý Ảnh với Cloudinary - Spring Boot

## 1. Tổng Quan Hệ Thống

### 1.1 Mục Đích
Xây dựng service xử lý upload, quản lý và xóa ảnh sử dụng Cloudinary cho hệ thống E-commerce, hỗ trợ đa loại ảnh (sản phẩm, avatar, shop logo/banner) với khả năng tối ưu hóa và bảo mật.

### 1.2 Phạm Vi Áp Dụng
- **Product Images**: Ảnh sản phẩm chính và gallery (theo variant)
- **User Avatar**: Ảnh đại diện người dùng
- **Shop Images**: Logo và banner của shop
- **Review Images**: Ảnh trong đánh giá sản phẩm

## 2. Cấu Trúc Dữ Liệu

### 2.1 Thông Tin Lưu Trữ Database
Các bảng cần lưu trữ thông tin Cloudinary:

**products**
- `main_image`: URL ảnh chính từ Cloudinary
- `main_image_public_id`: Public ID để xóa ảnh

**product_images**
- `image_url`: URL ảnh từ Cloudinary
- `public_id`: Public ID để xóa
- `display_order`: Thứ tự hiển thị
- `color`: Màu liên quan đến ảnh (nếu có)
- `variant_id`: NULL = ảnh chung, NOT NULL = ảnh theo variant

**users**
- `avatar_url`: URL avatar
- `avatar_public_id`: Public ID avatar

**shops**
- `logo_url`: URL logo shop
- `logo_public_id`: Public ID logo
- `banner_url`: URL banner shop
- `banner_public_id`: Public ID banner

**product_reviews**
- `images`: JSON array chứa URLs ảnh review
- `images_count`: Cache số lượng ảnh

## 3. Yêu Cầu Chức Năng

### 3.1 Upload Ảnh

#### 3.1.1 Upload Single Image
**Input:**
- File ảnh (MultipartFile)
- Loại ảnh (PRODUCT, AVATAR, SHOP_LOGO, SHOP_BANNER, REVIEW)
- Metadata tùy chọn (product_id, user_id, shop_id)

**Process:**
- Validate định dạng file (JPG, PNG, WEBP)
- Validate kích thước file (max 5MB)
- Validate dimensions (tùy loại ảnh)
- Transform ảnh theo preset
- Upload lên Cloudinary với folder structure phù hợp
- Lưu URL và public_id vào database

**Output:**
- URL ảnh đã upload
- Public ID
- Thông tin metadata (width, height, format, size)

#### 3.1.2 Upload Multiple Images
**Use Case:** Product gallery, review images

**Input:**
- List of MultipartFile
- Context information (product_id, variant_id nếu có)

**Process:**
- Validate từng file
- Upload song song với batch processing
- Lưu thứ tự upload vào display_order
- Transaction để đảm bảo tính toàn vẹn

**Output:**
- List URLs và public_ids
- Thông tin upload success/failed cho từng file

### 3.2 Quản Lý Ảnh

#### 3.2.1 Update/Replace Image
**Scenario:**
- Thay ảnh avatar người dùng
- Thay ảnh chính sản phẩm
- Thay logo/banner shop

**Process:**
- Upload ảnh mới
- Nếu thành công, xóa ảnh cũ từ Cloudinary
- Update database với URL và public_id mới
- Rollback nếu có lỗi

#### 3.2.2 Reorder Product Images
**Input:**
- Product ID hoặc Variant ID
- List image IDs với display_order mới

**Process:**
- Validate quyền sở hữu
- Update display_order trong database
- Return danh sách ảnh đã sắp xếp

### 3.3 Xóa Ảnh

#### 3.3.1 Delete Single Image
**Process:**
- Xóa từ Cloudinary bằng public_id
- Xóa record trong database
- Handle gracefully nếu ảnh không tồn tại trên Cloudinary

#### 3.3.2 Delete Cascade
**Scenario:** Khi xóa product, user, shop

**Process:**
- Query tất cả public_ids liên quan
- Batch delete từ Cloudinary
- Database cascade delete tự động
- Log errors nhưng không block transaction

#### 3.3.3 Cleanup Orphan Images
**Background Job:**
- Định kỳ scan database tìm ảnh không còn reference
- Xóa từ Cloudinary
- Xóa records trong database

## 4. Yêu Cầu Kỹ Thuật

### 4.1 Cloudinary Configuration

**Environment Variables:**
- `CLOUDINARY_CLOUD_NAME`: Tên cloud
- `CLOUDINARY_API_KEY`: API key
- `CLOUDINARY_API_SECRET`: API secret
- `CLOUDINARY_SECURE`: true (luôn dùng HTTPS)

**Folder Structure:**
```
ecommerce/
├── products/
│   ├── main/
│   └── gallery/
├── avatars/
├── shops/
│   ├── logos/
│   └── banners/
└── reviews/
```

### 4.2 Image Transformation Presets

**Product Images:**
- Thumbnail: 200x200, crop fill
- Medium: 600x600, crop fit
- Large: 1200x1200, crop limit
- Format: auto (WebP khi browser hỗ trợ)
- Quality: auto:good

**Avatar:**
- Size: 300x300, crop fill, gravity face
- Format: auto
- Quality: auto:best

**Shop Logo:**
- Size: 400x400, crop fit
- Background: transparent support
- Format: PNG/auto

**Shop Banner:**
- Size: 1200x400, crop fill
- Format: auto
- Quality: auto:good

### 4.3 Security & Validation

**File Validation:**
- Allowed extensions: jpg, jpeg, png, webp
- Max file size: 5MB (configurable)
- Min dimensions: 200x200 (product), 100x100 (avatar)
- Max dimensions: 4096x4096
- Content-type verification

**Access Control:**
- User chỉ upload avatar của chính mình
- Seller chỉ upload ảnh cho products/shops của mình
- Admin có full access

**Rate Limiting:**
- Max 10 uploads per minute per user
- Max 50 uploads per hour per user

### 4.4 Performance Optimization

**Caching:**
- Cache Cloudinary URLs với CDN headers
- Cache transformed image URLs
- Browser cache: 1 year cho immutable images

**Lazy Loading:**
- Return progressive JPEGs
- Support responsive images với srcset
- Implement blur placeholder

**Compression:**
- Auto compression với Cloudinary
- Lossy compression cho photos
- Lossless cho logos/graphics

## 5. Error Handling

### 5.1 Upload Errors
- **Invalid file format**: Return 400 với message cụ thể
- **File too large**: Return 413 với size limit
- **Cloudinary API error**: Retry 3 lần, log và return 500
- **Database save failed**: Rollback Cloudinary upload

### 5.2 Delete Errors
- **Image not found**: Log warning, continue
- **Cloudinary API error**: Retry, queue cho background job
- **Database constraint**: Handle foreign key errors

### 5.3 Fallback Strategy
- Default placeholder images cho missing images
- Graceful degradation khi Cloudinary down
- Queue system cho failed operations

## 6. API Endpoints Design

### 6.1 Product Images
- `POST /api/products/{id}/images` - Upload product images
- `DELETE /api/products/{id}/images/{imageId}` - Delete image
- `PUT /api/products/{id}/images/reorder` - Reorder images
- `PUT /api/products/{id}/main-image` - Update main image

### 6.2 User Avatar
- `POST /api/users/avatar` - Upload avatar
- `DELETE /api/users/avatar` - Delete avatar

### 6.3 Shop Images
- `POST /api/shops/{id}/logo` - Upload logo
- `POST /api/shops/{id}/banner` - Upload banner
- `DELETE /api/shops/{id}/logo` - Delete logo
- `DELETE /api/shops/{id}/banner` - Delete banner

### 6.4 Review Images
- `POST /api/reviews/{id}/images` - Upload review images
- `DELETE /api/reviews/{id}/images/{index}` - Delete review image

## 7. Logging & Monitoring

**Metrics to Track:**
- Upload success/failure rate
- Average upload time
- Storage usage
- Bandwidth usage
- Failed deletions queue size

**Logging Requirements:**
- Log all upload attempts với user_id, file info
- Log Cloudinary API errors với full context
- Audit log cho delete operations
- Performance logs cho slow uploads

## 8. Background Jobs

### 8.1 Cleanup Job
- Frequency: Daily at 2 AM
- Task: Xóa orphan images
- Report: Send summary email to admin

### 8.2 Retry Failed Operations
- Frequency: Every 30 minutes
- Task: Retry failed deletes từ queue
- Max retries: 5 lần

### 8.3 Storage Audit
- Frequency: Weekly
- Task: Compare database vs Cloudinary storage
- Report: Missing images, unused images

## 9. Testing Requirements

**Unit Tests:**
- Upload validation logic
- URL generation
- Public ID extraction

**Integration Tests:**
- Upload flow end-to-end
- Delete với cascade
- Error handling scenarios

**Load Tests:**
- Concurrent uploads
- Large file handling
- Batch operations performance

## 10. Migration Strategy

**Existing Images:**
- Script để migrate ảnh hiện tại lên Cloudinary
- Update database với URLs và public_ids mới
- Backup URLs cũ trước khi migrate
- Phased rollout theo từng loại ảnh