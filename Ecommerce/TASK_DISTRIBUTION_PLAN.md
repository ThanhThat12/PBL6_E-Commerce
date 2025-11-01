# 📋 WORK DISTRIBUTION PLAN - Chia việc implement BUYER APIs

## 🎯 Chiến lược chia việc

### **Nguyên tắc:**
1. **Chia theo feature/module** (không chia API cùng module)
2. **Mỗi người có 1 folder riêng** → Tránh conflict
3. **Shared interfaces/enums** → Định nghĩa trước, dùng chung
4. **Repository interfaces** → Viết trước, dùng chung
5. **DTOs & Entities** → Viết trước, dùng chung

---

## 📁 Cấu trúc folder sau khi chia

```
Ecommerce/src/main/java/com/PBL6/Ecommerce/
├── shared/
│   ├── dto/                          # ✅ Shared DTOs
│   │   ├── ProductReviewDTO.java
│   │   ├── ShopDTO.java
│   │   ├── OrderDTO.java
│   │   ├── WalletDTO.java
│   │   └── VoucherDTO.java
│   │
│   ├── entity/                       # ✅ Shared Entities (từ DB)
│   │   ├── ProductReview.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Shop.java
│   │   ├── Product.java
│   │   ├── Wallet.java
│   │   ├── WalletTransaction.java
│   │   └── Refund.java
│   │
│   ├── enum/                        # ✅ Shared Enums
│   │   ├── OrderStatus.java
│   │   ├── PaymentStatus.java
│   │   ├── PaymentMethod.java
│   │   ├── RefundStatus.java
│   │   └── TransactionType.java
│   │
│   ├── exception/                   # ✅ Shared Exceptions
│   │   ├── NotFoundException.java
│   │   ├── ForbiddenException.java
│   │   └── BadRequestException.java
│   │
│   └── request/                     # ✅ Shared Request DTOs
│       ├── CreateReviewRequest.java
│       ├── CreateOrderRequest.java
│       ├── CancelOrderRequest.java
│       └── ...
│
├── repository/                      # ✅ Shared Repositories
│   ├── ProductReviewRepository.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   ├── ShopRepository.java
│   ├── WalletRepository.java
│   ├── RefundRepository.java
│   └── ...
│
├── service/
│   ├── reviews/                     # 👤 Member 1
│   │   ├── ProductReviewService.java
│   │   └── ProductReviewServiceImpl.java
│   │
│   ├── checkout/                    # 👤 Member 2
│   │   ├── CheckoutService.java
│   │   ├── PaymentService.java
│   │   └── ShippingService.java
│   │
│   ├── order/                       # 👤 Member 3
│   │   ├── OrderService.java
│   │   └── OrderServiceImpl.java
│   │
│   ├── shop/                        # 👤 Member 4 (hoặc Member 1)
│   │   └── ShopService.java
│   │
│   ├── wallet/                      # 👤 Member 5 (hoặc Member 3)
│   │   └── WalletService.java
│   │
│   └── voucher/                     # 👤 Member 6 (hoặc Member 2)
│       └── VoucherService.java
│
└── controller/
    ├── reviews/                     # 👤 Member 1
    │   └── ProductReviewController.java
    │
    ├── checkout/                    # 👤 Member 2
    │   └── CheckoutController.java
    │
    ├── order/                       # 👤 Member 3
    │   └── OrderController.java
    │
    ├── shop/                        # 👤 Member 4
    │   └── ShopController.java
    │
    ├── wallet/                      # 👤 Member 5
    │   └── WalletController.java
    │
    └── voucher/                     # 👤 Member 6
        └── VoucherController.java
```

---

## 👥 TASK ASSIGNMENT - 6 Members

### **📌 Phase 0: Preparation (Shared - All members)**
**Duration:** 1 day
**Owner:** Team Lead

**Tasks:**
1. ✅ Create shared folder structure
2. ✅ Create & commit Entities
3. ✅ Create & commit Repositories (interfaces only)
4. ✅ Create & commit DTOs
5. ✅ Create & commit Requests
6. ✅ Create & commit Enums/Exceptions
7. ✅ Push to git
8. ✅ All members pull

