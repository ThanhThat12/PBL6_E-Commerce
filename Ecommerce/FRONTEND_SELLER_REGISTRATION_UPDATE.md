# Frontend Seller Registration Update Guide

## Overview
Cập nhật frontend để tích hợp với API đăng ký seller (Phase 1: COD only).

---

## 1. API Endpoints Summary

### 🌐 PUBLIC APIs (No auth required)

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/shops/{shopId}` | Lấy thông tin shop (public) | `ShopDTO` |
| GET | `/api/shops/user/{userId}` | Lấy shop theo user ID | `Shop` |
| GET | `/api/shops/check/{userId}` | Kiểm tra user có shop không | `Boolean` |

### 🔵 BUYER APIs (Role: BUYER)

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/seller/register` | Đăng ký seller (tạo shop PENDING) | `SellerRegistrationResponseDTO` |
| GET | `/api/seller/registration/status` | Lấy trạng thái đơn đăng ký | `RegistrationStatusDTO` |
| DELETE | `/api/seller/registration` | Hủy đơn bị từ chối (để đăng ký lại) | `Boolean` |
| GET | `/api/seller/registration/can-submit` | Kiểm tra có thể đăng ký không | `{canSubmit, message}` |

### 🟢 SELLER APIs (Role: SELLER)

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/seller/shop` | Lấy thông tin shop cơ bản (public info) | `ShopDTO` |
| GET | `/api/seller/shop/detail` | Lấy thông tin shop đầy đủ (KYC, GHN, address) | `ShopDetailDTO` |
| PUT | `/api/seller/shop` | Cập nhật thông tin shop | `ShopDetailDTO` ✅ |
| PUT | `/api/shops/{shopId}/ghn-credentials` | Cập nhật GHN credentials | `Shop` |
| GET | `/api/seller/shop/analytics` | Lấy thống kê doanh thu | `ShopAnalyticsDTO` |

### 🔴 ADMIN APIs (Role: ADMIN)

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/admin/seller-registrations/pending` | Danh sách đơn chờ duyệt | `Page<PendingApplicationDTO>` |
| GET | `/api/admin/seller-registrations/search?keyword=` | Tìm kiếm đơn đăng ký | `Page<PendingApplicationDTO>` |
| GET | `/api/admin/seller-registrations/pending/count` | Đếm số đơn chờ | `Long` |
| GET | `/api/admin/seller-registrations/{shopId}` | Chi tiết đơn đăng ký | `PendingApplicationDTO` |
| POST | `/api/admin/seller-registrations/approve` | Phê duyệt đơn | `SellerRegistrationResponseDTO` |
| POST | `/api/admin/seller-registrations/reject` | Từ chối đơn | `SellerRegistrationResponseDTO` |

---

## 2. DTO Structure Comparison

### Tại sao cần 2 DTO?

| | `ShopDTO` (Public) | `ShopDetailDTO` (Seller Only) |
|---|---|---|
| **Use case** | Guest/Buyer xem shop | Seller quản lý shop |
| **Security** | Không có thông tin nhạy cảm | Có KYC, GHN credentials |
| **Performance** | Lightweight | Full data |

### ShopDTO Fields (Public - 14 fields)
```javascript
{
  id, name, description, status, createdAt,
  logoUrl, bannerUrl,
  address, provinceName, districtName, wardName,
  rating, reviewCount,
  shopPhone, shopEmail
}
```

### ShopDetailDTO Fields (Seller - 40+ fields)
```javascript
{
  // Tất cả của ShopDTO +
  addressId, provinceId, districtId, wardCode, contactPhone, contactName,
  ghnShopId, ghnToken, ghnConfigured,
  maskedIdCardNumber, idCardName, kycVerified,
  idCardFrontUrl, idCardBackUrl, selfieWithIdUrl, // (chỉ khi PENDING/REJECTED)
  acceptCod, codFeePercentage,
  ownerId, ownerUsername, ownerFullName, ownerEmail, ownerPhone, ownerCreatedAt,
  submittedAt, reviewedAt, reviewedBy, rejectionReason
}
```

---

## 3. Data Structures (Chi tiết)

### 3.1 ShopDTO (GET /api/shops/{id} - Public)

