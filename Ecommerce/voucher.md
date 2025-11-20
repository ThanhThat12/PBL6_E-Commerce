# PROMPT: Voucher System (Seller & Admin)

## PROJECT CONTEXT
- **Backend**: Spring Boot 3.x, Java 17+, MySQL
- **Frontend**: React 18+, TailwindCSS, Axios
- **Database**: Sử dụng schema từ file `ecommerce1.sql`
- **Tables liên quan**: `vouchers`, `user_vouchers`, `shops`, `orders`

---

## BUSINESS REQUIREMENTS

### Voucher Types
```
1. SHOP VOUCHER (Seller tạo)
   - shop_id NOT NULL
   - Chỉ áp dụng cho orders thuộc shop đó
   - Seller tự quản lý vouchers của shop mình

2. PLATFORM VOUCHER (Admin tạo)
   - shop_id IS NULL
   - Áp dụng cho TẤT CẢ shops
   - Admin tạo và quản lý
```

### Voucher Workflow
```
1. Seller/Admin tạo voucher:
   - Code unique: SUMMER2025, FREESHIP50, etc.
   - Discount type: FIXED (cố định VNĐ) hoặc PERCENTAGE (%)
   - Min order value: Đơn tối thiểu để dùng voucher
   - Max uses: Số lượt dùng tối đa (NULL = unlimited)
   - Valid dates: Từ ngày - đến ngày

2. Buyer checkout:
   - Nhập voucher code
   - System validate voucher
   - Tính discount amount
   - Apply vào order

3. System apply voucher:
   - Tạo record trong user_vouchers
   - Increment vouchers.used_count
   - Save voucher_id vào orders
   - Giảm order.total_amount

4. Auto-expire:
   - Scheduled task check mỗi giờ
   - Set status = EXPIRED nếu validTo < NOW
```

### Discount Calculation
```
FIXED:
  discount = voucher.discount_amount
  Example: Giảm 50.000đ

PERCENTAGE:
  discount = MIN(order_value * discount_amount / 100, max_discount)
  Example: Giảm 10%, tối đa 100.000đ
    - Order 500k → Giảm 50k
    - Order 2M → Giảm 100k (max)
```

### Business Rules

1. **Voucher Creation:**
   - Code: UPPERCASE + NUMBERS only, 4-20 chars, unique
   - Discount amount > 0
   - If PERCENTAGE: max_discount required
   - validFrom < validTo
   - Seller chỉ tạo được cho shop của mình
   - Admin tạo platform voucher (shop_id = NULL)

2. **Voucher Validation:**
   - Code tồn tại
   - Status = ACTIVE
   - validFrom <= NOW <= validTo
   - used_count < max_uses (nếu có limit)
   - order_value >= min_order_value
   - Voucher thuộc shop HOẶC là platform voucher
   - User chưa dùng voucher này cho order này

3. **Voucher Usage:**
   - 1 order chỉ dùng 1 voucher
   - Không thể revert sau khi apply
   - Record vào user_vouchers khi apply

4. **Voucher Management:**
   - Seller: CRUD vouchers của shop
   - Admin: CRUD platform vouchers + xem all vouchers
   - Không xóa voucher đã có người dùng (set INACTIVE)
   - Có thể update: description, status, dates, max_uses
   - KHÔNG update: code, discount_amount, discount_type

---

## DATABASE SCHEMA REVIEW

```sql
-- vouchers table (đã có trong SQL)
CREATE TABLE `vouchers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) DEFAULT NULL,  -- NULL = platform voucher
  `code` varchar(50) NOT NULL UNIQUE,
  `description` varchar(255) DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  `discount_type` enum('FIXED','PERCENTAGE') DEFAULT 'FIXED',  -- CẦN THÊM
  `min_order_value` decimal(10,2) DEFAULT 0.00,
  `max_discount` decimal(10,2) DEFAULT NULL,  -- CẦN THÊM cho PERCENTAGE
  `max_uses` int(11) DEFAULT NULL,
  `used_count` int(11) DEFAULT 0,  -- CẦN THÊM để track
  `valid_from` datetime DEFAULT NULL,
  `valid_to` datetime DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','EXPIRED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_code` (`code`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_code` (`code`),
  KEY `idx_status_dates` (`status`, `valid_from`, `valid_to`),  -- CẦN THÊM
  CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`shop_id`) 
    REFERENCES `shops` (`id`) ON DELETE CASCADE
);

-- user_vouchers table (đã có)
CREATE TABLE `user_vouchers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `voucher_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_voucher_order` (`user_id`, `order_id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`)
);
```

