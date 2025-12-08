# Product Review System - Implementation Guide

## 📋 Tổng quan

Hệ thống đánh giá sản phẩm cho phép:
- **BUYER**: Đánh giá sản phẩm đã mua (đơn hàng COMPLETED), upload ảnh
- **SELLER**: Đánh giá sản phẩm từ shop **khác** (không phải shop của mình), phản hồi đánh giá khách
- **PUBLIC**: Xem đánh giá sản phẩm

### Tính năng Like/Report (Shopee-style):
- **Like**: Cả BUYER và SELLER có thể like đánh giá hữu ích
- **Report**: Báo cáo đánh giá vi phạm (spam, giả mạo, xúc phạm...)

---

## 🔐 Phân quyền (Role-based)

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/products/{id}/reviews` | GET | Public | Xem đánh giá sản phẩm |
| `/api/products/{id}/reviews` | POST | **BUYER/SELLER** | Tạo đánh giá |
| `/api/products/{id}/rating-summary` | GET | Public | Thống kê rating |
| `/api/reviews/images/upload` | POST | Authenticated | Upload ảnh tạm |
| `/api/reviews/{id}` | PUT | **BUYER/SELLER** | Sửa đánh giá (7 ngày) |
| `/api/reviews/{id}` | DELETE | Owner/ADMIN | Xóa đánh giá |
| `/api/reviews/{id}/reply` | POST | **SELLER** | Phản hồi đánh giá |
| `/api/reviews/{id}/like` | POST | **BUYER/SELLER** | Like/unlike đánh giá |
| `/api/reviews/{id}/like` | GET | Public | Xem trạng thái like |
| `/api/reviews/{id}/report` | POST | **BUYER/SELLER** | Báo cáo đánh giá |
| `/api/my-reviews` | GET | BUYER/SELLER | Đánh giá của tôi |
| `/api/my-shop/reviews/all` | GET | SELLER | Đánh giá của shop |
| `/api/admin/reviews/reports` | GET | **ADMIN** | Danh sách báo cáo |
| `/api/admin/reviews/reports/{id}` | PUT | **ADMIN** | Xử lý báo cáo |
| `/api/admin/reviews/reports/counts` | GET | **ADMIN** | Thống kê báo cáo |

---

## 🔄 Luồng Tạo Review

### Flow cho BUYER:
```
1. User login với role BUYER
2. User có đơn hàng status = COMPLETED chứa sản phẩm
3. (Tùy chọn) Upload ảnh → nhận URLs
4. POST /api/products/{productId}/reviews với body { rating, comment, images }
5. Backend tự tìm order COMPLETED phù hợp
```

### Flow cho SELLER (mua từ shop khác):
```
1. User login với role SELLER
2. User có đơn hàng COMPLETED chứa sản phẩm từ SHOP KHÁC
3. ⚠️ SELLER KHÔNG THỂ review sản phẩm của chính shop mình
4. POST /api/products/{productId}/reviews
```

### Request Body (CreateReviewRequestDTO):
```json
{
  "rating": 5,           // Bắt buộc: 1-5
  "comment": "...",      // Tùy chọn: max 2000 ký tự
  "images": [            // Tùy chọn: max 5 URLs từ Cloudinary
    "https://res.cloudinary.com/..."
  ]
}
```

### Validation Rules:
- ✅ User phải có role **BUYER** hoặc **SELLER**
- ✅ User phải có đơn hàng **COMPLETED** chứa sản phẩm
- ❌ **SELLER** không thể review sản phẩm của shop mình
- ✅ User chỉ review 1 lần/sản phẩm
- ✅ Rating: 1-5 (bắt buộc)
- ✅ Comment: max 2000 ký tự (tùy chọn)
- ✅ Images: max 5 URLs (tùy chọn)

---

## 👍 Like System

### Toggle Like:
```
POST /api/reviews/{reviewId}/like
Authorization: Bearer {jwt_token}
```

Response:
```json
{
  "status": 200,
  "data": {
    "reviewId": 1,
    "liked": true,
    "likesCount": 5
  },
  "message": "Đã like đánh giá"
}
```

### Get Like Status:
```
GET /api/reviews/{reviewId}/like
(Optional) Authorization: Bearer {jwt_token}
```

Response:
```json
{
  "status": 200,
  "data": {
    "reviewId": 1,
    "liked": true,      // false nếu chưa login
    "likesCount": 5
  }
}
```

---

## 🚩 Report System

### Report Types:
| Value | Label |
|-------|-------|
| `SPAM` | Spam / Quảng cáo |
| `INAPPROPRIATE` | Nội dung không phù hợp |
| `FAKE` | Đánh giá giả mạo |
| `OFFENSIVE` | Ngôn ngữ xúc phạm |
| `OTHER` | Lý do khác |

### Report Status:
| Value | Description |
|-------|-------------|
| `PENDING` | Chờ xử lý |
| `REVIEWED` | Đã xem xét |
| `RESOLVED` | Đã giải quyết |
| `DISMISSED` | Bỏ qua |

### Create Report:
```
POST /api/reviews/{reviewId}/report
Authorization: Bearer {jwt_token}
```

Body:
```json
{
  "reportType": "FAKE",
  "reason": "Đánh giá này là giả mạo vì người dùng chưa từng mua sản phẩm..."
}
```

### Admin - Get Reports:
```
GET /api/admin/reviews/reports?status=PENDING&page=0&size=10
Authorization: Bearer {admin_jwt_token}
```

### Admin - Update Report:
```
PUT /api/admin/reviews/reports/{reportId}?status=RESOLVED&adminNote=Đã xóa review vi phạm
Authorization: Bearer {admin_jwt_token}
```

---

## 🖼️ Upload Ảnh Review

### Endpoint:
```
POST /api/reviews/images/upload
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

