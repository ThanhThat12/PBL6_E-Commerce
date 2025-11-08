# 🔧 Hướng dẫn thêm code vào các Repository

## 1. ProductRepository.java

**File:** `d:\PBL6_v3\PBL6_E-Commerce\Ecommerce\src\main\java\com\PBL6\Ecommerce\repository\ProductRepository.java`

**Thêm code SAU dòng 99 (trước dấu `}` cuối cùng):**

```java
    // =====================================================
    // SELLER DASHBOARD APIs - Top selling products
    // =====================================================
    
    /**
     * Lấy top sản phẩm bán chạy của shop
     * Sắp xếp theo soldCount DESC
     * Returns Object[] với: [productId, productName, imageUrl, soldCount, revenue, price]
     */
    @Query(value = "SELECT p.id, p.name, p.main_image, p.sold_count, " +
                   "(p.sold_count * p.base_price), p.base_price " +
                   "FROM products p " +
                   "WHERE p.shop_id = :shopId " +
                   "ORDER BY p.sold_count DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findTopSellingProductsByShopIdNative(
        @Param("shopId") Long shopId, 
        @Param("limit") int limit
    );
```

---

## 2. SellerDashboardService.java

**File:** `d:\PBL6_v3\PBL6_E-Commerce\Ecommerce\src\main\java\com\PBL6\Ecommerce\service\SellerDashboardService.java`

**Sửa dòng 161 (method `getTopProducts`):**

**THAY THẾ:**
```java
return productRepository.findTopSellingProductsByShopId(shop.getId(), limit);
```

**BẰNG:**
```java
// Get raw data from native query
List<Object[]> rawData = productRepository.findTopSellingProductsByShopIdNative(
    shop.getId(), limit);

// Convert Object[] to TopProductDTO
return rawData.stream()
    .map(row -> new TopProductDTO(
        ((Number) row[0]).longValue(),      // productId
        (String) row[1],                     // productName
        (String) row[2],                     // imageUrl
        ((Number) row[3]).longValue(),       // soldCount
        (BigDecimal) row[4],                 // revenue
        (BigDecimal) row[5]                  // price
    ))
    .collect(Collectors.toList());
```

---

## 3. Fix BigDecimal.ROUND_HALF_UP deprecated

**File:** `SellerDashboardService.java` dòng 196

**THAY THẾ:**
```java
.divide(prevRevenue, 2, BigDecimal.ROUND_HALF_UP)
```

**BẰNG:**
```java
.divide(prevRevenue, 2, RoundingMode.HALF_UP)
```

**VÀ thêm import:**
```java
import java.math.RoundingMode;
```

---

## 4. Fix unused import

**File:** `SellerDashboardService.java` 

**XÓA dòng import:**
```java
import java.time.LocalDate;
```

---

## 5. Fix unused parameter

**File:** `SellerDashboardService.java` dòng 212

**THAY THẾ:**
```java
private List<RevenueDataDTO> groupOrdersByDate(List<Order> orders, String timeRange) {
```

**BẰNG:**
```java
private List<RevenueDataDTO> groupOrdersByDate(List<Order> orders) {
```

**VÀ cập nhật lời gọi ở dòng 127:**
```java
return groupOrdersByDate(orders);  // Xóa tham số timeRange
```

---

## ✅ Sau khi hoàn thành

Repository methods sẽ hoạt động đúng với Dashboard Service.