```javascript
{
  // Basic info
  id: 18,
  name: "My Shop",
  description: "Mô tả shop...",
  status: "ACTIVE",
  createdAt: "2024-11-20T10:30:00",
  
  // Branding
  logoUrl: "https://res.cloudinary.com/.../logo.jpg",
  bannerUrl: "https://res.cloudinary.com/.../banner.jpg",
  
  // Address (text only - no IDs for privacy)
  address: "123 Đường ABC, Phường X, Quận Y",
  provinceName: "Hồ Chí Minh",
  districtName: "Quận 1",
  wardName: "Phường Bến Nghé",
  
  // Rating
  rating: 4.85,
  reviewCount: 128,
  
  // Contact (public for buyers)
  shopPhone: "0987654321",
  shopEmail: "shop@email.com"
}
```

```javascript
{
  // Required fields
  shopName: "Tên shop",           // 3-100 ký tự
  shopPhone: "0123456789",        // 10-11 số, bắt đầu bằng 0
  shopEmail: "shop@example.com",  // Valid email
  idCardNumber: "001234567890",   // 9 số (CMND) hoặc 12 số (CCCD)
  idCardFrontUrl: "https://...",  // URL ảnh mặt trước CCCD
  idCardBackUrl: "https://...",   // URL ảnh mặt sau CCCD  
  idCardName: "Nguyễn Văn A",     // Họ tên trên CCCD (max 100 ký tự)
  
  // Optional fields
  description: "Mô tả shop",      // max 2000 ký tự
  
  // Address (chọn 1 trong 2 option)
  // Option 1: Dùng address có sẵn
  addressId: 123,
  // Option 2: Tạo address mới
  fullAddress: "123 Đường ABC",
  provinceId: 202,
  districtId: 1442,
  wardCode: "20101",
  provinceName: "Hồ Chí Minh",
  districtName: "Quận 1",
  wardName: "Phường Bến Nghé",
  contactPhone: "0123456789",
  contactName: "Người liên hệ",
  primaryAddress: false,
  
  // Branding (optional)
  logoUrl: "https://res.cloudinary.com/...",
  logoPublicId: "shops/logo_abc123",
  bannerUrl: "https://res.cloudinary.com/...",
  bannerPublicId: "shops/banner_xyz789",
  
  // KYC images (optional but recommended)
  idCardFrontPublicId: "kyc/front_123",
  idCardBackPublicId: "kyc/back_456",
  selfieWithIdUrl: "https://...",       // Ảnh selfie cầm CCCD
  selfieWithIdPublicId: "kyc/selfie_789"
}
```

### 2.2 RegistrationStatusDTO (GET /api/seller/registration/status)

```javascript
{
  shopId: 18,
  shopName: "My Shop",
  description: "Mô tả shop",
  shopPhone: "0987654321",
  shopEmail: "shop@email.com",
  status: "PENDING",              // PENDING | ACTIVE | REJECTED | SUSPENDED | CLOSED
  statusDescription: "Đang chờ duyệt",
  maskedIdCardNumber: "********7890",  // Số CCCD đã mask
  idCardName: "Nguyễn Văn A",
  fullAddress: "123 ABC, Q1, HCM",
  logoUrl: "https://...",
  submittedAt: "2024-12-05T10:30:00",
  reviewedAt: null,               // null nếu chưa review
  rejectionReason: null,          // Lý do từ chối (nếu REJECTED)
  acceptCod: true
}
```

### 2.3 ShopDetailDTO (GET /api/seller/shop/detail)

