# Status Update Summary - ACTIVE/PENDING/INACTIVE

## Changes Made

### Database Status Values (Shop.java)
```java
public enum ShopStatus {
    PENDING,   // Shop chờ duyệt
    ACTIVE,    // Shop đang hoạt động
    INACTIVE   // Shop bị vô hiệu hóa
}
```

### Status Mapping

| Database | Frontend Display | CSS Class | Icon |
|----------|------------------|-----------|------|
| ACTIVE | Active | status-verified | ✓ Green |
| PENDING | Pending | status-pending | ⏰ Yellow |
| INACTIVE | Inactive | status-suspended | ✗ Red |

### Backend Updates

#### SellerStatsDTO.java
```java
- verifiedSellers → activeSellers
- suspendedSellers → inactiveSellers
```

#### UserService.java
```java
// Updated countByStatus queries
- shopRepository.countByStatus("VERIFIED") → countByStatus("ACTIVE")
- shopRepository.countByStatus("SUSPENDED") → countByStatus("INACTIVE")
```

### Frontend Updates

#### SellersTable.jsx
```javascript
// Stats display
{ title: 'Active', value: stats.activeSellers, color: 'green' }
{ title: 'Inactive', value: stats.inactiveSellers, color: 'red' }

// Filter dropdown
<option value="ACTIVE">Active</option>
<option value="INACTIVE">Inactive</option>

// Status class mapping
case 'ACTIVE': return 'status-verified';
case 'INACTIVE': return 'status-suspended';
```

#### SellerDetailModal.jsx
```javascript
// Status select options
<option value="ACTIVE">Active</option>
<option value="INACTIVE">Inactive</option>

// Display function
getStatusDisplay(status) // Converts ACTIVE → Active
```

### API Documentation
Updated `SELLER_MANAGEMENT_APIs.md` with new status values:
- All references changed from VERIFIED → ACTIVE
- All references changed from SUSPENDED → INACTIVE

## Summary

✅ Database uses: `ACTIVE`, `PENDING`, `INACTIVE`  
✅ API returns: `ACTIVE`, `PENDING`, `INACTIVE`  
✅ Frontend displays: `Active`, `Pending`, `Inactive`  
✅ All mappings updated consistently across backend and frontend  
✅ Documentation updated  

No breaking changes - only renaming for consistency with database schema.
