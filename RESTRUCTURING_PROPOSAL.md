# Đề xuất Tái Cấu Trúc Project - PBL6 E-Commerce

**Ngày**: 19 Tháng 10, 2025  
**Phiên bản**: 1.0  
**Trạng thái**: Đề xuất (Chờ phê duyệt)

---

## 📋 Tóm tắt Executive

Dự án hiện tại sử dụng **Layer-based Architecture** (controller/service/domain/repository) dẫn đến:

- ❌ Khó bảo trì khi project phình to (11 controllers, 14 services trong cùng package)
- ❌ Coupling cao giữa các module
- ❌ Thiếu tổ chức rõ ràng cho DTOs (17 files trong cùng folder)
- ❌ Khó mở rộng và thêm tính năng mới

**Giải pháp**: Chuyển sang **Feature/Module-based Architecture** với Clean Architecture principles.

**Lợi ích**:

- ✅ Module độc lập, dễ bảo trì
- ✅ Giảm coupling, tăng cohesion
- ✅ Dễ test (unit test theo module)
- ✅ Onboarding dev mới nhanh hơn
- ✅ Chuẩn bị cho Microservices (nếu cần scale)

---

## 📊 So sánh Cấu trúc

### Cấu trúc Hiện tại (Layer-based)

```
src/main/java/com/PBL6/Ecommerce/
├── controller/                    ❌ 11 controllers (Auth + Product + Cart + User lẫn lộn)
│   ├── AuthController.java
│   ├── GoogleAuthController.java
│   ├── FacebookAuthController.java
│   ├── RefreshTokenController.java
│   ├── LogoutController.java
│   ├── UserController.java
│   ├── ForgotPasswordController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── CartController.java
│   └── GlobalExceptionHandler.java
│
├── service/                       ❌ 14 services (Business + Infrastructure không tách)
│   ├── AuthService.java
│   ├── GoogleAuthService.java
│   ├── FacebookAuthService.java
│   ├── RefreshTokenService.java
│   ├── TokenBlacklistService.java
│   ├── TokenCleanupScheduler.java
│   ├── LoginAttemptService.java
│   ├── UserService.java
│   ├── ForgotPasswordService.java
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   ├── EmailService.java           ← Infrastructure service lẫn với business
│   └── SmsService.java
│
├── domain/                        ❌ 14 entities + 17 DTOs không phân tách
│   ├── User.java
│   ├── Role.java
│   ├── RefreshToken.java
│   ├── Verification.java
│   ├── Product.java
│   ├── ProductAttribute.java
│   ├── Category.java
│   ├── CategoryAttribute.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Shop.java
│   └── dto/                       ← 17 DTOs trong cùng package
│       ├── LoginDTO.java
│       ├── GoogleLoginDTO.java
│       ├── FacebookLoginDTO.java
│       ├── RefreshTokenRequest.java
│       ├── AuthTokenResponse.java
│       ├── JwtResponse.java
│       ├── UserInfoDTO.java
│       ├── RegisterDTO.java
│       ├── CheckContactDTO.java
│       ├── ForgotPasswordDTO.java
│       ├── ResetPasswordDTO.java
│       ├── VerifyOtpDTO.java
│       ├── ProductDTO.java
│       ├── CategoryDTO.java
│       ├── AttributeDTO.java
│       ├── CartItemDTO.java
│       └── ResponseDTO.java
│
├── repository/                    ❌ 8 repositories không nhóm theo module
│   ├── UserRepository.java
│   ├── RefreshTokenRepository.java
│   ├── VerificationRepository.java
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── CategoryAttributeRepository.java
│   ├── CartRepository.java
│   └── CartItemRepository.java
│
├── config/                        ❌ Config không rõ ràng (Security + CORS + Web)
│   ├── SecurityConfig.java
│   ├── JwtFilter.java
│   ├── CorsConfig.java
│   └── WebConfig.java
│
└── util/                          ❌ Chỉ có TokenProvider (thiếu các util khác)
    └── TokenProvider.java
```