**⚠️ Database Migration cần thêm:**
```sql
-- Thêm cột discount_type
ALTER TABLE `vouchers` 
ADD COLUMN `discount_type` ENUM('FIXED','PERCENTAGE') DEFAULT 'FIXED' 
AFTER `discount_amount`;

-- Thêm cột max_discount
ALTER TABLE `vouchers` 
ADD COLUMN `max_discount` DECIMAL(10,2) DEFAULT NULL 
AFTER `min_order_value`;

-- Thêm cột used_count
ALTER TABLE `vouchers` 
ADD COLUMN `used_count` INT DEFAULT 0 
AFTER `max_uses`;

-- Thêm index
ALTER TABLE `vouchers` 
ADD INDEX `idx_status_dates` (`status`, `valid_from`, `valid_to`);
```

---

## BACKEND IMPLEMENTATION GUIDE

### 1. Entities & Enums

**Enum: DiscountType**
```java
public enum DiscountType {
    FIXED,      // Giảm cố định (VD: 50000)
    PERCENTAGE  // Giảm % (VD: 10)
}
```

**Enum: VoucherStatus**
```java
public enum VoucherStatus {
    ACTIVE,    // Đang hoạt động
    INACTIVE,  // Tạm dừng
    EXPIRED    // Hết hạn (auto-set by scheduled task)
}
```

**Entity: Voucher**
- Map với table `vouchers`
- Relationships:
  * `shop`: ManyToOne → Shop (nullable, NULL = platform voucher)
- Helper method:
  ```java
  public boolean isValid() {
      LocalDateTime now = LocalDateTime.now();
      return status == VoucherStatus.ACTIVE
          && (validFrom == null || now.isAfter(validFrom))
          && (validTo == null || now.isBefore(validTo))
          && (maxUses == null || usedCount < maxUses);
  }
  
  public BigDecimal calculateDiscount(BigDecimal orderValue) {
      if (discountType == DiscountType.FIXED) {
          return discountAmount;
      } else {
          BigDecimal percent = orderValue.multiply(discountAmount)
              .divide(new BigDecimal(100));
          return maxDiscount != null && percent.compareTo(maxDiscount) > 0 
              ? maxDiscount : percent;
      }
  }
  ```

**Entity: UserVoucher**
- Map với table `user_vouchers`
- Relationships:
  * `voucher`: ManyToOne → Voucher
  * `user`: ManyToOne → User
  * `order`: ManyToOne → Order (nullable)

### 2. DTOs Structure

**Request DTOs:**

**VoucherCreateRequest:**
- code: String (required, pattern: `^[A-Z0-9]{4,20}$`)
- description: String (optional, max 255)
- discountAmount: BigDecimal (required, > 0)
- discountType: DiscountType (required)
- minOrderValue: BigDecimal (default 0)
- maxDiscount: BigDecimal (required if PERCENTAGE)
- maxUses: Integer (optional, NULL = unlimited)
- validFrom: LocalDateTime (required)
- validTo: LocalDateTime (required)

**Validation logic:**
- validFrom < validTo
- If discountType = PERCENTAGE:
  * maxDiscount is required
  * discountAmount <= 100 (không quá 100%)

**VoucherUpdateRequest:**
- description: String (optional)
- status: VoucherStatus (optional)
- validFrom: LocalDateTime (optional)
- validTo: LocalDateTime (optional)
- maxUses: Integer (optional)

**VoucherValidationRequest:**
- code: String (required)
- shopId: Long (required - shop của order)
- orderValue: BigDecimal (required, > 0)

**Response DTOs:**

**VoucherResponse:**
- id, code, description
- discountAmount, discountType
- minOrderValue, maxDiscount
- maxUses, usedCount
- validFrom, validTo, status
- shopId, shopName (NULL if platform)
- createdAt
- Computed fields:
  * isValid: boolean
  * isExpired: boolean
  * remainingUses: int (maxUses - usedCount)

**VoucherValidationResponse:**
- valid: boolean
- message: String
- discountAmount: BigDecimal
- voucher: VoucherResponse (if valid)

### 3. Repository Layer

**VoucherRepository interface:**

