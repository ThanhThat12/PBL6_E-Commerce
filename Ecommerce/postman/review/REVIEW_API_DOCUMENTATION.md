# 📝 REVIEW API DOCUMENTATION - FRONTEND INTEGRATION GUIDE

> **Backend Status**: ✅ **HOÀN CHỈNH** - Sẵn sàng tích hợp frontend  
> **Database**: ✅ Đã có triggers tự động cập nhật rating  
> **Last Updated**: November 4, 2025

---

## 📊 TỔNG QUAN

Backend đã implement **đầy đủ** module Product Reviews với các tính năng:

- ✅ Hiển thị danh sách reviews của sản phẩm (pagination, filter, sort)
- ✅ User tạo review (chỉ khi đã mua hàng - COMPLETED order)
- ✅ User sửa/xóa review của mình
- ✅ Seller phản hồi review
- ✅ Upload ảnh trong review
- ✅ Tự động cập nhật rating của product và shop

---

## 🔑 BASE URLs

```
Local:       http://localhost:8080/api
Production:  https://your-domain.com/api
```

---

## 📋 API ENDPOINTS

### 1️⃣ **GET Reviews của Product** (Public)

```http
GET /api/products/{productId}/reviews
```

**Query Parameters:**
| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| `page` | integer | 0 | ❌ | Trang hiện tại (0-indexed) |
| `size` | integer | 10 | ❌ | Số reviews mỗi trang |
| `rating` | integer | null | ❌ | Filter theo rating (1-5) |
| `sortBy` | string | "newest" | ❌ | Sắp xếp: "newest", "oldest", "highest", "lowest" |

**Response Example:**
```json
{
  "content": [
    {
      "id": 123,
      "rating": 5,
      "comment": "Sản phẩm rất tốt, đóng gói cẩn thận!",
      "images": [
        "https://cloudinary.com/image1.jpg",
        "https://cloudinary.com/image2.jpg"
      ],
      "verifiedPurchase": true,
      "user": {
        "id": 45,
        "username": "buyer123",
        "fullName": "Nguyễn Văn A",
        "avatarUrl": "https://..."
      },
      "sellerResponse": "Cảm ơn bạn đã ủng hộ shop!",
      "sellerResponseDate": "2025-11-02T10:00:00",
      "createdAt": "2025-11-01T14:30:00",
      "updatedAt": "2025-11-01T14:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 45,
  "totalPages": 5,
  "last": false
}
```

**Frontend Usage:**
```javascript
// Example with React
const fetchProductReviews = async (productId, page = 0, rating = null) => {
  const params = new URLSearchParams({
    page: page,
    size: 10,
    sortBy: 'newest'
  });
  
  if (rating) {
    params.append('rating', rating);
  }
  
  const response = await fetch(
    `${API_BASE_URL}/products/${productId}/reviews?${params}`
  );
  return response.json();
};
```

---

### 2️⃣ **CREATE Review** (BUYER only - Requires JWT)

```http
POST /api/products/{productId}/reviews
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": 123,
  "rating": 5,
  "comment": "Sản phẩm tuyệt vời, giao hàng nhanh!",
  "images": [
    "https://cloudinary.com/uploaded-image1.jpg",
    "https://cloudinary.com/uploaded-image2.jpg"
  ]
}
```

**Validation Rules:**
- `orderId`: **Required** - Order phải tồn tại và thuộc về user
- `rating`: **Required** - Integer từ 1-5
- `comment`: Optional - Max 1000 characters
- `images`: Optional - Array of URLs (upload trước bằng Cloudinary API)

**Business Rules:**
- ✅ User phải đã mua sản phẩm (order status = `COMPLETED`)
- ✅ 1 user chỉ review 1 lần cho mỗi product
- ✅ Order phải chứa product đó

**Response:** `201 Created`
```json
{
  "id": 456,
  "rating": 5,
  "comment": "Sản phẩm tuyệt vời...",
  "images": ["https://..."],
  "verifiedPurchase": true,
  "user": {
    "id": 45,
    "username": "buyer123",
    "fullName": "Nguyễn Văn A",
    "avatarUrl": "https://..."
  },
  "sellerResponse": null,
  "sellerResponseDate": null,
  "createdAt": "2025-11-04T10:30:00",
  "updatedAt": "2025-11-04T10:30:00"
}
```

