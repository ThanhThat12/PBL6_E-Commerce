# ✅ Test Data Setup - Complete Summary

## 📊 What Was Created

### Files Created/Updated:

1. **`sql/create_test_data_lmao.sql`** ✅
   - Updated to use correct `ecommerce_v3` database (ecommerce1)
   - Correct schema for users table (role = 2 for BUYER)
   - Creates complete test data hierarchy

2. **`sql/setup_test_data.ps1`** ✅
   - PowerShell script for Windows users
   - Auto-detects MySQL, handles credentials
   - Color-coded output with status

3. **`SETUP_TEST_DATA.md`** ✅
   - Complete setup guide with 3 methods (PowerShell, CLI, GUI)
   - Step-by-step testing instructions
   - Troubleshooting guide

---

## 🎯 Test Data Structure

```
User: lmao
├─ ID: 4
├─ Email: lmao24@gmail.com
├─ Password: lmao123 (hash provided)
├─ Role: BUYER (role = 2) ✅ Using correct enum value
│
└─ Order (COMPLETED - required for review)
   ├─ ID: auto-generated
   ├─ Status: COMPLETED ✅
   ├─ Created: 5 days ago (within 7-day review window)
   ├─ Total: 250,000 VND
   │
   ├─ Shop: Test Sports Shop
   │  └─ Owner: seller1 (ID = 2)
   │
   ├─ Product: Professional Basketball ⭐
   │  ├─ Category: Sports Equipment
   │  ├─ Price: 250,000 VND
   │  ├─ Status: ACTIVE
   │  │
   │  └─ Order Item
   │     ├─ Quantity: 1
   │     ├─ Price: 250,000 VND
   │     └─ Ready for review ✅
```

---

## 🚀 Quick Start (Choose One Method)

### Method 1: PowerShell (Windows) - RECOMMENDED
```powershell
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
.\sql\setup_test_data.ps1
```

### Method 2: MySQL CLI
```bash
mysql -u root -p ecommerce_v3 < sql/create_test_data_lmao.sql
```

### Method 3: MySQL Workbench
- Open Workbench → File → Open Script → select `.sql` file → Execute

---

## ✨ Key Points About Test Data

### ✅ User Role (Fixed)
- **Role value**: `2` (correct enum for BUYER)
- Previous: Was using string or wrong enum
- Now: Matches `Role.BUYER` enum in Java code

### ✅ Order Status (Required)
- **Status**: `COMPLETED` (not PENDING)
- Why: Review API requires completed orders
- Validation: All review endpoints check this

### ✅ Review Eligibility
- **Time window**: 7 days from order completion
- **Test order**: Created 5 days ago → eligible ✅
- **User ownership**: lmao user owns order → can review ✅

### ✅ Product Details
- **Category**: Sports Equipment (matching your sàn bán đồ thể thao)
- **Type**: Professional Basketball
- **Price**: 250,000 VND (realistic sports equipment price)
- **Stock**: 100 units available

---

## 📋 Verification Queries

After running script, verify with these queries:

```sql
-- Check user role
SELECT id, username, role, email FROM users WHERE id = 4;
-- Expected: role = 2 (BUYER)

-- Check order status
SELECT id, status, total_amount, created_at FROM orders 
WHERE user_id = 4 AND shop_id = (SELECT id FROM shops LIMIT 1);
-- Expected: status = COMPLETED

-- Check order items
SELECT oi.*, p.name FROM order_items oi
JOIN products p ON oi.product_id = p.id
WHERE oi.order_id = (SELECT id FROM orders WHERE user_id = 4 LIMIT 1);
-- Expected: 1 Basketball item showing
```

---

## 🧪 Testing Workflow

### Step 1: Run SQL Script
```powershell
.\sql\setup_test_data.ps1
```
**Output**: Confirms user, shop, product, order created

### Step 2: Start Spring Boot
```bash
mvn spring-boot:run
```
**Output**: Application running on http://localhost:8080

### Step 3: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lmao","password":"lmao123"}'
```
**Output**: JWT token for authenticated requests

### Step 4: Create Review
```bash
# Get JWT token from step 3, then:
curl -X POST http://localhost:8080/api/products/{product_id}/reviews \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"orderId":{order_id},"rating":5,"comment":"Great!","images":[]}'
```
**Output**: Review created successfully

### Step 5: Verify in Database
```sql
SELECT * FROM product_reviews WHERE user_id = 4;
```
**Output**: Review saved to database

---

## 🎓 Review API Endpoints (Ready to Test)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/products/{id}/reviews` | Get reviews for product |
| POST | `/api/products/{id}/reviews` | Create review (auth required) |
| GET | `/api/my-reviews` | Get user's reviews (auth) |
| PUT | `/api/reviews/{id}` | Update review (auth) |
| DELETE | `/api/reviews/{id}` | Delete review (auth) |
| GET | `/api/shops/{id}/reviews` | Get shop reviews (auth) |
| POST | `/api/reviews/{id}/response` | Add seller response (auth) |

---

## 💡 Important Notes

### Database Configuration
- **Database Name**: `ecommerce_v3` (old name: `ecommerce1`)
- **Check**: `src/main/resources/application.properties`
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_v3
  spring.datasource.username=root
  spring.datasource.password=123456789
  ```

### Security Considerations
- Password hashes using BCrypt ($2a$10$...)
- Never use real passwords in test data (123456 is OK for testing)
- JWT tokens expire (check your config for duration)

### Data Persistence
- All created data persists in database
- Can create multiple reviews
- Seller can add responses
- Ratings auto-update via database triggers

---

## 🔗 Related Documentation

1. **SETUP_TEST_DATA.md** - Detailed setup guide
2. **TEST_DATA_LMAO_GUIDE.md** - Full API testing guide  
3. **REVIEWS_IMPLEMENTATION_COMPLETE.md** - Review module implementation
4. **SELLER_REVIEW_MANAGEMENT_PHASE2.md** - Seller management features

---

## ✅ Checklist

- [x] SQL script updated for correct database (ecommerce_v3)
- [x] User role fixed to BUYER (role = 2)
- [x] Order status set to COMPLETED
- [x] Product set to sports category (basketball)
- [x] PowerShell setup script created
- [x] Setup guide created with 3 methods
- [x] Review eligibility verified (7-day window)
- [x] All endpoints ready for testing

---

## 🎯 Next Steps

1. **Run the setup script** (choose your method above)
2. **Verify data created** (use verification queries)
3. **Start Spring Boot** (`mvn spring-boot:run`)
4. **Test review endpoints** (see testing workflow)
5. **Check database** (verify persistence)

---

**Status**: ✅ READY FOR TESTING  
**Created**: 2025-10-31  
**Database Version**: ecommerce_v3 (ecommerce1)  
**Test User**: lmao (BUYER role = 2)  