**Commit message:**
```
[feat] Initial setup: entities, repositories, DTOs, enums

- Add ProductReview, Order, Shop, Product entities
- Add repository interfaces
- Add DTOs and request objects
- Add enums and exceptions
```

---

## 📦 TASK DISTRIBUTION

### **👤 Member 1: Product Reviews** (HIGH PRIORITY)
**Duration:** 2-3 days
**Dependency:** Phase 0 complete

#### **Subtasks:**

**1.1. ProductReviewService** (4 hours)
```java
// src/main/java/com/PBL6/Ecommerce/service/reviews/ProductReviewService.java

public interface ProductReviewService {
    ProductReviewDTO getProductReviews(
        Long productId, int page, int size, 
        Integer rating, String sortBy
    );
    
    ProductReviewDTO createReview(
        Long productId, CreateReviewRequest request, String username
    );
    
    ProductReviewDTO updateReview(
        Long reviewId, UpdateReviewRequest request, String username
    );
    
    void deleteReview(Long reviewId, String username);
    
    Page<ProductReviewDTO> getMyReviews(
        String username, int page, int size
    );
}
```

**1.2. ProductReviewServiceImpl** (6 hours)
- Implement business logic
- Validation: user bought product, order status COMPLETED
- Duplicate check: unique_user_product constraint
- Time limit: 7 days edit
- Mapper to DTO

**1.3. ProductReviewController** (3 hours)
```java
@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasRole('BUYER')")
public class ProductReviewController {
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(...) { }
    
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewDTO> createReview(...) { }
    
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ProductReviewDTO> updateReview(...) { }
    
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(...) { }
    
    @GetMapping("/my-reviews")
    public ResponseEntity<Page<ProductReviewDTO>> getMyReviews(...) { }
}
```

**1.4. Unit Tests** (3 hours)
- Test create review (valid/invalid)
- Test update review (time limit)
- Test delete review
- Test duplicate review

**Commit structure:**
```
[feat] Product reviews module

commit 1: [feat] ProductReviewService interface
commit 2: [feat] ProductReviewServiceImpl with validations
commit 3: [feat] ProductReviewController with endpoints
commit 4: [test] Unit tests for product reviews
```

---

### **👤 Member 2: Checkout & Payment** (CRITICAL - START FIRST)
**Duration:** 3-4 days
**Dependency:** Phase 0 complete + GHN API config

#### **Subtasks:**

**2.1. ShippingService** (6 hours)
```java
public interface ShippingService {
    ShippingFeeResponse calculateShipping(
        CalculateShippingRequest request
    );
    
    String createShipment(Order order);
}
```

**Call GHN API** để tính phí shipping + expected delivery

**2.2. PaymentService** (4 hours)
```java
public interface PaymentService {
    String createMomoPayment(Order order);
    
    void processMomoCallback(MomoCallbackRequest request);
    
    void verifySignature(MomoCallbackRequest request);
}
```

**2.3. CheckoutService** (8 hours)
```java
public interface CheckoutService {
    OrderResponse checkout(CreateOrderRequest request, String username);
}
```

**Logic:**
- Validate address, shop, items
- Check stock
- Apply voucher
- Calculate: subtotal, discount, shipping, final
- Create order + items + shipment
- Decrease stock
- Clear cart
- Create platform fee (5%)
- Handle payment (COD vs MOMO)

**2.4. CheckoutController** (3 hours)
```java
@PostMapping("/checkout/calculate-shipping")
public ResponseEntity<ShippingFeeResponse> calculateShipping(...) { }

@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(...) { }

@PostMapping("/payment/momo/callback")
public ResponseEntity<Void> momoCallback(...) { }
```

**2.5. Unit Tests** (4 hours)
- Test calculate shipping
- Test create order (valid/invalid)
- Test stock check
- Test voucher application
- Test MOMO callback

**Commit structure:**
```
[feat] Checkout & Payment module

commit 1: [feat] ShippingService for GHN integration
commit 2: [feat] PaymentService for MOMO payment
commit 3: [feat] CheckoutService with order creation
commit 4: [feat] CheckoutController with endpoints
commit 5: [test] Unit tests for checkout/payment
```