**Error Responses:**
```json
// User chưa mua sản phẩm
{
  "status": 400,
  "message": "Bạn chưa mua sản phẩm này hoặc đơn hàng chưa hoàn thành"
}

// User đã review rồi
{
  "status": 400,
  "message": "Bạn đã đánh giá sản phẩm này rồi"
}
```

**Frontend Usage:**
```javascript
const createReview = async (productId, reviewData, authToken) => {
  const response = await fetch(
    `${API_BASE_URL}/products/${productId}/reviews`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify(reviewData)
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json();
};
```

---

### 3️⃣ **UPDATE Review** (BUYER only - Within 7 days)

```http
PUT /api/reviews/{reviewId}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Update: Sau 1 tuần sử dụng vẫn tốt",
  "images": [
    "https://cloudinary.com/new-image.jpg"
  ]
}
```

**Business Rules:**
- ✅ Chỉ edit được trong **7 ngày** sau khi tạo review
- ✅ User phải là owner của review

**Response:** `200 OK` (Same structure as CREATE)

**Error Responses:**
```json
// Quá 7 ngày
{
  "status": 400,
  "message": "Chỉ có thể chỉnh sửa review trong vòng 7 ngày"
}

// Không phải owner
{
  "status": 403,
  "message": "Bạn không có quyền chỉnh sửa review này"
}
```

---

### 4️⃣ **DELETE Review** (BUYER only)

```http
DELETE /api/reviews/{reviewId}
Authorization: Bearer <JWT_TOKEN>
```

**Response:** `204 No Content`

---

### 5️⃣ **GET My Reviews** (BUYER only)

```http
GET /api/reviews/my
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
| Parameter | Type | Default | Required |
|-----------|------|---------|----------|
| `page` | integer | 0 | ❌ |
| `size` | integer | 10 | ❌ |

**Response:** Same pagination structure as endpoint #1

---

### 6️⃣ **DELETE Review Images** (BUYER only)

```http
DELETE /api/reviews/{reviewId}/images
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "imageUrls": [
    "https://cloudinary.com/image-to-delete1.jpg",
    "https://cloudinary.com/image-to-delete2.jpg"
  ]
}
```

**Response:** `200 OK` - Updated review object

---

## 🎨 PRODUCT DTO CẬP NHẬT

Backend đã cập nhật `ProductDTO` để bao gồm thông tin review:

```json
{
  "id": 123,
  "name": "Nike Air Max 270",
  "description": "...",
  "mainImage": "https://...",
  "basePrice": 2990000,
  "rating": 4.75,           // ⭐ MỚI: Average rating
  "reviewCount": 128,       // ⭐ MỚI: Tổng số reviews
  "soldCount": 456,         // ⭐ MỚI: Đã bán
  "isActive": true,
  "category": {...},
  "shopName": "Nike Official Store",
  "variants": [...],
  "images": [...]
}
```

**Sử dụng trong Frontend:**
```jsx
// React Component Example
const ProductCard = ({ product }) => {
  return (
    <div className="product-card">
      <img src={product.mainImage} alt={product.name} />
      <h3>{product.name}</h3>
      <div className="rating">
        <StarRating value={product.rating} /> {/* 4.75/5.0 */}
        <span>({product.reviewCount} đánh giá)</span>
      </div>
      <div className="sold">Đã bán {product.soldCount}</div>
      <p className="price">{product.basePrice.toLocaleString('vi-VN')}đ</p>
    </div>
  );
};
```

---

## 🔐 AUTHENTICATION

Tất cả endpoints yêu cầu `@PreAuthorize("hasRole('BUYER')")` cần JWT token:

```javascript
// Add token to all requests
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('access_token')}`
};
```

**Token Claims:**
```json
{
  "username": "buyer123",
  "role": "BUYER",
  "exp": 1699123456
}
```

---

## 📊 RATING STATISTICS (Tính năng bổ sung - Optional)

Backend có thể mở rộng thêm endpoint để lấy thống kê rating:

```http
GET /api/products/{productId}/reviews/statistics
```

