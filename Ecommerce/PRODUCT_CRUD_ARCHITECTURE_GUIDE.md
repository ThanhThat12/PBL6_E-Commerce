# 📋 Hướng Dẫn Create/Update Product - Kiến Trúc Tách Riêng

## 🎯 **Tổng Quan Kiến Trúc Mới**

Sau khi tách riêng xử lý ảnh và review, việc tạo/cập nhật product được chia thành các bước độc lập:

```
1️⃣ CREATE/UPDATE PRODUCT (Basic Info) 
2️⃣ UPLOAD IMAGES (Separate endpoints)
3️⃣ MANAGE REVIEWS (Separate endpoints)
4️⃣ MANAGE VARIANTS (Can be included or separate)
```

---

## 🔧 **1. Entity vs Database Mapping**

### ✅ **Product Entity - Đã Cập Nhật**
```java
// Các fields mới được thêm để match với DB:
private String productCondition = "NEW";        // product_condition
private BigDecimal rating = BigDecimal.ZERO;    // rating  
private Integer reviewCount = 0;                 // review_count
private Integer soldCount = 0;                   // sold_count
private LocalDateTime createdAt;                 // created_at
private LocalDateTime updatedAt;                 // updated_at
```

### ✅ **ProductDTO - Đã Modernize**
```java
// Loại bỏ deprecated fields:
@Deprecated getStock() // → Use variants instead
@Deprecated getCondition() // → Use productCondition

// Thêm fields mới:
private BigDecimal rating;
private Integer reviewCount;
private Integer soldCount;
private String productCondition;
private LocalDateTime createdAt, updatedAt;
```

---

## 🚀 **2. Workflow Tạo Product Mới**

### **Step 1: Tạo Product Basic Info**
```javascript
// Frontend gọi API
POST /api/products
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max",
  "description": "Latest iPhone with advanced features",
  "basePrice": 25000000,
  "categoryId": 1,
  "productCondition": "NEW",
  "weightGrams": 240,
  "lengthCm": 16,
  "widthCm": 8,
  "heightCm": 1,
  "variants": [
    {
      "sku": "IP15-256-RED",
      "attributeValues": [
        {"attributeId": 2, "value": "Red"},
        {"attributeId": 1, "value": "256GB"}
      ],
      "price": 25000000,
      "stock": 50
    }
  ]
}
```

**Backend Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "name": "iPhone 15 Pro Max",
    "basePrice": 25000000,
    "isActive": true,
    "shopId": 45,
    "createdAt": "2025-11-25T12:00:00",
    "variants": [...],
    "images": [] // Empty - chưa upload
  },
  "message": "Product created successfully"
}
```

### **Step 2: Upload Main Image**
```javascript
POST /api/products/123/images/main
Content-Type: multipart/form-data

FormData: {
  file: [selected_image_file]
}
```

### **Step 3: Upload Gallery Images**
```javascript
POST /api/products/123/images/gallery
Content-Type: multipart/form-data

FormData: {
  files: [image1, image2, image3, ...]
}
```

### **Step 4: Upload Variant-Specific Images**
```javascript
// Upload cho variant màu "Red"
POST /api/products/123/images/variant?attributeValue=Red
Content-Type: multipart/form-data

FormData: {
  file: [red_variant_image]
}
```

---

## 🔄 **3. Workflow Update Product**

### **Step 1: Update Basic Info**
```javascript
PUT /api/products/123
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max Updated",
  "basePrice": 24000000,
  "description": "Updated description"
  // Chỉ gửi fields cần update
}
```

### **Step 2: Update Images (If Needed)**
```javascript
// Replace main image
POST /api/products/123/images/main

// Add more gallery images  
POST /api/products/123/images/gallery

// Delete specific image
DELETE /api/products/123/images/gallery/456

