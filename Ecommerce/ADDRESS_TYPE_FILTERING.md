# Address Type Filtering Implementation

## Overview
Implemented server-side and client-side filtering to separate seller and buyer address views. Buyers see only HOME/SHIPPING addresses (STORE addresses hidden), while sellers can see all address types.

## Technical Implementation

### Backend Changes

#### AddressController.java
Updated the `list()` endpoint to support query parameters:

```java
@GetMapping
public ResponseEntity<ResponseDTO<List<AddressResponseDTO>>> list(
    @RequestParam(required = false) List<String> excludeTypes,
    @RequestParam(required = false) List<String> types)
```

**Query Parameters:**
- `excludeTypes`: Array of type names to exclude (e.g., `?excludeTypes=STORE`)
- `types`: Array of type names to include (e.g., `?types=HOME,SHIPPING`)

**Filtering Logic:**
1. Fetch all user addresses from service layer
2. Filter by `excludeTypes` if provided (removes matching types)
3. Filter by `types` if provided (keeps only matching types)
4. Sort: primary first, then by creation date descending

**Example API Calls:**
```bash
# Buyer view - exclude STORE addresses
GET /addresses?excludeTypes=STORE

# Get only HOME and SHIPPING addresses
GET /addresses?types=HOME,SHIPPING

# Get all addresses (seller view)
GET /addresses
```

### Frontend Changes

#### userService.js
Updated `getAddresses()` to accept filtering options:

```javascript
export const getAddresses = async (options = {}) => {
  const params = {};
  if (options.excludeTypes && options.excludeTypes.length > 0) {
    params.excludeTypes = options.excludeTypes;
  }
  if (options.types && options.types.length > 0) {
    params.types = options.types;
  }
  const responseDTO = await api.get('addresses', { params });
  return responseDTO;
};
```

#### useAddresses.js Hook
Updated `fetchAddresses()` to pass `excludeTypes` to the API:

```javascript
const fetchAddresses = useCallback(async () => {
  console.log('Fetching addresses with excludeTypes:', excludeTypes);
  const response = await getAddresses({ excludeTypes });
  // ... rest of the logic
}, [excludeTypes, normalizeAddresses]);
```

**Hook Initialization:**
The hook accepts an `options` object with `excludeTypes`:

```javascript
const { addresses, loading, fetchAddresses } = useAddresses({ 
  excludeTypes: ['STORE'] 
});
```

#### AddressManagement.jsx Component
Already properly configured with buyer filtering:

```javascript
// Constant to prevent recreation on every render
const BUYER_EXCLUDE_TYPES = ['STORE'];

const {
  addresses,
  loading,
  fetchAddresses,
  // ...
} = useAddresses({ excludeTypes: BUYER_EXCLUDE_TYPES });
```

**Type Badge Display:**
Each address displays its type with a badge:

```jsx
{address.typeAddress && (
  <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs font-semibold rounded uppercase tracking-wide">
    {address.typeAddress}
  </span>
)}
```

## Address Type Enum

**TypeAddress values:**
- `HOME`: Home address
- `SHIPPING`: Shipping address
- `STORE`: Seller's store/warehouse address

## User Experience

### Buyer View
- **Excludes:** STORE addresses (hidden from list)
- **Shows:** HOME, SHIPPING addresses only
- **Type Badge:** Displayed for each address (HOME/SHIPPING)
- **Primary Badge:** "Mặc định" shown for primary address
- **Allows:** Creating duplicate addresses with different types

### Seller View (Dashboard)
- **Shows:** All address types (HOME, SHIPPING, STORE)
- **Type Badge:** Displayed for each address
- **Use Case:** STORE addresses used for GHN pickup locations

### Address Creation/Editing
- **Form Field:** `typeAddress` dropdown with HOME/SHIPPING/STORE options
- **Default Value:** HOME
- **Validation:** Required field
- **Duplicate Handling:** Same physical address allowed with different types

## Data Model

### Address Entity (Key Fields)
```java
public class Address {
    private Long id;
    private String fullAddress;     // Street address
    private String provinceName;
    private String districtName;
    private String wardName;
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    private String contactName;
    private String contactPhone;
    private TypeAddress typeAddress; // HOME, SHIPPING, STORE
    private boolean primaryAddress;  // Single primary per user
    private LocalDateTime createdAt;
    // ...
}
```

### TypeAddress Enum
```java
public enum TypeAddress {
    HOME,
    SHIPPING,
    STORE
}
```

## Testing Checklist

### Backend Tests
- [ ] `GET /addresses` without params returns all addresses
- [ ] `GET /addresses?excludeTypes=STORE` excludes STORE addresses
- [ ] `GET /addresses?types=HOME,SHIPPING` returns only HOME/SHIPPING
- [ ] `GET /addresses?excludeTypes=STORE&types=HOME` returns only HOME addresses
- [ ] Addresses are sorted: primary first, then by createdAt desc
- [ ] Invalid type names are ignored (no 500 error)

