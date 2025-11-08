# 🆕 Product Enhancements - Shipping Dimensions & Seller Create API

## 📋 Changes Summary

### 1. ✅ Added Shipping Dimensions to Product Entity

**Purpose:** Cho phép tính phí ship chính xác với GHN API

**Fields Added:**
- `weight_grams` (Integer) - Trọng lượng (gram)
- `length_cm` (Integer) - Chiều dài (cm)
- `width_cm` (Integer) - Chiều rộng (cm)
- `height_cm` (Integer) - Chiều cao (cm)

**Files Modified:**
- `Product.java` - Entity với 4 fields mới + getters/setters
- `ProductCreateDTO.java` - DTO để nhận dimensions từ frontend
- `ProductService.java` - Set dimensions khi tạo product

---

### 2. ✅ Added Seller Create Product API

**New Endpoint:**
```
POST /api/seller/products
```

**Request Body:**
```json
{
  "name": "Sản phẩm mới",
  "description": "Mô tả chi tiết",
  "basePrice": 299000,
  "categoryId": 1,
  "mainImage": "https://...",
  "weightGrams": 500,
  "lengthCm": 20,
  "widthCm": 15,
  "heightCm": 10
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Tạo sản phẩm thành công (chờ duyệt)",
  "data": {
    "id": 123,
    "name": "Sản phẩm mới",
    "isActive": false,
    ...
  }
}
```

**Features:**
- ✅ Seller tạo sản phẩm với `isActive = false` (chờ admin duyệt)
- ✅ Auto-detect seller's shop
- ✅ Validate category exists
- ✅ Include shipping dimensions
- ✅ Full error handling

**Files Modified:**
- `SellerProductController.java` - Added POST endpoint
- `SellerProductService.java` - Added `createSellerProduct()` method

---

### 3. ✅ Fixed Dashboard Top Selling Products Query

**Problem:** Method `findTopSellingProductsByShopId()` không tồn tại

**Solution:** Added native query to ProductRepository

**Query Added:**
```java
@Query(value = "SELECT p.* FROM products p " +
               "LEFT JOIN product_variants pv ON p.id = pv.product_id " +
               "WHERE p.shop_id = :shopId " +
               "GROUP BY p.id " +
               "ORDER BY COALESCE(SUM(pv.sold_count), 0) DESC " +
               "LIMIT :limit", 
       nativeQuery = true)
List<Product> findTopSellingProductsByShopId(@Param("shopId") Long shopId, @Param("limit") int limit);
```

**How it works:**
- JOIN với product_variants để lấy sold_count
- GROUP BY product.id
- ORDER BY tổng sold_count giảm dần
- LIMIT số lượng products trả về

**File Modified:**
- `ProductRepository.java`

---

## 🗄️ Database Migration

**File:** `sql/migration_add_product_dimensions.sql`

**Changes:**
```sql
ALTER TABLE products 
ADD COLUMN weight_grams INT COMMENT 'Trọng lượng sản phẩm (gram)',
ADD COLUMN length_cm INT COMMENT 'Chiều dài (cm)',
ADD COLUMN width_cm INT COMMENT 'Chiều rộng (cm)',
ADD COLUMN height_cm INT COMMENT 'Chiều cao (cm)';

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_products_shop_id ON products(shop_id);
CREATE INDEX IF NOT EXISTS idx_product_variants_sold_count ON product_variants(product_id, sold_count);
```

**Run Migration:**
```bash
mysql -u root -p ecommerce < sql/migration_add_product_dimensions.sql
```

---

## 🧪 Testing Guide

### 1. Test Seller Create Product:

```bash
POST http://localhost:8080/api/seller/products
Authorization: Bearer {seller_token}
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max",
  "description": "Điện thoại flagship 2024",
  "basePrice": 29990000,
  "categoryId": 1,
  "mainImage": "https://example.com/iphone15.jpg",
  "weightGrams": 221,
  "lengthCm": 16,
  "widthCm": 8,
  "heightCm": 1
}
```

**Expected Response:**
- Status: 200 OK
- Product created with `isActive = false`
- Shop auto-assigned to seller's shop

### 2. Test Dashboard Top Products:

```bash
GET http://localhost:8080/api/seller/dashboard/top-products?limit=5
Authorization: Bearer {seller_token}
```

**Expected Response:**
- List of top 5 products sorted by sold count
- Products from seller's shop only

### 3. Test GHN Shipping Fee Calculation:

```javascript
// Frontend code example
const calculateShipping = async (productId) => {
  const product = await productService.getProduct(productId);
  
  const shippingRequest = {
    to_district_id: 1234,
    to_ward_code: "5678",
    weight: product.weightGrams,
    length: product.lengthCm,
    width: product.widthCm,
    height: product.heightCm
  };
  
  const fee = await ghnService.calculateFee(shippingRequest);
  console.log('Shipping fee:', fee);
};
```

---

## 📊 API Summary Update

### Phase 2 - Product APIs (Updated):

| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| `/api/seller/products` | POST | ✅ NEW | Create product (pending approval) |
| `/api/seller/products` | GET | ✅ | List with filters |
| `/api/seller/products/{id}` | GET | ✅ | Get detail |
| `/api/seller/products/{id}` | PUT | ✅ | Update product |
| `/api/seller/products/{id}/status` | PATCH | ✅ | Toggle status |

**Total: 5 APIs (was 4)** ✅

---

## 🔧 Technical Details

### Product Dimensions Usage:

**1. GHN API Integration:**
```java
// When creating order
Map<String, Object> ghnPayload = new HashMap<>();
ghnPayload.put("weight", product.getWeightGrams());
ghnPayload.put("length", product.getLengthCm());
ghnPayload.put("width", product.getWidthCm());
ghnPayload.put("height", product.getHeightCm());

// Calculate shipping fee
BigDecimal shippingFee = ghnService.calculateFee(ghnPayload);
```

**2. Frontend Form:**
```jsx
<Form.Item 
  label="Trọng lượng (gram)" 
  name="weightGrams"
  rules={[{ required: true, message: 'Vui lòng nhập trọng lượng' }]}
>
  <InputNumber min={1} style={{ width: '100%' }} />
</Form.Item>

<Form.Item 
  label="Kích thước (cm)" 
  required
>
  <Space>
    <Form.Item name="lengthCm" noStyle>
      <InputNumber placeholder="Dài" min={1} />
    </Form.Item>
    <Form.Item name="widthCm" noStyle>
      <InputNumber placeholder="Rộng" min={1} />
    </Form.Item>
    <Form.Item name="heightCm" noStyle>
      <InputNumber placeholder="Cao" min={1} />
    </Form.Item>
  </Space>
</Form.Item>
```

---

## ✅ Validation Rules

### Product Dimensions:
- All dimensions are **optional** (nullable)
- If provided, must be positive integers
- Weight in grams (not kg)
- Dimensions in centimeters (not meters)

### Create Product:
- `name`: Required, 3-200 characters
- `description`: Optional, max 2000 characters
- `basePrice`: Required, must be > 0
- `categoryId`: Required, must exist
- `mainImage`: Optional
- Dimensions: All optional

---

## 🎯 Benefits

### For Sellers:
✅ Tạo sản phẩm dễ dàng qua API riêng  
✅ Không cần admin role  
✅ Tự động chờ duyệt (isActive = false)  
✅ Nhập dimensions cho ship chính xác  

### For System:
✅ Tính phí ship chính xác với GHN  
✅ Tracking top selling products dễ dàng  
✅ Dashboard stats hoạt động tốt  
✅ Better UX với estimated shipping  

### For Customers:
✅ Thấy phí ship chính xác trước khi đặt  
✅ Ít bị tính sai phí  
✅ Minh bạch về kích thước sản phẩm  

---

## 📝 Notes

### Default Dimensions:
Nếu seller không nhập dimensions khi tạo product, các giá trị sẽ là `null`. Có thể set defaults:
- Weight: 500g (small item)
- Length: 20cm
- Width: 15cm
- Height: 10cm

### Backward Compatibility:
- Old products without dimensions: Vẫn hoạt động bình thường
- GHN API: Sẽ dùng default values nếu dimensions null
- Frontend: Can show "Chưa cập nhật" if dimensions missing

### Future Enhancements:
- Add dimension validation based on category
- Auto-suggest dimensions from similar products
- Dimension calculator from images (ML)
- Bulk update dimensions for existing products

---

## ✨ Summary

**3 major improvements completed:**

1. ✅ **Shipping Dimensions** - 4 new fields cho Product
2. ✅ **Seller Create API** - POST /api/seller/products
3. ✅ **Top Selling Query** - Fixed dashboard error

**Files modified:** 6 Java files + 1 SQL migration  
**New APIs:** 1 (Create Product for Seller)  
**Database changes:** 4 new columns + 2 indexes  

**Ready for testing!** 🚀