### Request:
```
files: [file1, file2, ...] (1-5 files)
```

### Response:
```json
{
  "status": 200,
  "data": [
    {
      "url": "https://res.cloudinary.com/...",
      "publicId": "review-temp/...",
      "width": 800,
      "height": 600
    }
  ],
  "message": "Tải lên 1 ảnh thành công"
}
```

---

## 📊 Response DTO (ProductReviewDTO)

```json
{
  "id": 1,
  "rating": 5,
  "comment": "Sản phẩm tốt",
  "images": ["https://..."],
  "verifiedPurchase": true,
  "sellerResponse": "Cảm ơn bạn!",
  "sellerResponseDate": "2025-11-29 10:30:00",
  "createdAt": "2025-11-28 15:00:00",
  "updatedAt": null,
  "userId": 10,
  "userName": "buyer123",
  "userFullName": "Nguyễn Văn A",
  "userAvatarUrl": "https://...",
  "productId": 1,
  "productName": "Bóng rổ Wilson",
  "orderId": 50,
  "likesCount": 5,
  "isLikedByCurrentUser": true
}
```

---

## ⚠️ Common Errors

### 403 Forbidden
**Nguyên nhân**: User không có role BUYER/SELLER
**Giải pháp**: Login với tài khoản có role phù hợp

### 400 Bad Request - "Không tìm thấy đơn hàng hoàn tất"
**Nguyên nhân**: User chưa có đơn hàng COMPLETED chứa sản phẩm này
**Giải pháp**: Chỉ có thể review sau khi đơn hàng hoàn thành

### 400 Bad Request - "Bạn không thể đánh giá sản phẩm của chính shop mình"
**Nguyên nhân**: SELLER cố review sản phẩm của shop mình
**Giải pháp**: SELLER chỉ có thể review sản phẩm từ shop khác

### 400 Bad Request - "Bạn đã đánh giá sản phẩm này rồi"
**Nguyên nhân**: User đã review sản phẩm này
**Giải pháp**: Sử dụng PUT để sửa review (trong 7 ngày)