---

### **👤 Member 3: Order Management** (HIGH PRIORITY)
**Duration:** 2-3 days
**Dependency:** Phase 0 complete + CheckoutService

#### **Subtasks:**

**3.1. OrderService** (6 hours)
```java
public interface OrderService {
    Page<OrderDTO> getOrders(
        String username, String status, int page, int size
    );
    
    OrderDetailDTO getOrderDetail(Long orderId, String username);
    
    void cancelOrder(Long orderId, CancelOrderRequest request, String username);
    
    void completeOrder(Long orderId, String username);
}
```

**3.2. OrderServiceImpl** (8 hours)
**Cancel logic:**
- Check ownership
- Check status = PENDING only
- Restore stock
- Process refund if paid
- Add to wallet
- Create wallet transaction

**Complete logic:**
- Check shipment status = DELIVERED
- Update order status = COMPLETED
- For COD: payment_status = PAID
- Update product.sold_count

**3.3. OrderController** (3 hours)
```java
@GetMapping("/api/orders")
public ResponseEntity<Page<OrderDTO>> getOrders(...) { }

@GetMapping("/api/orders/{orderId}")
public ResponseEntity<OrderDetailDTO> getOrderDetail(...) { }

@PostMapping("/api/orders/{orderId}/cancel")
public ResponseEntity<Void> cancelOrder(...) { }

@PostMapping("/api/orders/{orderId}/complete")
public ResponseEntity<Void> completeOrder(...) { }
```

**3.4. Unit Tests** (4 hours)
- Test list orders (filter by status)
- Test get order detail (permission check)
- Test cancel order (stock restore, refund)
- Test complete order (sold_count update)

**Commit structure:**
```
[feat] Order management module

commit 1: [feat] OrderService interface
commit 2: [feat] OrderServiceImpl with cancel/complete logic
commit 3: [feat] OrderController with endpoints
commit 4: [test] Unit tests for order management
```

---

### **👤 Member 4: Shop Information** (MEDIUM PRIORITY)
**Duration:** 1 day
**Dependency:** Phase 0 complete

#### **Subtasks:**

**4.1. ShopService** (4 hours)
```java
public interface ShopService {
    ShopDTO getShop(Long shopId);
    
    Page<ProductDTO> getShopProducts(
        Long shopId, int page, int size, Long categoryId, String sortBy
    );
    
    Page<ProductReviewDTO> getShopReviews(
        Long shopId, int page, int size
    );
}
```

**4.2. ShopController** (2 hours)
```java
@GetMapping("/api/shops/{shopId}")
public ResponseEntity<ShopDTO> getShop(...) { }

@GetMapping("/api/shops/{shopId}/products")
public ResponseEntity<Page<ProductDTO>> getShopProducts(...) { }

@GetMapping("/api/shops/{shopId}/reviews")
public ResponseEntity<Page<ProductReviewDTO>> getShopReviews(...) { }
```

**4.3. Unit Tests** (2 hours)
- Test get shop info
- Test list shop products with filters
- Test list shop reviews

**Commit structure:**
```
[feat] Shop information module

commit 1: [feat] ShopService interface
commit 2: [feat] ShopController with endpoints
commit 3: [test] Unit tests for shop
```

---

### **👤 Member 5: Wallet** (MEDIUM PRIORITY)
**Duration:** 1 day
**Dependency:** Phase 0 complete + OrderService (refund logic)

#### **Subtasks:**

**5.1. WalletService** (3 hours)
```java
public interface WalletService {
    WalletDTO getWallet(String username);
    
    Page<WalletTransactionDTO> getTransactions(
        String username, int page, int size, String type
    );
    
    void addToWallet(User user, BigDecimal amount, 
                     TransactionType type, String description);
}
```

**5.2. WalletController** (2 hours)
```java
@GetMapping("/api/wallet")
public ResponseEntity<WalletDTO> getWallet(...) { }

@GetMapping("/api/wallet/transactions")
public ResponseEntity<Page<WalletTransactionDTO>> getTransactions(...) { }
```

**5.3. Unit Tests** (2 hours)
- Test get wallet balance
- Test list transactions
- Test add to wallet