**Response Example:**
```json
{
  "averageRating": 4.75,
  "totalReviews": 128,
  "ratingDistribution": {
    "5": 85,  // 85 reviews 5 sao
    "4": 30,
    "3": 10,
    "2": 2,
    "1": 1
  },
  "verifiedPurchaseCount": 120,
  "withImagesCount": 45,
  "withSellerResponseCount": 20
}
```

**Frontend Display:**
```jsx
const RatingDistribution = ({ stats }) => {
  return (
    <div className="rating-stats">
      <h3>Đánh giá sản phẩm</h3>
      <div className="average">
        <span className="score">{stats.averageRating}</span>
        <StarRating value={stats.averageRating} />
        <p>{stats.totalReviews} đánh giá</p>
      </div>
      
      <div className="distribution">
        {[5,4,3,2,1].map(star => (
          <div key={star} className="bar">
            <span>{star} ⭐</span>
            <ProgressBar 
              value={stats.ratingDistribution[star]} 
              max={stats.totalReviews} 
            />
            <span>{stats.ratingDistribution[star]}</span>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## 🖼️ IMAGE UPLOAD FLOW

Backend **KHÔNG** handle upload ảnh. Frontend cần upload trước lên Cloudinary:

### Flow:
1. User chọn ảnh
2. Frontend upload lên **Cloudinary** (hoặc AWS S3)
3. Lấy URL từ Cloudinary
4. Gửi URL trong `images` array khi POST/PUT review

**Example với Cloudinary:**
```javascript
const uploadToCloudinary = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'your_upload_preset');
  
  const response = await fetch(
    'https://api.cloudinary.com/v1_1/your_cloud_name/image/upload',
    {
      method: 'POST',
      body: formData
    }
  );
  
  const data = await response.json();
  return data.secure_url; // Return URL
};

// Sử dụng khi tạo review
const handleSubmitReview = async () => {
  // 1. Upload ảnh trước
  const imageUrls = await Promise.all(
    selectedFiles.map(file => uploadToCloudinary(file))
  );
  
  // 2. Tạo review với URLs
  const reviewData = {
    orderId: currentOrderId,
    rating: selectedRating,
    comment: reviewText,
    images: imageUrls
  };
  
  await createReview(productId, reviewData, authToken);
};
```

---

## ✅ CHECKLIST TÍCH HỢP FRONTEND

### Phase 1: Hiển thị Reviews
- [ ] Component hiển thị danh sách reviews trên product detail page
- [ ] Pagination component
- [ ] Filter theo rating (1-5 sao)
- [ ] Sort dropdown (newest, oldest, highest, lowest)
- [ ] Hiển thị rating distribution chart
- [ ] Show seller response (nếu có)

### Phase 2: Tạo/Sửa Review
- [ ] Form tạo review (rating, comment, upload ảnh)
- [ ] Validate user đã mua hàng (check order history)
- [ ] Upload ảnh lên Cloudinary
- [ ] Show preview ảnh trước khi submit
- [ ] Edit review form (chỉ hiện trong 7 ngày)
- [ ] Delete review confirmation modal

### Phase 3: My Reviews Page
- [ ] Page hiển thị tất cả reviews của user
- [ ] Link đến product từ review
- [ ] Quick edit/delete actions

### Phase 4: Product Card
- [ ] Hiển thị `rating` và `reviewCount` trên product card
- [ ] Star rating component
- [ ] "Đã bán X" badge

---

## 🐛 ERROR HANDLING

**Common Errors:**
```javascript
const handleReviewError = (error) => {
  switch(error.status) {
    case 400:
      // Validation error hoặc business rule violation
      toast.error(error.message);
      break;
    case 401:
      // Token expired
      router.push('/login');
      break;
    case 403:
      // Not owner of review
      toast.error('Bạn không có quyền thực hiện hành động này');
      break;
    case 404:
      // Review/Product not found
      toast.error('Không tìm thấy đánh giá');
      break;
    default:
      toast.error('Có lỗi xảy ra, vui lòng thử lại');
  }
};
```

---

## 📞 CONTACT & SUPPORT

**Backend Developer:** [Your Name]  
**API Issues:** Report tại GitHub Issues  
**Documentation:** This file

---

**Last Updated:** November 4, 2025  
**Backend Version:** 1.0.0  
**API Status:** ✅ Production Ready
