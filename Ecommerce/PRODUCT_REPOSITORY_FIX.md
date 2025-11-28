# Fix: ProductRepository Query Issues

## Problem
The JPQL queries in `ProductRepository.java` were using the `new` keyword (constructor expressions) with aggregate functions like `SUM()`, `AVG()`, and `COALESCE()`. This is not supported in JPQL and causes errors.

## Root Cause
JPQL constructor expressions (`new ClassName(...)`) cannot directly use aggregate functions in their parameters. This is a limitation of JPQL specification.

## Solution
Converted the problematic queries to use **native SQL queries** with **interface projections**:

### 1. Created Interface Projection
**File**: `AdminListProductProjection.java`
- Created an interface with getter methods matching the SQL column aliases
- Spring Data JPA automatically implements this interface to map native query results

### 2. Updated Repository Methods
**File**: `ProductRepository.java`
- Changed from JPQL with `new` constructor to native SQL queries
- Changed return type from `Page<AdminListProductDTO>` to `Page<AdminListProductProjection>`
- Added `nativeQuery = true` to all four affected queries:
  - `findAllProductsForAdmin()`
  - `findProductsByCategory()`
  - `findProductsByStatus()`
  - `findAllProductsForAdminWithSearch()`

### 3. Updated Service Layer
**File**: `AdminProductService.java`
- Added `convertToDTO()` method to convert projections to DTOs
- Updated all methods to map projection results to DTOs using `.map(this::convertToDTO)`

## Technical Details

### Native SQL Query Structure
```sql
SELECT p.id AS productId,
       p.name AS productName,
       p.main_image AS mainImage,
       c.name AS categoryName,
       p.base_price AS basePrice,
       COALESCE(SUM(pv.stock), 0) AS totalStock,
       p.is_active AS isActive,
       COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN oi.quantity ELSE 0 END), 0) AS sales,
       COALESCE(AVG(pr.rating), 0.0) AS rating
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN product_variants pv ON pv.product_id = p.id
LEFT JOIN order_items oi ON oi.variant_id = pv.id
LEFT JOIN orders o ON oi.order_id = o.id
LEFT JOIN product_reviews pr ON pr.product_id = p.id
GROUP BY p.id, p.name, p.main_image, c.name, p.base_price, p.is_active
```

### Interface Projection Pattern
```java
public interface AdminListProductProjection {
    Long getProductId();
    String getProductName();
    // ... other getters matching SQL aliases
}
```

### Service Layer Conversion
```java
private AdminListProductDTO convertToDTO(AdminListProductProjection projection) {
    return new AdminListProductDTO(
        projection.getProductId(),
        projection.getProductName(),
        // ... other fields
    );
}
```

## Benefits
1. ✅ **Works with aggregate functions**: Native SQL supports complex aggregations
2. ✅ **Type-safe**: Interface projections provide compile-time type checking
3. ✅ **Clean separation**: Repository returns projections, service converts to DTOs
4. ✅ **Performance**: Native SQL can be optimized by the database
5. ✅ **Maintainable**: Clear separation of concerns

## Files Modified
1. `AdminListProductProjection.java` (NEW)
2. `ProductRepository.java` (MODIFIED)
3. `AdminProductService.java` (MODIFIED)

## Testing Recommendations
Test all four affected endpoints:
1. GET `/api/admin/products` - Get all products
2. GET `/api/admin/products/search?name={name}` - Search products
3. GET `/api/admin/products/category/{categoryName}` - Filter by category
4. GET `/api/admin/products/status/{status}` - Filter by status

Verify that:
- Pagination works correctly
- Aggregate values (totalStock, sales, rating) are calculated correctly
- NULL values are handled properly (COALESCE)
- Performance is acceptable
