# Seller Registration Feature - Phase 1 (COD Only)

## Overview
This feature allows **BUYER** (role=2) to apply to become a **SELLER** (role=1) through admin approval workflow.

## Changes Made

### 1. Database/Entity Changes (`Shop.java`)
- Added contact fields: `shopPhone`, `shopEmail`
- Added KYC fields: `idCardNumber`, `idCardFrontUrl`, `idCardBackUrl`, `selfieWithIdUrl`, `idCardName`
- Added payment fields: `acceptCod` (default: true), `codFeePercentage` (default: 2%)
- Added review tracking: `submittedAt`, `reviewedAt`, `reviewedBy`, `rejectionReason`
- Added rating fields: `rating`, `reviewCount`
- Updated `ShopStatus` enum: added `REJECTED`, `SUSPENDED`, `CLOSED`

### 2. New DTOs
- `SellerRegistrationRequestDTO` - For buyer submitting registration
- `RegistrationStatusDTO` - Response with masked sensitive data
- `PendingApplicationDTO` - For admin viewing full KYC data
- `AdminApprovalDTO` - Admin approve request
- `AdminRejectionDTO` - Admin reject request with reason
- `SellerRegistrationResponseDTO` - Generic response wrapper

### 3. New Service (`SellerRegistrationService.java`)
- `submitRegistration()` - Buyer submits application, creates PENDING shop
- `getRegistrationStatus()` - Get current registration status
- `approveRegistration()` - Admin approves, upgrades user to SELLER
- `rejectRegistration()` - Admin rejects with reason
- `cancelRejectedApplication()` - Allow resubmission after rejection
- `getPendingApplications()` - Admin gets list of pending apps
- `searchPendingApplications()` - Search by name/email/phone

### 4. Updated Controllers
**ShopController** - New buyer endpoints:
- `POST /api/seller/register` - Submit registration
- `GET /api/seller/registration/status` - Check status
- `DELETE /api/seller/registration` - Cancel rejected application
- `GET /api/seller/registration/can-submit` - Check eligibility

**AdminSellerRegistrationController** - New admin endpoints:
- `GET /api/admin/seller-registrations/pending` - List pending
- `GET /api/admin/seller-registrations/search?keyword=` - Search
- `GET /api/admin/seller-registrations/pending/count` - Count for badge
- `GET /api/admin/seller-registrations/{shopId}` - Get detail
- `POST /api/admin/seller-registrations/approve` - Approve
- `POST /api/admin/seller-registrations/reject` - Reject

### 5. Security Config Updates
- Added new endpoints with proper role restrictions

### 6. SQL Migration
- `sql/V1__seller_registration_phase1.sql` - Add new columns

---

## API Usage Examples

### BUYER: Submit Registration
```http
POST /api/seller/register
Authorization: Bearer <buyer_jwt_token>
Content-Type: application/json

{
  "shopName": "My Awesome Shop",
  "description": "Selling quality products",
  "shopPhone": "0901234567",
  "shopEmail": "shop@example.com",
  "fullAddress": "123 Main St, District 1",
  "provinceId": 202,
  "districtId": 1454,
  "wardCode": "21012",
  "provinceName": "Hồ Chí Minh",
  "districtName": "Quận 1",
  "wardName": "Phường Bến Nghé",
  "contactPhone": "0901234567",
  "contactName": "Nguyen Van A",
  "idCardNumber": "123456789012",
  "idCardFrontUrl": "https://res.cloudinary.com/xxx/image/upload/v1/kyc/front.jpg",
  "idCardFrontPublicId": "kyc/front",
  "idCardBackUrl": "https://res.cloudinary.com/xxx/image/upload/v1/kyc/back.jpg",
  "idCardBackPublicId": "kyc/back",
  "selfieWithIdUrl": "https://res.cloudinary.com/xxx/image/upload/v1/kyc/selfie.jpg",
  "selfieWithIdPublicId": "kyc/selfie",
  "idCardName": "NGUYEN VAN A"
}
```

**Response (201 Created):**
```json
{
  "statusCode": 201,
  "error": null,
  "message": "Đăng ký bán hàng thành công! Đơn của bạn đang chờ xét duyệt.",
  "data": {
    "success": true,
    "message": "Đăng ký bán hàng thành công! Đơn của bạn đang chờ xét duyệt.",
    "shopId": 15,
    "shopName": "My Awesome Shop",
    "status": "PENDING",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### BUYER: Check Registration Status
```http
GET /api/seller/registration/status
Authorization: Bearer <buyer_jwt_token>
```

### ADMIN: Get Pending Applications
```http
GET /api/admin/seller-registrations/pending?page=0&size=10
Authorization: Bearer <admin_jwt_token>
```

### ADMIN: Approve Application
```http
POST /api/admin/seller-registrations/approve
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "shopId": 15,
  "note": "KYC verified successfully"
}
```

### ADMIN: Reject Application
```http
POST /api/admin/seller-registrations/reject
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "shopId": 15,
  "rejectionReason": "ID card photo is blurry. Please resubmit with clear photos."
}
```

---

## Workflow

1. **BUYER submits registration** → Shop created with status `PENDING`
2. **Admin reviews application** → Views KYC documents
3. **Admin approves/rejects:**
   - **Approve:** Shop status → `ACTIVE`, User role → `SELLER`
   - **Reject:** Shop status → `REJECTED`, User remains `BUYER`
4. **If rejected:** Buyer can delete rejected application and resubmit

---

## Phase 2 (Future)
- Add MoMo payment method
- Add `momoPhone`, `momoName` fields
- Add `acceptMomo` boolean