Custom queries cần implement:
```java
// Tìm voucher by code
Optional<Voucher> findByCode(String code);

// Lấy vouchers của shop
List<Voucher> findByShopIdAndStatus(Long shopId, VoucherStatus status);

// Lấy platform vouchers (shop_id IS NULL)
List<Voucher> findByShopIdIsNullAndStatus(VoucherStatus status);

// Lấy available vouchers cho shop (shop + platform)
@Query("SELECT v FROM Voucher v WHERE " +
       "(v.shop.id = :shopId OR v.shop IS NULL) " +
       "AND v.status = 'ACTIVE' " +
       "AND v.validFrom <= :now AND v.validTo >= :now " +
       "AND (v.maxUses IS NULL OR v.usedCount < v.maxUses)")
List<Voucher> findAvailableVouchersForShop(Long shopId, LocalDateTime now);

// Auto-expire vouchers
@Query("SELECT v FROM Voucher v WHERE " +
       "v.status = 'ACTIVE' AND v.validTo < :now")
List<Voucher> findExpiredVouchers(LocalDateTime now);
```

**UserVoucherRepository interface:**
```java
// Check user đã dùng voucher này chưa
boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

// Lấy voucher usage của user
List<UserVoucher> findByUserId(Long userId);
```

### 4. Service Layer

**VoucherService methods:**

---

**1. createShopVoucher(Long sellerId, VoucherCreateRequest request)**

**Logic:**
1. Tìm shop của seller:
   ```java
   Shop shop = shopRepository.findByOwnerId(sellerId)
       .orElseThrow(() -> new NotFoundException("Bạn chưa có shop"));
   ```

2. Validate code chưa tồn tại:
   ```java
   if (voucherRepository.existsByCode(request.getCode())) {
       throw new DuplicateException("Mã voucher đã tồn tại");
   }
   ```

3. Validate PERCENTAGE voucher:
   ```java
   if (request.getDiscountType() == DiscountType.PERCENTAGE) {
       if (request.getMaxDiscount() == null) {
           throw new ValidationException("Giảm tối đa là bắt buộc cho voucher %");
       }
       if (request.getDiscountAmount().compareTo(new BigDecimal(100)) > 0) {
           throw new ValidationException("Phần trăm giảm không được > 100");
       }
   }
   ```

4. Validate dates:
   ```java
   if (request.getValidFrom().isAfter(request.getValidTo())) {
       throw new ValidationException("Ngày bắt đầu phải trước ngày kết thúc");
   }
   ```

5. Tạo voucher với shop_id = shop.id

6. Save và return VoucherResponse

---

**2. createPlatformVoucher(VoucherCreateRequest request)**

**Logic:**
1. Giống createShopVoucher nhưng:
   - Không cần tìm shop
   - Set shop_id = NULL
   - Chỉ admin được gọi (check ở Controller với @PreAuthorize)

---

**3. validateVoucher(VoucherValidationRequest request)**

**Logic:**
1. Tìm voucher by code:
   ```java
   Voucher voucher = voucherRepository.findByCode(request.getCode())
       .orElseThrow(() -> new NotFoundException("Mã voucher không tồn tại"));
   ```

2. Validate status = ACTIVE:
   ```java
   if (voucher.getStatus() != VoucherStatus.ACTIVE) {
       return VoucherValidationResponse.invalid("Voucher không còn hiệu lực");
   }
   ```

3. Validate dates:
   ```java
   LocalDateTime now = LocalDateTime.now();
   if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
       return VoucherValidationResponse.invalid("Voucher chưa đến thời gian sử dụng");
   }
   if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
       return VoucherValidationResponse.invalid("Voucher đã hết hạn");
   }
   ```

4. Validate max uses:
   ```java
   if (voucher.getMaxUses() != null && voucher.getUsedCount() >= voucher.getMaxUses()) {
       return VoucherValidationResponse.invalid("Voucher đã hết lượt sử dụng");
   }
   ```

5. Validate min order value:
   ```java
   if (request.getOrderValue().compareTo(voucher.getMinOrderValue()) < 0) {
       return VoucherValidationResponse.invalid(
           "Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ"
       );
   }
   ```

6. Validate voucher thuộc shop hoặc platform:
   ```java
   if (voucher.getShop() != null && 
       !voucher.getShop().getId().equals(request.getShopId())) {
       return VoucherValidationResponse.invalid("Voucher không áp dụng cho shop này");
   }
   ```

7. Calculate discount:
   ```java
   BigDecimal discount = voucher.calculateDiscount(request.getOrderValue());
   ```

8. Return success response với discount amount

---

**4. applyVoucher(User user, Order order, String voucherCode)**

**Called when order is created**

**Logic:**
1. Validate voucher (call validateVoucher)

2. Check user chưa dùng voucher này:
   ```java
   if (userVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
       throw new ValidationException("Bạn đã sử dụng voucher này rồi");
   }
   ```

