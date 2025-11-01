# 🏀 Setup Test Data - Product Review Testing

## Overview
This guide helps you create test data for the **Product Review Module** using user **lmao**.

## ✅ Database Information

- **Database**: `ecommerce1` (or `ecommerce_v3`)
- **User**: `root`
- **Password**: `123456789` (from your application.properties)
- **Host**: `localhost:3306`

## 📝 Test Credentials

```
Username: lmao
Email: lmao24@gmail.com
Password: lmao123
Role: BUYER (role = 2)
```

## 🚀 Method 1: PowerShell Script (Recommended for Windows)

```powershell
# Run from project root directory
.\sql\setup_test_data.ps1
```

**Features:**
- Auto-detects MySQL installation
- Shows progress with colored output
- Displays created credentials
- Error handling

### If password prompt appears:
```powershell
# With custom password
.\sql\setup_test_data.ps1 -MySQLPassword "123456789"
```

---

## 🚀 Method 2: MySQL Command Line

```bash
mysql -u root -p ecommerce_v3 < sql/create_test_data_lmao.sql
```

Or if using Windows PowerShell:
```powershell
Get-Content .\sql\create_test_data_lmao.sql | mysql -u root -p123456789 ecommerce_v3
```

---

## 🚀 Method 3: MySQL Workbench GUI

1. **Open MySQL Workbench**
2. **Connect** to your MySQL server
3. **Open File**: `sql/create_test_data_lmao.sql`
4. **Execute** (Ctrl+Shift+Enter or click ⚡ icon)
5. **View Results** in output panel

---

## ✨ Created Resources

After running the script, these will be created:

| Resource | Value | Notes |
|----------|-------|-------|
| User ID | 4 | Username: lmao |
| Shop | Test Sports Shop | Owner: seller1 (id=2) |
| Product | Professional Basketball | Price: 250,000 VND |
| Category | Sports Equipment | For sports products |
| Order | COMPLETED | Status required for review |
| Order Items | 1x Basketball | 250,000 VND |

## 🔍 Verify Test Data

```sql
-- Check user
SELECT id, username, email, role FROM users WHERE username = 'lmao';

-- Check shop
SELECT id, name, owner_id FROM shops WHERE name = 'Test Sports Shop';

-- Check product
SELECT id, name, base_price FROM products WHERE name = 'Professional Basketball';

-- Check order
SELECT id, user_id, shop_id, status FROM orders 
WHERE user_id = 4 AND status = 'COMPLETED';

-- Check order items
SELECT oi.id, p.name, oi.quantity, oi.price 
FROM order_items oi
JOIN products p ON oi.product_id = p.id
WHERE oi.order_id = (SELECT id FROM orders WHERE user_id = 4 LIMIT 1);
```

---

## 🧪 Test Review API

### 1. Start Application
```bash
mvn spring-boot:run
```

Server will start at: `http://localhost:8080`

### 2. Login (Get JWT Token)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "lmao",
    "password": "lmao123"
  }'
```

**Response** (example):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "user": {
    "id": 4,
    "username": "lmao",
    "role": "BUYER"
  }
}
```

### 3. Get Product Reviews
```bash
# Replace {product_id} with actual ID from script output
curl http://localhost:8080/api/products/{product_id}/reviews
```

### 4. Create Review

```bash
# Replace {product_id} and {order_id} with actual IDs
# Replace <JWT_TOKEN> with token from login response

curl -X POST http://localhost:8080/api/products/{product_id}/reviews \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": {order_id},
    "rating": 5,
    "comment": "Great quality basketball! Perfect for training.",
    "images": []
  }'
```

### 5. Get My Reviews

```bash
curl -X GET http://localhost:8080/api/my-reviews \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 6. Update Review

```bash
# Replace {review_id} with actual review ID from creation response

curl -X PUT http://localhost:8080/api/reviews/{review_id} \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 4,
    "comment": "Good quality, but a bit heavy.",
    "images": []
  }'
```

### 7. Delete Review

```bash
curl -X DELETE http://localhost:8080/api/reviews/{review_id} \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 📚 Additional Resources

- **Full API Guide**: See `TEST_DATA_LMAO_GUIDE.md`
- **Review Module Docs**: See `REVIEWS_IMPLEMENTATION_COMPLETE.md`
- **Seller Management**: See `SELLER_REVIEW_MANAGEMENT_PHASE2.md`

---

## ⚠️ Troubleshooting

### ❌ "Access denied for user 'root'"
- Check password is correct in `application.properties`
- Try: `mysql -u root -p -h localhost`

### ❌ "Database doesn't exist: ecommerce_v3"
- Check `application.properties` for correct DB name
- Run full schema script first if needed

### ❌ "Duplicate entry for key 'username'"
- User already exists (that's OK!)
- Script uses `INSERT IGNORE` to prevent duplicates

### ❌ "Column doesn't exist"
- Make sure you're using the correct database version
- Check schema matches your current Hibernate mapping

---

## 📋 Checklist

- [ ] MySQL server is running
- [ ] Database `ecommerce_v3` exists
- [ ] Run SQL script successfully
- [ ] Verify all resources created (use queries above)
- [ ] Start Spring Boot application
- [ ] Login successfully with credentials
- [ ] Create review via API
- [ ] View reviews in database

---

## 💡 Tips

1. **Keep JWT Token Handy**: Copy it for multiple API calls
2. **Test One Step At A Time**: Start with GET requests before POST
3. **Check Logs**: Spring Boot logs show validation errors
4. **Use Postman**: Import the test data guide into Postman for easy testing
5. **DB Queries**: Use MySQL Workbench to verify data persistence

---

**Last Updated**: 2025-10-31  
**Version**: 1.0  
**Database**: ecommerce_v3 (ecommerce1)
