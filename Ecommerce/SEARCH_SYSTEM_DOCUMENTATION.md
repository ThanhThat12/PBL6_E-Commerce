# Hệ Thống Tìm Kiếm - Documentation

> **Tài liệu tổng hợp tất cả các tính năng tìm kiếm đã được implement**  
> Cập nhật: 01/12/2025

---

## 📋 Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Kiến Trúc Hệ Thống](#2-kiến-trúc-hệ-thống)
3. [Backend APIs](#3-backend-apis)
4. [Frontend Components](#4-frontend-components)
5. [Database Schema](#5-database-schema)
6. [Tính Năng Chi Tiết](#6-tính-năng-chi-tiết)
7. [Hướng Dẫn Sử Dụng](#7-hướng-dẫn-sử-dụng)

---

## 1. Tổng Quan

### Các Tính Năng Đã Implement

| Phase | Tính Năng | Trạng Thái |
|-------|-----------|------------|
| Phase 1 | Search Suggestions (Gợi ý tìm kiếm) | ✅ Hoàn thành |
| Phase 1 | Product Search with Filters | ✅ Hoàn thành |
| Phase 2 | Search History (Lịch sử tìm kiếm) | ✅ Hoàn thành |
| Phase 2 | Trending Searches (Tìm kiếm xu hướng) | ✅ Hoàn thành |
| Phase 2 | Faceted Search | ✅ Hoàn thành |
| Phase 3 | Shop Search (Tìm kiếm shop) | ✅ Hoàn thành |
| Phase 3 | Search by Shop Name | ✅ Hoàn thành |
| Phase 3 | Simplified SearchBar (Enter/Click only) | ✅ Hoàn thành |
| Phase 3 | Redis Caching | ⏳ Pending |
| Phase 3 | Analytics Dashboard | ⏳ Pending |

---

## 2. Kiến Trúc Hệ Thống

### Backend Structure

```
src/main/java/com/PBL6/Ecommerce/
├── controller/
│   └── SearchController.java          # REST endpoints cho search
├── service/
│   ├── SearchService.java             # Interface
│   └── impl/SearchServiceImpl.java    # Business logic
├── repository/
│   ├── ProductRepository.java         # Product queries (bao gồm shop name search)
│   ├── ShopRepository.java            # Shop queries
│   ├── SearchHistoryRepository.java   # User search history
│   └── TrendingSearchRepository.java  # Trending searches
├── dto/
│   ├── SearchSuggestionDTO.java       # Suggestion response (products, shops, trending)
│   ├── FacetedSearchResponseDTO.java  # Faceted search với filters
│   └── SearchHistoryDTO.java          # User history
└── entity/
    ├── SearchHistory.java             # Lịch sử tìm kiếm
    └── TrendingSearch.java            # Xu hướng tìm kiếm
```

### Frontend Structure

```
src/
├── components/
│   ├── common/Navbar/
│   │   └── SearchBar.jsx              # Thanh tìm kiếm (simplified)
│   └── shop/
│       └── ShopCard.jsx               # Card hiển thị shop
├── pages/
│   ├── products/
│   │   ├── ProductListPage.jsx        # Trang danh sách sản phẩm + shop results
│   │   └── ProductDetailPage.jsx      # Chi tiết sản phẩm (có shop info)
│   └── shops/
│       └── ShopDetailPage.jsx         # Trang chi tiết shop
└── services/
    └── searchService.js               # API calls cho search
```

---

## 3. Backend APIs

### 3.1 Search Suggestions

```http
GET /api/search/suggestions?q={query}&limit={limit}
```

**Response:**
```json
{
  "products": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "highlightedName": "<strong>iPhone</strong> 15 Pro",
      "imageUrl": "https://...",
      "price": 25990000,
      "category": "Điện thoại"
    }
  ],
  "shops": [
    {
      "id": 1,
      "name": "Apple Store VN",
      "highlightedName": "<strong>Apple</strong> Store VN",
      "logoUrl": "https://...",
      "productCount": 150,
      "rating": 4.8
    }
  ],
  "trending": ["iPhone", "Samsung", "Laptop Gaming"],
  "history": ["iPhone 15", "Macbook Pro"]
}
```

### 3.2 Search Shops

```http
GET /api/search/shops?q={query}&limit={limit}
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "TechShop VN",
    "highlightedName": "<strong>Tech</strong>Shop VN",
    "logoUrl": "https://...",
    "productCount": 250,
    "rating": 4.7
  }
]
```

### 3.3 Faceted Search

```http
GET /api/search/faceted?q={query}&category={id}&minPrice={price}&maxPrice={price}&rating={rating}&sort={sortBy}&page={page}&size={size}
```

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| q | String | Từ khóa tìm kiếm (tên sản phẩm HOẶC tên shop) |
| category | Long | ID danh mục |
| minPrice | Double | Giá tối thiểu |
| maxPrice | Double | Giá tối đa |
| rating | Double | Đánh giá tối thiểu |
| sort | String | Sắp xếp: `price_asc`, `price_desc`, `newest`, `popular` |
| page | Integer | Trang (mặc định: 0) |
| size | Integer | Số item/trang (mặc định: 20) |

**Response:**
```json
{
  "products": {
    "content": [...],
    "totalElements": 150,
    "totalPages": 8
  },
  "facets": {
    "categories": [
      {"id": 1, "name": "Điện thoại", "count": 50}
    ],
    "priceRanges": [
      {"min": 0, "max": 5000000, "count": 30},
      {"min": 5000000, "max": 10000000, "count": 45}
    ],
    "ratings": [
      {"rating": 5, "count": 20},
      {"rating": 4, "count": 35}
    ]
  },
  "totalResults": 150
}
```

### 3.4 Search History

```http
GET /api/search/history?limit={limit}
Authorization: Bearer {token}
```

```http
DELETE /api/search/history/{id}
Authorization: Bearer {token}
```

```http
DELETE /api/search/history
Authorization: Bearer {token}
```

### 3.5 Trending Searches

```http
GET /api/search/trending?limit={limit}
```

**Response:**
```json
["iPhone 15", "Samsung Galaxy", "Laptop Gaming", "Tai nghe", "Đồng hồ thông minh"]
```

### 3.6 Record Search (Lưu lịch sử)

```http
POST /api/search/record?q={query}
Authorization: Bearer {token} (optional)
```

### 3.7 Shop Detail (Public)

```http
GET /api/shops/{id}
```

---

## 4. Frontend Components

### 4.1 SearchBar (Simplified)

**File:** `src/components/common/Navbar/SearchBar.jsx`

**Tính năng:**
- ✅ Chỉ tìm kiếm khi nhấn Enter hoặc click nút tìm kiếm
- ✅ Không có dropdown gợi ý tự động
- ❌ Đã xóa voice search
- ❌ Không có autocomplete khi gõ

**Code Flow:**
```
User gõ từ khóa → Nhấn Enter/Click → Navigate to /products?search={query}
```

### 4.2 ProductListPage (Search Results)

**File:** `src/pages/products/ProductListPage.jsx`

**Tính năng:**
- ✅ Hiển thị sản phẩm matching từ khóa
- ✅ Hiển thị shops matching từ khóa (section riêng)
- ✅ Tìm kiếm bao gồm cả tên shop
- ✅ Filters: category, price, rating
- ✅ Sorting: giá, mới nhất, phổ biến

### 4.3 ShopCard

**File:** `src/components/shop/ShopCard.jsx`

**Hiển thị:**
- Logo shop
- Tên shop
- Rating (sao)
- Số lượng sản phẩm
- Link đến shop detail

### 4.4 ShopDetailPage

**File:** `src/pages/shops/ShopDetailPage.jsx`

**Route:** `/shops/:shopId`

**Tính năng:**
- Header với thông tin shop
- Grid sản phẩm của shop
- Pagination

### 4.5 ProductDetailPage (Cải tiến)

**File:** `src/pages/products/ProductDetailPage.jsx`

**Thay đổi:**
- ✅ Thêm Shop Info Card nổi bật (logo, tên, rating, số SP, địa điểm)
- ✅ Click shop card → điều hướng đến `/shops/:id`
- ❌ Đã xóa nút "Mua ngay"
- ❌ Đã xóa nút Wishlist (trái tim)
- ✅ Cải thiện phần thông tin chi tiết với link shop

---

## 5. Database Schema

### 5.1 Search History Table

```sql
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_keyword ON search_history(keyword);
```

### 5.2 Trending Search Table

```sql
CREATE TABLE trending_search (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(255) NOT NULL UNIQUE,
    search_count BIGINT DEFAULT 0,
    last_searched TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trending_search_count ON trending_search(search_count DESC);
CREATE INDEX idx_trending_keyword ON trending_search(keyword);
```

---

## 6. Tính Năng Chi Tiết

### 6.1 Tìm Kiếm Theo Tên Shop

**ProductRepository.java** - Query JPQL đã được cập nhật:

```java
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.category " +
       "LEFT JOIN FETCH p.shop " +
       "WHERE p.isActive = true " +
       "AND p.status = 'APPROVED' " +
       "AND (:name IS NULL OR :name = '' " +
       "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(p.shop.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       // ... other filters
       )
Page<Product> findProductsWithFilters(...);
```

**Kết quả:** Khi user tìm "TechShop", sẽ trả về tất cả sản phẩm có:
- Tên sản phẩm chứa "TechShop" HOẶC
- Thuộc shop có tên chứa "TechShop"

### 6.2 Trending Search Algorithm

```java
// Tăng count mỗi khi có search
@Transactional
public void recordSearch(String keyword, Long userId) {
    // 1. Lưu vào history (nếu có user)
    if (userId != null) {
        searchHistoryRepository.save(new SearchHistory(userId, keyword));
    }
    
    // 2. Cập nhật trending
    TrendingSearch trending = trendingSearchRepository.findByKeyword(keyword);
    if (trending == null) {
        trending = new TrendingSearch(keyword);
    }
    trending.incrementCount();
    trending.setLastSearched(LocalDateTime.now());
    trendingSearchRepository.save(trending);
}

// Lấy top trending
public List<String> getTrendingSearches(int limit) {
    return trendingSearchRepository
        .findTopByOrderBySearchCountDesc(PageRequest.of(0, limit))
        .stream()
        .map(TrendingSearch::getKeyword)
        .collect(Collectors.toList());
}
```

### 6.3 Security Configuration

**Endpoints Public (không cần auth):**

```java
.requestMatchers(
    "/api/search/**",           // Tất cả search endpoints
    "/api/shops/{id}",          // Shop detail public
    "/api/products/**"          // Products public
).permitAll()
```

---

## 7. Hướng Dẫn Sử Dụng

### 7.1 Tìm Kiếm Sản Phẩm

1. Gõ từ khóa vào thanh tìm kiếm
2. Nhấn **Enter** hoặc click **nút tìm kiếm**
3. Kết quả hiển thị:
   - **Shops matching** (nếu có)
   - **Sản phẩm matching** (theo tên SP hoặc tên shop)

### 7.2 Lọc Kết Quả

- **Danh mục:** Click vào category filter
- **Giá:** Chọn khoảng giá
- **Đánh giá:** Chọn số sao tối thiểu
- **Sắp xếp:** Giá tăng/giảm, mới nhất, phổ biến

### 7.3 Xem Shop

1. Từ kết quả tìm kiếm → Click vào **ShopCard**
2. Từ trang chi tiết sản phẩm → Click vào **Shop Info Card**
3. Điều hướng đến `/shops/:shopId`

### 7.4 API Integration (Frontend)

```javascript
// searchService.js

// Tìm kiếm shops
export const searchShops = async (query, limit = 5) => {
  const response = await api.get(ENDPOINTS.SEARCH.SHOPS, {
    params: { q: query, limit }
  });
  return response.data;
};

// Tìm kiếm có filter
export const searchProducts = async (params) => {
  const response = await api.get(ENDPOINTS.SEARCH.FACETED, { params });
  return response.data;
};
```

---

## 📝 Ghi Chú Kỹ Thuật

### Những Thay Đổi Chính

1. **SearchBar Simplified:**
   - Xóa dropdown suggestions khi gõ
   - Xóa voice search
   - Chỉ search on Enter/Click

2. **ProductDetailPage:**
   - Thêm Shop Info Card với gradient UI
   - Xóa nút "Mua ngay"
   - Xóa nút Wishlist
   - Cải thiện thông tin chi tiết

3. **ProductRepository:**
   - Thêm search by shop name trong `findProductsWithFilters`
   - Thêm `findProductsForSuggestionIncludingShopName`

4. **New Components:**
   - `ShopCard.jsx` - Hiển thị shop trong search results
   - `ShopDetailPage.jsx` - Trang chi tiết shop

5. **New Endpoints:**
   - `GET /api/search/shops` - Tìm kiếm shops
   - `GET /api/shops/{id}` - Chi tiết shop (public)

---

## 🔮 Tính Năng Pending

| Tính Năng | Mô Tả | Priority |
|-----------|-------|----------|
| Redis Caching | Cache trending searches và suggestions | Medium |
| Analytics Dashboard | Admin panel cho search analytics | Low |
| Elasticsearch | Full-text search với Elasticsearch | Future |
| Search Autocomplete | Gợi ý real-time khi gõ | Optional |

---

> **Tác giả:** GitHub Copilot  
> **Dự án:** PBL6 E-Commerce  
> **Version:** 3.0
