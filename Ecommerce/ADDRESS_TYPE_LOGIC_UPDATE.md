# Address Type Logic Update

**Date:** December 16, 2025  
**Updated By:** GitHub Copilot

## 🎯 Objective

Update address management logic to properly handle HOME and STORE address types while keeping OTHER and SHIPPING types in enum (for backward compatibility).

---

## 📋 Changes Summary

### 1. **AddressResponseDTO** - Added `typeAddress` Field

**File:** `AddressResponseDTO.java`

**Changes:**
```java
// Added import
import com.PBL6.Ecommerce.constant.TypeAddress;

// Added field
private TypeAddress typeAddress;

// Added getter/setter
public TypeAddress getTypeAddress() { return typeAddress; }
public void setTypeAddress(TypeAddress typeAddress) { this.typeAddress = typeAddress; }
```

**Purpose:** Frontend now receives the address type (HOME/STORE) to handle UI logic accordingly.

---

### 2. **AddressController** - Updated `toDto()` Method

**File:** `AddressController.java`

**Changes:**
```java
private AddressResponseDTO toDto(Address a) {
    // ... existing fields ...
    d.setTypeAddress(a.getTypeAddress()); // NEW LINE
    // ... rest ...
}
```

**Purpose:** Include `typeAddress` in API response.

---

### 3. **AddressService** - Updated Primary Address Logic

**File:** `AddressService.java`

#### 3.1 `createForUser()` Method

**Before:**
```java
if (req.primaryAddress) {
    addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
        .ifPresent(prev -> {
            prev.setPrimaryAddress(false);
            addressRepository.save(prev);
        });
}
```

**After:**
```java
// Primary logic ONLY for HOME addresses
// STORE addresses don't have primary concept (only 1 store address per user)
if (req.primaryAddress) {
    // Unset all other HOME addresses' primary flag
    addressRepository.findByUserId(userId).stream()
            .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME && addr.isPrimaryAddress())
            .forEach(prev -> {
                prev.setPrimaryAddress(false);
                addressRepository.save(prev);
            });
}
```

---

#### 3.2 `updateForUser()` Method

**Before:**
```java
if (req.primaryAddress && !a.isPrimaryAddress()) {
    addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
        .ifPresent(prev -> {
            if (!prev.getId().equals(a.getId())) {
                prev.setPrimaryAddress(false);
                addressRepository.save(prev);
            }
        });
}
```

**After:**
```java
// Primary logic ONLY for HOME addresses
if (req.primaryAddress && !a.isPrimaryAddress() && a.getTypeAddress() == TypeAddress.HOME) {
    // Unset all other HOME addresses' primary flag
    addressRepository.findByUserId(userId).stream()
            .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME 
                         && addr.isPrimaryAddress() 
                         && !addr.getId().equals(a.getId()))
            .forEach(prev -> {
                prev.setPrimaryAddress(false);
                addressRepository.save(prev);
            });
}
```

---

#### 3.3 `markPrimary()` Method

**Before:**
```java
@Transactional
public Address markPrimary(Long userId, Long addressId) {
    Address a = addressRepository.findById(addressId)
            .orElseThrow(() -> new AddressNotFoundException(addressId));
    
    if (!a.getUser().getId().equals(userId)) {
        throw new UnauthorizedAddressAccessException(addressId);
    }

    addressRepository.findFirstByUserIdAndTypeAddress(userId, TypeAddress.HOME)
            .ifPresent(prev -> {
                if (!prev.getId().equals(a.getId())) {
                    prev.setPrimaryAddress(false);
                    addressRepository.save(prev);
                }
            });
    
    a.setPrimaryAddress(true);
    return addressRepository.save(a);
}
```

**After:**
```java
@Transactional
public Address markPrimary(Long userId, Long addressId) {
    Address a = addressRepository.findById(addressId)
            .orElseThrow(() -> new AddressNotFoundException(addressId));
    
    if (!a.getUser().getId().equals(userId)) {
        throw new UnauthorizedAddressAccessException(addressId);
    }

    // Primary concept ONLY applies to HOME addresses
    // STORE addresses cannot be marked as primary (only 1 store per user)
    if (a.getTypeAddress() != TypeAddress.HOME) {
        throw new IllegalArgumentException("Chỉ có địa chỉ HOME mới có thể được đặt làm mặc định. Địa chỉ STORE không cần đánh dấu mặc định.");
    }

    // Unset all other HOME addresses' primary flag
    addressRepository.findByUserId(userId).stream()
            .filter(addr -> addr.getTypeAddress() == TypeAddress.HOME 
                         && addr.isPrimaryAddress() 
                         && !addr.getId().equals(a.getId()))
            .forEach(prev -> {
                prev.setPrimaryAddress(false);
                addressRepository.save(prev);
            });
    
    a.setPrimaryAddress(true);
    return addressRepository.save(a);
}
```