**Commit structure:**
```
[feat] Wallet module

commit 1: [feat] WalletService interface
commit 2: [feat] WalletController with endpoints
commit 3: [test] Unit tests for wallet
```

---

### **👤 Member 6: Vouchers** (LOW PRIORITY)
**Duration:** 1 day
**Dependency:** Phase 0 complete

#### **Subtasks:**

**6.1. VoucherService** (3 hours)
```java
public interface VoucherService {
    List<VoucherDTO> getAvailableVouchers(Long shopId);
    
    VoucherValidationResponse validateVoucher(
        String code, Long shopId, BigDecimal orderAmount
    );
}
```

**6.2. VoucherController** (2 hours)
```java
@GetMapping("/api/vouchers/available")
public ResponseEntity<List<VoucherDTO>> getAvailableVouchers(...) { }

@PostMapping("/api/vouchers/apply")
public ResponseEntity<VoucherValidationResponse> validateVoucher(...) { }
```

**6.3. Unit Tests** (2 hours)
- Test get available vouchers
- Test validate voucher (min order, expiry, active)

**Commit structure:**
```
[feat] Voucher module

commit 1: [feat] VoucherService interface
commit 2: [feat] VoucherController with endpoints
commit 3: [test] Unit tests for vouchers
```

---

## 📅 TIMELINE

### **Week 1 (Days 1-5)**

| Day | Task | Owner | Status |
|-----|------|-------|--------|
| Day 1 | Phase 0: Setup shared | Lead | 🔴 |
| Day 2-3 | Product Reviews | Member 1 | 🔴 |
| Day 2-4 | Checkout & Payment | Member 2 | 🔴 |
| Day 4-5 | Order Management | Member 3 | 🔴 |
| Day 5 | Shop Info | Member 4 | 🔴 |
| Day 5 | Wallet | Member 5 | 🔴 |
| Day 5 | Vouchers | Member 6 | 🔴 |

### **Week 2 (Days 6-10)**

| Task | Details |
|------|---------|
| Integration Testing | Test all modules together |
| API Testing | Postman/Insomnia |
| Bug Fixes | Fix issues from testing |
| Documentation | Update API docs |
| Code Review | All members review |
| Merge to main | Prepare for deployment |

---

## 🔗 GIT WORKFLOW

### **Branch naming:**
```
main                          # Production
├── develop                   # Integration branch
│   ├── feature/reviews       # Member 1
│   ├── feature/checkout      # Member 2
│   ├── feature/orders        # Member 3
│   ├── feature/shop-info     # Member 4
│   ├── feature/wallet        # Member 5
│   └── feature/vouchers      # Member 6
│
└── shared/setup              # Phase 0 branch
```

### **Commit workflow:**

**Phase 0 (Shared):**
```bash
git checkout develop
git pull origin develop
git checkout -b shared/setup
# Create shared folder structure + entities + repos
git add .
git commit -m "[feat] Phase 0: Entities, repositories, DTOs"
git push origin shared/setup
# Create Pull Request
# Lead reviews & merges to develop
git checkout develop
git pull origin develop
```

**Phase 1 (Each member):**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/reviews  # For Member 1

# Work on reviews
git add .
git commit -m "[feat] ProductReviewService interface"
git add .
git commit -m "[feat] ProductReviewServiceImpl implementation"
git add .
git commit -m "[feat] ProductReviewController endpoints"
git add .
git commit -m "[test] Unit tests for reviews"

git push origin feature/reviews

