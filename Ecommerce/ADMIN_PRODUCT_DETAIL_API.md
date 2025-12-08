# API Chi Tiết Sản Phẩm cho Admin

## Tổng Quan
API này cung cấp thông tin chi tiết đầy đủ về sản phẩm dành cho Admin, bao gồm:
- Thông tin cơ bản của sản phẩm
- Thông tin category và shop
- Tất cả variants với attributes và ảnh
- Tất cả images của product
- Thống kê: tổng stock, tổng đã bán, rating, số lượng reviews

## Endpoint

```
GET /api/admin/products/{productId}/detail
```

### Authorization
- Yêu cầu: `ROLE_ADMIN`
- Header: `Authorization: Bearer {adminToken}`

### Response Mẫu

```json
{
  "statusCode": 200,
  "error": null,
  "message": "Product detail retrieved successfully",
  "data": {
    "id": 1,
    "name": "Áo Barcelona 2024/25 Chính Hãng",
    "basePrice": 500000,
    "description": "Áo sân nhà Barcelona mùa giải 2024/25",
    "mainImage": "https://res.cloudinary.com/xxx/image.jpg",
    "category": {
      "id": 3,
      "name": "Clothing"
    },
    "shop": {
      "id": 1,
      "name": "Shop Chính Hãng"
    },
    "isActive": true,
    "weightGrams": 300,
    "dimensions": {
      "heightCm": 5,
      "lengthCm": 25,
      "widthCm": 20
    },
    "totalStock": 1230,
    "totalSold": 57,
    "averageRating": 4.8,
    "reviewCount": 23,
    "createdAt": "2025-11-20T10:00:00",
    "updatedAt": "2025-11-25T15:30:00",
    "images": [
      {
        "id": 1,
        "url": "https://res.cloudinary.com/xxx/image1.jpg",
        "isMain": true
      },
      {
        "id": 2,
        "url": "https://res.cloudinary.com/xxx/image2.jpg",
        "isMain": false
      }
    ],
    "variants": [
      {
        "id": 3,
        "sku": "BAR-HOME-S",
        "price": 500000,
        "stock": 80,
        "sold": 12,
        "attributes": [
          {
            "name": "Size",
            "value": "S"
          },
          {
            "name": "Màu",
            "value": "Xanh-đỏ"
          }
        ],
        "images": [
          "https://res.cloudinary.com/xxx/variant1.jpg",
          "https://res.cloudinary.com/xxx/variant2.jpg"
        ]
      },
      {
        "id": 4,
        "sku": "BAR-HOME-M",
        "price": 500000,
        "stock": 100,
        "sold": 20,
        "attributes": [
          {
            "name": "Size",
            "value": "M"
          },
          {
            "name": "Màu",
            "value": "Xanh-đỏ"
          }
        ],
        "images": [
          "https://res.cloudinary.com/xxx/variant3.jpg"
        ]
      }
    ]
  }
}
```

## Các File Đã Tạo/Cập Nhật

### 1. DTOs (Domain Transfer Objects)

#### `AdminProductDetailDTO.java`
DTO chính chứa toàn bộ thông tin chi tiết sản phẩm

#### `AdminProductVariantDTO.java`
DTO cho variant sản phẩm (SKU, giá, stock, sold, attributes, images)

#### `AdminProductImageDTO.java`
DTO cho ảnh sản phẩm (id, url, isMain)

#### `AdminProductAttributeDTO.java`
DTO cho thuộc tính variant (name, value) - ví dụ: Size: M, Màu: Xanh

#### `AdminCategoryDetailDTO.java`
DTO cho thông tin category (id, name)

#### `AdminShopDetailDTO.java`
DTO cho thông tin shop (id, name)

#### `AdminProductDimensionsDTO.java`
DTO cho kích thước sản phẩm (heightCm, lengthCm, widthCm)

### 2. Repository

#### `OrderItemRepository.java`
Đã thêm method:
```java
@Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
       "JOIN oi.order o " +
       "WHERE oi.variant.id = :variantId AND o.status = 'COMPLETED'")
Long getTotalSoldByVariantId(@Param("variantId") Long variantId);
```

### 3. Service

#### `AdminProductService.java`
Đã thêm method `getProductDetail(Long productId)` với logic:

1. **Lấy Product entity** từ database
2. **Map basic info**: id, name, price, description, images, isActive, weight, timestamps
3. **Map category**: id và name
4. **Map shop**: id và name  
5. **Set dimensions**: height, length, width
6. **Tính thống kê**:
   - `totalStock`: SUM(stock) từ tất cả variants
   - `totalSold`: SUM(quantity) từ order_items với status = COMPLETED
   - `averageRating`: AVG(rating) từ product_reviews
   - `reviewCount`: COUNT(*) từ product_reviews
7. **Lấy tất cả images** của product từ `product_images`
8. **Lấy tất cả variants** với:
   - Stock và sold từ `product_variants` và `order_items`
   - Attributes từ `product_variant_values` join `product_attributes`
   - Images của variant từ `product_images` với `imageType = 'VARIANT'`

### 4. Controller

#### `AdminProductController.java`
Đã thêm endpoint:
```java
@GetMapping("/{productId}/detail")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseDTO<AdminProductDetailDTO>> getProductDetail(@PathVariable Long productId)
```

## Cách Tính Toán

### Total Stock
```sql
SELECT COALESCE(SUM(pv.stock), 0) 
FROM product_variants pv 
WHERE pv.product_id = ?
```

### Total Sold
```sql
SELECT COALESCE(SUM(oi.quantity), 0) 
FROM order_items oi 
JOIN orders o ON oi.order_id = o.id 
WHERE oi.variant_id IN (SELECT id FROM product_variants WHERE product_id = ?)
  AND o.status = 'COMPLETED'
```

### Average Rating
```sql
SELECT AVG(pr.rating) 
FROM product_reviews pr 
WHERE pr.product_id = ?
```

### Review Count
```sql
SELECT COUNT(*) 
FROM product_reviews pr 
WHERE pr.product_id = ?
```

### Variant Sold
```sql
SELECT COALESCE(SUM(oi.quantity), 0) 
FROM order_items oi 
JOIN orders o ON oi.order_id = o.id 
WHERE oi.variant_id = ? 
  AND o.status = 'COMPLETED'
```

## Testing

### Postman Example

```bash
GET http://localhost:8081/api/admin/products/1/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### cURL Example

```bash
curl -X GET \
  'http://localhost:8081/api/admin/products/1/detail' \
  -H 'Authorization: Bearer YOUR_ADMIN_TOKEN'
```

## Error Handling

### Product Not Found (404)
```json
{
  "statusCode": 404,
  "error": "NOT_FOUND",
  "message": "Product not found with id: 999",
  "data": null
}
```

### Unauthorized (401)
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "data": null
}
```

### Forbidden (403)
```json
{
  "statusCode": 403,
  "error": "FORBIDDEN",
  "message": "Access denied. Admin role required",
  "data": null
}
```

## Database Schema

### Tables Involved
- `products` - Thông tin cơ bản sản phẩm
- `categories` - Danh mục sản phẩm
- `shops` - Thông tin shop
- `product_variants` - Các biến thể sản phẩm
- `product_variant_values` - Giá trị thuộc tính của variant
- `product_attributes` - Định nghĩa thuộc tính (Size, Màu, etc.)
- `product_images` - Ảnh sản phẩm và variant
- `order_items` - Chi tiết đơn hàng (để tính sold)
- `orders` - Đơn hàng (để filter COMPLETED)
- `product_reviews` - Đánh giá sản phẩm (rating, reviewCount)

## Performance Optimization

### Eager Loading
Service sử dụng các query tối ưu:
- Join fetch cho category và shop
- Batch query cho variants
- Lazy loading cho images

### Indexing Recommendations
```sql
CREATE INDEX idx_order_items_variant ON order_items(variant_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);
```

## Checklist Hoàn Thành

- [x] Tạo tất cả DTOs cần thiết
- [x] Thêm query methods vào Repository
- [x] Implement logic trong Service
- [x] Tạo endpoint trong Controller
- [x] Test compilation - Không có lỗi
- [x] Xử lý exceptions
- [x] Tối ưu query
- [x] Viết documentation

## Next Steps (Tùy Chọn)

1. **Caching**: Thêm Redis cache cho product detail
2. **Pagination**: Nếu variants quá nhiều, có thể paginate
3. **Filtering**: Thêm filter cho variants (theo stock, sold, etc.)
4. **Export**: API export product detail sang Excel/PDF
5. **Audit Log**: Log mỗi lần admin xem chi tiết sản phẩm