```javascript
{
  // Basic info
  shopId: 18,
  name: "My Shop",
  description: "...",
  status: "ACTIVE",
  createdAt: "2024-11-20T...",
  
  // Contact
  shopPhone: "0987654321",
  shopEmail: "shop@email.com",
  
  // Branding
  logoUrl: "https://...",
  logoPublicId: "shops/logo_...",
  bannerUrl: "https://...",
  bannerPublicId: "shops/banner_...",
  
  // Address (từ Address entity với TypeAddress.STORE)
  addressId: 45,
  fullAddress: "123 ABC",
  provinceId: 202,
  districtId: 1442,
  wardCode: "20101",
  provinceName: "Hồ Chí Minh",
  districtName: "Quận 1",
  wardName: "Phường Bến Nghé",
  contactPhone: "0123456789",
  contactName: "Người nhận hàng",
  
  // GHN Credentials
  ghnShopId: "12345",
  ghnToken: "abc...",
  ghnConfigured: true,           // true nếu cả 2 field đều có giá trị
  
  // KYC Status (masked)
  maskedIdCardNumber: "********7890",
  idCardName: "Nguyễn Văn A",
  kycVerified: true,             // true nếu status = ACTIVE
  
  // KYC Images (chỉ show khi PENDING/REJECTED để seller review)
  idCardFrontUrl: "https://...",
  idCardBackUrl: "https://...",
  selfieWithIdUrl: "https://...",
  
  // Payment
  acceptCod: true,
  codFeePercentage: 2.00,
  
  // Rating
  rating: 5.00,
  reviewCount: 0,
  
  // Owner info
  ownerId: 5,
  ownerUsername: "seller1",
  ownerFullName: "Nguyễn Văn A",
  ownerEmail: "seller@email.com",
  ownerPhone: "0912345678",
  ownerCreatedAt: "2024-01-15T...",
  
  // Review tracking
  submittedAt: "2024-12-01T...",
  reviewedAt: "2024-12-02T...",
  reviewedBy: 1,                  // Admin ID
  rejectionReason: null
}
```

### 2.4 UpdateShopDTO (PUT /api/seller/shop)

```javascript
{
  // Basic (optional - chỉ gửi field cần update)
  name: "Tên shop mới",
  description: "Mô tả mới",
  status: "ACTIVE",               // ACTIVE hoặc INACTIVE only
  
  // Contact
  shopPhone: "0987654321",
  shopEmail: "new@email.com",
  
  // Branding
  logoUrl: "https://...",
  logoPublicId: "...",
  bannerUrl: "https://...",
  bannerPublicId: "...",
  
  // Address - Option 1: Use existing address ID
  pickupAddressId: 123,
  
  // Address - Option 2: Create/update full address
  fullAddress: "456 XYZ",
  provinceId: 202,
  districtId: 1443,
  wardCode: "20102",
  provinceName: "Hồ Chí Minh",
  districtName: "Quận 2",
  wardName: "Phường Thảo Điền",
  contactPhone: "0999888777",
  contactName: "Người mới",
  
  // GHN
  ghnShopId: "99999",
  ghnToken: "new_token_..."
}
```

### 2.5 PendingApplicationDTO (Admin - GET /api/admin/seller-registrations/{id})

```javascript
{
  shopId: 18,
  shopName: "Shop ABC",
  description: "...",
  shopPhone: "0987654321",
  shopEmail: "shop@email.com",
  logoUrl: "https://...",
  
  // Full address
  fullAddress: "123 ABC",
  provinceName: "Hồ Chí Minh",
  districtName: "Quận 1",
  wardName: "Phường X",
  
  // Owner info (for verification)
  ownerId: 5,
  ownerUsername: "user123",
  ownerFullName: "Nguyễn Văn A",
  ownerEmail: "user@email.com",
  ownerPhone: "0912345678",
  ownerAvatar: "https://...",
  ownerCreatedAt: "2024-01-01T...",
  
  // Full KYC (NOT masked - admin needs to verify)
  idCardNumber: "001234567890",
  idCardName: "Nguyễn Văn A",
  idCardFrontUrl: "https://...",
  idCardBackUrl: "https://...",
  selfieWithIdUrl: "https://...",
  
  status: "PENDING",
  submittedAt: "2024-12-05T...",
  acceptCod: true
}
```

### 2.6 Admin Approval/Rejection DTOs

```javascript
// AdminApprovalDTO
{
  shopId: 18,
  adminNote: "Đã xác minh KYC"   // Optional
}

// AdminRejectionDTO
{
  shopId: 18,
  rejectionReason: "Ảnh CCCD không rõ ràng, vui lòng chụp lại"  // Required, 10-1000 ký tự
}
```

---

## 3. Frontend Implementation Checklist

### 3.1 Buyer Flow

