# 📸 Review Images - Shopee Style Implementation

## 🎯 **OVERVIEW**

Module Review đã được cập nhật để xử lý ảnh theo style của **Shopee**:
- ✅ **Merge images** khi update (giữ ảnh cũ + thêm ảnh mới)
- ✅ **Giới hạn tối đa 5 ảnh** per review
- ✅ **Tự động loại bỏ duplicate** URLs
- ✅ **API riêng để xóa ảnh** cụ thể

---

## 📋 **BUSINESS RULES**

### **1. Create Review (POST /api/products/{id}/reviews)**
- Max 5 ảnh
- Validation: `images.size() <= 5`
- Error: `"Chỉ được upload tối đa 5 ảnh"`

### **2. Update Review (PUT /api/reviews/{id})**
**Behavior: MERGE (không ghi đè)**
- Load ảnh cũ từ DB
- Thêm ảnh mới từ request
- Loại bỏ duplicate (check URL)
- Validate tổng số <= 5 ảnh
- Error: `"Tổng số ảnh không được vượt quá 5 ảnh. Hiện có X ảnh cũ, bạn chỉ có thể thêm tối đa Y ảnh mới."`

**Example:**
```json
// Current review có 3 ảnh:
["image1.jpg", "image2.jpg", "image3.jpg"]

// PUT request với 2 ảnh mới:
{
  "rating": 5,
  "comment": "Updated",
  "images": ["image4.jpg", "image5.jpg"]
}

// Result: 5 ảnh (merge)
["image1.jpg", "image2.jpg", "image3.jpg", "image4.jpg", "image5.jpg"]

// Nếu PUT thêm 3 ảnh nữa:
{
  "images": ["image4.jpg", "image5.jpg", "image6.jpg"]
}
// ❌ Error: "Tổng số ảnh không được vượt quá 5 ảnh. Hiện có 3 ảnh cũ, bạn chỉ có thể thêm tối đa 2 ảnh mới."
```

### **3. Remove Images (DELETE /api/reviews/{id}/images)**
**Behavior: Xóa ảnh cụ thể**
- Cho phép xóa 1 hoặc nhiều ảnh
- Không cần gửi lại toàn bộ list
- Validation: user ownership + 7-day limit

**Example:**
```json
// Current review có 5 ảnh:
["img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg", "img5.jpg"]

// DELETE /api/reviews/1/images
// Body:
{
  "imageUrls": ["img2.jpg", "img4.jpg"]
}

// Result: 3 ảnh còn lại
["img1.jpg", "img3.jpg", "img5.jpg"]
```

---

## 🔧 **API DOCUMENTATION**

### **1. Create Review**
```http
POST /api/products/{productId}/reviews
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 123,
  "rating": 5,
  "comment": "Great product!",
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review2.jpg"
  ]
}

Response 201:
{
  "id": 1,
  "rating": 5,
  "comment": "Great product!",
  "images": [
    "https://cdn.example.com/review1.jpg",
    "https://cdn.example.com/review2.jpg"
  ],
  ...
}
```

**Validation:**
- ✅ `images.length <= 5`
- ❌ Error 400: `"Chỉ được upload tối đa 5 ảnh"`

---

### **2. Update Review (MERGE mode)**
```http
PUT /api/reviews/{reviewId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "rating": 5,
  "comment": "Updated review with more images",
  "images": [
    "https://cdn.example.com/review3.jpg",
    "https://cdn.example.com/review4.jpg"
  ]
}

Response 200:
{
  "id": 1,
  "rating": 5,
  "comment": "Updated review with more images",
  "images": [
    "https://cdn.example.com/review1.jpg",  // ← Ảnh cũ giữ nguyên
    "https://cdn.example.com/review2.jpg",  // ← Ảnh cũ giữ nguyên
    "https://cdn.example.com/review3.jpg",  // ← Ảnh mới
    "https://cdn.example.com/review4.jpg"   // ← Ảnh mới
  ],
  ...
}
```

**Validation:**
- ✅ `existingImages.length + newImages.length <= 5`
- ✅ Duplicate URLs tự động loại bỏ
- ❌ Error 400: `"Tổng số ảnh không được vượt quá 5 ảnh. Hiện có 2 ảnh cũ, bạn chỉ có thể thêm tối đa 3 ảnh mới."`

---

