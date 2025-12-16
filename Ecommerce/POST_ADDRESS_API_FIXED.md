# POST /api/me/addresses - API Documentation & Implementation

## 📋 Summary

Đã sửa logic tạo address và cập nhật Swagger documentation chi tiết để frontend hiểu rõ kỳ vọng của API.

### 🔧 Core Fix

**Problem**: Khi tạo address mới với `primaryAddress=true`, API chỉ unset **1** previous primary address thay vì unset **TẤT CẢ** primary addresses cũ, gây lỗi ở frontend.

**Solution**: 
```java
// OLD (Sai - chỉ unset 1):
addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
        .ifPresent(prev -> {
            prev.setPrimaryAddress(false);
            addressRepository.save(prev);
        });

// NEW (Đúng - unset TẤT CẢ):
List<Address> allHomeAddresses = addressRepository.findByUserId(userId).stream()
        .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME && addr.isPrimaryAddress())
        .toList();

for (Address prev : allHomeAddresses) {
    prev.setPrimaryAddress(false);
    addressRepository.save(prev);
}
```

---

## 📊 API Response Examples

### ✅ Case 1: Create PRIMARY HOME Address (Success)

**Request**:
```json
POST /api/me/addresses
Content-Type: application/json
Authorization: Bearer <jwt_token>

{
  "fullAddress": "123 Đường Nguyễn Huệ, Quận 1, TP.HCM",
  "provinceId": 202,
  "districtId": 1442,
  "wardCode": "21308",
  "contactName": "Nguyễn Văn A",
  "contactPhone": "0912345678",
  "typeAddress": "HOME",
  "primaryAddress": true
}
```

**Response** (201 Created):
```json
{
  "status": 201,
  "error": null,
  "message": "Tạo địa chỉ thành công",
  "data": {
    "id": 123,
    "fullAddress": "123 Đường Nguyễn Huệ, Quận 1, TP.HCM",
    "provinceId": 202,
    "districtId": 1442,
    "wardCode": "21308",
    "provinceName": "Ho Chi Minh",
    "districtName": "District 1",
    "wardName": "Ward 1",
    "contactName": "Nguyễn Văn A",
    "contactPhone": "0912345678",
    "primaryAddress": true,
    "typeAddress": "HOME",
    "createdAt": "2025-12-14T10:30:00",
    "updatedAt": "2025-12-14T10:30:00"
  }
}
```

**Auto-effects**:
- ✅ Address created with `primaryAddress=true`
- ✅ **ALL** previous HOME addresses with `primaryAddress=true` → `primaryAddress=false`
- ✅ User now has exactly 1 primary HOME address (the newly created one)

---

### ✅ Case 2: Create Non-Primary HOME Address (Success)

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "456 Đường Tôn Thất Đạm, Quận 4, TP.HCM",
  "provinceId": 202,
  "districtId": 1445,
  "wardCode": "21320",
  "contactName": "Nguyễn Văn B",
  "contactPhone": "0987654321",
  "typeAddress": "HOME",
  "primaryAddress": false
}
```

**Response** (201 Created):
```json
{
  "status": 201,
  "error": null,
  "message": "Tạo địa chỉ thành công",
  "data": {
    "id": 124,
    "fullAddress": "456 Đường Tôn Thất Đạm, Quận 4, TP.HCM",
    "contactName": "Nguyễn Văn B",
    "contactPhone": "0987654321",
    "primaryAddress": false,
    "typeAddress": "HOME",
    "createdAt": "2025-12-14T11:45:00",
    "updatedAt": "2025-12-14T11:45:00"
  }
}
```

**Auto-effects**:
- ✅ Address created with `primaryAddress=false`
- ✅ Existing primary address remains unchanged
- ✅ User can later set this as primary via PUT endpoint

---

### ✅ Case 3: Create STORE Address for Seller (Success)

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "789 Kho hàng - Đường Điện Biên Phủ, Quận 10",
  "provinceId": 202,
  "districtId": 1443,
  "wardCode": "21315",
  "contactName": "Shop Manager",
  "contactPhone": "0966777888",
  "typeAddress": "STORE",
  "primaryAddress": false
}
```

