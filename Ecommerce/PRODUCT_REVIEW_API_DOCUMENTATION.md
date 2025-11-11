# 📝 Product Review API Documentation

## 🎯 Overview
Complete CRUD API system for product reviews with seller response functionality.

## 🚀 API Endpoints

### 1️⃣ Tạo đánh giá sản phẩm
**POST** `/api/reviews`
- **Authentication**: Required (BUYER role)
- **Description**: Buyer tạo review cho sản phẩm đã mua

**Request Body:**
```json
{
  "productId": 1,
  "orderId": 123,
  "rating": 5,
  "comment": "Sản phẩm rất tốt!",
  "images": [
    "https://image1.jpg",
    "https://image2.jpg"
  ]
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "error": null,
  "message": "Tạo đánh giá thành công",
  "data": {
    "id": 1,
    "rating": 5,
    "comment": "Sản phẩm rất tốt!",
    "images": ["https://image1.jpg", "https://image2.jpg"],
    "verifiedPurchase": true,
    "sellerResponse": null,
    "sellerResponseDate": null,
    "createdAt": "2025-11-06 10:30:00",
    "updatedAt": "2025-11-06 10:30:00",
    "userId": 1,
    "userName": "buyer1",
    "userFullName": "Nguyễn Văn A",
    "userAvatarUrl": "https://avatar.jpg",
    "productId": 1,
    "productName": "Gym Gloves",
    "orderId": 123
  }
}
```

### 2️⃣ Xem danh sách đánh giá của sản phẩm
**GET** `/api/products/{productId}/reviews`
- **Authentication**: None (Public)
- **Description**: Lấy tất cả review + thông tin người dùng, ảnh, phản hồi shop

**Query Parameters:**
- `page` (int, default: 0) - Số trang
- `size` (int, default: 10) - Kích thước trang
- `rating` (int, optional) - Lọc theo rating (1-5)
- `sortBy` (string, default: "newest") - Sắp xếp: "newest", "oldest", "highest", "lowest"

**Example:** `GET /api/products/1/reviews?page=0&size=10&rating=5&sortBy=newest`

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách đánh giá thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "rating": 5,
        "comment": "Sản phẩm rất tốt!",
        "images": ["https://image1.jpg"],
        "verifiedPurchase": true,
        "sellerResponse": "Cảm ơn bạn đã mua hàng!",
        "sellerResponseDate": "2025-11-06 11:00:00",
        "createdAt": "2025-11-06 10:30:00",
        "updatedAt": "2025-11-06 10:30:00",
        "userId": 1,
        "userName": "buyer1",
        "userFullName": "Nguyễn Văn A",
        "userAvatarUrl": "https://avatar.jpg",
        "productId": 1,
        "productName": "Gym Gloves",
        "orderId": 123
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 0,
    "size": 10
  }
}
```

### 3️⃣ Cập nhật đánh giá
**PUT** `/api/reviews/{reviewId}`
- **Authentication**: Required (BUYER role)
- **Description**: Buyer chỉnh sửa review của chính mình (chỉ trong vòng 7 ngày)

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Cập nhật: Sau 1 tuần dùng vẫn tốt",
  "images": ["https://new-image.jpg"]
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Cập nhật đánh giá thành công",
  "data": {
    // Similar structure as create response
  }
}
```

### 4️⃣ Seller phản hồi đánh giá
**POST** `/api/reviews/{reviewId}/reply`
- **Authentication**: Required (SELLER role)
- **Description**: Seller (chủ shop) trả lời review của khách

**Request Body:**
```json
{
  "sellerResponse": "Cảm ơn bạn đã mua hàng! Chúng tôi rất vui khi bạn hài lòng với sản phẩm."
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Thêm phản hồi thành công",
  "data": {
    // Review with seller response added
  }
}
```

### 5️⃣ Xóa đánh giá
**DELETE** `/api/reviews/{reviewId}`
- **Authentication**: Required (ADMIN hoặc chính người viết review)
- **Description**: Admin hoặc chính người viết review được phép xóa

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Xóa đánh giá thành công",
  "data": "Review đã được xóa"
}
```

### 6️⃣ Xem thống kê & điểm trung bình sản phẩm
**GET** `/api/products/{productId}/rating-summary`
- **Authentication**: None (Public)
- **Description**: Trả về thống kê rating của sản phẩm

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy thống kê đánh giá thành công",
  "data": {
    "averageRating": 4.5,
    "totalReviews": 100,
    "starCounts": {
      "5": 60,
      "4": 25,
      "3": 10,
      "2": 3,
      "1": 2
    }
  }
}
```