### **3. Remove Specific Images**
```http
DELETE /api/reviews/{reviewId}/images
Authorization: Bearer {token}
Content-Type: application/json

{
  "imageUrls": [
    "https://cdn.example.com/review2.jpg",
    "https://cdn.example.com/review4.jpg"
  ]
}

Response 200:
{
  "id": 1,
  "images": [
    "https://cdn.example.com/review1.jpg",  // ← Giữ lại
    "https://cdn.example.com/review3.jpg"   // ← Giữ lại
  ],
  ...
}
```

**Validation:**
- ✅ User ownership
- ✅ Within 7-day limit
- ❌ Error 400: `"Danh sách imageUrls không được rỗng"`
- ❌ Error 403: `"Bạn không có quyền sửa review này"`
- ❌ Error 400: `"Chỉ có thể sửa review trong vòng 7 ngày"`

---

## 📊 **USE CASES**

### **Case 1: User tạo review với 2 ảnh**
```json
POST /api/products/2/reviews
{ "images": ["img1.jpg", "img2.jpg"] }
→ Success: 2 images saved
```

### **Case 2: User thêm 3 ảnh nữa (update)**
```json
PUT /api/reviews/1
{ "images": ["img3.jpg", "img4.jpg", "img5.jpg"] }
→ Success: Total 5 images (2 old + 3 new)
```

### **Case 3: User cố thêm ảnh thứ 6**
```json
PUT /api/reviews/1
{ "images": ["img6.jpg"] }
→ Error 400: "Tổng số ảnh không được vượt quá 5 ảnh. Hiện có 5 ảnh cũ, bạn chỉ có thể thêm tối đa 0 ảnh mới."
```

### **Case 4: User xóa 2 ảnh cụ thể**
```json
DELETE /api/reviews/1/images
{ "imageUrls": ["img2.jpg", "img4.jpg"] }
→ Success: 3 images left
```

### **Case 5: User thêm lại ảnh sau khi xóa**
```json
PUT /api/reviews/1
{ "images": ["img6.jpg", "img7.jpg"] }
→ Success: Total 5 images (3 old + 2 new)
```

---

## 🎨 **FRONTEND INTEGRATION**

### **Recommended Flow:**

```javascript
// 1. Create review với ảnh
const createReview = async (productId, data) => {
  // Limit upload to 5 files
  if (data.images.length > 5) {
    alert("Chỉ được upload tối đa 5 ảnh");
    return;
  }
  
  await fetch(`/api/products/${productId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(data)
  });
};

// 2. Update review - thêm ảnh mới
const addMoreImages = async (reviewId, newImages) => {
  const response = await fetch(`/api/reviews/${reviewId}`, {
    method: 'PUT',
    body: JSON.stringify({
      images: newImages // Backend sẽ merge với ảnh cũ
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    alert(error.message); // "Tổng số ảnh không được vượt quá 5..."
  }
};

// 3. Xóa ảnh cụ thể
const removeImages = async (reviewId, imageUrls) => {
  await fetch(`/api/reviews/${reviewId}/images`, {
    method: 'DELETE',
    body: JSON.stringify({ imageUrls })
  });
};

// 4. UI Flow giống Shopee
const ReviewEditUI = () => {
  const [review, setReview] = useState(null);
  
  const handleRemoveImage = (imageUrl) => {
    // Xóa 1 ảnh cụ thể
    removeImages(review.id, [imageUrl]);
  };
  
  const handleAddImages = (newFiles) => {
    const currentCount = review.images.length;
    const maxAllowed = 5 - currentCount;
    
    if (newFiles.length > maxAllowed) {
      alert(`Chỉ có thể thêm tối đa ${maxAllowed} ảnh nữa`);
      return;
    }
    
    // Upload files to CDN first
    const newUrls = await uploadToCDN(newFiles);
    
    // Call API to merge
    addMoreImages(review.id, newUrls);
  };
  
  return (
    <div>
      <div className="image-list">
        {review.images.map(img => (
          <div key={img} className="image-item">
            <img src={img} />
            <button onClick={() => handleRemoveImage(img)}>
              ❌ Xóa
            </button>
          </div>
        ))}
      </div>
      
      <input
        type="file"
        multiple
        accept="image/*"
        onChange={handleAddImages}
        disabled={review.images.length >= 5}
      />
      
      <p>
        {review.images.length}/5 ảnh
      </p>
    </div>
  );
};
```

---

## ⚙️ **TECHNICAL IMPLEMENTATION**

### **Service Layer:**
```java
// ProductReviewServiceImpl.java

private static final int MAX_REVIEW_IMAGES = 5;

@Override
public ProductReviewDTO updateReview(...) {
    // Load ảnh cũ
    List<String> finalImages = new ArrayList<>();
    if (review.getImages() != null) {
        finalImages.addAll(objectMapper.readValue(...));
    }
    
    // Thêm ảnh mới (loại bỏ duplicate)
    if (request.getImages() != null) {
        for (String newImage : request.getImages()) {
            if (!finalImages.contains(newImage)) {
                finalImages.add(newImage);
            }
        }
    }
    
    // Validate tổng số
    if (finalImages.size() > MAX_REVIEW_IMAGES) {
        throw new BadRequestException(...);
    }
    
    // Save
    review.setImages(objectMapper.writeValueAsString(finalImages));
}
```

---

## 🧪 **TESTING**

### **Test Case 1: Create with 5 images**
```bash
curl -X POST http://localhost:8080/api/products/2/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 4,
    "rating": 5,
    "comment": "Test 5 images",
    "images": ["img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg", "img5.jpg"]
  }'

Expected: ✅ 201 Created
```

### **Test Case 2: Create with 6 images (should fail)**
```bash
curl -X POST http://localhost:8080/api/products/2/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "images": ["img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg", "img5.jpg", "img6.jpg"]
  }'

Expected: ❌ 400 Bad Request
{ "message": "Chỉ được upload tối đa 5 ảnh" }
```

### **Test Case 3: Update merge (2 old + 2 new = 4)**
```bash
# Assume review id=1 has 2 images

curl -X PUT http://localhost:8080/api/reviews/1 \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "rating": 5,
    "images": ["img3.jpg", "img4.jpg"]
  }'

