# 📋 SELLER REGISTRATION - REQUIRED INFORMATION

## ✅ Implemented: Enhanced Seller Registration with Additional Requirements

Date: November 5, 2025

### 🎯 Overview
Đã cập nhật quy trình đăng ký từ BUYER lên SELLER với yêu cầu thông tin đầy đủ hơn.

---

## 📝 Required Information for Seller Registration

### **1. Shop Information** (Yêu cầu)
- **Shop Name** (`shopName`)
  - Không được để trống
  - Tối đa 255 ký tự
  - Phải độc nhất
  - VD: "My Awesome Shop"

- **Shop Description** (`shopDescription`)
  - Tùy chọn
  - Tối đa 1000 ký tự
  - VD: "Best products in town with great service"

- **Shop Phone** (`shopPhone`)
  - Không được để trống
  - Format Việt Nam: 0xxxxxxxxx hoặc +84xxxxxxxxx
  - Độc nhất giữa các seller
  - VD: "0987654321"

- **Shop Address** (`shopAddress`)
  - Không được để trống
  - Tối đa 500 ký tự
  - VD: "123 Main Street, District 1, Ho Chi Minh City"

---

### **2. Personal Information** (Mới thêm) ⭐

- **Full Name** (`fullName`) **[NEW]**
  - Không được để trống
  - Từ 2 đến 100 ký tự
  - Được lưu vào `User.fullName`
  - VD: "Nguyễn Văn A"

- **Primary Category** (`primaryCategory`) **[NEW]**
  - Loại sản phẩm chính bán
  - Không được để trống
  - Giá trị hợp lệ: "Accessories", "Bags", "Clothing", "Fitness Equipment", "Shoes", "Sports Equipment"
  - VD: "Clothing"

---

## 📊 DTO Structure

### Request DTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegistrationDTO {
    private String shopName;           // Tên shop - Required
    private String shopDescription;    // Mô tả shop - Optional
    private String shopPhone;          // Số điện thoại - Required
    private String shopAddress;        // Địa chỉ - Required
    private String fullName;           // Họ tên - Required [NEW]
    private String primaryCategory;    // Loại sản phẩm - Required [NEW]
}
```

### Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegistrationResponseDTO {
    private Long shopId;
    private String shopName;
    private String message;
    private boolean autoApproved;
}
```

---

## 🔄 Data Processing

### What Happens During Registration:

1. **Shop Creation** (Shop entity)
   - `name` ← shopName
   - `address` ← shopAddress
   - `description` ← shopDescription
   - `status` ← ACTIVE
   - `owner` ← Current User

2. **User Update** (User entity)
   - `fullName` ← fullName [NEW]
   - `phoneNumber` ← shopPhone
   - `role` ← SELLER (upgraded from BUYER)

3. **Note on Primary Category**
   - `primaryCategory` được ghi nhận nhưng lưu ý: **không có trường lưu trong database**
   - Có thể sử dụng cho:
     - Validation mục đích
     - Suggestion cho seller khi tạo sản phẩm đầu tiên
     - Future: Thêm trường vào Shop entity nếu cần

---

## 🔐 Validation Rules

| Field | Required | Format | Max Length | Special Rules |
|-------|----------|--------|------------|---------------|
| shopName | ✅ | Text | 255 | Unique |
| shopDescription | ❌ | Text | 1000 | - |
| shopPhone | ✅ | 0xxxxxxxxx | - | Unique, Vietnamese format |
| shopAddress | ✅ | Text | 500 | - |
| fullName | ✅ | Text | 100 (min 2) | - |
| primaryCategory | ✅ | Enum | - | "Accessories", "Bags", "Clothing", etc. |

---

## 📌 API Endpoint

### Register as Seller
```http
POST /api/seller/register
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "shopName": "My Awesome Shop",
  "shopDescription": "Best products in town",
  "shopPhone": "0987654321",
  "shopAddress": "123 Main Street, City",
  "fullName": "Nguyễn Văn A",
  "primaryCategory": "Clothing"
}
```

### Success Response (201)
```json
{
  "code": 201,
  "message": "Đăng ký seller thành công! Role đã được nâng cấp.",
  "data": {
    "shopId": 1,
    "shopName": "My Awesome Shop",
    "message": "Đăng ký seller thành công!",
    "autoApproved": true
  }
}
```

### Error Cases
```json
// 400: Missing required fields
{
  "code": 400,
  "error": "Họ tên không được để trống",
  "message": "Đăng ký seller thất bại"
}

// 409: Duplicate phone or shop name
{
  "code": 409,
  "error": "Số điện thoại đã được sử dụng bởi seller khác",
  "message": "Đăng ký seller thất bại"
}
```

---

## 🧪 Test Data (Postman)

### Valid Request
```json
{
  "shopName": "Test Sports Shop",
  "shopDescription": "Professional sports equipment",
  "shopPhone": "0912345678",
  "shopAddress": "456 Sports Lane, District 2, HCM",
  "fullName": "Trần Thị B",
  "primaryCategory": "Sports Equipment"
}
```

### Available Categories
- Accessories
- Bags
- Clothing
- Fitness Equipment
- Shoes
- Sports Equipment

---

## 📂 Modified Files

1. ✅ `SellerRegistrationDTO.java`
   - Added `fullName` field with validation
   - Added `primaryCategory` field with validation

2. ✅ `ShopService.java`
   - Updated `createShopFromSellerRegistration()` method
   - Now sets `user.fullName` and `user.phoneNumber`

3. ✅ `Seller_API.postman_collection.json`
   - Updated sample request body with new fields

---

## 💡 Future Enhancements

### Phase 2: Optional Improvements
1. **Add `primaryCategory` to Shop entity**
   ```java
   @Column(name = "primary_category", length = 50)
   private String primaryCategory;
   ```

2. **Store category for product filtering**
   - Use `primaryCategory` as default filter for seller's shop

3. **Suggest first product type**
   - Based on `primaryCategory` during first product creation

4. **Category-based dashboard**
   - Show category-relevant analytics and recommendations

---

## ✅ Completion Checklist

- ✅ Added `fullName` requirement to registration
- ✅ Added `primaryCategory` requirement to registration
- ✅ Updated DTO with validation
- ✅ Updated Service to persist `fullName` and `phoneNumber`
- ✅ Updated Postman collection with new fields
- ✅ Validation rules implemented
- ✅ No database schema changes needed (uses existing User fields)
- ✅ Backward compatible with existing seller records

---

## 🚀 Testing Instructions

1. **Login as BUYER**
2. **Call POST /api/seller/register** with all required fields:
   ```json
   {
     "shopName": "Your Shop Name",
     "shopDescription": "Optional description",
     "shopPhone": "0xxxxxxxxx",
     "shopAddress": "Your address",
     "fullName": "Your Full Name",
     "primaryCategory": "Clothing"
   }
   ```
3. **Verify response**: Code 201, auto-approved status
4. **Check User record**: `fullName` and `phoneNumber` updated
5. **Verify role upgrade**: User is now SELLER

---

**Implementation Status**: ✅ **COMPLETE**

**Last Updated**: November 5, 2025