**Vấn đề cụ thể**:

1. **Tìm kiếm khó**: Để làm việc với Auth, phải mở 4 packages (controller/service/domain/repository)
2. **Dependency hell**: Service phụ thuộc chéo, khó refactor
3. **Không có exception layer**: Thiếu custom exceptions, error handling không nhất quán
4. **DTOs lộn xộn**: Không biết DTO nào thuộc module nào
5. **Config rải rác**: Security config ở nhiều nơi

---

### Cấu trúc Đề xuất (Feature/Module-based + Clean Architecture)

```
src/main/java/com/PBL6/Ecommerce/
│
├── EcommerceApplication.java
│
├── common/                        ✅ Shared components
│   ├── dto/
│   │   └── ResponseDTO.java      ← Generic response wrapper
│   ├── exception/                 ✅ NEW: Centralized exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ValidationException.java
│   │   └── RateLimitException.java
│   └── util/                      ✅ Common utilities
│       ├── DateUtils.java
│       └── StringUtils.java
│
├── security/                      ✅ Complete Auth & Security module
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java        ✅ NEW: JWT-specific config
│   │   └── CorsConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── GoogleAuthController.java
│   │   ├── FacebookAuthController.java
│   │   ├── RefreshTokenController.java
│   │   └── LogoutController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── GoogleAuthService.java
│   │   ├── FacebookAuthService.java
│   │   ├── RefreshTokenService.java
│   │   ├── TokenBlacklistService.java
│   │   ├── TokenCleanupScheduler.java
│   │   └── LoginAttemptService.java
│   ├── domain/
│   │   ├── RefreshToken.java
│   │   └── Verification.java
│   ├── repository/
│   │   ├── RefreshTokenRepository.java
│   │   └── VerificationRepository.java
│   ├── dto/                       ✅ Auth-specific DTOs only
│   │   ├── LoginDTO.java
│   │   ├── GoogleLoginDTO.java
│   │   ├── FacebookLoginDTO.java
│   │   ├── AuthTokenResponse.java
│   │   ├── RefreshTokenRequest.java
│   │   └── JwtResponse.java
│   ├── filter/
│   │   └── JwtFilter.java
│   └── util/
│       └── TokenProvider.java
│
├── user/                          ✅ User management module
│   ├── controller/
│   │   ├── UserController.java
│   │   └── ForgotPasswordController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── ForgotPasswordService.java
│   ├── domain/
│   │   ├── User.java
│   │   └── Role.java
│   ├── repository/
│   │   └── UserRepository.java
│   └── dto/
│       ├── UserInfoDTO.java
│       ├── RegisterDTO.java
│       ├── CheckContactDTO.java
│       ├── ForgotPasswordDTO.java
│       ├── ResetPasswordDTO.java
│       └── VerifyOtpDTO.java
│
├── product/                       ✅ Product & Category module
│   ├── controller/
│   │   ├── ProductController.java
│   │   └── CategoryController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   └── CategoryService.java
│   ├── domain/
│   │   ├── Product.java
│   │   ├── ProductAttribute.java
│   │   ├── Category.java
│   │   └── CategoryAttribute.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── CategoryRepository.java
│   │   └── CategoryAttributeRepository.java
│   └── dto/
│       ├── ProductDTO.java
│       ├── CategoryDTO.java
│       └── AttributeDTO.java
│
├── cart/                          ✅ Shopping Cart module
│   ├── controller/
│   │   └── CartController.java
│   ├── service/
│   │   └── CartService.java
│   ├── domain/
│   │   ├── Cart.java
│   │   └── CartItem.java
│   ├── repository/
│   │   ├── CartRepository.java
│   │   └── CartItemRepository.java
│   └── dto/
│       └── CartItemDTO.java
│
├── order/                         ✅ Order module (ready for future development)
│   ├── controller/
│   ├── service/
│   ├── domain/
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── repository/
│   └── dto/
│
├── shop/                          ✅ Shop/Vendor module (future)
│   ├── controller/
│   ├── service/
│   ├── domain/
│   │   └── Shop.java
│   ├── repository/
│   └── dto/
│
├── notification/                  ✅ Infrastructure services
│   └── service/
│       ├── EmailService.java
│       └── SmsService.java
│
└── config/                        ✅ App-level config only
    └── WebConfig.java
```