Expected: ✅ 200 OK
{ "images": ["img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg"] }
```

### **Test Case 4: Update exceed limit**
```bash
# Assume review id=1 has 5 images

curl -X PUT http://localhost:8080/api/reviews/1 \
  -d '{ "images": ["img6.jpg"] }'

Expected: ❌ 400 Bad Request
{
  "message": "Tổng số ảnh không được vượt quá 5 ảnh. Hiện có 5 ảnh cũ, bạn chỉ có thể thêm tối đa 0 ảnh mới."
}
```

### **Test Case 5: Remove specific images**
```bash
curl -X DELETE http://localhost:8080/api/reviews/1/images \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "imageUrls": ["img2.jpg", "img4.jpg"]
  }'

Expected: ✅ 200 OK
{ "images": ["img1.jpg", "img3.jpg", "img5.jpg"] }
```

---

## 📝 **CHANGELOG**

### **v2.0 - Shopee Style Images (Nov 1, 2025)**
- ✅ Added `MAX_REVIEW_IMAGES = 5` constant
- ✅ Create review: validate max 5 images
- ✅ Update review: MERGE mode (không ghi đè)
- ✅ Update review: auto remove duplicates
- ✅ Update review: validate total <= 5
- ✅ New endpoint: `DELETE /api/reviews/{id}/images` to remove specific images
- ✅ Improved error messages with current/max info

### **Migration Notes:**
- ✅ No database changes required (images stored as JSON)
- ✅ Backward compatible: existing reviews work as before
- ✅ Frontend needs update to support new remove endpoint

---

## 🎯 **SUMMARY**

| Action | Old Behavior | New Behavior (Shopee Style) |
|--------|-------------|----------------------------|
| **Create** | No limit | ✅ Max 5 images |
| **Update** | Overwrite all images | ✅ MERGE (keep old + add new) |
| **Update** | No duplicate check | ✅ Auto remove duplicates |
| **Update** | No validation | ✅ Validate total <= 5 |
| **Remove** | Must send full list | ✅ DELETE specific images API |

**Benefits:**
- 🎨 Better UX (giống Shopee)
- 🚀 Không mất ảnh cũ khi update
- 🔒 Giới hạn abuse (max 5)
- 🎯 Flexible image management

---

**Status:** ✅ IMPLEMENTED & READY TO TEST

**Next Steps:**
1. Test all scenarios with Postman
2. Update frontend to use new DELETE endpoint
3. Add UI for "remove image" button
4. Deploy to staging
