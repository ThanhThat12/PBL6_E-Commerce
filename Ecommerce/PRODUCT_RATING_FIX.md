# Product Rating System - Real-time Review Integration

## Vấn đề
- Rating của sản phẩm hiển thị trên frontend không phản ánh đúng điểm review thực tế
- Field `rating` và `review_count` trong bảng `products` không được cập nhật khi có review mới/sửa/xóa
- Dẫn đến hiển thị rating "giả" không liên kết với `product_review`

## Giải pháp

### 1. Backend Changes

#### ProductService.java
**Thêm dependency:**
```java
@Autowired
private ProductReviewRepository productReviewRepository;
```

**Thêm method cập nhật rating:**
```java
@Transactional
public void updateProductRating(Long productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    
    // Get actual average rating from reviews
    Double averageRating = productReviewRepository.getAverageRatingByProductId(productId);
    long reviewCount = productReviewRepository.countByProductId(productId);
    
    // Update product with real data
    if (averageRating != null && averageRating > 0) {
        product.setRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
    } else {
        product.setRating(BigDecimal.ZERO);
    }
    
    product.setReviewCount((int) reviewCount);
    product.setUpdatedAt(LocalDateTime.now());
    
    productRepository.save(product);
}
```

**Cập nhật convertToProductDTO:**
- Thay vì dùng cached value từ DB, tính toán real-time từ reviews:
```java
// Calculate real-time rating from reviews
Double averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
long actualReviewCount = productReviewRepository.countByProductId(product.getId());

if (averageRating != null && averageRating > 0) {
    dto.setRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
} else {
    dto.setRating(BigDecimal.ZERO);
}
dto.setReviewCount((int) actualReviewCount);
```

#### ProductReviewService.java
**Thêm dependency:**
```java
@Autowired
private ProductService productService;
```

**Gọi updateProductRating sau mỗi thao tác:**

1. **Tạo review mới:**
```java
review = productReviewRepository.save(review);
productService.updateProductRating(productId);
```

2. **Sửa review:**
```java
review = productReviewRepository.save(review);
productService.updateProductRating(review.getProduct().getId());
```

3. **Xóa review (Admin only):**
```java
Long productId = review.getProduct().getId();
productReviewRepository.delete(review);
productService.updateProductRating(productId);
```

### 2. Luồng hoạt động

```
User tạo/sửa/xóa review
    ↓
ProductReviewService xử lý
    ↓
Lưu vào product_review table
    ↓
Gọi ProductService.updateProductRating()
    ↓
Tính toán lại average rating từ tất cả reviews
    ↓
Cập nhật products.rating và products.review_count
    ↓
Frontend hiển thị rating chính xác
```

### 3. Database Schema

**product_review table** (source of truth):
- rating: INTEGER (1-5)
- product_id: Foreign key to products

**products table** (cached values):
- rating: DECIMAL(3,2) - Trung bình từ reviews
- review_count: INTEGER - Số lượng reviews

### 4. API Responses

**GET /api/products/{id}**
```json
{
  "id": 123,
  "name": "Product Name",
  "rating": 4.75,          // ← Calculated from reviews
  "reviewCount": 128,       // ← Count from product_review
  "soldCount": 450
}
```

**GET /api/products/{id}/rating-summary**
```json
{
  "averageRating": 4.75,
  "totalReviews": 128,
  "ratingDistribution": {
    "5": 80,
    "4": 30,
    "3": 12,
    "2": 4,
    "1": 2
  }
}
```

## Lợi ích

1. **Độ chính xác cao:** Rating luôn phản ánh đúng reviews thực tế
2. **Tự động cập nhật:** Không cần chạy batch job hoặc cron
3. **Transparent:** Mọi thao tác review đều trigger cập nhật rating
4. **Performance:** Vẫn cache rating trong products table để query nhanh
5. **Real-time calculation:** convertToProductDTO tính toán trực tiếp từ reviews

## Testing

### Test Cases

1. **Tạo review mới:**
   - Product ban đầu có rating = 0, reviewCount = 0
   - User tạo review với rating = 5
   - Verify: Product.rating = 5.00, reviewCount = 1

2. **Nhiều reviews:**
   - Product có 3 reviews: 5★, 4★, 3★
   - Average = (5+4+3)/3 = 4.00
   - Verify: Product.rating = 4.00, reviewCount = 3

3. **Sửa review:**
   - Product có average = 4.00
   - User sửa review từ 3★ → 5★
   - New average = (5+4+5)/3 = 4.67
   - Verify: Product.rating = 4.67

4. **Xóa review:**
   - Product có 3 reviews, average = 4.00
   - Admin xóa 1 review
   - Verify: reviewCount giảm, rating được tính lại

## Migration Notes

### Cập nhật rating cho products hiện có:

```sql
-- Script để sync rating từ product_review sang products
UPDATE products p
SET 
  rating = COALESCE(
    (SELECT AVG(pr.rating) FROM product_review pr WHERE pr.product_id = p.id),
    0
  ),
  review_count = (
    SELECT COUNT(*) FROM product_review pr WHERE pr.product_id = p.id
  ),
  updated_at = NOW()
WHERE p.id > 0;
```

## Frontend Integration

Frontend không cần thay đổi gì - rating được trả về tự động qua ProductDTO với giá trị chính xác từ reviews.

**ProductDetailPage.jsx:**
```javascript
const rating = product.averageRating || 0;  // From real reviews
const soldCount = product.soldCount || 0;
```

## Monitoring

Check logs để verify rating updates:
```
INFO  - Updated product 123 rating to 4.75 (from 128 reviews)
INFO  - Created review 456 for product 123 by user john_doe via product-detail endpoint
```

## Future Enhancements

1. **Background sync job:** Chạy hàng đêm để đảm bảo consistency
2. **Redis cache:** Cache rating calculations cho products hot
3. **Elasticsearch:** Index ratings cho advanced filtering/sorting
4. **Analytics:** Track rating trends over time

---

**Status:** ✅ Completed  
**Date:** 2025-12-09  
**Files Modified:**
- `ProductService.java` - Added updateProductRating() method
- `ProductReviewService.java` - Trigger rating update on review operations
- `convertToProductDTO()` - Calculate real-time rating from reviews