### Frontend Tests
- [ ] Buyer view: STORE addresses hidden
- [ ] Seller dashboard: All addresses visible including STORE
- [ ] Type badge displays correctly (HOME/SHIPPING/STORE)
- [ ] Primary badge displays correctly
- [ ] Can create duplicate addresses with different types
- [ ] Edit form properly loads typeAddress field
- [ ] Create form defaults to HOME type

### Integration Tests
- [ ] User switches from seller to buyer view → STORE addresses disappear
- [ ] User switches from buyer to seller view → STORE addresses appear
- [ ] Creating STORE address in seller view → visible in seller view only
- [ ] Setting STORE address as primary → works but hidden in buyer view

## Performance Considerations

### Server-Side Filtering Benefits
1. **Network Efficiency**: Only requested addresses sent to client
2. **Security**: STORE addresses not exposed to buyer context
3. **Consistency**: Single source of truth for filtering logic

### Client-Side Fallback
The `normalizeAddresses()` function still filters by `excludeTypes` client-side for:
- Backward compatibility
- Additional safety layer
- Handling edge cases

## Future Enhancements

### Phase 1 - GHN Integration (From tasks.md)
1. **PickupLocation Entity**: Link STORE addresses to GHN shop configuration
2. **Address Validation**: Validate STORE addresses with GHN location codes
3. **Fee Calculator**: Use STORE address as pickup location for shipping fees
4. **Shipment Creation**: Use STORE address for GHN order creation

### Phase 2 - Advanced Features
1. **Address Templates**: Quick create based on existing addresses
2. **Address Verification**: Google Maps API integration
3. **Bulk Operations**: Import/export addresses (seller dashboard)
4. **Address History**: Track changes and usage

## Related Documentation

- [specs/001-ghn-address-spec/spec.md](../specs/001-ghn-address-spec/spec.md) - Full feature specification
- [specs/001-ghn-address-spec/plan.md](../specs/001-ghn-address-spec/plan.md) - Implementation plan
- [specs/001-ghn-address-spec/data-model.md](../specs/001-ghn-address-spec/data-model.md) - Entity definitions
- [specs/001-ghn-address-spec/contracts/api-contracts.md](../specs/001-ghn-address-spec/contracts/api-contracts.md) - API specifications
- [specs/001-ghn-address-spec/tasks.md](../specs/001-ghn-address-spec/tasks.md) - Implementation tasks (93 tasks)

## Implementation Status

✅ **Completed:**
- Backend query parameter support (excludeTypes, types)
- Frontend service layer (getAddresses with options)
- Frontend hook (useAddresses with excludeTypes)
- Frontend component (AddressManagement with BUYER_EXCLUDE_TYPES)
- Type badge display
- Primary address sorting

⏳ **Testing Required:**
- End-to-end buyer/seller view switching
- Edge cases (null types, invalid type names)
- Performance with large address lists

📋 **Pending (From tasks.md):**
- 93 tasks for full GHN integration
- Phase 1: Setup (7 tasks)
- Phase 2: Foundational (25 tasks)
- Phase 3: US3 Address Validation (13 tasks)
- Phase 4: US1 Fee Calculation (13 tasks)
- Phase 5: US2 Shipment Creation (27 tasks)
- Phase 6: Polish & Docs (8 tasks)

## Notes

### Design Decisions

1. **Why Server-Side Filtering?**
   - Security: Prevent exposing STORE addresses to buyer context
   - Performance: Reduce payload size
   - Consistency: Single source of truth

2. **Why Allow Duplicate Addresses Across Types?**
   - Same physical location may have different purposes (home vs shipping vs store)
   - Type badge provides clear visual distinction
   - No confusion with primary address badge

3. **Why Primary Address is Global?**
   - Single primary per user (not per type)
   - Simplifies checkout flow
   - User can set any address (any type) as primary

4. **Why Client-Side Filtering Remains?**
   - Backward compatibility
   - Additional safety layer
   - Handles edge cases gracefully

### Known Limitations

1. **No Role-Based Access Control (RBAC):**
   - Filtering is UI-driven, not permission-based
   - All users can create any address type
   - Future: Add role check (SELLER role required for STORE type)

2. **No Address Type Transition:**
   - Cannot convert HOME → STORE via edit
   - Must delete and recreate
   - Future: Add type conversion API

3. **No Address Usage Tracking:**
   - Cannot see which orders used which address
   - Cannot prevent deletion of in-use addresses
   - Future: Add usage history and soft delete

## Contact

For questions or issues, refer to:
- Feature specification: `specs/001-ghn-address-spec/`
- Implementation tasks: `specs/001-ghn-address-spec/tasks.md`
- API documentation: `specs/001-ghn-address-spec/contracts/api-contracts.md`