# Create Pull Request on GitHub
# Other members review & approve
# Merge to develop
```

**Merge to main:**
```bash
git checkout main
git pull origin main
git merge develop
git push origin main
git tag v1.0.0
```

---

## 📝 CODE REVIEW CHECKLIST

Trước khi merge, member khác phải check:

- [ ] Code follows naming conventions
- [ ] All methods have JavaDoc
- [ ] All endpoints tested
- [ ] Exception handling correct
- [ ] SQL injection prevention (parameterized queries)
- [ ] Permission checks (@PreAuthorize)
- [ ] No hardcoded values
- [ ] Logging added for important operations
- [ ] DTOs mapped correctly
- [ ] No N+1 query problems

---

## 🚨 CONFLICT RESOLUTION

### **If conflict occurs:**

1. **Never modify shared folder files** (entities, repos, enums)
2. **Use git merge tool:**
   ```bash
   git merge feature/checkout
   git status  # See conflicts
   git mergetool  # Open merge tool
   ```

3. **If file conflict:**
   ```bash
   git checkout --ours shared/dto/ProductReviewDTO.java  # Keep current
   git checkout --theirs service/reviews/ProductReviewService.java  # Accept incoming
   ```

4. **If stubborn conflict:**
   ```bash
   git merge --abort  # Cancel merge
   # Contact team lead for manual resolution
   ```

---

## 📊 PROGRESS TRACKING

**Create shared document:**

```markdown
# Development Progress

## Phase 0: Setup ✅
- [ ] Entities created
- [ ] Repositories created
- [ ] DTOs created
- [ ] Pushed to develop

## Phase 1: Features
- [ ] Reviews (Member 1) - 60%
- [ ] Checkout (Member 2) - 40%
- [ ] Orders (Member 3) - 30%
- [ ] Shop Info (Member 4) - 0%
- [ ] Wallet (Member 5) - 0%
- [ ] Vouchers (Member 6) - 0%

## Issues
- GHN API integration needs config
- MOMO test keys needed
- ...
```

---

## 🎯 DEPENDENCY GRAPH

```
Phase 0: Setup (All)
├── Member 1: Reviews (Independent)
├── Member 2: Checkout → Phase 0
│   ├── Shipping Service → GHN API
│   └── Payment Service → MOMO
├── Member 3: Orders → Member 2 (OrderResponse)
├── Member 4: Shop → Independent
├── Member 5: Wallet → Member 3 (Refund logic)
└── Member 6: Vouchers → Independent
```

---

## 📦 FILES TO CREATE FIRST (Phase 0)

### **Shared Entities:**
```
src/main/java/com/PBL6/Ecommerce/domain/

ProductReview.java
Order.java
OrderItem.java
Shop.java
Product.java
Wallet.java
WalletTransaction.java
Refund.java
Cart.java
CartItem.java
```

### **Shared Repositories:**
```
src/main/java/com/PBL6/Ecommerce/repository/

ProductReviewRepository.java
OrderRepository.java
OrderItemRepository.java
ShopRepository.java
ProductRepository.java
WalletRepository.java
RefundRepository.java
CartRepository.java
CartItemRepository.java
```

### **Shared DTOs:**
```
src/main/java/com/PBL6/Ecommerce/shared/dto/

ProductReviewDTO.java
OrderDTO.java
OrderDetailDTO.java
ShopDTO.java
ProductDTO.java
WalletDTO.java
WalletTransactionDTO.java
VoucherDTO.java
```

### **Shared Request Objects:**
```
src/main/java/com/PBL6/Ecommerce/shared/request/

CreateReviewRequest.java
UpdateReviewRequest.java
CreateOrderRequest.java
CalculateShippingRequest.java
CancelOrderRequest.java
```

### **Shared Enums:**
```
src/main/java/com/PBL6/Ecommerce/shared/enum/

OrderStatus.java
PaymentStatus.java
PaymentMethod.java
RefundStatus.java
TransactionType.java
```

---

## ✅ FINAL CHECKLIST

Before Phase 1 starts:

- [ ] All members have git access
- [ ] Branch structure created
- [ ] Shared folder setup
- [ ] All entities created
- [ ] All repositories created
- [ ] DTOs created
- [ ] Push to develop
- [ ] All members have pulled
- [ ] Build passes on all machines
- [ ] Test database configured

---

## 📞 COMMUNICATION

**Daily standup:** 09:00 AM
- What did you do yesterday?
- What will you do today?
- Any blockers?

**Slack channel:** #ecommerce-api-dev
- Share progress
- Ask questions
- Report issues

**Weekly demo:** Friday 5 PM
- Show working features
- Demo to stakeholders

---

**🎉 Following this plan, team có thể implement 20 APIs trong 2 tuần mà không conflict!**
