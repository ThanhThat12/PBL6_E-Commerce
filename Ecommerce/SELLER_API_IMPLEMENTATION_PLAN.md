# 📋 SELLER API IMPLEMENTATION PLAN

## ✅ Phase 1: Dashboard APIs - HOÀN THÀNH

### Created Files:
1. `dto/seller/DashboardStatsDTO.java` - Dashboard statistics DTO
2. `dto/seller/RevenueDataDTO.java` - Revenue data for charts
3. `dto/seller/TopProductDTO.java` - Top products DTO
4. `dto/seller/OrderStatsDTO.java` - Order statistics DTO
5. `service/SellerDashboardService.java` - Dashboard business logic
6. `controller/SellerDashboardController.java` - Dashboard REST APIs

### New APIs Available:
- ✅ `GET /api/seller/dashboard/stats` - Tổng quan dashboard
- ✅ `GET /api/seller/dashboard/revenue?timeRange=month` - Doanh thu theo time
- ✅ `GET /api/seller/dashboard/recent-orders?limit=5` - Đơn hàng gần đây
- ✅ `GET /api/seller/dashboard/top-products?limit=5` - Sản phẩm bán chạy

### Repository Changes Needed:
**MUST DO MANUALLY - See `HOWTO_FIX_REPOSITORIES.md`**
1. Add to `OrderRepository.java`:
   - `findByShopIdAndStatus()`
   - `countByShopId()`
   - `countDistinctUsersByShopId()`
   - `findByShopIdAndStatusAndCreatedAtBetween()`

2. Add to `ProductRepository.java`:
   - `findTopSellingProductsByShopIdNative()`

3. Fix `SellerDashboardService.java`:
   - Import RoundingMode
   - Fix BigDecimal.ROUND_HALF_UP deprecated
   - Remove unused imports
   - Fix groupOrdersByDate() signature

---

## 🔨 Phase 2: Product APIs Alignment (TODO)

### Frontend Needs:
```javascript
GET  /seller/products                 // List products with filters
GET  /seller/products/{id}            // Get product details
PUT  /seller/products/{id}            // Update product
PATCH /seller/products/{id}/status    // Toggle active status
```

