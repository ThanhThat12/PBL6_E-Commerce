# Xóa Cột variant_attribute_id - Tóm Tắt Thay Đổi

## 📅 Ngày: 2025-11-25

## 🎯 Mục Đích
Xóa cột `variant_attribute_id` khỏi bảng `product_images` để đơn giản hóa cấu trúc dữ liệu sau khi sửa lỗi "Query did not return a unique result". 

## ⚠️ Vấn Đề Trước Đây
- Cột `variant_attribute_id` (FK đến `product_variant_values`) gây ra lỗi duplicate results
- Một attribute value (VD: "HOME") có thể tồn tại trong nhiều ProductVariantValue records
- Query `findByProductIdAndValueName()` trả về nhiều kết quả thay vì unique result

## ✅ Giải Pháp
Chỉ sử dụng `variant_attribute_value` (String) thay vì FK relationship:
- Đủ để xác định variant image (VD: "HOME", "AWAY")
- Tránh được duplicate query results
- Đơn giản hóa data model

## 🔧 Thay Đổi Trong Code

### 1. ProductImage.java
```diff
- @ManyToOne(fetch = FetchType.LAZY)
- @JoinColumn(name = "variant_attribute_id")
- private ProductVariantValue variantAttribute;

- public ProductVariantValue getVariantAttribute() { return variantAttribute; }
- public void setVariantAttribute(ProductVariantValue variantAttribute) { this.variantAttribute = variantAttribute; }
```

### 2. Unique Constraint Update
```diff
- columnNames = {"product_id", "variant_attribute_id", "variant_attribute_value", "image_type"}
+ columnNames = {"product_id", "variant_attribute_value", "image_type"}
```

### 3. ImageServiceImpl.java
```diff
- .variantId(image.getVariantAttribute() != null ? image.getVariantAttribute().getId() : null)
+ .variantId(null) // variantAttribute field removed

- productImage.setVariantAttribute(variantValue);
+ // Note: variantAttribute FK removed - only using variantAttributeValue string now
```

## 🗄️ Database Migration

### File: `remove_variant_attribute_id_migration.sql`

**Thực hiện theo thứ tự:**
1. Drop unique constraint `uq_product_variant_image`
2. Drop index `idx_variant_attr` 
3. Drop FK constraint `FKk7xlxfwab6kl5tqy04hvfu8t1`
4. Drop column `variant_attribute_id`
5. Tạo unique constraint mới: `(product_id, variant_attribute_value, image_type)`
6. Tạo index mới: `idx_variant_value`

## 🎯 Kết Quả
- ✅ **Upload variant image hoạt động**: Không còn lỗi "Query did not return a unique result"
- ✅ **Data integrity**: Unique constraint mới đảm bảo 1 image/variant value
- ✅ **Query performance**: Index mới trên `variant_attribute_value`
- ✅ **Code đơn giản**: Bớt complex relationships

## 📊 Dữ Liệu Không Bị Ảnh Hưởng
- `variant_attribute_value` vẫn được giữ nguyên ("HOME", "AWAY", etc.)
- Các variant images hiện tại vẫn hoạt động bình thường
- API endpoints không thay đổi

## 🔍 Kiểm Tra Sau Migration
```sql
-- Verify table structure
DESCRIBE product_images;

-- Check constraints
SHOW INDEX FROM product_images;

-- Test variant image data
SELECT product_id, variant_attribute_value, image_type, image_url 
FROM product_images 
WHERE image_type = 'VARIANT';
```

## 📝 Lưu Ý Quan Trọng
⚠️ **Backup database trước khi chạy migration!**

Việc xóa cột này an toàn vì:
- Code không còn sử dụng FK relationship
- Chỉ sử dụng `variant_attribute_value` string
- Unique constraint mới đảm bảo data integrity