3. Tạo UserVoucher:
   ```java
   UserVoucher userVoucher = new UserVoucher();
   userVoucher.setVoucher(voucher);
   userVoucher.setUser(user);
   userVoucher.setOrder(order);
   userVoucher.setUsedAt(LocalDateTime.now());
   userVoucherRepository.save(userVoucher);
   ```

4. Increment voucher.usedCount:
   ```java
   voucher.setUsedCount(voucher.getUsedCount() + 1);
   voucherRepository.save(voucher);
   ```

5. Update order.voucher_id = voucher.id

**⚠️ Important:** Method này phải trong @Transactional

---

**5. getShopVouchers(Long shopId, VoucherStatus status)**

**Logic:**
```java
if (status != null) {
    return voucherRepository.findByShopIdAndStatus(shopId, status);
} else {
    return voucherRepository.findByShopId(shopId);
}
// Map to VoucherResponse
```

---

**6. getPlatformVouchers(VoucherStatus status)**

**Logic:**
```java
if (status != null) {
    return voucherRepository.findByShopIdIsNullAndStatus(status);
} else {
    return voucherRepository.findByShopIdIsNull();
}
// Map to VoucherResponse
```

---

**7. getAvailableVouchers(Long shopId)**

**Buyer get vouchers có thể dùng cho shop**

**Logic:**
```java
LocalDateTime now = LocalDateTime.now();
List<Voucher> vouchers = voucherRepository.findAvailableVouchersForShop(shopId, now);
// Map to VoucherResponse với isValid = true
```

---

**8. updateVoucher(Long voucherId, Long sellerId, VoucherUpdateRequest request)**

**Logic:**
1. Tìm voucher và validate ownership:
   ```java
   Voucher voucher = voucherRepository.findById(voucherId)
       .orElseThrow(() -> new NotFoundException("Voucher không tồn tại"));
   
   // Check seller owns this voucher
   if (voucher.getShop() != null && 
       !voucher.getShop().getOwner().getId().equals(sellerId)) {
       throw new UnauthorizedException("Bạn không có quyền sửa voucher này");
   }
   ```

2. Update allowed fields:
   ```java
   if (request.getDescription() != null) {
       voucher.setDescription(request.getDescription());
   }
   if (request.getStatus() != null) {
       voucher.setStatus(request.getStatus());
   }
   if (request.getValidFrom() != null) {
       voucher.setValidFrom(request.getValidFrom());
   }
   if (request.getValidTo() != null) {
       voucher.setValidTo(request.getValidTo());
   }
   if (request.getMaxUses() != null) {
       voucher.setMaxUses(request.getMaxUses());
   }
   ```

3. Validate dates nếu update:
   ```java
   if (voucher.getValidFrom().isAfter(voucher.getValidTo())) {
       throw new ValidationException("Ngày bắt đầu phải trước ngày kết thúc");
   }
   ```

4. Save và return

---

**9. deleteVoucher(Long voucherId, Long sellerId)**

**Logic:**
1. Tìm voucher và validate ownership (giống update)

2. Check voucher đã được sử dụng chưa:
   ```java
   if (voucher.getUsedCount() > 0) {
       // Soft delete: set INACTIVE
       voucher.setStatus(VoucherStatus.INACTIVE);
       voucherRepository.save(voucher);
   } else {
       // Hard delete
       voucherRepository.delete(voucher);
   }
   ```

---

**10. autoExpireVouchers() - Scheduled Task**

**Run every hour:**
```java
@Scheduled(cron = "0 0 * * * *")
public void autoExpireVouchers() {
    LocalDateTime now = LocalDateTime.now();
    List<Voucher> expiredVouchers = voucherRepository.findExpiredVouchers(now);
    
    expiredVouchers.forEach(voucher -> {
        voucher.setStatus(VoucherStatus.EXPIRED);
    });
    
    voucherRepository.saveAll(expiredVouchers);
    
    // Log
    if (!expiredVouchers.isEmpty()) {
        log.info("Auto-expired {} vouchers", expiredVouchers.size());
    }
}
```

Enable scheduling:
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
```

### 5. Controller Layer

**VoucherController (Seller + Buyer)**

**Endpoints:**

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/vouchers` | SELLER | Tạo voucher shop |
| GET | `/api/v1/vouchers/shop/{shopId}` | PUBLIC | Get shop vouchers |
| PUT | `/api/v1/vouchers/{id}` | SELLER | Update voucher |
| DELETE | `/api/v1/vouchers/{id}` | SELLER | Delete voucher |
| POST | `/api/v1/vouchers/validate` | PUBLIC | Validate voucher |
| GET | `/api/v1/vouchers/available/{shopId}` | PUBLIC | Get available vouchers |