### 400 Bad Request - "Bạn đã báo cáo đánh giá này rồi"
**Nguyên nhân**: User đã report review này
**Giải pháp**: Chờ admin xử lý

---

## 🗃️ Database Schema

### Table: `product_review`
```sql
CREATE TABLE product_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT,
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  images TEXT,                    -- JSON array of URLs
  verified_purchase BOOLEAN DEFAULT FALSE,
  seller_response TEXT,
  seller_response_date DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME,
  FOREIGN KEY (product_id) REFERENCES product(id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (order_id) REFERENCES orders(id),
  UNIQUE (user_id, product_id)
);
```

### Table: `review_like`
```sql
CREATE TABLE review_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  review_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (review_id) REFERENCES product_review(id) ON DELETE CASCADE,
  UNIQUE (user_id, review_id)
);
```

### Table: `review_report`
```sql
CREATE TABLE review_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  reported_by_id BIGINT NOT NULL,
  report_type ENUM('SPAM', 'INAPPROPRIATE', 'FAKE', 'OFFENSIVE', 'OTHER') NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
  admin_note TEXT,
  reviewed_by_id BIGINT,
  reviewed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (review_id) REFERENCES product_review(id) ON DELETE CASCADE,
  FOREIGN KEY (reported_by_id) REFERENCES user(id),
  FOREIGN KEY (reviewed_by_id) REFERENCES user(id)
);
```

---

## 📁 Files Structure

### Backend:
```
src/main/java/com/PBL6/Ecommerce/
├── controller/
│   ├── ProductReviewController.java    # Main review CRUD + Like + Report
│   └── ReviewImageController.java      # Image upload
├── service/
│   └── ProductReviewService.java       # Business logic
├── repository/
│   ├── ProductReviewRepository.java
│   ├── ReviewLikeRepository.java
│   └── ReviewReportRepository.java
├── domain/
│   ├── ProductReview.java
│   ├── ReviewLike.java
│   ├── ReviewReport.java
│   └── dto/
│       ├── CreateReviewRequestDTO.java
│       ├── UpdateReviewRequestDTO.java
│       ├── ProductReviewDTO.java
│       ├── ProductRatingSummaryDTO.java
│       ├── SellerReplyRequestDTO.java
│       ├── ShopReviewsGroupedDTO.java
│       ├── TempImageUploadResponseDTO.java
│       ├── ReviewLikeResponseDTO.java
│       ├── ReportReviewRequestDTO.java
│       └── ReviewReportDTO.java
└── config/
    └── SecurityConfig.java             # Endpoint permissions
```

### Frontend:
```
src/
├── services/
│   └── reviewService.js                # API calls (including like/report)
├── components/
│   └── review/
│       ├── ReviewSection.jsx           # Container with like/report handlers
│       ├── ReviewCard.jsx              # Display + like/report buttons
│       ├── WriteReviewModal.jsx        # Create/edit modal
│       ├── RatingInput.jsx             # Star rating input
│       └── ImageUploader.jsx           # Image upload component
└── pages/
    ├── ProductDetailPage.jsx           # Shows ReviewSection (view-only)
    └── OrderDetailPage.jsx             # Has "Đánh giá" button for COMPLETED orders
```

---

## 📝 Summary

- ✅ **Like/Report** theo style Shopee/Lazada
- ✅ Ảnh được lưu trên **Cloudinary**
- ✅ Review từ **trang Đơn hàng** (không phải Product Detail)
- ✅ Chỉ đơn hàng **COMPLETED** mới được review
- ✅ **SELLER** có thể review sản phẩm từ **shop khác**
- ❌ **SELLER** không thể review sản phẩm của **shop mình**
- ✅ Seller có thể **phản hồi** đánh giá
- ✅ Admin có thể **xử lý báo cáo**