// Reorder images
PUT /api/products/123/images/gallery/reorder
{
  "imageIds": [789, 456, 123] // New order
}
```

---

## 🎨 **4. Frontend Implementation Example**

### **React - Create Product Component**
```jsx
const CreateProductPage = () => {
  const [productData, setProductData] = useState({
    name: '',
    description: '',
    basePrice: 0,
    categoryId: null,
    variants: []
  });
  const [mainImage, setMainImage] = useState(null);
  const [galleryImages, setGalleryImages] = useState([]);
  
  const handleCreateProduct = async () => {
    try {
      // Step 1: Create product basic info
      const response = await fetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(productData)
      });
      
      const { data: product } = await response.json();
      const productId = product.id;
      
      // Step 2: Upload main image if exists
      if (mainImage) {
        const formData = new FormData();
        formData.append('file', mainImage);
        
        await fetch(`/api/products/${productId}/images/main`, {
          method: 'POST',
          body: formData
        });
      }
      
      // Step 3: Upload gallery images if any
      if (galleryImages.length > 0) {
        const formData = new FormData();
        galleryImages.forEach(img => formData.append('files', img));
        
        await fetch(`/api/products/${productId}/images/gallery`, {
          method: 'POST', 
          body: formData
        });
      }
      
      // Step 4: Upload variant images if any
      // ... handle variant images
      
      toast.success('Product created successfully!');
      navigate(`/products/${productId}`);
      
    } catch (error) {
      toast.error('Failed to create product');
    }
  };
  
  return (
    <form onSubmit={handleCreateProduct}>
      {/* Product basic fields */}
      <input 
        value={productData.name}
        onChange={(e) => setProductData({...productData, name: e.target.value})}
        placeholder="Product name"
      />
      
      {/* Main image upload */}
      <input 
        type="file" 
        onChange={(e) => setMainImage(e.target.files[0])}
        accept="image/*"
      />
      
      {/* Gallery images upload */}
      <input 
        type="file" 
        multiple
        onChange={(e) => setGalleryImages([...e.target.files])}
        accept="image/*"
      />
      
      <button type="submit">Create Product</button>
    </form>
  );
};
```

---

## 🔒 **5. Backend Controller Updates**

### **ProductController Enhancement**
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(
        @PathVariable Long id, 
        @Valid @RequestBody ProductUpdateDTO request,
        Authentication authentication) {
    
    ProductDTO product = productService.updateProduct(id, request, authentication);
    return ResponseDTO.success(product, "Product updated successfully");
}
```

### **Service Layer Pattern**
```java
@Transactional
public ProductDTO updateProduct(Long id, ProductUpdateDTO request, Authentication auth) {
    Product product = findProductById(id);
    
    // Validate ownership
    validateProductOwnership(product, auth);
    
    // Update only non-null fields
    if (request.getName() != null) product.setName(request.getName());
    if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
    if (request.getDescription() != null) product.setDescription(request.getDescription());
    // ... other fields
    
    product.setUpdatedAt(LocalDateTime.now());
    productRepository.save(product);
    
    return mapToProductDTO(product);
}
```

---

## 📊 **6. API Endpoints Summary**

### **Core Product APIs**
```
POST   /api/products                    - Create product
GET    /api/products/{id}               - Get product by ID  
PUT    /api/products/{id}               - Update product
DELETE /api/products/{id}               - Delete product
GET    /api/products                    - List products (paginated)
GET    /api/products/search             - Search products
```

### **Image Management APIs**
```
POST   /api/products/{id}/images/main           - Upload/replace main image
DELETE /api/products/{id}/images/main           - Delete main image
POST   /api/products/{id}/images/gallery        - Upload gallery images
GET    /api/products/{id}/images/gallery        - Get gallery images
DELETE /api/products/{id}/images/gallery/{imgId} - Delete gallery image
POST   /api/products/{id}/images/variant        - Upload variant image
DELETE /api/products/{id}/images/variant        - Delete variant image
```

### **Review Management APIs**
```
GET    /api/products/{id}/reviews        - Get product reviews
POST   /api/products/{id}/reviews        - Create review
PUT    /api/reviews/{reviewId}           - Update review  
DELETE /api/reviews/{reviewId}           - Delete review
```

---

## 🎯 **7. Lợi Ích Của Kiến Trúc Mới**

### ✅ **Separation of Concerns**
- Product basic info
- Image management  
- Review system
- Variant handling

### ✅ **Better Performance**
- Upload images async after product creation
- Lazy loading images
- Independent caching strategies

### ✅ **Enhanced UX**
- Users can create product first, add images later
- Progress indicators for each step
- Better error handling per component

### ✅ **Maintenance Benefits**
- Each module can be developed/tested independently
- Clearer responsibilities
- Easier to add features (e.g., image optimization, review moderation)

---

## 🚨 **Migration Notes**

1. **Database**: Entity fields đã được cập nhật match với schema
2. **DTOs**: ProductDTO modernized, deprecated methods marked
3. **Controllers**: Image/Review endpoints already separated
4. **Frontend**: Update to use new multi-step workflow

Kiến trúc mới này đảm bảo scalability và maintainability tốt hơn! 🚀