**Implementation notes:**
- Dùng `@PreAuthorize("hasRole('SELLER')")` cho seller endpoints
- Extract sellerId từ `@AuthenticationPrincipal UserDetails`
- Validate input với `@Valid`
- Return `ApiResponse<T>` wrapper

---

**AdminVoucherController**

**Endpoints:**

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/admin/vouchers` | ADMIN | Tạo platform voucher |
| GET | `/api/v1/admin/vouchers` | ADMIN | Get platform vouchers |
| GET | `/api/v1/admin/vouchers/all` | ADMIN | Get all vouchers |
| PUT | `/api/v1/admin/vouchers/{id}` | ADMIN | Update voucher |
| DELETE | `/api/v1/admin/vouchers/{id}` | ADMIN | Delete voucher |

**Implementation notes:**
- Dùng `@PreAuthorize("hasRole('ADMIN')")` cho tất cả endpoints
- Admin có thể update/delete cả shop vouchers và platform vouchers

---

## FRONTEND IMPLEMENTATION GUIDE

### 1. API Service Layer

**File: `src/services/voucherService.js`**

Functions cần tạo:
```javascript
// SELLER
createVoucher(data)           // POST /api/v1/vouchers
getShopVouchers(shopId, status) // GET /api/v1/vouchers/shop/:shopId
updateVoucher(id, data)       // PUT /api/v1/vouchers/:id
deleteVoucher(id)             // DELETE /api/v1/vouchers/:id

// BUYER
validateVoucher(code, shopId, orderValue) // POST /api/v1/vouchers/validate
getAvailableVouchers(shopId)  // GET /api/v1/vouchers/available/:shopId

// ADMIN
createPlatformVoucher(data)   // POST /api/v1/admin/vouchers
getPlatformVouchers(status)   // GET /api/v1/admin/vouchers
getAllVouchers(status)        // GET /api/v1/admin/vouchers/all
```

**Error handling:**
- Wrap trong try-catch
- Return error message từ `response.data.message`
- Handle network errors

### 2. SELLER Components

**Component 1: Voucher Management Page**

**Route:** `/seller/vouchers`

**Layout:**
```
Header (Title + Create Button)
  ↓
Filter Tabs (ALL | ACTIVE | INACTIVE | EXPIRED)
  ↓
Voucher Cards Grid (3 columns desktop, 1 mobile)
```

**Voucher Card UI:**
- Code (large, bold) với copy button
- Type badge (FIXED/PERCENTAGE)
- Discount value với icon
- Min order value
- Usage stats: "Đã dùng X/Y" (progress bar)
- Valid dates
- Status badge
- Action buttons: Edit | Delete

**Features:**
- Filter by status với tabs
- Search by code
- Sort by: created date, usage count
- Empty state với illustration
- Loading skeleton

---

**Component 2: Voucher Create/Edit Form**

**Modal/Page form với sections:**

**Section 1: Basic Info**
- Code input:
  * Auto uppercase
  * Pattern validation: `^[A-Z0-9]{4,20}$`
  * Real-time check duplicate (debounced API call)
  * Character counter
- Description textarea (optional, max 255)

**Section 2: Discount Settings**
- Discount type radio:
  * Fixed (VNĐ icon)
  * Percentage (% icon)
- Discount amount input:
  * Number format
  * For PERCENTAGE: max 100
- Max discount input:
  * Show only if PERCENTAGE
  * Required indicator
- Min order value:
  * Number format VNĐ
  * Default 0

**Section 3: Usage Limits**
- Max uses input:
  * Number
  * Placeholder: "Không giới hạn"
  * Info text: "Để trống nếu không giới hạn"

**Section 4: Valid Period**
- Date range picker:
  * Valid from (datetime)
  * Valid to (datetime)
  * Validate: from < to
  * Show duration (X ngày)

**Validation:**
- Real-time validation với error messages
- Disable submit nếu có lỗi
- Highlight invalid fields
- Summary errors at top

---

**Component 3: Voucher Statistics Dashboard**

**Optional feature - Route:** `/seller/vouchers/stats`

**Widgets:**
- Total vouchers (by status)
- Most used vouchers (top 5)
- Total discount given
- Unused vouchers warning
- Expiring soon (next 7 days)

### 3. BUYER Components

**Component 1: Voucher Selector at Checkout**

**Location:** Checkout page

**UI:**
```
[Input: Nhập mã voucher] [Áp dụng button]
       ↓ (click "Xem voucher có sẵn")