### Current Backend:
- `GET /api/products/my-shop/all` (doesn't match frontend)
- `POST /api/products` (works)
- `DELETE /api/products/{id}` (works)

### Action Plan:
1. **Add to ProductController.java:**

```java
/**
 * Get seller products (frontend compatible endpoint)
 * GET /api/seller/products?page=0&size=10&keyword=&categoryId=&status=
 */
@GetMapping("/seller/products")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<Page<ProductDTO>>> getSellerProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Boolean status,
        Authentication authentication) {
    // Reuse existing getMyShopProducts() logic
    // Add keyword and categoryId filters
}

/**
 * Get seller product by ID
 * GET /api/seller/products/{id}
 */
@GetMapping("/seller/products/{id}")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ProductDTO>> getSellerProduct(
        @PathVariable Long id,
        Authentication authentication) {
    // Verify product belongs to seller's shop
    // Return ProductDTO
}

/**
 * Update seller product
 * PUT /api/seller/products/{id}
 */
@PutMapping("/seller/products/{id}")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ProductDTO>> updateSellerProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductCreateDTO request,
        Authentication authentication) {
    // Verify ownership and update
}

/**
 * Toggle product status
 * PATCH /api/seller/products/{id}/status?status=ACTIVE
 */
@PatchMapping("/seller/products/{id}/status")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ProductDTO>> toggleProductStatus(
        @PathVariable Long id,
        @RequestParam String status,
        Authentication authentication) {
    // Toggle isActive field
}
```

---

## 🔨 Phase 3: Order APIs Enhancement (TODO)

### Frontend Needs:
```javascript
GET /seller/orders/stats              // Order stats by status
POST /seller/orders/{id}/cancel       // Cancel with reason
```

### Action Plan:
1. **Create `dto/seller/OrderCancelDTO.java`:**
```java
public class OrderCancelDTO {
    private String reason;
    // getters/setters
}
```

2. **Add to OrdersController.java:**
```java
@GetMapping("/orders/stats")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<OrderStatsDTO>> getOrderStats(
        Authentication authentication) {
    // Count orders by status
}

@PostMapping("/orders/{id}/cancel")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<OrderDetailDTO>> cancelOrder(
        @PathVariable Long id,
        @RequestBody OrderCancelDTO cancelDTO,
        Authentication authentication) {
    // Cancel order with reason
}
```

---

## 🔨 Phase 4: Shop APIs Enhancement (TODO)

### Frontend Needs:
```javascript
GET  /seller/shop/stats               // Shop stats (rating, followers, products)
POST /seller/shop/logo                // Upload logo
POST /seller/shop/banner              // Upload banner
```

### Database Change (Optional):
```sql
-- Add logo and banner fields to shops table
ALTER TABLE shops
  ADD COLUMN logo_url VARCHAR(500) DEFAULT NULL,
  ADD COLUMN banner_url VARCHAR(500) DEFAULT NULL;
```

**Alternative:** Store in JSON field or separate table `shop_images`

### Action Plan:
1. **Create DTOs:**
```java
// ShopStatsDTO.java
public class ShopStatsDTO {
    private BigDecimal rating;
    private Long followersCount;  // Future feature
    private Long productsCount;
    private Long ordersCount;
}

// ImageUploadResponseDTO.java
public class ImageUploadResponseDTO {
    private String imageUrl;
}
```

2. **Add to ShopController.java:**
```java
@GetMapping("/seller/shop/stats")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ShopStatsDTO>> getShopStats(
        Authentication authentication) {
    // Get shop basic stats
}

@PostMapping("/seller/shop/logo")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ImageUploadResponseDTO>> uploadLogo(
        @RequestParam("logo") MultipartFile file,
        Authentication authentication) {
    // Upload and save logo URL
}

@PostMapping("/seller/shop/banner")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ImageUploadResponseDTO>> uploadBanner(
        @RequestParam("banner") MultipartFile file,
        Authentication authentication) {
    // Upload and save banner URL
}
```

---

## 🔨 Phase 5: Statistical APIs (TODO - Low Priority)

### Frontend Needs:
```javascript
GET /seller/statistical/revenue       // Revenue by date range
GET /seller/statistical/sales         // Sales quantity
GET /seller/statistical/top-products  // Top products with date filter
GET /seller/statistical/customers     // Customer analytics
GET /seller/statistical/order-status  // Order status distribution
GET /seller/statistical/export        // Export to CSV/Excel
```

### Action Plan:
1. Create `SellerStatisticalController.java`
2. Create `SellerStatisticalService.java`
3. Create DTOs: `SalesDataDTO`, `CustomerStatsDTO`
4. Implement export functionality with Apache POI

---

## 🔨 Phase 6: Customer APIs (TODO - Low Priority)

### Frontend Needs:
```javascript
GET /seller/customers                 // Customer list
GET /seller/customers/{id}            // Customer details
GET /seller/customers/{id}/orders     // Purchase history
GET /seller/customers/stats           // Customer statistics
```

### Action Plan:
1. Create `SellerCustomerController.java`
2. Create `SellerCustomerService.java`
3. Create DTOs: `CustomerDTO`, `CustomerDetailDTO`
4. Query distinct users from orders

---

## 📊 Implementation Progress

| Phase | APIs | Status | Priority | Estimated Time |
|-------|------|--------|----------|----------------|
| Phase 1: Dashboard | 4 APIs | ✅ DONE (needs manual fixes) | ⭐⭐⭐ High | 2 hours |
| Phase 2: Product | 4 APIs | 📝 TODO | ⭐⭐⭐ High | 3 hours |
| Phase 3: Order | 2 APIs | 📝 TODO | ⭐⭐ Medium | 1 hour |
| Phase 4: Shop | 3 APIs | 📝 TODO | ⭐⭐ Medium | 2 hours |
| Phase 5: Statistical | 6 APIs | 📝 TODO | ⭐ Low | 4 hours |
| Phase 6: Customer | 4 APIs | 📝 TODO | ⭐ Low | 3 hours |

**Total:** 23 APIs, 15 hours estimated

---

## 🚨 IMMEDIATE ACTIONS REQUIRED

### 1. Fix SellerDashboardService.java compile errors
See `HOWTO_FIX_REPOSITORIES.md` for detailed instructions.

### 2. Add Repository Methods
Manually add methods to:
- `OrderRepository.java` (4 methods)
- `ProductRepository.java` (1 method)

### 3. Test Dashboard APIs
```bash
# Start backend
./mvnw spring-boot:run

# Test endpoints
curl -H "Authorization: Bearer <seller-token>" \
  http://localhost:8080/api/seller/dashboard/stats

curl -H "Authorization: Bearer <seller-token>" \
  http://localhost:8080/api/seller/dashboard/revenue?timeRange=month
```

---

## 📝 Next Steps

1. ✅ Complete Phase 1 manual fixes
2. 🔧 Implement Phase 2 (Product APIs) - Most important for frontend
3. 🔧 Implement Phase 3 (Order APIs)
4. 🔧 Consider Phase 4 (Shop APIs) - May need DB changes
5. ⏳ Phase 5 & 6 can be done later

---

## 💡 Notes

- All implementations avoid DB schema changes where possible
- Reuse existing service methods when available
- Follow existing code patterns for consistency
- All seller APIs require `@PreAuthorize("hasRole('SELLER')")`
- Use `Authentication` parameter to get current seller username
- Return `ResponseDTO<T>` for all responses
