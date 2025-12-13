# Admin Dashboard API Implementation

## Overview
Created 4 APIs for admin dashboard statistics matching the frontend Dashboard.jsx requirements.

## APIs Created

### 1. Sales Overview
**Endpoint:** `GET /api/admin/dashboard/stats`

**Response:**
```json
{
  "totalRevenue": 2847532.50,
  "totalOrders": 14358,
  "activeCustomers": 8924,
  "conversionRate": 94.2,
  "revenueGrowth": 24.7,
  "ordersGrowth": 32.1,
  "customersGrowth": 18.6,
  "conversionGrowth": 3.2
}
```

**Metrics:**
- Total Revenue: Sum of all COMPLETED orders
- Total Orders: Total count of all orders
- Active Customers: Count of activated users with USER role
- Conversion Rate: (Completed orders / Total orders) * 100
- Growth percentages: Comparison of last 30 days vs previous 30 days

---

### 2. Sales by Category
**Endpoint:** `GET /api/admin/dashboard/sales-by-category`

**Response:**
```json
[
  {
    "categoryName": "Electronics",
    "totalSales": 145000.00,
    "orderCount": 523
  },
  {
    "categoryName": "Fashion",
    "totalSales": 98000.00,
    "orderCount": 412
  }
]
```

**Data:**
- Category Name
- Total Sales: Sum of order amounts for that category (COMPLETED orders)
- Order Count: Number of COMPLETED orders containing products from that category

---

### 3. Top Selling Products
**Endpoint:** `GET /api/admin/dashboard/top-products?limit=10`

**Query Parameters:**
- `limit` (optional, default: 10): Number of top products to return

**Response:**
```json
[
  {
    "productId": 1,
    "productName": "iPhone 15 Pro Max",
    "categoryName": "Electronics",
    "mainImage": "https://...",
    "quantitySold": 2847,
    "totalRevenue": 2847000.00,
    "status": "Active"
  }
]
```

**Sorting:** By quantity sold (descending)

**Data:**
- Product ID, Name, Category, Main Image
- Quantity Sold: Total quantity from COMPLETED orders
- Total Revenue: Sum of (price * quantity) from COMPLETED orders
- Status: Active/Inactive based on product.isActive

---

### 4. Recent Orders
**Endpoint:** `GET /api/admin/dashboard/recent-orders?limit=10`

**Query Parameters:**
- `limit` (optional, default: 10): Number of recent orders to return

**Response:**
```json
[
  {
    "orderId": 2024001,
    "customerName": "Alexander Chen",
    "customerEmail": "alexander.chen@email.com",
    "orderDate": "2024-10-13T10:30:00",
    "totalAmount": 2459.99,
    "status": "COMPLETED"
  }
]
```

**Sorting:** By order creation date (descending - newest first)

**Data:**
- Order ID
- Customer Name & Email
- Order Date (createdAt)
- Total Amount
- Order Status (PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED)

---

## Files Created/Modified

### DTOs Created:
1. ✅ `AdminDashboardStatsDTO.java` - Sales overview data
2. ✅ `SalesByCategoryDTO.java` - Category sales breakdown
3. ✅ `TopSellingProductDTO.java` - Top products data
4. ✅ `RecentOrderDTO.java` - Recent orders data

### Services Implemented:
- ✅ `AdminDashboardService.java`
  - `getDashboardStats()` - Sales overview with growth calculations
  - `getSalesByCategory()` - Category sales breakdown
  - `getTopSellingProducts(limit)` - Top selling products
  - `getRecentOrders(limit)` - Recent orders

### Controllers Implemented:
- ✅ `AdminDashboardController.java`
  - All 4 endpoints with proper documentation
  - `@PreAuthorize("hasRole('ADMIN')")` - Admin access only

### Repository Methods Added:

**OrderRepository:**
- `calculateRevenueByDateRange(startDate, endDate)` - Revenue by date range
- `countByCreatedAtBetween(startDate, endDate)` - Orders count by date range
- `findTopSellingProducts(pageable)` - Top products with sales data
- `findRecentOrders(pageable)` - Recent orders sorted by date

**UserRepository:**
- `countByRoleAndCreatedAtBetween(role, startDate, endDate)` - Users count by role and date

**CategoryRepository:**
- `getCategoriesWithStats()` - Categories with sales statistics

**AdminCategoryStatsDTO:**
- Added new constructor for sales by category data
- Added fields: categoryName, totalRevenue, orderCount

---

## Security
All endpoints require **ADMIN role** authentication via `@PreAuthorize("hasRole('ADMIN')")`

---

## Testing
Test with Postman or any API client:
1. Login as admin to get JWT token
2. Add token to Authorization header: `Bearer {token}`
3. Call endpoints:
   - `GET http://localhost:8080/api/admin/dashboard/stats`
   - `GET http://localhost:8080/api/admin/dashboard/sales-by-category`
   - `GET http://localhost:8080/api/admin/dashboard/top-products?limit=10`
   - `GET http://localhost:8080/api/admin/dashboard/recent-orders?limit=10`

---

## Frontend Integration
Update `Dashboard.jsx` to fetch real data:

```javascript
import { getDashboardStats, getSalesByCategory, getTopSellingProducts, getRecentOrders } from '../../../services/adminDashboardService';

useEffect(() => {
  const fetchData = async () => {
    try {
      const [stats, categories, products, orders] = await Promise.all([
        getDashboardStats(),
        getSalesByCategory(),
        getTopSellingProducts(10),
        getRecentOrders(10)
      ]);
      
      setStatsData(stats);
      setCategoryData(categories);
      setTopProducts(products);
      setRecentOrders(orders);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    }
  };
  
  fetchData();
}, []);
```

Create `src/services/adminDashboardService.js`:
```javascript
import api from '../api';

export const getDashboardStats = async () => {
  const response = await api.get('/admin/dashboard/stats');
  return response.data;
};

export const getSalesByCategory = async () => {
  const response = await api.get('/admin/dashboard/sales-by-category');
  return response.data;
};

export const getTopSellingProducts = async (limit = 10) => {
  const response = await api.get('/admin/dashboard/top-products', { params: { limit } });
  return response.data;
};

export const getRecentOrders = async (limit = 10) => {
  const response = await api.get('/admin/dashboard/recent-orders', { params: { limit } });
  return response.data;
};
```

---

## Status
✅ All 4 APIs implemented
✅ All DTOs created
✅ Service layer complete
✅ Controller layer complete
✅ Repository methods added
✅ No compilation errors
✅ Ready for testing