#### SellerRegistrationPage.jsx
```
□ Kiểm tra canSubmit trước khi hiện form (GET /api/seller/registration/can-submit)
□ Form đăng ký với các field:
  □ Tên shop (required, 3-100 ký tự)
  □ Số điện thoại shop (required, pattern: ^0[0-9]{9,10}$)
  □ Email shop (required, email format)
  □ Mô tả (optional, max 2000 ký tự)
  □ Logo upload (optional)
  □ Banner upload (optional) ← MỚI THÊM
  □ Địa chỉ lấy hàng (addressId hoặc tạo mới với GHN address picker)
  □ Số CMND/CCCD (required, 9 hoặc 12 số)
  □ Họ tên trên CCCD (required)
  □ Ảnh mặt trước CCCD (required)
  □ Ảnh mặt sau CCCD (required)
  □ Ảnh selfie cầm CCCD (optional nhưng recommended)
□ Validation frontend match với backend
□ Submit form (POST /api/seller/register)
□ Handle errors:
  □ 403: Không phải BUYER
  □ 409: Đã có đơn đăng ký hoặc tên shop trùng hoặc CCCD đã dùng
  □ 400: Validation error
```

#### RegistrationStatusPage.jsx
```
□ Gọi GET /api/seller/registration/status
□ Hiển thị theo status:
  □ PENDING: "Đang chờ duyệt" + thời gian nộp
  □ REJECTED: Lý do từ chối + nút "Đăng ký lại"
  □ ACTIVE: Redirect đến /seller/shop
□ Nút "Hủy đơn bị từ chối" (DELETE /api/seller/registration)
```

### 3.2 Seller Flow

#### MyShop.jsx (Shop Management)
```
□ Gọi GET /api/seller/shop/detail để lấy đầy đủ thông tin
□ Hiển thị các section:
  □ Status alert (PENDING/REJECTED/ACTIVE với màu tương ứng)
  □ Basic info form (name, description)
  □ Contact form (shopPhone, shopEmail)
  □ Branding upload (logo, banner)
  □ Address section (GHN address picker)
  □ GHN Credentials section (ghnShopId, ghnToken)
  □ KYC Info display (masked, read-only)
  □ Rating & reviews display
  □ Owner info display
□ Submit form (PUT /api/seller/shop)
□ Chỉ enable form khi status = ACTIVE
```

### 3.3 Admin Flow

#### AdminSellerRegistrations.jsx
```
□ Tab "Chờ duyệt" với badge count
□ Gọi GET /api/admin/seller-registrations/pending (paginated)
□ Search box (GET /api/admin/seller-registrations/search?keyword=)
□ Table columns:
  □ Tên shop
  □ Người đăng ký (avatar, username, email)
  □ SĐT
  □ Ngày nộp
  □ Actions: [Xem chi tiết] [Duyệt] [Từ chối]
□ Modal xem chi tiết với full KYC images
□ Modal xác nhận duyệt
□ Modal từ chối với input lý do (required, 10-1000 ký tự)
```

---

## 4. Cloudinary Upload Folders

| Type | Folder | Example |
|------|--------|---------|
| Shop Logo | `shops/logos/` | `shops/logos/shop_18_logo` |
| Shop Banner | `shops/banners/` | `shops/banners/shop_18_banner` |
| KYC Front | `kyc/front/` | `kyc/front/user_5_front` |
| KYC Back | `kyc/back/` | `kyc/back/user_5_back` |
| KYC Selfie | `kyc/selfie/` | `kyc/selfie/user_5_selfie` |

---

## 5. Validation Rules Summary

| Field | Rules |
|-------|-------|
| shopName | Required, 3-100 chars, unique among ACTIVE/PENDING |
| shopPhone | Required, pattern: `^0[0-9]{9,10}$` |
| shopEmail | Required, valid email format |
| description | Optional, max 2000 chars |
| idCardNumber | Required, 9 digits (CMND) or 12 digits (CCCD), **unique per shop** |
| idCardName | Required, max 100 chars |
| idCardFrontUrl | Required |
| idCardBackUrl | Required |
| selfieWithIdUrl | Optional |
| rejectionReason | Required on reject, 10-1000 chars |

---

## 6. Status Flow

```
                    ┌─────────────┐
                    │   BUYER     │
                    │ (not SELLER)│
                    └──────┬──────┘
                           │ Submit Registration
                           ▼
                    ┌─────────────┐
              ┌────►│   PENDING   │◄────┐
              │     └──────┬──────┘     │
              │            │            │
              │    ┌───────┴───────┐    │
              │    │               │    │
              │    ▼               ▼    │
        ┌─────┴────┐         ┌─────────┴┐
        │  ACTIVE  │         │ REJECTED │
        │ (SELLER) │         │ (BUYER)  │
        └──────────┘         └──────────┘
                                   │
                                   │ Cancel + Resubmit
                                   ▼
                             (Back to PENDING)
```