---

## 🎯 Lợi ích Chi tiết

### 1. **Modularity & Maintainability**

**Trước**:

```java
// Phải mở 4 packages để hiểu Auth flow
controller/AuthController.java
service/AuthService.java
domain/dto/LoginDTO.java
repository/UserRepository.java
```

**Sau**:

```java
// Tất cả Auth logic trong 1 module
security/
  ├── controller/AuthController.java
  ├── service/AuthService.java
  ├── dto/LoginDTO.java
  └── repository/...
```

### 2. **Giảm Coupling**

**Trước**:

```java
// CartService phụ thuộc vào ProductService, UserService
public class CartService {
    @Autowired ProductService productService; // ❌ Cross-package dependency
    @Autowired UserService userService;       // ❌
}
```

**Sau**:

```java
// Module boundaries rõ ràng
cart.service.CartService → product.domain.Product  // ✅ Chỉ phụ thuộc domain
cart.service.CartService → user.domain.User        // ✅
```

### 3. **Testability**

**Trước**:

```java
// Test phải mock nhiều service từ nhiều package
@Test
void testAddToCart() {
    // Mock ProductService từ service package
    // Mock UserService từ service package
    // ...
}
```

**Sau**:

```java
// Test theo module, dependencies rõ ràng
@Test
void testAddToCart() {
    // Mock chỉ trong cart module
    // Dependencies injection rõ ràng
}
```

### 4. **Scalability**

**Trước**: Thêm feature mới → thêm vào controller/, service/, domain/ → càng lúc càng lộn xộn

**Sau**: Thêm feature mới → tạo module mới hoàn toàn độc lập

```
payment/              ← NEW module
  ├── controller/
  ├── service/
  ├── domain/
  └── dto/
```

### 5. **Team Collaboration**

**Trước**: 2 dev làm Auth và Product → conflict merge ở controller/, service/

**Sau**: Dev A làm `security/`, Dev B làm `product/` → không conflict

---

## 🔄 Migration Plan

### Phase 1: Preparation (1 ngày)

1. **Tạo branch mới**:

```bash
git checkout -b refactor/modular-architecture
```

2. **Backup hiện tại**:

```bash
git tag before-restructure
```

3. **Tạo packages mới** (empty):

```bash
mkdir -p src/main/java/com/PBL6/Ecommerce/{common,security,user,product,cart,order,shop,notification}
```

### Phase 2: Move Files (2-3 ngày)

**Thứ tự di chuyển** (từ ít phụ thuộc → nhiều phụ thuộc):

1. **Common package** (không phụ thuộc gì):

   - ResponseDTO → common/dto/
   - Tạo exception classes → common/exception/
   - GlobalExceptionHandler → common/exception/

2. **Security module** (phụ thuộc User):

   - Auth controllers → security/controller/
   - Auth services → security/service/
   - RefreshToken, Verification → security/domain/
   - Auth DTOs → security/dto/
   - JwtFilter, TokenProvider → security/filter/, security/util/
   - SecurityConfig, CorsConfig → security/config/

3. **User module**:

   - UserController, ForgotPasswordController → user/controller/
   - UserService, ForgotPasswordService → user/service/
   - User, Role → user/domain/
   - User DTOs → user/dto/
   - UserRepository, VerificationRepository → user/repository/

4. **Product module**:

   - Product controllers → product/controller/
   - Product services → product/service/
   - Product entities → product/domain/
   - Product DTOs → product/dto/
   - Product repositories → product/repository/

5. **Cart module**:

   - CartController → cart/controller/
   - CartService → cart/service/
   - Cart, CartItem → cart/domain/
   - Cart DTOs → cart/dto/
   - Cart repositories → cart/repository/

