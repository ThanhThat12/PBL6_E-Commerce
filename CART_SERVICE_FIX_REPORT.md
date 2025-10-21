# ✅ Cart Service & API - Fixed & Refactored

**Ngày:** 21/10/2025  
**Người thực hiện:** GitHub Copilot + ThanhThat12  
**Trạng thái:** ✅ Đã hoàn thành

---

## 🎯 Vấn Đề Ban Đầu

### ❌ Lỗi Critical

```java
// CartService.java - LỖI
if (product.getStock() < quantity) {  // ❌ Product không có getStock()
    throw new RuntimeException("Sản phẩm không đủ tồn kho");
}
```

**Nguyên nhân:**

- `Product` entity không có field `stock`
- Stock chỉ tồn tại trong `ProductVariant` entity
- CartItem đang reference `Product` thay vì `ProductVariant`
- CartController bị comment toàn bộ

---

## 🔧 Các Thay Đổi Đã Thực Hiện

### 1. **CartItem.java** - Changed Relationship

**Trước:**

```java
@ManyToOne
@JoinColumn(name = "product_id")
private Product product;  // ❌ Sai
```

**Sau:**

```java
@ManyToOne
@JoinColumn(name = "product_variant_id")
private ProductVariant productVariant;  // ✅ Đúng
```

**Lý do:**

- Stock được quản lý ở level ProductVariant, không phải Product
- Một Product có nhiều variants (màu, size...), mỗi variant có stock riêng

---

### 2. **CartItemRepository.java** - Updated Methods

**Trước:**

```java
Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
```

**Sau:**

```java
Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);
```

---

### 3. **CartService.java** - Complete Refactor

**Thay đổi chính:**

#### Dependency Injection

```java
// ❌ Trước - Không cần thiết
private final ProductRepository productRepository;
private final UserRepository userRepository;

// ✅ Sau - Clean & focused
private final ProductVariantRepository productVariantRepository;
```

#### addToCart() Method

```java
// ✅ Sau
public Cart addToCart(User user, Long productVariantId, int quantity) {
    ProductVariant productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant không tồn tại"));

    // Kiểm tra tồn kho từ ProductVariant
    if (productVariant.getStock() < quantity) {
        throw new RuntimeException("Sản phẩm không đủ tồn kho. Tồn kho hiện tại: " + productVariant.getStock());
    }

    CartItem cartItem = cartItemRepository.findByCartAndProductVariant(cart, productVariant)
            .orElseGet(() -> {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProductVariant(productVariant);  // ✅ Dùng ProductVariant
                item.setQuantity(0);
                return item;
            });

    // ... rest of logic
}
```

#### Các methods khác

- `updateQuantity()` - Cập nhật với productVariantId
- `removeFromCart()` - Xóa với productVariantId
- `clearCart()` - Không đổi
- `getCartItemCount()` - Không đổi

---

### 4. **DTOs Mới** - Created 4 New DTOs

#### AddToCartRequest.java

```java
public class AddToCartRequest {
    private Long productVariantId;  // ✅ Dùng productVariantId
    private int quantity;
}
```

#### UpdateCartQuantityRequest.java

```java
public class UpdateCartQuantityRequest {
    private int quantity;
}
```

#### CartItemResponseDTO.java

```java
public class CartItemResponseDTO {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String productSku;
    private String productImage;
    private BigDecimal price;
    private Integer stock;
    private int quantity;
    private BigDecimal subtotal;  // ✅ Tính sẵn subtotal
}
```

#### CartResponseDTO.java

```java
public class CartResponseDTO {
    private Long cartId;
    private List<CartItemResponseDTO> items;
    private int totalItems;
    private BigDecimal totalAmount;  // ✅ Tổng tiền giỏ hàng
}
```

---

### 5. **CartController.java** - Completely Rewritten

**Trước:** Bị comment toàn bộ

**Sau:** 6 API endpoints hoàn chỉnh