---

## 7. Error Codes

| Code | Message | Action |
|------|---------|--------|
| 400 | Validation error | Show field errors |
| 403 | Chỉ tài khoản BUYER mới có thể đăng ký | Redirect to home |
| 404 | Chưa có đơn đăng ký | Show registration form |
| 409 | Đã có đơn đăng ký đang chờ | Show status page |
| 409 | Tên shop đã tồn tại | Clear shopName field |
| 409 | Số CMND/CCCD đã được sử dụng | Clear idCardNumber, show error |
| 500 | Lỗi hệ thống | Retry or contact support |

---

## 8. Services to Create/Update

### sellerRegistrationService.js (Updated)
```javascript
import api from './api';

// ========== BUYER APIs ==========
export const submitSellerRegistration = (data) => 
  api.post('/seller/register', data);

export const getRegistrationStatus = () => 
  api.get('/seller/registration/status');

export const cancelRejectedApplication = () => 
  api.delete('/seller/registration');

export const canSubmitRegistration = () => 
  api.get('/seller/registration/can-submit');

// ========== ADMIN APIs ==========
export const getPendingApplications = (page = 0, size = 10) => 
  api.get(`/admin/seller-registrations/pending?page=${page}&size=${size}`);

export const searchPendingApplications = (keyword, page = 0, size = 10) => 
  api.get(`/admin/seller-registrations/search?keyword=${keyword}&page=${page}&size=${size}`);

export const getPendingCount = () => 
  api.get('/admin/seller-registrations/pending/count');

export const getApplicationDetail = (shopId) => 
  api.get(`/admin/seller-registrations/${shopId}`);

export const approveRegistration = (shopId, adminNote = '') => 
  api.post('/admin/seller-registrations/approve', { shopId, adminNote });

export const rejectRegistration = (shopId, rejectionReason) => 
  api.post('/admin/seller-registrations/reject', { shopId, rejectionReason });
```

### shopService.js (Updated)
```javascript
import api from './api';

// Get shop basic info
export const getShop = () => 
  api.get('/seller/shop');

// Get shop full details (for seller dashboard)
export const getShopDetail = () => 
  api.get('/seller/shop/detail');

// Update shop info
export const updateShop = (data) => 
  api.put('/seller/shop', data);

// Update GHN credentials
export const updateGhnCredentials = (shopId, data) => 
  api.put(`/shops/${shopId}/ghn-credentials`, data);

// Get shop analytics
export const getShopAnalytics = (year) => 
  api.get(`/seller/shop/analytics${year ? `?year=${year}` : ''}`);
```

---

## 9. Important Notes

### 🔴 CCCD Uniqueness
- Mỗi số CCCD chỉ được đăng ký **1 shop** (ACTIVE hoặc PENDING)
- Nếu shop bị REJECTED, có thể xóa và đăng ký lại với cùng CCCD
- Backend đã có check: `existsByIdCardNumberAndStatusIn`

### 🔴 Image Upload Order
1. Upload images to Cloudinary **trước**
2. Nhận được URL và publicId
3. Include trong request body khi submit

### 🔴 Address Handling
- **Option 1**: Chọn address có sẵn (`addressId`)
- **Option 2**: Tạo address mới với full GHN address fields
- Backend sẽ set `TypeAddress.STORE` cho address

### 🔴 GHN Configuration
- Seller cần cấu hình GHN credentials **sau** khi được approve
- `ghnConfigured: true` nghĩa là đã sẵn sàng ship hàng
- Không cần GHN khi đăng ký, chỉ cần khi bắt đầu bán

---

## 10. Testing Checklist

```
□ BUYER đăng ký shop mới → status PENDING
□ BUYER đăng ký với CCCD đã dùng → error 409
□ BUYER đăng ký với tên shop trùng → error 409
□ BUYER check status khi PENDING → hiển thị "Đang chờ duyệt"
□ ADMIN duyệt → shop ACTIVE, user role = SELLER
□ ADMIN từ chối → shop REJECTED, user vẫn BUYER
□ BUYER hủy đơn REJECTED → có thể đăng ký lại
□ SELLER xem shop detail → đầy đủ thông tin
□ SELLER cập nhật shop → thành công
□ SELLER cập nhật GHN credentials → thành công
```
