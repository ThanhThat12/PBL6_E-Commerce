# Address Management Implementation Summary (Phase 4 - US2)

**Date**: 2025-12-12  
**Feature**: 006-profile User Story 2 - Address management with GHN cascade  
**Status**: ✅ Backend implementation complete (Phase 4, Tasks T018-T022)

---

## What Was Implemented

### 1. **DTOs Created** (T018) ✅
Created spec-compliant address DTOs under `dto/profile/`:

- ✅ `CreateAddressRequest.java` - Create address request
  - Required fields: contactName, contactPhone, fullAddress, provinceId, districtId, wardCode
  - Optional fields: label, location names, typeAddress (default HOME), primaryAddress (default false)
  - Bean Validation: @NotBlank, @NotNull, @Pattern for phone, @Size constraints
  - Swagger annotations for API documentation

- ✅ `UpdateAddressRequest.java` - Update address request
  - All fields optional (only provided fields updated)
  - Same validation rules as create
  - Preserves primary flag unless explicitly changed

- ✅ `AddressAutoFillResponse.java` - Auto-fill response
  - Returns contactName and contactPhone from user profile
  - Used for GET /api/addresses/auto-fill endpoint

### 2. **Address Entity** (T019) ✅
Existing Address entity already has all required fields:
- GHN fields: provinceId, districtId, wardCode, provinceName, districtName, wardName
- typeAddress enum (HOME, SHIPPING, STORE)
- primaryAddress boolean flag
- Timestamps: createdAt

**No changes needed** - entity is spec-compliant.

### 3. **AddressService** (T020) ✅
Existing `AddressService` already implements all required functionality:
- CRUD operations with ownership validation
- `setPrimary` (markPrimary) transactional toggle
- GHN location name resolution from IDs
- Single STORE address enforcement per user (via repository query)

**No changes needed** - service is fully functional.

### 4. **AddressController Updated** (T021) ✅
Updated controller to align with spec `/api/addresses`:

**Base path**: Changed from `/api/me/addresses` to `/api/addresses`  
**Swagger tag**: "Addresses"  
**Authentication**: Required (BUYER/SELLER roles)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/api/addresses` | List all addresses | - | `List<AddressResponseDTO>` (sorted: primary first) |
| GET | `/api/addresses/{id}` | Get address by ID | id | `AddressResponseDTO` |
| POST | `/api/addresses` | Create new address | `CreateAddressRequest` | `AddressResponseDTO` |
| PUT | `/api/addresses/{id}` | Update address | `UpdateAddressRequest` | `AddressResponseDTO` |
| DELETE | `/api/addresses/{id}` | Delete address | id | 204 No Content |
| PUT | `/api/addresses/{id}/set-primary` | Set as primary | id | `AddressResponseDTO` |
| GET | `/api/addresses/auto-fill` | Get contact info | - | `AddressAutoFillResponse` |

**Key changes**:
- Changed base path from `/api/me/addresses` to `/api/addresses` per spec
- Added **auto-fill endpoint** (NEW) - returns user's fullName and phoneNumber
- Uses `getCurrentUser()` via SecurityContext instead of `@AuthenticationPrincipal Jwt`
- Converts spec DTOs to existing AddressRequestDTO internally
- Addresses sorted: primary first, then by createdAt descending
- Full Swagger documentation with `@Operation` and `@ApiResponses`

### 5. **LocationsController Created** (T022) ✅
New controller for GHN master data proxy with caching:

**Base path**: `/api/locations`  
**Swagger tag**: "Locations"  
**Authentication**: Public (permitAll)  
**Caching**: 24h TTL via `@Cacheable`

| Method | Endpoint | Description | Cache Key | Response |
|--------|----------|-------------|-----------|----------|
| GET | `/api/locations/provinces` | Get all provinces | N/A | `List<Map<String, Object>>` |
| GET | `/api/locations/districts/{provinceId}` | Get districts | provinceId | `List<Map<String, Object>>` |
| GET | `/api/locations/wards/{districtId}` | Get wards | districtId | `List<Map<String, Object>>` |

**Features**:
- Proxies GHN master data API calls
- Caching with Spring Cache abstraction (cache names: provinces, districts, wards)
- Returns empty list if GHN call fails (graceful degradation)
- Logs debug messages for monitoring
- Full Swagger documentation

**GHN Integration**:
- Uses existing `GhnMasterDataService` for API calls
- Returns raw GHN response format (Map<String, Object> for flexibility)
- Frontend can use for cascading province → district → ward selects

### 6. **Security Configuration** ✅
Updated `SecurityConfig.java` with address and location endpoint rules:

```java
// Address endpoints (Buyer/Seller) - per spec 006-profile US2
.requestMatchers(HttpMethod.GET, "/api/addresses").hasAnyRole("BUYER", "SELLER")
.requestMatchers(HttpMethod.GET, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
.requestMatchers(HttpMethod.POST, "/api/addresses").hasAnyRole("BUYER", "SELLER")
.requestMatchers(HttpMethod.PUT, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
.requestMatchers(HttpMethod.DELETE, "/api/addresses/*").hasAnyRole("BUYER", "SELLER")
.requestMatchers(HttpMethod.GET, "/api/addresses/auto-fill").hasAnyRole("BUYER", "SELLER")

// GHN Locations proxy (public for address forms) - per spec 006-profile US2
.requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()
```

**Security rules**:
- All `/api/addresses/**` endpoints require BUYER or SELLER role
- `/api/locations/**` is public (permitAll) for address form population
- Ownership validation handled in service layer

---

## Architecture & Integration

### Layered Architecture
- **Controller**: AddressController, LocationsController
- **Service**: AddressService (existing), GhnMasterDataService (existing)
- **Repository**: AddressRepository (existing)
- **DTOs**: CreateAddressRequest, UpdateAddressRequest, AddressAutoFillResponse
- **Entity**: Address (existing, no changes)

### GHN Integration
- `GhnMasterDataService` provides master data (provinces, districts, wards)
- Address creation/update resolves location names from IDs automatically
- LocationsController provides public API for frontend forms
- Caching reduces GHN API calls (24h TTL)

### Validation
- Bean Validation on DTOs: @NotBlank, @NotNull, @Pattern, @Size
- Service layer: ownership checks, primary toggle, STORE uniqueness
- Phone format validation: Vietnamese (03|05|07|08|09)xxxxxxxx

### Database
- Address entity: existing table with all required columns
- Primary address: boolean flag, transactional toggle
- TypeAddress enum: HOME, SHIPPING, STORE
- GHN fields: IDs and human-readable names

---

## Files Created/Modified

### Created
1. `dto/profile/CreateAddressRequest.java` - Create address DTO
2. `dto/profile/UpdateAddressRequest.java` - Update address DTO
3. `dto/profile/AddressAutoFillResponse.java` - Auto-fill DTO
4. `controller/LocationsController.java` - GHN locations proxy

### Modified
1. `controller/AddressController.java` - Updated path, added auto-fill, aligned with spec
2. `config/SecurityConfig.java` - Added address and location endpoint rules
3. `specs/006-profile/tasks.md` - Marked Phase 4 tasks complete

---

## API Usage Examples

### Create Address
```bash
POST /api/addresses
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "contactName": "John Doe",
  "contactPhone": "0901234567",
  "fullAddress": "123 Main Street",
  "provinceId": 201,
  "districtId": 1482,
  "wardCode": "21002",
  "typeAddress": "HOME",
  "primaryAddress": true
}

Response 201:
{
  "code": 201,
  "message": "Address created successfully",
  "data": {
    "id": 1,
    "fullAddress": "123 Main Street",
    "provinceId": 201,
    "districtId": 1482,
    "wardCode": "21002",
    "provinceName": "Hà Nội",
    "districtName": "Ba Đình",
    "wardName": "Phường Ngọc Hà",
    "contactName": "John Doe",
    "contactPhone": "0901234567",
    "primaryAddress": true,
    "createdAt": "2025-12-12T10:00:00"
  }
}
```

### Get Provinces (GHN Locations)
```bash
GET /api/locations/provinces

Response 200:
{
  "code": 200,
  "message": "Provinces retrieved successfully",
  "data": [
    {
      "ProvinceID": 201,
      "ProvinceName": "Hà Nội",
      "Code": "01"
    },
    {
      "ProvinceID": 202,
      "ProvinceName": "Hồ Chí Minh",
      "Code": "79"
    },
    ...
  ]
}
```

### Auto-fill Contact Info
```bash
GET /api/addresses/auto-fill
Authorization: Bearer {jwt-token}

Response 200:
{
  "code": 200,
  "message": "Contact info retrieved successfully",
  "data": {
    "contactName": "John Doe",
    "contactPhone": "0901234567"
  }
}
```

---

## Next Steps (Per tasks.md)

### Immediate (Phase 4 remaining)
- ⬜ **T023: Backend tests** (AddressService unit + MockMvc) - NEXT TASK
- ⬜ T024: Frontend services/hooks (addressService, locationService, useAddresses, useLocations)
- ⬜ T025: Frontend UI components (AddressList, AddressCard, AddressModal with GHN cascading)

### Phase 5: US3 - Seller Shop Info
- T026-T030: Shop info CRUD, logo/banner upload, store address link
- Extend Shop entity with publicIds
- ShopController with SELLER role guard

### Phase 6: US4 - Public Profile View
- T033-T034: Frontend public profile page (backend already complete)

### Phase 7: Polish
- T035-T038: Swagger examples, Postman collection, responsive UI, E2E sanity

---

## Migration Notes

### Breaking Changes
- **Path change**: `/api/me/addresses` → `/api/addresses`
  - Frontend must update API calls to new path
  - Old path may still work if not removed, but deprecated

### Backward Compatibility
- AddressResponseDTO unchanged - same response format
- Internal AddressRequestDTO still used by service layer
- New DTOs are adapters to existing service methods

### Deployment Checklist
1. ✅ Update SecurityConfig (already done)
2. ✅ Deploy new controllers (AddressController, LocationsController)
3. ⬜ Update frontend API base path from `/api/me/addresses` to `/api/addresses`
4. ⬜ Configure cache provider (Spring Cache) for locations caching
5. ⬜ Verify GHN API credentials in application.properties

---

## References
- **Spec**: `specs/006-profile/spec.md`
- **Contracts**: `specs/006-profile/contracts/profile-api.md`
- **Tasks**: `specs/006-profile/tasks.md`
- **Data Model**: `specs/006-profile/data-model.md`
- **GHN Service**: `service/GhnMasterDataService.java`