### 7️⃣ Lấy danh sách review của 1 user
**GET** `/api/users/{userId}/reviews`
- **Authentication**: None (Public)
- **Description**: Xem tất cả review mà user đó đã viết

**Query Parameters:**
- `page` (int, default: 0) - Số trang
- `size` (int, default: 10) - Kích thước trang

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách đánh giá của người dùng thành công",
  "data": {
    // Paginated list of reviews
  }
}
```

### 8️⃣ Lấy danh sách review của mình
**GET** `/api/my-reviews`
- **Authentication**: Required (BUYER role)
- **Description**: Xem tất cả review mà mình đã viết

**Query Parameters:**
- `page` (int, default: 0) - Số trang
- `size` (int, default: 10) - Kích thước trang

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "message": "Lấy danh sách đánh giá của bạn thành công",
  "data": {
    // Paginated list of user's reviews
  }
}
```

## 🔒 Authentication

### JWT Token
Tất cả các endpoint yêu cầu authentication cần có JWT token trong header:
```
Authorization: Bearer {jwt_token}
```

### Roles
- **BUYER**: Có thể tạo, sửa, xóa review của mình
- **SELLER**: Có thể phản hồi review của sản phẩm trong shop
- **ADMIN**: Có thể xóa bất kỳ review nào

## 📝 Business Rules

### Tạo Review
1. ✅ User phải có role BUYER
2. ✅ Order phải thuộc về user
3. ✅ Order status phải là COMPLETED
4. ✅ Order phải chứa sản phẩm được review
5. ✅ User chỉ được review 1 lần cho mỗi sản phẩm
6. ✅ Rating phải từ 1-5
7. ✅ Comment tối đa 1000 ký tự

### Cập nhật Review
1. ✅ Chỉ người tạo review mới được sửa
2. ✅ Chỉ được sửa trong vòng 7 ngày từ khi tạo
3. ✅ Rating phải từ 1-5
4. ✅ Comment tối đa 1000 ký tự

### Seller Reply
1. ✅ Chỉ seller của shop chứa sản phẩm mới được reply
2. ✅ Seller response tối đa 500 ký tự

### Xóa Review
1. ✅ Admin có thể xóa bất kỳ review nào
2. ✅ Người tạo review có thể xóa review của mình

## 🚦 Error Handling

### Common Error Codes
- `400` - Bad Request (validation errors, business rule violations)
- `401` - Unauthorized (missing or invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource not found)
- `500` - Internal Server Error

### Error Response Format
```json
{
  "status": 400,
  "error": "ERROR_CODE",
  "message": "Chi tiết lỗi",
  "data": null
}
```

## 🧪 Testing

### Postman Collection
Tham khảo Postman collection có sẵn tại:
- `postman/review/Product_Reviews_API.postman_collection.json`
- `postman/review/Review_API_Local.postman_environment.json`

### Test Flow
1. **Login** để lấy JWT token
2. **Tạo Order** và đặt status = COMPLETED
3. **Tạo Review** cho sản phẩm trong order
4. **Xem Reviews** (public)
5. **Seller Reply** (nếu có role SELLER)
6. **Update Review** (trong vòng 7 ngày)

## 📊 Database Schema

### Table: product_reviews
```sql
CREATE TABLE `product_reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` longtext DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `images` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`images`)),
  `rating` int(11) NOT NULL,
  `seller_response` longtext DEFAULT NULL,
  `seller_response_date` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `verified_purchase` bit(1) NOT NULL DEFAULT b'1',
  `order_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product_review` (`user_id`,`product_id`),
  KEY `FK35kxxqe2g9r4mww80w9e3tnw9` (`product_id`),
  KEY `FKkpu8o8nqopc4lcqcfnnlpq5vg` (`order_id`),
  CONSTRAINT `FK35kxxqe2g9r4mww80w9e3tnw9` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK58i39bhws2hss3tbcvdmrm60f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKkpu8o8nqopc4lcqcfnnlpq5vg` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

## 🎉 Success!

API đã được implement đầy đủ với tất cả các tính năng yêu cầu:
- ✅ CRUD operations cho reviews
- ✅ Seller response functionality  
- ✅ Rating summary & statistics
- ✅ Proper authentication & authorization
- ✅ Business rule validation
- ✅ Error handling
- ✅ Pagination & sorting
- ✅ Complete documentation