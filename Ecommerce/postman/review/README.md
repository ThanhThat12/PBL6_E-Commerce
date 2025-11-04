# 📮 Postman Collection - Product Reviews API

## 📦 Files Created

- **Product_Reviews_API.postman_collection.json** - Complete CRUD API collection
- **Review_API_Local.postman_environment.json** - Local development environment

---

## 🚀 Quick Start

### Step 1: Import to Postman

#### Option A: Using Postman Desktop
1. Open Postman Desktop
2. Click **Import** button (top left)
3. Select files:
   - `Product_Reviews_API.postman_collection.json`
   - `Review_API_Local.postman_environment.json`
4. Click **Import**

#### Option B: Drag & Drop
1. Open Postman
2. Drag both JSON files into Postman window
3. Files will be imported automatically

---

### Step 2: Setup Environment

1. Click **Environments** (left sidebar)
2. Select **"Review API - Local Development"**
3. Set as **Active Environment** (checkmark icon)

---

### Step 3: Get Product & Order IDs

**Run SQL script first** to create test data:

```bash
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce\sql
mysql -u root -p123456789 ecommerce_v2 < create_test_data_lmao.sql
```

**Copy IDs from SQL output:**
```
📦 RESOURCES CREATED
Shop ID: <shop_id>
Product ID: <product_id>    ← Copy this
Order ID: <order_id>          ← Copy this
```

**Update Environment Variables:**
1. Click **Environments** → **Review API - Local Development**
2. Set values:
   - `product_id` = <your_product_id>
   - `order_id` = <your_order_id>
   - `shop_id` = <your_shop_id>
3. Click **Save**

---

### Step 4: Start Spring Boot

```bash
cd d:\AuthTest\PBL6_E-Commerce\Ecommerce
mvn spring-boot:run
```

Wait for: `Started EcommerceApplication in X seconds`

---

### Step 5: Test API Flow

#### 5.1 Login (Get JWT Token)
1. Open collection: **Product Reviews API - CRUD**
2. Folder: **0. Authentication**
3. Request: **Login as Buyer (lmao)**
4. Click **Send**

✅ **Expected Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

🎯 **JWT token auto-saved** to `{{jwt_token}}` variable!

---

#### 5.2 Create Review
1. Folder: **2. Customer - Create Review**
2. Request: **Create Review - 5 Stars**
3. Check request body:
   ```json
   {
     "orderId": {{order_id}},
     "rating": 5,
     "comment": "Bóng rổ chất lượng tuyệt vời!...",
     "images": []
   }
   ```
4. Click **Send**

✅ **Expected Response:**
```json
{
  "status": "success",
  "message": "Review created successfully",
  "data": {
    "id": 1,
    "rating": 5,
    "comment": "Bóng rổ chất lượng tuyệt vời!...",
    "productId": 123,
    "userId": 4,
    "userName": "lmao",
    "createdAt": "2025-10-31T...",
    ...
  }
}
```

🎯 **Review ID auto-saved** to `{{review_id}}` variable!

---

#### 5.3 Get Product Reviews (Public)
1. Folder: **1. Customer - Read Reviews**
2. Request: **Get Product Reviews (Public)**
3. Click **Send** (no auth needed)

✅ **Expected Response:**
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "rating": 5,
        "comment": "Bóng rổ chất lượng tuyệt vời!...",
        "userName": "lmao",
        ...
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

#### 5.4 Update Review
1. Folder: **3. Customer - Update Review**
2. Request: **Update Review - Change Rating & Comment**
3. Modify body:
   ```json
   {
     "rating": 4,
     "comment": "[CẬP NHẬT] Sau 1 tuần sử dụng...",
     "images": []
   }
   ```
4. Click **Send**

✅ **Expected:** 200 OK with updated review

---

#### 5.5 Get My Reviews
1. Folder: **1. Customer - Read Reviews**
2. Request: **Get My Reviews**
3. Click **Send**

✅ **Expected:** List of all your reviews

---

#### 5.6 Delete Review
1. Folder: **4. Customer - Delete Review**
2. Request: **Delete Review**
3. Click **Send**

✅ **Expected:** 204 No Content

---

### Step 6: Test Seller Features

#### 6.1 Login as Seller
1. Folder: **0. Authentication**
2. Request: **Login as Seller (seller1)**
3. Click **Send**

Credentials:
- Username: `seller1`
- Password: `seller123`

---

#### 6.2 View Shop Reviews
1. Folder: **5. Seller - Manage Reviews**
2. Request: **Get Shop Reviews**
3. Click **Send**

✅ **Expected:** All reviews for seller's products

---

#### 6.3 Get Shop Statistics
1. Request: **Get Shop Review Statistics**
2. Click **Send**

✅ **Expected Response:**
```json
{
  "status": "success",
  "data": {
    "totalReviews": 5,
    "averageRating": 4.2,
    "ratingDistribution": {
      "oneStar": 0,
      "twoStars": 1,
      "threeStars": 1,
      "fourStars": 2,
      "fiveStars": 1
    }
  }
}
```

---

#### 6.4 Add Seller Response
1. Request: **Add Seller Response to Review**
2. Modify body:
   ```json
   {
     "sellerResponse": "Cảm ơn bạn đã mua hàng! 🏀"
   }
   ```
3. Click **Send**