**Key Change:** Now throws exception if trying to mark STORE address as primary.

---

## 🎨 Business Rules

### HOME Address Type
- ✅ User can have **multiple** HOME addresses
- ✅ User can mark **one** HOME address as primary (default)
- ✅ When setting new primary, old primary HOME is automatically unmarked
- ✅ `primaryAddress` flag is meaningful and functional

### STORE Address Type
- ✅ User (seller) has **only ONE** STORE address
- ❌ `primaryAddress` concept does NOT apply to STORE
- ❌ Cannot call `POST /api/me/addresses/{id}/primary` on STORE address
- ✅ If attempted, API returns: `IllegalArgumentException` with message

### OTHER & SHIPPING Types
- ⚠️ **Kept in enum for backward compatibility**
- ⚠️ Not actively used in UI
- ⚠️ Existing data with these types won't break
- ℹ️ Can be cleaned up in future migration

---

## 📡 API Changes

### Response Format Update

**Before:**
```json
{
  "status": 200,
  "message": "Lấy danh sách địa chỉ thành công",
  "data": [
    {
      "id": 1,
      "fullAddress": "123 Nguyen Van A",
      "provinceId": 202,
      "provinceName": "Hồ Chí Minh",
      // ... other fields ...
      "primaryAddress": true
      // typeAddress NOT included
    }
  ]
}
```

**After:**
```json
{
  "status": 200,
  "message": "Lấy danh sách địa chỉ thành công",
  "data": [
    {
      "id": 1,
      "fullAddress": "123 Nguyen Van A",
      "provinceId": 202,
      "provinceName": "Hồ Chí Minh",
      // ... other fields ...
      "primaryAddress": true,
      "typeAddress": "HOME"  // ← NEW FIELD
    }
  ]
}
```

---

### Error Handling

**New Error Case:**

**Request:**
```http
POST /api/me/addresses/5/primary
Authorization: Bearer {token}
```