6. **Order & Shop modules**:

   - Order, OrderItem → order/domain/
   - Shop → shop/domain/

7. **Notification module**:

   - EmailService, SmsService → notification/service/

8. **Config**:
   - WebConfig → config/

### Phase 3: Update Imports (1 ngày)

**Tool hỗ trợ**: IntelliJ IDEA auto-import refactoring

Hoặc dùng script:

```bash
# Find and replace imports
find . -name "*.java" -exec sed -i 's/com\.PBL6\.Ecommerce\.controller/com.PBL6.Ecommerce.security.controller/g' {} +
```

### Phase 4: Testing (2 ngày)

1. **Unit tests**: Chạy từng module
2. **Integration tests**: Chạy toàn bộ flow
3. **Manual testing**: Postman collection
4. **Build verification**: `mvn clean install`

### Phase 5: Documentation (1 ngày)

1. Update README.md
2. Tạo module architecture diagram
3. Update API documentation

**Tổng thời gian ước tính**: 7-10 ngày (1.5-2 tuần)

---

## 📝 Migration Script (PowerShell)

Tôi sẽ cung cấp script tự động để di chuyển files:

```powershell
# migrate.ps1
$baseDir = "src/main/java/com/PBL6/Ecommerce"

# 1. Create new directories
$modules = @(
    "common/dto",
    "common/exception",
    "common/util",
    "security/config",
    "security/controller",
    "security/service",
    "security/domain",
    "security/repository",
    "security/dto",
    "security/filter",
    "security/util",
    "user/controller",
    "user/service",
    "user/domain",
    "user/repository",
    "user/dto",
    "product/controller",
    "product/service",
    "product/domain",
    "product/repository",
    "product/dto",
    "cart/controller",
    "cart/service",
    "cart/domain",
    "cart/repository",
    "cart/dto",
    "order/domain",
    "shop/domain",
    "notification/service"
)

foreach ($module in $modules) {
    New-Item -Path "$baseDir/$module" -ItemType Directory -Force
}

# 2. Move files with package update
function Move-JavaFile {
    param($source, $dest, $oldPackage, $newPackage)

    $content = Get-Content $source -Raw
    $content = $content -replace $oldPackage, $newPackage
    Set-Content -Path $dest -Value $content
    Remove-Item $source
}

# Example: Move AuthController
Move-JavaFile `
    -source "$baseDir/controller/AuthController.java" `
    -dest "$baseDir/security/controller/AuthController.java" `
    -oldPackage "package com.PBL6.Ecommerce.controller" `
    -newPackage "package com.PBL6.Ecommerce.security.controller"

# ... repeat for all files
```

---

## 🏗️ Package Info Documentation

Mỗi module sẽ có `package-info.java` để document:

```java
/**
 * Security Module - Authentication & Authorization
 *
 * <p>This module contains all security-related components:
 * <ul>
 *   <li>JWT token management (creation, validation, rotation)</li>
 *   <li>OAuth2 integration (Google, Facebook)</li>
 *   <li>Refresh token mechanism with rotation</li>
 *   <li>Logout and token blacklisting</li>
 *   <li>Rate limiting for auth endpoints</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 * <ul>
 *   <li>user module (User domain)</li>
 *   <li>common module (ResponseDTO, exceptions)</li>
 * </ul>
 *
 * @since 2.0
 * @author PBL6 Team
 */
package com.PBL6.Ecommerce.security;
```

---

## 🎨 Architecture Diagram

### Before (Layer-based)

```
┌─────────────────────────────────────────┐
│           Controllers Layer             │
│  (Auth, User, Product, Cart mixed)      │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│            Services Layer               │
│  (Business + Infrastructure mixed)      │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│          Domain/DTO Layer               │
│     (Entities + DTOs in one place)      │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│         Repository Layer                │
└─────────────────────────────────────────┘
```