```java
@RestController
@RequestMapping("/api/cart")
public class CartController {
    // ... dependencies ...

    @PostMapping                              // Thêm vào giỏ
    @GetMapping                               // Xem giỏ hàng
    @PutMapping("/{productVariantId}")        // Cập nhật số lượng
    @DeleteMapping("/{productVariantId}")     // Xóa khỏi giỏ
    @DeleteMapping("/clear")                  // Xóa toàn bộ giỏ
    @GetMapping("/count")                     // Đếm số items
}
```

**Helper Methods:**

- `getCurrentUser()` - Lấy user từ SecurityContext
- `convertToCartResponseDTO()` - Convert Cart entity → DTO với tính toán subtotal & total

---

## 📋 API Endpoints Chi Tiết

### 1. **POST /api/cart** - Thêm vào giỏ hàng

**Request:**

```json
{
  "productVariantId": 1,
  "quantity": 2
}
```

**Response (Success):**

```json
{
  "code": 200,
  "error": null,
  "message": "Đã thêm vào giỏ hàng",
  "data": "Sản phẩm đã được thêm vào giỏ hàng thành công"
}
```

**Response (Error - Hết stock):**

```json
{
  "code": 400,
  "error": "Số lượng vượt quá tồn kho. Tồn kho hiện tại: 5",
  "message": "Thất bại",
  "data": null
}
```

---

### 2. **GET /api/cart** - Xem giỏ hàng

**Response:**

```json
{
  "code": 200,
  "error": null,
  "message": "Lấy giỏ hàng thành công",
  "data": {
    "cartId": 1,
    "items": [
      {
        "id": 1,
        "productVariantId": 5,
        "productName": "iPhone 15 Pro",
        "productSku": "IPH15-PRO-256-BLK",
        "productImage": "https://example.com/image.jpg",
        "price": 29990000,
        "stock": 10,
        "quantity": 2,
        "subtotal": 59980000
      }
    ],
    "totalItems": 2,
    "totalAmount": 59980000
  }
}
```

---

### 3. **PUT /api/cart/{productVariantId}** - Cập nhật số lượng

**Request:**

```json
{
  "quantity": 5
}
```

**Response:**

```json
{
  "code": 200,
  "error": null,
  "message": "Cập nhật thành công",
  "data": "Số lượng sản phẩm đã được cập nhật"
}
```

---

### 4. **DELETE /api/cart/{productVariantId}** - Xóa khỏi giỏ

**Response:**

```json
{
  "code": 200,
  "error": null,
  "message": "Xóa thành công",
  "data": "Sản phẩm đã được xóa khỏi giỏ hàng"
}
```

---

### 5. **DELETE /api/cart/clear** - Xóa toàn bộ giỏ

**Response:**

```json
{
  "code": 200,
  "error": null,
  "message": "Xóa thành công",
  "data": "Giỏ hàng đã được làm trống"
}
```

---

### 6. **GET /api/cart/count** - Đếm số items

**Response:**

```json
{
  "code": 200,
  "error": null,
  "message": "Lấy số lượng thành công",
  "data": 3
}
```

---

## 🗄️ Database Schema Changes

### cart_items Table - Column Change

**Trước:**

```sql
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT,
    product_id BIGINT,  -- ❌ Sai
    quantity INT,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)  -- ❌
);
```

**Sau:**

```sql
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT,
    product_variant_id BIGINT,  -- ✅ Đúng
    quantity INT,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)  -- ✅
);
```

**⚠️ Migration Required:**

```sql
-- Nếu có data cũ, cần migrate:
ALTER TABLE cart_items
    DROP FOREIGN KEY fk_cart_items_product;

ALTER TABLE cart_items
    CHANGE COLUMN product_id product_variant_id BIGINT;

ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_product_variant
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id);
```

---

## ✅ Testing Checklist

### Unit Tests (Service Layer)

- [ ] CartService.addToCart() - Valid variant
- [ ] CartService.addToCart() - Out of stock
- [ ] CartService.addToCart() - Variant not found
- [ ] CartService.addToCart() - Add same variant twice (should update quantity)
- [ ] CartService.updateQuantity() - Valid update
- [ ] CartService.updateQuantity() - Exceed stock
- [ ] CartService.removeFromCart() - Valid removal
- [ ] CartService.clearCart() - Clear all items
- [ ] CartService.getCartItemCount() - Calculate total

### Integration Tests (Controller Layer)