**Response (if address #5 is STORE type):**
```json
{
  "status": 400,
  "message": "Chỉ có địa chỉ HOME mới có thể được đặt làm mặc định. Địa chỉ STORE không cần đánh dấu mặc định.",
  "data": null
}
```

---

## 🔧 Frontend Integration

### Required Frontend Changes

#### 1. **Update Type Definitions**

```typescript
// types/address.ts
export interface Address {
  id: number;
  fullAddress: string;
  provinceId: number;
  districtId: number;
  wardCode: string;
  provinceName: string;
  districtName: string;
  wardName: string;
  contactName: string;
  contactPhone: string;
  primaryAddress: boolean;
  typeAddress: 'HOME' | 'STORE' | 'SHIPPING' | 'OTHER'; // NEW
  createdAt: string;
}
```

---

#### 2. **Update UI Components**

**Address Card Component:**

```jsx
// Before
<button onClick={() => setAsPrimary(address.id)}>
  Đặt làm mặc định
</button>

// After - Only show for HOME addresses
{address.typeAddress === 'HOME' && (
  <button onClick={() => setAsPrimary(address.id)}>
    Đặt làm mặc định
  </button>
)}
```

---

#### 3. **Update useAddress Hook**

```javascript
// useAddress.js

const handleSetPrimary = useCallback(async (addressId) => {
  setActionLoading(addressId);
  try {
    const address = addresses.find(a => a.id === addressId);
    
    // Validate: only HOME can be primary
    if (address?.typeAddress !== 'HOME') {
      toast.warning('Chỉ địa chỉ HOME mới có thể đặt làm mặc định');
      return { success: false, error: 'Invalid address type' };
    }
    
    const response = await setAsPrimary(addressId);
    // ... rest of logic ...
  } catch (error) {
    // Handle error
  } finally {
    setActionLoading(null);
  }
}, [addresses]);
```

---

#### 4. **Filter Addresses by Type**

```javascript
// Filter HOME addresses
const homeAddresses = addresses.filter(addr => addr.typeAddress === 'HOME');

// Get STORE address (should be only 1)
const storeAddress = addresses.find(addr => addr.typeAddress === 'STORE');

// Get primary HOME address
const primaryHome = homeAddresses.find(addr => addr.primaryAddress);
```

---

## ✅ Validation & Testing

### Test Cases

#### TC1: Create HOME Address with Primary
```http
POST /api/me/addresses
{
  "fullAddress": "123 ABC",
  "provinceId": 202,
  "districtId": 1450,
  "wardCode": "21012",
  "contactName": "Nguyen Van A",
  "contactPhone": "0912345678",
  "primaryAddress": true
}
```

**Expected:**
- New HOME address created
- `typeAddress` = "HOME"
- `primaryAddress` = true
- Any existing primary HOME → `primaryAddress` = false

---

#### TC2: Try to Mark STORE Address as Primary
```http
POST /api/me/addresses/5/primary
```

**Expected:**
- Status: 400 Bad Request
- Error: "Chỉ có địa chỉ HOME mới có thể được đặt làm mặc định..."

---

#### TC3: Get All Addresses
```http
GET /api/me/addresses
```

**Expected Response:**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "typeAddress": "HOME",
      "primaryAddress": true,
      ...
    },
    {
      "id": 2,
      "typeAddress": "HOME",
      "primaryAddress": false,
      ...
    },
    {
      "id": 3,
      "typeAddress": "STORE",
      "primaryAddress": false,
      ...
    }
  ]
}
```

---

#### TC4: Update HOME Address to Primary
```http
PUT /api/me/addresses/2
{
  "fullAddress": "456 XYZ",
  "provinceId": 202,
  "districtId": 1450,
  "wardCode": "21012",
  "contactPhone": "0987654321",
  "primaryAddress": true
}
```

**Expected:**
- Address #2 updated
- Address #2 `primaryAddress` = true
- Old primary HOME (e.g., #1) → `primaryAddress` = false

---

## 🛡️ Backward Compatibility

### Database
- ✅ No schema changes required
- ✅ Existing addresses with OTHER/SHIPPING types remain intact
- ✅ Enum still has 4 values: HOME, STORE, SHIPPING, OTHER

### API
- ✅ All existing endpoints work
- ✅ Only added `typeAddress` field to response (non-breaking)
- ✅ Existing frontend code won't break (new field is optional)

### Migration Path
If needed to clean up OTHER/SHIPPING:
```sql
-- Optional: Migrate OTHER/SHIPPING → HOME
UPDATE addresses 
SET type_address = 'HOME' 
WHERE type_address IN ('OTHER', 'SHIPPING');
```

---

## 🌍 GHN Integration

### ✅ No Impact on GHN

All GHN-related functionality remains unchanged:
- Province/District/Ward data fetching
- Address resolution (name → ID)
- Shipping fee calculation
- Order creation

**Why?** 
- GHN only cares about: `provinceId`, `districtId`, `wardCode`, `fullAddress`
- `typeAddress` is internal to our system
- Primary logic is also internal (for UI default selection)

---

## 📊 Summary

| Aspect | Before | After |
|--------|--------|-------|
| **API Response** | No `typeAddress` | Includes `typeAddress` |
| **Primary Logic** | Generic | HOME-only |
| **STORE Primary** | Allowed (wrong) | ❌ Rejected with error |
| **Enum Values** | 4 types | Still 4 (compatibility) |
| **Frontend** | No type check | Must check `typeAddress` |
| **GHN Impact** | - | ✅ No impact |

---

## 🚀 Deployment Steps

1. **Backend:**
   - ✅ Deploy updated Java classes
   - ✅ No database migration needed
   - ✅ Test endpoints with Postman/Swagger

2. **Frontend:**
   - Update type definitions
   - Update UI components (hide primary button for STORE)
   - Update useAddress hook validation
   - Test address management flows

3. **Verification:**
   - Test creating HOME addresses with primary flag
   - Test trying to mark STORE as primary (should fail)
   - Test GHN integration (province/district/ward)
   - Test shipping fee calculation

---

## 🔗 Related Files

### Backend
- `Address.java` - Entity (no change)
- `AddressRequestDTO.java` - Request DTO (no change)
- `AddressResponseDTO.java` - Response DTO (**changed**)
- `AddressController.java` - Controller (**changed**)
- `AddressService.java` - Service logic (**changed**)
- `TypeAddress.java` - Enum (no change)
- `AddressRepository.java` - Repository (no change)

### Frontend (to be updated)
- `src/services/userService.js`
- `src/hooks/useAddress.js`
- `src/components/*/AddressCard.jsx`
- `src/components/*/AddressForm.jsx`

---

## ✅ Checklist

- [x] Added `typeAddress` to `AddressResponseDTO`
- [x] Updated `AddressController.toDto()` to include `typeAddress`
- [x] Updated `createForUser()` primary logic (HOME-only)
- [x] Updated `updateForUser()` primary logic (HOME-only)
- [x] Updated `markPrimary()` to reject STORE addresses
- [x] Added error message for STORE primary attempt
- [x] Verified GHN integration not affected
- [ ] Frontend: Update type definitions
- [ ] Frontend: Hide primary button for STORE addresses
- [ ] Frontend: Add validation in useAddress hook
- [ ] Test all address CRUD operations
- [ ] Test GHN province/district/ward loading

---

**Status:** ✅ Backend Implementation Complete  
**Next:** Frontend Integration Required