**Response** (201 Created):
```json
{
  "status": 201,
  "error": null,
  "message": "Tạo địa chỉ thành công",
  "data": {
    "id": 125,
    "fullAddress": "789 Kho hàng - Đường Điện Biên Phủ, Quận 10",
    "contactName": "Shop Manager",
    "contactPhone": "0966777888",
    "primaryAddress": false,
    "typeAddress": "STORE",
    "createdAt": "2025-12-14T12:00:00",
    "updatedAt": "2025-12-14T12:00:00"
  }
}
```

**Auto-effects**:
- ✅ STORE address created (will be used as `from_address` for GHN shipments)
- ✅ `primaryAddress` ignored (always false for STORE)
- ⚠️ Seller can now ONLY UPDATE this STORE address, cannot create another one

---

### ❌ Error Case 1: Trying to Create STORE When One Already Exists

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "Kho mới",
  "districtId": 1442,
  "contactPhone": "0912345678",
  "typeAddress": "STORE"
}
```

**Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "IllegalStateException",
  "message": "Bạn chỉ có thể có một địa chỉ cửa hàng. Vui lòng cập nhật địa chỉ hiện tại thay vì tạo mới.",
  "data": null
}
```

**Frontend Action**:
- ❌ Show error toast: "Bạn chỉ có thể có một địa chỉ cửa hàng"
- ❌ Disable "Create STORE" button if seller already has 1 STORE
- ✅ Show "Edit STORE" button instead for seller

---

### ❌ Error Case 2: Trying to Set STORE as Primary

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "789 Kho hàng...",
  "districtId": 1443,
  "contactPhone": "0966777888",
  "typeAddress": "STORE",
  "primaryAddress": true
}
```

**Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "IllegalArgumentException",
  "message": "Địa chỉ STORE không được đánh dấu là địa chỉ mặc định",
  "data": null
}
```

**Frontend Action**:
- ❌ Disable checkbox "Đặt làm mặc định" when typeAddress = "STORE"
- ✅ Show helpful text: "Địa chỉ cửa hàng không cần đặt làm mặc định"

---

### ❌ Error Case 3: Invalid Address Type

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "...",
  "contactPhone": "0912345678",
  "typeAddress": "OFFICE"
}
```

**Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "IllegalArgumentException",
  "message": "Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE",
  "data": null
}
```

**Frontend Action**:
- ❌ Validate typeAddress on client-side before submitting
- ✅ Show dropdown with ONLY: "HOME" or "STORE" options

---

### ❌ Error Case 4: Invalid Phone Format

**Request**:
```json
POST /api/me/addresses

{
  "fullAddress": "...",
  "contactPhone": "12345"
}
```

**Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "MethodArgumentNotValidException",
  "message": "Số điện thoại không hợp lệ",
  "data": null
}
```

**Valid Formats**:
- ✅ `0912345678` (0 + 9 hoặc 10 digits)
- ✅ `0912345678` (0 + 9 hoặc 10 digits)
- ✅ `+84912345678` (+84 + 9 hoặc 10 digits)
- ❌ `12345` (quá ngắn)
- ❌ `+91912345678` (không phải Việt Nam)

---

## 📝 Swagger Documentation Updates

### 1. **POST Endpoint Summary**
```yaml
Operation: Create new address
Summary: Create a new address for the current user
Description: |
  When primaryAddress=true for HOME type, automatically unsets primary flag 
  for ALL other HOME addresses (ensures only one primary per user).
  STORE type addresses cannot be set as primary and limited to 1 per seller.