- [ ] POST /api/cart - Success case
- [ ] POST /api/cart - Unauthorized (401)
- [ ] POST /api/cart - Out of stock (400)
- [ ] GET /api/cart - Empty cart
- [ ] GET /api/cart - Cart with items
- [ ] PUT /api/cart/{id} - Update quantity
- [ ] DELETE /api/cart/{id} - Remove item
- [ ] DELETE /api/cart/clear - Clear cart
- [ ] GET /api/cart/count - Count items

### Edge Cases

- [ ] Add to cart with quantity = 0
- [ ] Add to cart with negative quantity
- [ ] Update to quantity > stock
- [ ] Remove non-existent item
- [ ] Multiple concurrent add operations

---

## 🔒 Security & Authentication

**✅ Tất cả endpoints yêu cầu authentication:**

- Sử dụng `SecurityContextHolder` để lấy user hiện tại
- Mỗi user chỉ xem/sửa được giỏ hàng của mình
- Không thể thao tác với giỏ hàng của user khác

**Error Handling:**

- `RuntimeException("Chưa đăng nhập")` nếu không authenticated
- `RuntimeException("User không tồn tại")` nếu user không tìm thấy trong DB

---

## 📊 Performance Considerations

### Optimizations

1. ✅ **Lazy Loading:** ProductVariant → Product relation
2. ✅ **Single Query:** findByCartAndProductVariant thay vì 2 queries
3. ✅ **Bulk Delete:** deleteByCartId() sử dụng @Query

### Potential Improvements

- [ ] Add caching cho cart data (Redis)
- [ ] Batch operations cho multiple items
- [ ] Pagination cho cart items (nếu cart rất lớn)
- [ ] Add indexes: cart_id, product_variant_id

---

## 🚀 Next Steps

### Immediate (Đã xong)

- [x] Refactor CartItem entity
- [x] Update CartItemRepository
- [x] Refactor CartService
- [x] Create DTOs
- [x] Implement CartController
- [x] Test basic flow

### Short-term (Cần làm)

- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Add validation annotations (@Valid, @Min, @Max)
- [ ] Add API documentation (Swagger annotations)
- [ ] Handle concurrent operations (optimistic locking)

### Long-term

- [ ] Implement cart expiration (auto-clear after X days)
- [ ] Add "Save for later" feature
- [ ] Implement cart sharing (send cart link)
- [ ] Add cart recommendations
- [ ] Implement guest cart (before login)

---

## 📝 Notes & Best Practices

### Why ProductVariant instead of Product?

**Scenario:** iPhone 15 Pro có nhiều variants:

- iPhone 15 Pro 256GB Black - Stock: 10
- iPhone 15 Pro 256GB White - Stock: 5
- iPhone 15 Pro 512GB Black - Stock: 3

Nếu dùng `Product`:

- ❌ Không biết user chọn màu/dung lượng nào
- ❌ Không biết variant nào còn stock
- ❌ Không tính đúng giá (variants có giá khác nhau)

Nếu dùng `ProductVariant`:

- ✅ Biết chính xác variant user chọn
- ✅ Check stock chính xác
- ✅ Tính giá chính xác
- ✅ Có thể thêm nhiều variants khác nhau vào cart

---

## 🎯 Summary

**Đã fix:**

- ✅ Product.getStock() error → Dùng ProductVariant.getStock()
- ✅ CartItem relationship → Product → ProductVariant
- ✅ CartService logic → Hoàn toàn refactored
- ✅ CartController → Uncommented & rewritten
- ✅ DTOs → Created 4 new DTOs
- ✅ API endpoints → 6 endpoints đầy đủ

**Files changed:**

1. CartItem.java
2. CartItemRepository.java
3. CartService.java
4. CartController.java
5. AddToCartRequest.java (new)
6. UpdateCartQuantityRequest.java (new)
7. CartItemResponseDTO.java (new)
8. CartResponseDTO.java (new)

**Compile status:** ✅ No errors (IDE có thể cần reindex)

---

**Tác giả:** GitHub Copilot + ThanhThat12  
**Ngày hoàn thành:** 21/10/2025  
**Review status:** 🟡 Cần test & review