✅ **Expected:** 200 OK with updated review including seller response

---

## 📚 Collection Structure

```
📮 Product Reviews API - CRUD
├── 0. Authentication (2 requests)
│   ├── Login as Buyer (lmao)
│   └── Login as Seller (seller1)
│
├── 1. Customer - Read Reviews (2 requests)
│   ├── Get Product Reviews (Public)
│   └── Get My Reviews
│
├── 2. Customer - Create Review (3 requests)
│   ├── Create Review - 5 Stars
│   ├── Create Review - 4 Stars with Images
│   └── Create Review - 3 Stars (Medium)
│
├── 3. Customer - Update Review (2 requests)
│   ├── Update Review - Change Rating & Comment
│   └── Update Review - Add Images
│
├── 4. Customer - Delete Review (1 request)
│   └── Delete Review
│
├── 5. Seller - Manage Reviews (4 requests)
│   ├── Get Shop Reviews
│   ├── Get Shop Review Statistics
│   ├── Add Seller Response to Review
│   └── Update Seller Response
│
└── 6. Error Cases (5 requests)
    ├── Create Review - Unauthorized (No Token)
    ├── Create Review - Invalid Rating
    ├── Create Review - Duplicate Review
    ├── Update Review - Not Owner
    └── Delete Review - Review Not Found
```

**Total: 19 API requests**

---

## 🎯 Environment Variables

The collection uses these variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | API base URL | `http://localhost:8080/api` |
| `jwt_token` | JWT Bearer Token | Auto-saved from login |
| `product_id` | Product ID for testing | Get from SQL script output |
| `order_id` | Order ID for testing | Get from SQL script output |
| `review_id` | Review ID | Auto-saved after creating review |
| `shop_id` | Shop ID for seller APIs | Get from SQL script output |

---

## ✅ Features Included

### Customer Features (BUYER Role)
- ✅ View product reviews (public, no auth)
- ✅ View my reviews (authenticated)
- ✅ Create review (requires completed order)
- ✅ Update review (own reviews only)
- ✅ Delete review (own reviews only)
- ✅ Upload images with review
- ✅ Pagination & sorting
- ✅ Filter by rating

### Seller Features (SELLER Role)
- ✅ View all shop reviews
- ✅ Get review statistics (total, average, distribution)
- ✅ Add response to customer review
- ✅ Update seller response
- ✅ Pagination & filtering

### Auto-Features
- ✅ JWT token auto-saved after login
- ✅ Review ID auto-saved after creation
- ✅ Product rating auto-updated (database triggers)
- ✅ Shop rating auto-updated (database triggers)

---

## 🐛 Testing Error Cases

The collection includes 5 error test cases:

1. **401 Unauthorized** - No JWT token
2. **400 Bad Request** - Invalid rating (> 5)
3. **400 Bad Request** - Duplicate review
4. **403 Forbidden** - Update other user's review
5. **404 Not Found** - Review doesn't exist

Test these in folder: **6. Error Cases**

---

## 🔧 Troubleshooting

### Issue: "jwt_token variable is empty"
**Solution:**
1. Run **Login as Buyer** request first
2. Check response has `accessToken`
3. Token should auto-save to `{{jwt_token}}`

---

### Issue: "Product not found"
**Solution:**
1. Run SQL script: `create_test_data_lmao.sql`
2. Copy `product_id` from output
3. Update environment variable

---

### Issue: "Order not found or not completed"
**Solution:**
1. Verify order exists: `SELECT * FROM orders WHERE id = <order_id>`
2. Check order status: `status = 'COMPLETED'`
3. Check order belongs to user: `user_id = 4`

---

### Issue: "Cannot review - outside 7-day window"
**Solution:**
1. Check order date: `SELECT created_at FROM orders WHERE id = <order_id>`
2. Order must be within 7 days
3. Re-run SQL script to create new order

---

### Issue: "User has already reviewed this product"
**Solution:**
1. Each user can only review a product once
2. Use **Update Review** instead
3. Or delete existing review first

---

## 📖 API Documentation

For detailed validation rules and business logic, see:
- **REVIEWS_IMPLEMENTATION_COMPLETE.md**
- **SELLER_REVIEW_MANAGEMENT_PHASE2.md**
- **TEST_DATA_LMAO_GUIDE.md**

---

## 🎉 Success Checklist

Before testing, ensure:

- ✅ Postman Desktop installed (v10+)
- ✅ Collection imported successfully
- ✅ Environment imported and activated
- ✅ SQL script executed (test data created)
- ✅ `product_id` and `order_id` set in environment
- ✅ Spring Boot application running
- ✅ Database `ecommerce_v2` exists
- ✅ User `lmao` exists (id=4, role=2)
- ✅ Order status = COMPLETED

---

## 🚀 Quick Test Flow (5 minutes)

1. **Import** collection + environment → 30 seconds
2. **Run SQL script** → 10 seconds
3. **Update** environment variables → 20 seconds
4. **Start** Spring Boot → 30 seconds
5. **Login** as buyer → 5 seconds
6. **Create** review → 5 seconds
7. **Get** product reviews → 5 seconds
8. **Update** review → 5 seconds
9. **Login** as seller → 5 seconds
10. **Add** seller response → 5 seconds

**Total: ~2 minutes** ⚡

---

**🎯 Ready to test! Happy testing! 🏀**
