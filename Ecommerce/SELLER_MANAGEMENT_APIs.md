# Seller Management APIs Documentation

## Overview
APIs for managing sellers in the admin dashboard, including listing, filtering, statistics, and detail view.

---

## 1. Get All Sellers
**Endpoint:** `GET /api/admin/users/sellers`

**Description:** Retrieve a list of all sellers with their shop information.

**Authorization:** `ADMIN` role required

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Seller users retrieved successfully",
  "data": [
    {
      "id": 1,
      "shopName": "TechStore",
      "phoneNumber": "+1234567890",
      "email": "seller@techstore.com",
      "totalProducts": 150,
      "status": "ACTIVE",
      "revenue": 45230.50
    }
  ]
}
```

---

## 2. Get Sellers by Status (Filter)
**Endpoint:** `GET /api/admin/users/sellers?status={status}`

**Description:** Retrieve sellers filtered by their shop status.

**Authorization:** `ADMIN` role required

**Parameters:**
- `status` (query, optional): Filter by status
  - `ACTIVE` - Only active sellers
  - `PENDING` - Only pending sellers
  - `INACTIVE` - Only inactive sellers
  - Omit or empty for all sellers

**Example Request:**
```
GET /api/admin/users/sellers?status=ACTIVE
GET /api/admin/users/sellers?status=PENDING
GET /api/admin/users/sellers?status=INACTIVE
GET /api/admin/users/sellers (all sellers)
```

**Response:** Same as Get All Sellers, but filtered by status.

---

## 3. Get Seller Statistics
**Endpoint:** `GET /api/admin/users/sellers/stats`

**Description:** Get statistics about sellers (total, verified, pending, suspended).

**Authorization:** `ADMIN` role required

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "Seller statistics retrieved successfully",
  "data": {
    "totalSellers": 456,
    "activeSellers": 342,
    "pendingSellers": 89,
    "inactiveSellers": 25
  }
}
```

---

## 4. Get Seller Detail
**Endpoint:** `GET /api/admin/users/detail/{userId}`

**Description:** Get detailed information about a specific seller.

**Authorization:** `ADMIN` role required

**Path Parameters:**
- `userId` (Long): The seller's user ID

**Response:**
```json
{
  "status": 200,
  "error": null,
  "message": "User detail retrieved successfully",
  "data": {
    "id": 1,
    "username": "seller_user",
    "fullName": "John Doe",
    "email": "seller@techstore.com",
    "phoneNumber": "+1234567890",
    "role": "SELLER",
    "activated": true,
    "createdAt": "2024-01-10T10:30:00",
    "shopName": "TechStore",
    "shopAddress": "123 Main St, City, Country",
    "shopDescription": "Leading electronics store",
    "shopStatus": "ACTIVE",
    "totalProductsSeller": 150,
    "totalOrdersSeller": 320,
    "totalRevenue": 45230.50
  }
}
```

---

## DTOs

### ListSellerUserDTO
```java
{
  "id": Long,
  "shopName": String,
  "phoneNumber": String,
  "email": String,
  "totalProducts": Integer,
  "status": String, // ACTIVE, PENDING, INACTIVE
  "revenue": Double
}
```

### SellerStatsDTO
```java
{
  "totalSellers": Long,
  "activeSellers": Long,
  "pendingSellers": Long,
  "inactiveSellers": Long
}
```

### AdminUserDetailDTO (Seller fields)
```java
{
  // Common fields
  "id": Long,
  "username": String,
  "fullName": String,
  "email": String,
  "phoneNumber": String,
  "role": String,
  "activated": boolean,
  "createdAt": LocalDateTime,
  
  // Seller-specific fields
  "shopName": String,
  "shopAddress": String,
  "shopDescription": String,
  "shopStatus": String, // ACTIVE, PENDING, INACTIVE
  "totalProductsSeller": Integer,
  "totalOrdersSeller": Integer,
  "totalRevenue": Double
}
```

---

## Status Values

Shop status can be one of:
- `ACTIVE` - Seller is active and can sell products
- `PENDING` - Seller registration is pending approval
- `INACTIVE` - Seller account is inactive/suspended

---

## Frontend Integration

### Service Methods (adminService.js)

```javascript
// Get all sellers
const sellers = await getSellers();

// Get seller statistics
const stats = await getSellerStats();

// Get sellers by status
const activeSellers = await getSellersByStatus('ACTIVE');
const pendingSellers = await getSellersByStatus('PENDING');
const inactiveSellers = await getSellersByStatus('INACTIVE');

// Get seller detail
const sellerDetail = await getSellerDetail(sellerId);
```

### Component Integration (SellersTable.jsx)

The component now:
1. Fetches sellers from API on mount
2. Fetches and displays real-time statistics
3. Filters sellers by status using the status filter dropdown (All/Active/Pending/Inactive)
4. Displays seller details in a modal when clicking "View Details"
5. Shows loading and error states appropriately

---

## Testing

### Using cURL

```bash
# Get all sellers
curl -X GET http://localhost:8081/api/admin/users/sellers \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Get active sellers only
curl -X GET "http://localhost:8081/api/admin/users/sellers?status=ACTIVE" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Get seller statistics
curl -X GET http://localhost:8081/api/admin/users/sellers/stats \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Get seller detail
curl -X GET http://localhost:8081/api/admin/users/detail/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## Implementation Notes

1. **Repository Updates:**
   - Added `countByStatus(String status)` to `ShopRepository`
   - Added `findByStatus(String status)` to `ShopRepository`

2. **Service Updates:**
   - Added `getSellerStats()` method
   - Added `getSellersByStatus(String status)` method
   - Both methods use existing `getSellerUsers()` as a base

3. **Controller Updates:**
   - Modified `getSellerUsers()` to accept optional `status` query parameter
   - Added `getSellerStats()` endpoint

4. **Frontend Updates:**
   - Real-time data fetching from API
   - Status-based filtering
   - Statistics display
   - Error handling and loading states

---

## Future Enhancements

- [ ] Pagination support for large seller lists
- [ ] Search functionality (by name, email, phone)
- [ ] Sorting options (by revenue, products, date)
- [ ] Bulk operations (approve/suspend multiple sellers)
- [ ] Export functionality (CSV, Excel)
- [ ] Update seller status API (approve/suspend)