[Voucher List Modal]
```

**Voucher List Modal:**
- List available vouchers
- Each item:
  * Code
  * Description
  * Discount info
  * Min order requirement
  * "Áp dụng" button
- Auto-select when click item
- Show validation result immediately

**Validation Flow:**
1. User enters code hoặc chọn từ list
2. Click "Áp dụng"
3. Call validateVoucher API
4. Show result:
   - Success: Green message + discount amount
   - Error: Red message + reason
5. Update order summary with discount

**UI States:**
- No voucher applied
- Voucher validating (loading)
- Voucher valid (success green)
- Voucher invalid (error red)
- Voucher applied (lock, show remove button)

---

**Component 2: Available Vouchers Modal**

**Trigger:** "Xem voucher có sẵn" link

**UI:**
- Shop vouchers section
- Platform vouchers section
- Each voucher card:
  * Visual design với brand colors
  * Code (highlighted)
  * Discount amount (large)
  * Description
  * Min order (if > 0)
  * Expiry date
  * "Sao chép" button
  * "Áp dụng" button
- Empty state nếu không có voucher

**Features:**
- Filter: Có thể dùng / Tất cả
- Sort: Discount cao → thấp
- Search by code
- Copy code to clipboard

### 4. ADMIN Components

**Component: Platform Voucher Management**

**Route:** `/admin/vouchers`

**Tabs:**
- Platform Vouchers (shop_id = NULL)
- All Vouchers (shop + platform)

**Features:**
- All features giống Seller management
- Thêm column "Shop" để hiển thị voucher thuộc shop nào
- Can edit/delete any voucher
- Create platform voucher form

### 5. UI/UX Guidelines

**Voucher Card Design:**
```
┌─────────────────────────────────┐
│ 💰 SUMMER2025    [PERCENTAGE] │
│                                 │
│ Giảm 10% • Tối đa 100k         │
│ Đơn tối thiểu: 200k            │
│                                 │
│ ▓▓▓▓▓░░░░░ 50/100 đã dùng     │
│                                 │
│ 📅 01/06 - 30/06/2025          │
│ [ACTIVE]      [Sửa] [Xóa]     │
└─────────────────────────────────┘
```

**Color Scheme:**
- FIXED vouchers: Blue theme
- PERCENTAGE vouchers: Orange theme
- ACTIVE: Green
- INACTIVE: Gray
- EXPIRED: Red

**Animations:**
- Fade in voucher cards
- Hover effect: slight scale up
- Copy code: success animation
- Apply voucher: smooth transition

**Responsive:**
- Mobile: Stack vertically, full width cards
- Tablet: 2 columns
- Desktop: 3 columns

**Icons:**
- 💰 Money icon for code
- 📦 Box icon for min order
- 📅 Calendar for dates
- ✓ Check for applied
- ✕ X for remove
- 📋 Copy for copy action

---

## VALIDATION RULES

### Backend Validation
- Code: ^[A-Z0-9]{4,20}$, unique
- Discount amount: > 0
- Discount type PERCENTAGE:
  * maxDiscount required
  * discountAmount <= 100
- Min order value: >= 0
- Valid dates: validFrom < validTo
- Max uses: > 0 or NULL
- Seller ownership for shop vouchers
- Voucher not used for hard delete

### Frontend Validation
- Real-time code format validation
- Async duplicate check (debounced)
- Required field indicators
- Number format validation
- Date range validation
- Conditional validation (maxDiscount for PERCENTAGE)
- Confirm dialog for delete

---

## API RESPONSE EXAMPLES

**Success - Create Voucher:**
```json
{
  "success": true,
  "message": "Tạo voucher thành công",
  "data": {
    "id": 1,
    "code": "SUMMER2025",
    "description": "Giảm giá mùa hè",
    "discountAmount": 10.00,
    "discountType": "PERCENTAGE",
    "minOrderValue": 200000.00,
    "maxDiscount": 100000.00,
    "maxUses": 100,
    "usedCount": 0,
    "validFrom": "2025-06-01T00:00:00",
    "validTo": "2025-06-30T23:59:59",
    "status": "ACTIVE",
    "shopId": 1,
    "shopName": "My Shop",
    "isValid": true,
    "isExpired": false,
    "remainingUses": 100
  }
}
```

**Success - Validate Voucher:**
```json
{
  "success": true,
  "message": "Voucher hợp lệ",
  "data": {
    "valid": true,
    "message": "Áp dụng voucher thành công",
    "discountAmount": 50000.00,
    "voucher": {
      "id": 1,
      "code": "SUMMER2025",
      "discountType": "PERCENTAGE",
      "discountAmount": 10.00
    }
  }
}
```

**Error - Invalid Voucher:**
```json
{
  "success": false,
  "message": "Đơn hàng tối thiểu 200.000đ",
  "data": {
    "valid": false,
    "message": "Đơn hàng tối thiểu 200.000đ",
    "discountAmount": 0,
    "voucher": null
  }
}
```

**Error - Duplicate Code:**
```json
{
  "success": false,
  "message": "Mã voucher đã tồn tại",
  "data": null
}
```

---

## TESTING CHECKLIST

### Backend Testing

**Voucher Creation:**
- [ ] Seller tạo voucher cho shop thành công
- [ ] Admin tạo platform voucher (shop_id = NULL) thành công
- [ ] Không cho phép code trùng
- [ ] Validate PERCENTAGE voucher phải có maxDiscount
- [ ] Validate validFrom < validTo
- [ ] Validate discountAmount > 0
- [ ] Validate PERCENTAGE discount <= 100

**Voucher Validation:**
- [ ] Validate code không tồn tại → error
- [ ] Validate status != ACTIVE → error
- [ ] Validate chưa đến thời gian sử dụng → error
- [ ] Validate đã hết hạn → error
- [ ] Validate hết lượt sử dụng → error
- [ ] Validate order value < min order value → error
- [ ] Validate voucher không thuộc shop → error
- [ ] Validate FIXED discount đúng
- [ ] Validate PERCENTAGE discount đúng (không vượt maxDiscount)

**Voucher Application:**
- [ ] Apply voucher thành công
- [ ] Tạo UserVoucher record
- [ ] Increment usedCount
- [ ] User không thể dùng 1 voucher 2 lần
- [ ] Transaction rollback nếu có lỗi

**Voucher Management:**
- [ ] Seller chỉ xem được vouchers của shop mình
- [ ] Seller không thể update vouchers của shop khác
- [ ] Admin xem được tất cả vouchers
- [ ] Update voucher thành công (allowed fields only)
- [ ] Không update được code, discountAmount, discountType
- [ ] Soft delete voucher đã sử dụng
- [ ] Hard delete voucher chưa sử dụng

**Scheduled Tasks:**
- [ ] Auto-expire vouchers sau validTo
- [ ] Chỉ expire vouchers có status = ACTIVE

### Frontend Testing

**Seller Features:**
- [ ] Form validation hoạt động
- [ ] Real-time duplicate code check
- [ ] Create voucher thành công
- [ ] Update voucher thành công
- [ ] Delete voucher với confirmation
- [ ] Filter by status hoạt động
- [ ] Search by code hoạt động
- [ ] Responsive trên mobile
- [ ] Loading states hiển thị
- [ ] Error messages hiển thị rõ ràng

**Buyer Features:**
- [ ] Hiển thị available vouchers
- [ ] Apply voucher thành công
- [ ] Show discount amount trong order summary
- [ ] Validation errors hiển thị
- [ ] Copy code to clipboard
- [ ] Remove applied voucher
- [ ] Auto-calculate discount khi apply
- [ ] Voucher modal responsive

**Admin Features:**
- [ ] Create platform voucher thành công
- [ ] View all vouchers (shop + platform)
- [ ] Filter và search hoạt động
- [ ] Update any voucher
- [ ] Delete any voucher

---

## PERFORMANCE OPTIMIZATION

### Database Indexes
```sql
-- Already have
CREATE INDEX idx_shop ON vouchers(shop_id);
CREATE INDEX idx_code ON vouchers(code);