### After (Module-based)

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Security   │  │     User     │  │   Product    │  │     Cart     │
│              │  │              │  │              │  │              │
│ ┌──────────┐ │  │ ┌──────────┐ │  │ ┌──────────┐ │  │ ┌──────────┐ │
│ │Controller│ │  │ │Controller│ │  │ │Controller│ │  │ │Controller│ │
│ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │
│      │       │  │      │       │  │      │       │  │      │       │
│ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │
│ │ Service  │ │  │ │ Service  │ │  │ │ Service  │ │  │ │ Service  │ │
│ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │
│      │       │  │      │       │  │      │       │  │      │       │
│ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │
│ │  Domain  │ │  │ │  Domain  │ │  │ │  Domain  │ │  │ │  Domain  │ │
│ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │
│      │       │  │      │       │  │      │       │  │      │       │
│ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │  │ ┌────▼─────┐ │
│ │Repository│ │  │ │Repository│ │  │ │Repository│ │  │ │Repository│ │
│ └──────────┘ │  │ └──────────┘ │  │ └──────────┘ │  │ └──────────┘ │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
        │                 │                 │                 │
        └─────────────────┴─────────────────┴─────────────────┘
                                 │
                     ┌───────────▼──────────┐
                     │   Common/Shared      │
                     │ (DTO, Exceptions)    │
                     └──────────────────────┘
```

---

## ⚠️ Risks & Mitigation

| Risk                     | Impact | Mitigation                                             |
| ------------------------ | ------ | ------------------------------------------------------ |
| Import errors after move | High   | Use IDE refactoring, run tests after each module       |
| Lost functionality       | High   | Comprehensive testing, git tag before start            |
| Merge conflicts          | Medium | Do refactor in dedicated branch, communicate with team |
| Learning curve           | Low    | Provide documentation, team training                   |

---

## 🚀 Future Enhancements (Post-refactor)

1. **API Versioning**: `/api/v1/security/auth`, `/api/v2/...`
2. **Hexagonal Architecture**: Separate domain from infrastructure
3. **CQRS**: Command/Query separation for complex modules
4. **Event-driven**: Module communication via events (Spring Events)
5. **Microservices Ready**: Each module can become a service
6. **Multi-tenancy**: Shop module for multi-vendor support

---

## 📊 Metrics & Success Criteria

| Metric           | Before                              | Target After                     | Measurement         |
| ---------------- | ----------------------------------- | -------------------------------- | ------------------- |
| Package cohesion | Low (mixed concerns)                | High (single responsibility)     | Code review         |
| Coupling         | High (cross-package dependencies)   | Low (clear boundaries)           | Dependency analysis |
| Onboarding time  | ~3 days (find code across packages) | ~1 day (module-based navigation) | Team feedback       |
| Build time       | Baseline                            | Same or better                   | CI/CD metrics       |
| Test coverage    | Current %                           | +10% (easier to test modules)    | JaCoCo report       |

---

## ✅ Approval Checklist

- [ ] Team review and approval
- [ ] Create backup branch/tag
- [ ] Allocate 2 weeks for migration
- [ ] Prepare test environment
- [ ] Update CI/CD pipelines
- [ ] Schedule team training session
- [ ] Update project documentation

---

## 📞 Next Steps

1. **Review đề xuất này** → Approve hoặc request changes
2. **Tôi sẽ tạo migration script** → PowerShell + Bash
3. **Thực hiện migration từng phase** → Commit sau mỗi module
4. **Testing & verification** → Đảm bảo không regression
5. **Merge vào main** → Production ready

---

**Câu hỏi cần trả lời**:

1. Bạn có muốn tôi triển khai ngay không? (Y/N)
2. Có module nào cần ưu tiên không? (security/product/cart/...)
3. Có muốn giữ cấu trúc cũ song song trong 1 sprint transition không?
4. Có cần tôi tạo migration script tự động không?

**Recommended**: Approve và cho phép tôi thực hiện Phase 1-2 (tạo structure + move security module) để bạn xem kết quả cụ thể trước khi commit toàn bộ.

---

**Người đề xuất**: GitHub Copilot AI  
**Ngày tạo**: 19/10/2025  
**Trạng thái**: ⏳ Chờ phê duyệt
