# Fix: TypeAddress Enum Conversion Error

## 🐛 Error
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "No enum constant com.PBL6.Ecommerce.constant.TypeAddress.",
  "status": 500
}
```

**Root Cause**: Database có địa chỉ với `type_address` = NULL hoặc empty string, JPA không thể convert sang enum TypeAddress.

## ✅ Solutions Applied

### 1. Entity Level Protection (Address.java)

**Default Value:**
```java
@Enumerated(EnumType.STRING)
@Column(name = "type_address", length = 50, nullable = false)
private TypeAddress typeAddress = TypeAddress.HOME; // Default to HOME
```

**PostLoad Hook:**
```java
@PostLoad
public void handleNullTypeAddress() {
    if (this.typeAddress == null) {
        this.typeAddress = TypeAddress.HOME;
    }
}
```

### 2. Database Fix (SQL Script)

Run this SQL to fix existing NULL values:

```sql
-- Check how many NULL values exist
SELECT COUNT(*) FROM addresses WHERE type_address IS NULL OR type_address = '';

-- Update NULL to 'HOME'
UPDATE addresses 
SET type_address = 'HOME' 
WHERE type_address IS NULL OR type_address = '';

-- Verify
SELECT type_address, COUNT(*) as count 
FROM addresses 
GROUP BY type_address;
```

**Location**: `d:/Proj_Nam4/PBL6_E-Commerce/Ecommerce/sql/fix_null_type_address.sql`

### 3. Service Validation (AddressService.java)

Already validates on create/update:
```java
TypeAddress type = TypeAddress.HOME;
if (req.typeAddress != null && !req.typeAddress.isBlank()) {
    try {
        type = TypeAddress.valueOf(req.typeAddress.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Loại địa chỉ không hợp lệ. Chỉ chấp nhận: HOME hoặc STORE");
    }
}
```

## 📋 Steps to Fix

### Step 1: Run SQL Script
```bash
# Connect to MySQL
mysql -u root -p your_database

# Run the fix script
source d:/Proj_Nam4/PBL6_E-Commerce/Ecommerce/sql/fix_null_type_address.sql
```

Or run directly:
```sql
UPDATE addresses 
SET type_address = 'HOME' 
WHERE type_address IS NULL OR type_address = '';
```

### Step 2: Restart Spring Boot Application
```bash
cd d:/Proj_Nam4/PBL6_E-Commerce/Ecommerce
mvn clean install
mvn spring-boot:run
```

### Step 3: Test API
```bash
# Test GET /api/me/addresses
curl -X GET "http://localhost:8080/api/me/addresses" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Expected response:
```json
{
  "status": 200,
  "message": "Lấy danh sách địa chỉ thành công",
  "data": [
    {
      "id": 1,
      "typeAddress": "HOME",
      "fullAddress": "...",
      "primaryAddress": true,
      ...
    }
  ]
}
```

## 🔍 Verification

### Check Database
```sql
-- All addresses should have valid type_address
SELECT id, user_id, type_address, full_address 
FROM addresses 
WHERE type_address IS NULL OR type_address = '';
-- Should return 0 rows

-- Check distribution
SELECT type_address, COUNT(*) as count 
FROM addresses 
GROUP BY type_address;
-- Should only show 'HOME' and 'STORE'
```

### Check Application Logs
Look for these logs after restart:
```
✅ No errors about enum conversion
✅ Addresses loading successfully
✅ API /api/me/addresses returns 200
```

## 🚨 Prevention

### For Future Address Creation
Always provide `typeAddress` in API requests:

**Example 1: Create HOME address**
```json
POST /api/me/addresses
{
  "fullAddress": "123 Main St",
  "provinceId": 1,
  "districtId": 10,
  "wardCode": "001",
  "typeAddress": "HOME",
  "primaryAddress": true
}
```

**Example 2: Create STORE address**
```json
POST /api/me/addresses
{
  "fullAddress": "Shop Address",
  "provinceId": 1,
  "districtId": 10,
  "wardCode": "001",
  "typeAddress": "STORE",
  "primaryAddress": false
}
```

### Database Constraint (Optional)
Add NOT NULL constraint to prevent future NULL values:
```sql
ALTER TABLE addresses 
MODIFY COLUMN type_address VARCHAR(50) NOT NULL DEFAULT 'HOME';
```

## 📊 Valid TypeAddress Values

| Value | Description | Business Rules |
|-------|-------------|----------------|
| `HOME` | Buyer's delivery address | Can have multiple, one primary |
| `STORE` | Seller's warehouse/shop | Only one per seller, no primary |

## 🔧 Troubleshooting

### If error persists after SQL fix:

1. **Clear JPA Cache**
   ```bash
   # Restart application
   mvn spring-boot:run
   ```

2. **Check Entity Mapping**
   ```java
   // Verify Address.java has:
   @Enumerated(EnumType.STRING)
   @Column(name = "type_address", length = 50, nullable = false)
   private TypeAddress typeAddress = TypeAddress.HOME;
   ```

3. **Check Database Connection**
   ```bash
   # Verify you're updating the correct database
   mysql -u root -p -e "SELECT DATABASE();"
   ```

4. **Verify SQL Update**
   ```sql
   -- Check if update worked
   SELECT type_address, COUNT(*) 
   FROM addresses 
   GROUP BY type_address;
   ```

### If specific user still getting error:

```sql
-- Find problematic addresses for a user
SELECT id, user_id, type_address, full_address, created_at
FROM addresses
WHERE user_id = YOUR_USER_ID
ORDER BY id;

-- Fix specific user's addresses
UPDATE addresses 
SET type_address = 'HOME' 
WHERE user_id = YOUR_USER_ID 
  AND (type_address IS NULL OR type_address = '');
```

## 📝 Summary of Changes

| File | Change | Purpose |
|------|--------|---------|
| `Address.java` | Added `@PostLoad` hook | Handle null values on load |
| `Address.java` | Set default value `= TypeAddress.HOME` | Prevent null on new entities |
| `Address.java` | Changed column to `nullable = false` | Enforce NOT NULL in JPA |
| `fix_null_type_address.sql` | SQL update script | Fix existing NULL data |

---

**Status**: ✅ Fixed  
**Date**: 2025-12-14  
**Impact**: Prevents "No enum constant" error when loading addresses  
**Action Required**: Run SQL script to fix existing data