-- Need to add
CREATE INDEX idx_status_dates ON vouchers(status, valid_from, valid_to);
CREATE INDEX idx_shop_status ON vouchers(shop_id, status);
```

### Caching Strategy
**Consider caching:**
- Available vouchers cho mỗi shop (TTL: 5 minutes)
- Platform vouchers (TTL: 10 minutes)
- Voucher validation results (TTL: 1 minute)

**Implementation:**
```java
@Cacheable(value = "availableVouchers", key = "#shopId")
public List<VoucherResponse> getAvailableVouchers(Long shopId) {
    // ...
}

@CacheEvict(value = "availableVouchers", allEntries = true)
public VoucherResponse createVoucher(...) {
    // ...
}
```

### Query Optimization
- Use JOIN FETCH để tránh N+1 queries
- Pagination cho list endpoints
- Select only needed fields

---

## SECURITY CONSIDERATIONS

1. **Authorization:**
   - Seller chỉ CRUD vouchers của shop mình
   - Buyer chỉ xem, không tạo/sửa/xóa
   - Admin có full access

2. **Input Validation:**
   - Sanitize code input (chỉ A-Z0-9)
   - Validate discount amounts (không âm)
   - Validate dates (logical order)

3. **Rate Limiting:**
   - Limit voucher creation: 10/hour per seller
   - Limit validation requests: 100/hour per user
   - Prevent brute-force code guessing

4. **Audit Logging:**
   - Log voucher creation/updates
   - Log voucher applications
   - Track who approved/rejected

---

## EDGE CASES & ERROR HANDLING

### Edge Cases to Handle

1. **Voucher Expiry During Checkout:**
   - User applies voucher
   - Voucher expires before payment
   - Solution: Re-validate voucher before order creation

2. **Concurrent Usage (Race Condition):**
   - Multiple users apply voucher simultaneously
   - usedCount might exceed maxUses
   - Solution: Use database-level locking or optimistic locking
   ```java
   @Version
   private Long version; // Add to Voucher entity
   ```

3. **Timezone Issues:**
   - Store all dates in UTC
   - Convert to user timezone for display
   - Validate with server timezone

4. **Partial Discount:**
   - Discount > order value
   - Solution: discount = MIN(discount, order_value)

5. **Voucher Deleted After Apply:**
   - User applied voucher
   - Seller deletes voucher
   - Order still has voucher_id
   - Solution: Soft delete (set INACTIVE)

### Error Messages

**User-friendly messages:**
- ❌ "Mã voucher không tồn tại"
- ❌ "Voucher đã hết hạn sử dụng"
- ❌ "Voucher đã hết lượt sử dụng"
- ❌ "Đơn hàng tối thiểu [amount]đ"
- ❌ "Voucher chỉ áp dụng cho shop [shop_name]"
- ❌ "Bạn đã sử dụng voucher này rồi"
- ❌ "Voucher chưa đến thời gian sử dụng"

---

## FUTURE ENHANCEMENTS

### Phase 2 Features
1. **Voucher Collections:**
   - Buyer "lưu" vouchers yêu thích
   - Notification khi voucher sắp hết hạn

2. **Advanced Discount Rules:**
   - Category-specific vouchers
   - Product-specific vouchers
   - First-time buyer vouchers
   - Tiered discounts (mua nhiều giảm nhiều)

3. **Voucher Analytics:**
   - Conversion rate per voucher
   - Revenue impact analysis
   - A/B testing vouchers

4. **Dynamic Vouchers:**
   - Auto-generate codes
   - Personalized vouchers per user
   - Flash sale vouchers

5. **Voucher Sharing:**
   - Referral vouchers
   - Social media share vouchers
   - Gift vouchers

### Phase 3 Features
1. **Stacking Vouchers:**
   - Allow multiple vouchers per order
   - Rules engine for combinations

2. **Voucher Marketplace:**
   - Users trade/gift vouchers
   - Secondary market

---

## NOTES & BEST PRACTICES

### Development Tips

1. **Testing:**
   - Write unit tests cho calculateDiscount()
   - Integration tests cho apply voucher flow
   - Test với nhiều timezone
   - Test concurrent usage

2. **Documentation:**
   - Document discount calculation logic
   - API documentation với Swagger
   - Add examples in code comments

3. **Logging:**
   - Log voucher applications
   - Log validation failures (for analytics)
   - Monitor usage patterns

4. **Code Organization:**
   ```
   service/
   ├── VoucherService.java
   ├── VoucherValidationService.java  // Separate validation logic
   └── VoucherStatisticsService.java  // Analytics
   ```

5. **Constants:**
   ```java
   public class VoucherConstants {
       public static final int CODE_MIN_LENGTH = 4;
       public static final int CODE_MAX_LENGTH = 20;
       public static final String CODE_PATTERN = "^[A-Z0-9]{4,20}$";
       public static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");
   }
   ```

### Common Pitfalls to Avoid

1. ❌ Không validate voucher trước khi apply
2. ❌ Không handle timezone properly
3. ❌ Không lock khi increment usedCount (race condition)
4. ❌ Hard delete vouchers đã sử dụng
5. ❌ Không cache frequently accessed data
6. ❌ Không validate discount không vượt order value
7. ❌ Không log voucher usage (mất data analytics)

### Deployment Checklist

- [ ] Run database migrations (thêm columns)
- [ ] Create indexes
- [ ] Enable scheduled tasks
- [ ] Configure cache (Redis/Caffeine)
- [ ] Set up monitoring cho voucher usage
- [ ] Test in staging với production-like data
- [ ] Prepare rollback plan
- [ ] Document API changes
- [ ] Train seller users
- [ ] Announce feature to users