Security: bearerAuth (JWT required)
```

### 2. **Response Codes**

| Code | Status | Description |
|------|--------|-------------|
| `201` | Created | ✅ Address created successfully. If primaryAddress=true, other HOME addresses automatically unmarked as primary. |
| `400` | Bad Request | ❌ Validation errors (invalid type, STORE limit, phone format, location IDs) |
| `401` | Unauthorized | 🔐 JWT token missing, expired, or invalid |
| `404` | Not Found | ⚠️ User not found in system |

### 3. **Request DTO Fields**

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `fullAddress` | String | ✅ Yes | Full delivery address | "123 Nguyễn Huệ, Quận 1" |
| `provinceId` | Integer | ⚠️ Maybe | GHN Province ID (better accuracy) | 202 |
| `districtId` | Integer | ⚠️ Maybe | GHN District ID (better accuracy) | 1442 |
| `wardCode` | String | ⚠️ Maybe | GHN Ward Code (better accuracy) | "21308" |
| `provinceName` | String | ⚠️ Maybe | Human-readable province (auto-resolved if ID missing) | "Ho Chi Minh" |
| `districtName` | String | ⚠️ Maybe | Human-readable district (auto-resolved if ID missing) | "District 1" |
| `wardName` | String | ⚠️ Maybe | Human-readable ward (auto-resolved if code missing) | "Ward 1" |
| `contactName` | String | ❌ No | Person name for this address | "Nguyễn Văn A" |
| `contactPhone` | String | ✅ Yes | Vietnamese phone (0xxx or +84xx) | "0912345678" |
| `typeAddress` | String | ❌ No | HOME or STORE (default: HOME) | "HOME" |
| `primaryAddress` | Boolean | ❌ No | Set as primary (HOME only, default: false) | true |

### 4. **Response DTO Fields**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `id` | Long | Address unique ID | 123 |
| `fullAddress` | String | Full delivery address | "123 Nguyễn Huệ, Quận 1" |
| `provinceId` | Integer | GHN Province ID | 202 |
| `districtId` | Integer | GHN District ID | 1442 |
| `wardCode` | String | GHN Ward Code | "21308" |
| `provinceName` | String | Human-readable from GHN | "Ho Chi Minh" |
| `districtName` | String | Human-readable from GHN | "District 1" |
| `wardName` | String | Human-readable from GHN | "Ward 1" |
| `contactName` | String | Person name | "Nguyễn Văn A" |
| `contactPhone` | String | Phone number | "0912345678" |
| `primaryAddress` | Boolean | Is primary? | true |
| `typeAddress` | String | HOME or STORE | "HOME" |
| `createdAt` | DateTime | Created timestamp (ISO 8601) | "2025-12-14T10:30:00" |
| `updatedAt` | DateTime | Last update (ISO 8601) | "2025-12-14T14:22:15" |

---

## 🎯 Frontend Implementation Checklist

### ✅ Form Validation
- [ ] `fullAddress`: Required, max 500 characters
- [ ] `contactPhone`: Required, Vietnamese format (0xxx or +84xx, 9-10 digits)
- [ ] `typeAddress`: Dropdown with HOME / STORE only (default: HOME)
- [ ] `primaryAddress`: Show checkbox ONLY when typeAddress = HOME
- [ ] `provinceName`/`districtName`/`wardName`: Optional, will be auto-resolved if IDs provided

### ✅ Error Handling
- [ ] 400 Bad Request: Display error message from API
- [ ] 401 Unauthorized: Redirect to login
- [ ] 404 Not Found: Show "User not found" message
- [ ] Validate STORE limit: Disable "Create STORE" if seller already has 1

### ✅ Primary Address Logic
- [ ] When user sets `primaryAddress=true`, show confirmation: "This will unset other primary addresses"
- [ ] After creation success (201), refresh address list (existing primary should now have `primaryAddress=false`)
- [ ] For STORE addresses, always send `primaryAddress=false` (or omit it)

### ✅ Location Resolution
- [ ] Provide GHN province/district/ward dropdowns (use `/api/ghn/master/*` endpoints)
- [ ] Send both IDs and names for best accuracy
- [ ] If user provides names only, API will auto-resolve IDs and names

### ✅ STORE Address Handling
- [ ] For sellers: Show separate "Địa chỉ cửa hàng" section
- [ ] If seller has 0 STORE: Show "Create STORE" button
- [ ] If seller has 1 STORE: Hide "Create STORE", show "Edit STORE" instead
- [ ] Disable delete button for STORE (shows error when clicked)
- [ ] STORE address used as `from_address` for GHN shipments

---

## 🔄 Request Flow Diagram

```
Frontend POST /api/me/addresses
    ↓
    ├─ Validate required fields (fullAddress, contactPhone)
    ├─ Validate typeAddress (HOME or STORE only)
    ├─ If typeAddress=STORE & primaryAddress=true → Error 400
    ├─ If typeAddress=STORE & seller already has 1 → Error 400
    ├─ Resolve location names via GHN (if not provided)
    ├─ If typeAddress=HOME & primaryAddress=true:
    │  └─ Unset primaryAddress=false for ALL existing HOME addresses
    │
    └─ Save Address with auto @PreUpdate updatedAt
         ↓
    Response 201 Created
         ↓
    Frontend Actions:
    ├─ Show success toast
    ├─ Refresh address list
    ├─ Update "primary address" indicator in other HOME addresses
    └─ Disable "Create STORE" button if this was STORE
```

---

## Files Modified

✅ **AddressService.java**
- Fixed `createForUser()` to unset ALL previous primary HOME addresses

✅ **AddressController.java**
- Added comprehensive `@Operation` documentation
- Added detailed `@ApiResponses` with all error cases
- Added imports for Swagger annotations

✅ **AddressRequestDTO.java**
- Added `@Schema` annotations to all fields
- Enhanced JavaDoc comments with business logic details
- Added examples and descriptions for Swagger

✅ **AddressResponseDTO.java**
- Added `@Schema` annotations to all fields
- Added descriptions and example values

---

## Testing with Postman

### 1. Create Primary HOME
```
POST http://localhost:8080/api/me/addresses
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "fullAddress": "123 Nguyễn Huệ, Q1, TP.HCM",
  "districtId": 1442,
  "wardCode": "21308",
  "contactName": "Nguyễn A",
  "contactPhone": "0912345678",
  "typeAddress": "HOME",
  "primaryAddress": true
}
```

### 2. Create Non-Primary HOME
```
{
  "fullAddress": "456 Tôn Thất Đạm, Q4, TP.HCM",
  "districtId": 1445,
  "wardCode": "21320",
  "contactName": "Nguyễn B",
  "contactPhone": "0987654321",
  "typeAddress": "HOME",
  "primaryAddress": false
}
```

### 3. Get All Addresses (verify primary unset)
```
GET http://localhost:8080/api/me/addresses
Authorization: Bearer <jwt>
```
→ Should show first address has `primaryAddress=false`, second has `primaryAddress=true`

### 4. Try to Create STORE When Already Has One
```
{
  "fullAddress": "Kho mới...",
  "districtId": 1443,
  "contactPhone": "0966777888",
  "typeAddress": "STORE"
}
```
→ Should return 400: "Bạn chỉ có thể có một địa chỉ cửa hàng"

---

## Summary for Frontend Team

**Key Changes**:
1. ✅ API now unsets **ALL** previous primary HOME addresses when creating new primary
2. ✅ Detailed Swagger docs explain auto-primary logic
3. ✅ Error messages clear: STORE limit, STORE cannot be primary, invalid type
4. ✅ DTO fields have examples and descriptions

**Frontend Should**:
- Show "Unset other primary addresses?" confirmation before setting primary=true
- Validate typeAddress on client-side (dropdown: HOME / STORE only)
- Hide primaryAddress checkbox when typeAddress = STORE
- Disable "Create STORE" if seller already has 1 STORE address
- Refresh address list after successful creation

This ensures smooth integration with backend and prevents frontend errors! 🚀
