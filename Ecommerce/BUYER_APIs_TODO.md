# 🛒 BUYER APIs - Danh sách cần implement

## 📋 Tổng quan

Phân tích các API còn thiếu cho **BUYER** dựa trên database `ecommerce_complete_v2.1.sql`

---

## ✅ APIs ĐÃ CÓ (Đã implement)

### **1. Authentication & Profile**
- ✅ POST `/api/auth/register` - Đăng ký
- ✅ POST `/api/auth/login` - Đăng nhập
- ✅ POST `/api/auth/refresh` - Refresh token
- ✅ GET `/api/profile` - Xem profile
- ✅ PUT `/api/profile` - Cập nhật profile

### **2. Address Management**
- ✅ GET `/api/addresses` - Danh sách địa chỉ
- ✅ POST `/api/addresses` - Thêm địa chỉ
- ✅ PUT `/api/addresses/{id}` - Sửa địa chỉ
- ✅ DELETE `/api/addresses/{id}` - Xóa địa chỉ
- ✅ POST `/api/addresses/{id}/set-primary` - Đặt địa chỉ mặc định

### **3. Products (Basic)**
- ✅ GET `/api/products` - Danh sách sản phẩm (pagination)
- ✅ GET `/api/products/{id}` - Chi tiết sản phẩm
- ✅ GET `/api/products/search` - Tìm kiếm sản phẩm

### **4. Cart (Basic)**
- ✅ GET `/api/cart` - Xem giỏ hàng
- ✅ POST `/api/cart/items` - Thêm vào giỏ
- ✅ PUT `/api/cart/items/{id}` - Cập nhật số lượng
- ✅ DELETE `/api/cart/items/{id}` - Xóa khỏi giỏ

---

## ❌ APIs CẦN BỔ SUNG (Critical)

---

## 📦 **1. PRODUCT REVIEWS** (Quan trọng nhất)

### **1.1. Xem reviews của sản phẩm**

```java
GET /api/products/{productId}/reviews
```

**Query params:**
- `page` (int, default: 0)
- `size` (int, default: 10)
- `rating` (optional, 1-5) - Lọc theo số sao
- `sortBy` (optional: "newest", "oldest", "highest", "lowest")

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "user": {
        "id": 3,
        "username": "buyer1",
        "fullName": "Nguyễn Văn A",
        "avatarUrl": "https://..."
      },
      "rating": 5,
      "comment": "Sản phẩm rất tốt, giao hàng nhanh!",
      "images": [
        "https://image1.jpg",
        "https://image2.jpg"
      ],
      "verifiedPurchase": true,
      "sellerResponse": "Cảm ơn bạn đã mua hàng!",
      "sellerResponseDate": "2025-10-29T10:30:00",
      "createdAt": "2025-10-28T15:20:00",
      "updatedAt": "2025-10-28T15:20:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "currentPage": 0,
  "size": 10
}
```

**Backend logic:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductReviewController {
    
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Integer rating,
        @RequestParam(defaultValue = "newest") String sortBy
    ) {
        // 1. Validate product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found"));
        
        // 2. Build sort
        Sort sort = switch(sortBy) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "highest" -> Sort.by("rating").descending();
            case "lowest" -> Sort.by("rating").ascending();
            default -> Sort.by("createdAt").descending(); // newest
        };
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 3. Query reviews
        Page<ProductReview> reviews;
        if (rating != null) {
            reviews = reviewRepository.findByProductIdAndRating(
                productId, rating, pageable
            );
        } else {
            reviews = reviewRepository.findByProductId(productId, pageable);
        }
        
        // 4. Map to DTO (include user info, hide sensitive data)
        Page<ProductReviewDTO> result = reviews.map(this::mapToDTO);
        
        return ResponseEntity.ok(result);
    }
}
```

---

### **1.2. Tạo review (sau khi mua hàng)**

```java
POST /api/products/{productId}/reviews
```

**Request body:**
```json
{
  "orderId": 123,
  "rating": 5,
  "comment": "Sản phẩm rất tốt!",
  "images": [
    "https://image1.jpg",
    "https://image2.jpg"
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "rating": 5,
  "comment": "Sản phẩm rất tốt!",
  "images": ["https://image1.jpg"],
  "verifiedPurchase": true,
  "createdAt": "2025-10-30T10:00:00"
}
```

**Backend logic:**
```java
@PostMapping("/{productId}/reviews")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<ProductReviewDTO> createReview(
    @PathVariable Long productId,
    @RequestBody @Valid CreateReviewRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Get current user
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    // 2. Validate product exists
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));
    
    // 3. CRITICAL: Validate user đã mua sản phẩm này
    Order order = orderRepository.findById(request.getOrderId())
        .orElseThrow(() -> new NotFoundException("Order not found"));
    
    // Check order belongs to user
    if (!order.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Order không thuộc về bạn");
    }
    
    // Check order status = COMPLETED
    if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
        throw new BadRequestException("Chỉ review được khi đơn hàng đã hoàn thành");
    }
    
    // Check order contains this product
    boolean hasProduct = order.getItems().stream()
        .anyMatch(item -> item.getProduct().getId().equals(productId));
    
    if (!hasProduct) {
        throw new BadRequestException("Đơn hàng không chứa sản phẩm này");
    }
    
    // 4. Check if already reviewed
    if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
        throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
    }
    
    // 5. Create review
    ProductReview review = new ProductReview();
    review.setProduct(product);
    review.setUser(user);
    review.setOrder(order);
    review.setRating(request.getRating());
    review.setComment(request.getComment());
    review.setImages(objectMapper.writeValueAsString(request.getImages())); // JSON
    
    reviewRepository.save(review);
    
    // 6. Trigger sẽ tự động update product.rating và shop.rating
    
    return ResponseEntity.status(201).body(mapToDTO(review));
}
```

**Validation:**
```java
public class CreateReviewRequest {
    @NotNull
    private Long orderId;
    
    @Min(1) @Max(5)
    @NotNull
    private Integer rating;
    
    @Size(max = 1000)
    private String comment;
    
    private List<String> images; // URLs uploaded to S3/Cloudinary
}
```

---

### **1.3. Sửa review (chỉ trong 7 ngày)**

```java
PUT /api/reviews/{reviewId}
```

**Request body:**
```json
{
  "rating": 4,
  "comment": "Cập nhật: Sau 1 tuần dùng vẫn tốt",
  "images": ["https://new-image.jpg"]
}
```

**Backend logic:**
```java
@PutMapping("/reviews/{reviewId}")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<ProductReviewDTO> updateReview(
    @PathVariable Long reviewId,
    @RequestBody @Valid UpdateReviewRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Find review
    ProductReview review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));
    
    // 2. Check ownership
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    if (!review.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Bạn không có quyền sửa review này");
    }
    
    // 3. Check time limit (7 days)
    LocalDateTime createdAt = review.getCreatedAt();
    LocalDateTime now = LocalDateTime.now();
    
    if (Duration.between(createdAt, now).toDays() > 7) {
        throw new BadRequestException("Chỉ có thể sửa review trong vòng 7 ngày");
    }
    
    // 4. Update
    review.setRating(request.getRating());
    review.setComment(request.getComment());
    review.setImages(objectMapper.writeValueAsString(request.getImages()));
    
    reviewRepository.save(review);
    
    // Trigger tự động update rating
    
    return ResponseEntity.ok(mapToDTO(review));
}
```

---

### **1.4. Xóa review**

```java
DELETE /api/reviews/{reviewId}
```

**Backend logic:**
```java
@DeleteMapping("/reviews/{reviewId}")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<Void> deleteReview(
    @PathVariable Long reviewId,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // Similar validation như update
    // Check ownership
    // Delete review
    // Trigger tự động update rating
    
    reviewRepository.deleteById(reviewId);
    
    return ResponseEntity.noContent().build();
}
```

---

### **1.5. Xem reviews của mình**

```java
GET /api/my-reviews
```

**Query params:**
- `page`, `size`

**Response:** Danh sách reviews user đã viết

**Backend logic:**
```java
@GetMapping("/my-reviews")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<Page<ProductReviewDTO>> getMyReviews(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @AuthenticationPrincipal UserDetails userDetails
) {
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    Page<ProductReview> reviews = reviewRepository.findByUserId(user.getId(), pageable);
    
    return ResponseEntity.ok(reviews.map(this::mapToDTO));
}
```

---

## 🎯 **2. SHOP INFORMATION**

### **2.1. Xem thông tin shop**

```java
GET /api/shops/{shopId}
```

**Response:**
```json
{
  "id": 1,
  "name": "Nike Official Store",
  "description": "Cửa hàng chính hãng Nike",
  "address": "123 Nguyễn Huệ, Q1, HCM",
  "phone": "0901234567",
  "email": "contact@nike.vn",
  "rating": 4.8,
  "reviewCount": 1234,
  "productCount": 56,
  "status": "ACTIVE",
  "createdAt": "2025-01-01T00:00:00"
}
```

**Backend logic:**
```java
@GetMapping("/shops/{shopId}")
public ResponseEntity<ShopDTO> getShop(@PathVariable Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new NotFoundException("Shop not found"));
    
    // Count products
    long productCount = productRepository.countByShopId(shopId);
    
    ShopDTO dto = mapToDTO(shop);
    dto.setProductCount(productCount);
    
    return ResponseEntity.ok(dto);
}
```

---

### **2.2. Xem sản phẩm của shop**

```java
GET /api/shops/{shopId}/products
```

**Query params:**
- `page`, `size`
- `categoryId` (optional)
- `sortBy` (price_asc, price_desc, newest, popular)

**Response:** Page<ProductDTO>

---

### **2.3. Xem reviews của shop**

```java
GET /api/shops/{shopId}/reviews
```

**Response:** Tất cả reviews của các products thuộc shop này

**Backend logic:**
```java
@GetMapping("/shops/{shopId}/reviews")
public ResponseEntity<Page<ProductReviewDTO>> getShopReviews(
    @PathVariable Long shopId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    // Validate shop exists
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow();
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    // Query reviews của tất cả products thuộc shop
    Page<ProductReview> reviews = reviewRepository.findByProductShopId(shopId, pageable);
    
    return ResponseEntity.ok(reviews.map(this::mapToDTO));
}
```

**Repository method:**
```java
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    @Query("SELECT pr FROM ProductReview pr " +
           "JOIN pr.product p " +
           "WHERE p.shop.id = :shopId " +
           "ORDER BY pr.createdAt DESC")
    Page<ProductReview> findByProductShopId(@Param("shopId") Long shopId, Pageable pageable);
}
```

---

## 💰 **3. CHECKOUT & PAYMENT**

### **3.1. Calculate shipping fee (GHN)**

```java
POST /api/checkout/calculate-shipping
```

**Request body:**
```json
{
  "shopId": 1,
  "addressId": 2,
  "items": [
    {
      "variantId": 10,
      "quantity": 2
    }
  ]
}
```

**Response:**
```json
{
  "shippingFee": 30000,
  "serviceType": "Giao hàng tiêu chuẩn",
  "expectedDelivery": "2025-11-05T00:00:00"
}
```

**Backend logic:**
```java
@PostMapping("/checkout/calculate-shipping")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<ShippingFeeResponse> calculateShipping(
    @RequestBody CalculateShippingRequest request
) {
    // 1. Get address
    Address address = addressRepository.findById(request.getAddressId())
        .orElseThrow();
    
    // 2. Get shop
    Shop shop = shopRepository.findById(request.getShopId())
        .orElseThrow();
    
    // 3. Calculate total weight & dimensions
    int totalWeight = 0;
    for (var item : request.getItems()) {
        ProductVariant variant = variantRepository.findById(item.getVariantId())
            .orElseThrow();
        totalWeight += variant.getWeight() * item.getQuantity();
    }
    
    // 4. Call GHN API
    GHNShippingFeeRequest ghnRequest = GHNShippingFeeRequest.builder()
        .toDistrictId(address.getDistrictId())
        .toWardCode(address.getWardCode())
        .weight(totalWeight)
        .build();
    
    GHNShippingFeeResponse ghnResponse = ghnService.calculateShippingFee(ghnRequest);
    
    // 5. Map response
    ShippingFeeResponse response = new ShippingFeeResponse();
    response.setShippingFee(ghnResponse.getTotal());
    response.setServiceType(ghnResponse.getServiceTypeName());
    response.setExpectedDelivery(ghnResponse.getExpectedDeliveryTime());
    
    return ResponseEntity.ok(response);
}
```

---

### **3.2. Create order (Checkout)**

```java
POST /api/orders
```

**Request body:**
```json
{
  "shopId": 1,
  "addressId": 2,
  "voucherCode": "NIKE20",
  "paymentMethod": "COD",
  "items": [
    {
      "variantId": 10,
      "quantity": 2
    }
  ],
  "note": "Giao giờ hành chính"
}
```

**Response:**
```json
{
  "orderId": 123,
  "totalAmount": 1500000,
  "shippingFee": 30000,
  "discount": 100000,
  "finalAmount": 1430000,
  "paymentUrl": "https://momo.vn/payment/...", // Nếu MOMO
  "status": "PENDING"
}
```

**Backend logic:**
```java
@PostMapping("/orders")
@PreAuthorize("hasRole('BUYER')")
@Transactional
public ResponseEntity<OrderResponse> createOrder(
    @RequestBody @Valid CreateOrderRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    // 1. Validate address
    Address address = addressRepository.findById(request.getAddressId())
        .orElseThrow();
    
    if (!address.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException("Address không thuộc về bạn");
    }
    
    // 2. Validate shop
    Shop shop = shopRepository.findById(request.getShopId())
        .orElseThrow();
    
    // 3. Calculate order amount
    BigDecimal subtotal = BigDecimal.ZERO;
    List<OrderItem> orderItems = new ArrayList<>();
    
    for (var item : request.getItems()) {
        ProductVariant variant = variantRepository.findById(item.getVariantId())
            .orElseThrow();
        
        // Check stock
        if (variant.getStock() < item.getQuantity()) {
            throw new BadRequestException("Không đủ hàng: " + variant.getProduct().getName());
        }
        
        BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        subtotal = subtotal.add(itemTotal);
        
        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(variant.getProduct());
        orderItem.setVariant(variant);
        orderItem.setVariantName(getVariantName(variant)); // "Size M, Red"
        orderItem.setQuantity(item.getQuantity());
        orderItem.setPrice(variant.getPrice());
        
        orderItems.add(orderItem);
        
        // Decrease stock
        variant.setStock(variant.getStock() - item.getQuantity());
        variantRepository.save(variant);
    }
    
    // 4. Apply voucher (if any)
    BigDecimal discount = BigDecimal.ZERO;
    Voucher voucher = null;
    
    if (request.getVoucherCode() != null) {
        voucher = voucherRepository.findByCode(request.getVoucherCode())
            .orElseThrow(() -> new NotFoundException("Voucher not found"));
        
        // Validate voucher
        if (!voucher.getShop().getId().equals(shop.getId())) {
            throw new BadRequestException("Voucher không thuộc shop này");
        }
        
        if (subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Đơn hàng chưa đủ điều kiện dùng voucher");
        }
        
        discount = voucher.getDiscountAmount();
    }
    
    // 5. Calculate shipping fee (call GHN)
    BigDecimal shippingFee = calculateShippingFee(address, orderItems);
    
    // 6. Calculate final amount
    BigDecimal finalAmount = subtotal.subtract(discount).add(shippingFee);
    
    // 7. Create order
    Order order = new Order();
    order.setUser(user);
    order.setShop(shop);
    order.setVoucher(voucher);
    order.setTotalAmount(finalAmount);
    order.setMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
    order.setStatus(OrderStatus.PENDING);
    order.setPaymentStatus(PaymentStatus.UNPAID);
    
    orderRepository.save(order);
    
    // 8. Save order items
    for (OrderItem item : orderItems) {
        item.setOrder(order);
        orderItemRepository.save(item);
    }
    
    // 9. Create shipment
    Shipment shipment = new Shipment();
    shipment.setOrder(order);
    shipment.setReceiverName(address.getLabel());
    shipment.setReceiverPhone(address.getContactPhone());
    shipment.setReceiverAddress(buildFullAddress(address));
    shipment.setProvince(address.getProvinceName());
    shipment.setDistrict(address.getDistrictName());
    shipment.setWard(address.getWardName());
    shipment.setShippingFee(shippingFee);
    shipment.setStatus(ShipmentStatus.PENDING);
    
    shipmentRepository.save(shipment);
    
    order.setShipment(shipment);
    orderRepository.save(order);
    
    // 10. Clear cart items
    Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
    if (cart != null) {
        for (var item : request.getItems()) {
            cartItemRepository.deleteByCartIdAndVariantId(cart.getId(), item.getVariantId());
        }
    }
    
    // 11. Create platform fee (5%)
    BigDecimal feeAmount = finalAmount.multiply(BigDecimal.valueOf(0.05));
    PlatformFee platformFee = new PlatformFee();
    platformFee.setOrder(order);
    platformFee.setSeller(shop.getOwner());
    platformFee.setFeePercent(BigDecimal.valueOf(5.00));
    platformFee.setFeeAmount(feeAmount);
    platformFeeRepository.save(platformFee);
    
    // 12. Handle payment
    OrderResponse response = new OrderResponse();
    response.setOrderId(order.getId());
    response.setTotalAmount(subtotal);
    response.setShippingFee(shippingFee);
    response.setDiscount(discount);
    response.setFinalAmount(finalAmount);
    response.setStatus(order.getStatus().name());
    
    if (request.getPaymentMethod().equals("MOMO")) {
        // Create MOMO payment
        String paymentUrl = momoService.createPayment(order);
        response.setPaymentUrl(paymentUrl);
    }
    // COD: No payment URL needed
    
    return ResponseEntity.status(201).body(response);
}
```

---

### **3.3. MOMO payment callback**

```java
POST /api/payment/momo/callback
```

**Request body:** MOMO gửi về

**Backend logic:**
```java
@PostMapping("/payment/momo/callback")
public ResponseEntity<Void> momoCallback(@RequestBody MomoCallbackRequest request) {
    // 1. Verify signature
    if (!momoService.verifySignature(request)) {
        throw new BadRequestException("Invalid signature");
    }
    
    // 2. Get order
    Long orderId = Long.parseLong(request.getOrderId());
    Order order = orderRepository.findById(orderId)
        .orElseThrow();
    
    // 3. Update payment status
    if (request.getResultCode() == 0) { // Success
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setMomoTransId(request.getTransId());
        order.setPaidAt(LocalDateTime.now());
        
        // Update order status to PROCESSING
        order.setStatus(OrderStatus.PROCESSING);
    } else {
        order.setPaymentStatus(PaymentStatus.FAILED);
    }
    
    orderRepository.save(order);
    
    return ResponseEntity.ok().build();
}
```

---

## 📋 **4. ORDER MANAGEMENT**

### **4.1. Xem danh sách đơn hàng**

```java
GET /api/orders
```

**Query params:**
- `status` (optional: PENDING, PROCESSING, COMPLETED, CANCELLED)
- `page`, `size`

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "shop": {
        "id": 1,
        "name": "Nike Official"
      },
      "totalAmount": 1500000,
      "status": "PROCESSING",
      "paymentStatus": "PAID",
      "method": "MOMO",
      "createdAt": "2025-10-30T10:00:00",
      "itemCount": 3
    }
  ],
  "totalElements": 10,
  "totalPages": 1
}
```

**Backend logic:**
```java
@GetMapping("/orders")
@PreAuthorize("hasRole('BUYER')")
public ResponseEntity<Page<OrderDTO>> getOrders(
    @RequestParam(required = false) String status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @AuthenticationPrincipal UserDetails userDetails
) {
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    Page<Order> orders;
    if (status != null) {
        orders = orderRepository.findByUserIdAndStatus(
            user.getId(), 
            OrderStatus.valueOf(status), 
            pageable
        );
    } else {
        orders = orderRepository.findByUserId(user.getId(), pageable);
    }
    
    return ResponseEntity.ok(orders.map(this::mapToDTO));
}
```

---

### **4.2. Xem chi tiết đơn hàng**

```java
GET /api/orders/{orderId}
```

**Response:**
```json
{
  "id": 123,
  "shop": {
    "id": 1,
    "name": "Nike Official",
    "phone": "0901234567"
  },
  "items": [
    {
      "id": 1,
      "product": {
        "id": 10,
        "name": "Nike Air Max",
        "image": "https://..."
      },
      "variantName": "Size 42, Black",
      "quantity": 2,
      "price": 500000
    }
  ],
  "shipment": {
    "status": "IN_TRANSIT",
    "trackingUrl": "https://ghn.vn/tracking/...",
    "expectedDelivery": "2025-11-05"
  },
  "totalAmount": 1500000,
  "shippingFee": 30000,
  "discount": 100000,
  "finalAmount": 1430000,
  "status": "PROCESSING",
  "paymentStatus": "PAID",
  "method": "MOMO",
  "createdAt": "2025-10-30T10:00:00"
}
```

---

### **4.3. Hủy đơn hàng**

```java
POST /api/orders/{orderId}/cancel
```

**Request body:**
```json
{
  "reason": "Đặt nhầm size"
}
```

**Backend logic:**
```java
@PostMapping("/orders/{orderId}/cancel")
@PreAuthorize("hasRole('BUYER')")
@Transactional
public ResponseEntity<Void> cancelOrder(
    @PathVariable Long orderId,
    @RequestBody CancelOrderRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Get order
    Order order = orderRepository.findById(orderId)
        .orElseThrow();
    
    // 2. Check ownership
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();
    
    if (!order.getUser().getId().equals(user.getId())) {
        throw new ForbiddenException();
    }
    
    // 3. Check status (chỉ hủy được khi PENDING)
    if (!order.getStatus().equals(OrderStatus.PENDING)) {
        throw new BadRequestException("Không thể hủy đơn hàng đang xử lý");
    }
    
    // 4. Update status
    order.setStatus(OrderStatus.CANCELLED);
    order.setCancellationReason(request.getReason());
    order.setCancelledBy(CancelledBy.USER);
    
    orderRepository.save(order);
    
    // 5. Restore stock
    for (OrderItem item : order.getItems()) {
        ProductVariant variant = item.getVariant();
        variant.setStock(variant.getStock() + item.getQuantity());
        variantRepository.save(variant);
    }
    
    // 6. Refund if paid
    if (order.getPaymentStatus().equals(PaymentStatus.PAID)) {
        // Create refund request
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(order.getTotalAmount());
        refund.setReason(request.getReason());
        refund.setStatus(RefundStatus.REQUESTED);
        
        refundRepository.save(refund);
        
        // Add to wallet
        Wallet wallet = walletRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Wallet w = new Wallet();
                w.setUser(user);
                w.setBalance(BigDecimal.ZERO);
                return walletRepository.save(w);
            });
        
        wallet.setBalance(wallet.getBalance().add(order.getTotalAmount()));
        walletRepository.save(wallet);
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(order.getTotalAmount());
        transaction.setType(TransactionType.REFUND);
        transaction.setDescription("Hoàn tiền đơn hàng #" + orderId);
        transaction.setRelatedOrder(order);
        
        walletTransactionRepository.save(transaction);
        
        // Update refund
        refund.setStatus(RefundStatus.COMPLETED);
        refund.setTransaction(transaction);
        refundRepository.save(refund);
    }
    
    return ResponseEntity.noContent().build();
}
```

---

### **4.4. Xác nhận đã nhận hàng**

```java
POST /api/orders/{orderId}/complete
```

**Backend logic:**
```java
@PostMapping("/orders/{orderId}/complete")
@PreAuthorize("hasRole('BUYER')")
@Transactional
public ResponseEntity<Void> completeOrder(
    @PathVariable Long orderId,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Validate ownership
    // 2. Check shipment status = DELIVERED
    // 3. Update order status = COMPLETED
    // 4. Update shipment status
    // 5. Update product sold_count
    
    Order order = orderRepository.findById(orderId)
        .orElseThrow();
    
    order.setStatus(OrderStatus.COMPLETED);
    
    if (order.getMethod().equals(PaymentMethod.COD)) {
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
    }
    
    orderRepository.save(order);
    
    // Update sold_count for products
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        product.setSoldCount(product.getSoldCount() + item.getQuantity());
        productRepository.save(product);
    }
    
    return ResponseEntity.ok().build();
}
```

---

## 💳 **5. WALLET (Ví tiền)**

### **5.1. Xem số dư ví**

```java
GET /api/wallet
```

**Response:**
```json
{
  "balance": 500000,
  "currency": "VND"
}
```

---

### **5.2. Xem lịch sử giao dịch**

```java
GET /api/wallet/transactions
```

**Query params:**
- `page`, `size`
- `type` (optional: DEPOSIT, REFUND, ORDER_PAYMENT)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "amount": 100000,
      "type": "REFUND",
      "description": "Hoàn tiền đơn hàng #123",
      "createdAt": "2025-10-30T10:00:00"
    }
  ]
}
```

---

## 🔔 **6. VOUCHERS**

### **6.1. Xem vouchers có thể dùng**

```java
GET /api/vouchers/available
```

**Query params:**
- `shopId` (optional)

**Response:**
```json
[
  {
    "id": 1,
    "code": "NIKE20",
    "description": "Giảm 100k cho đơn từ 500k",
    "discountAmount": 100000,
    "minOrderValue": 500000,
    "validTo": "2025-12-31T23:59:59",
    "shop": {
      "id": 1,
      "name": "Nike Official"
    }
  }
]
```

---

### **6.2. Áp dụng voucher**

```java
POST /api/vouchers/apply
```

**Request body:**
```json
{
  "code": "NIKE20",
  "shopId": 1,
  "orderAmount": 600000
}
```

**Response:**
```json
{
  "valid": true,
  "discountAmount": 100000,
  "finalAmount": 500000,
  "message": "Áp dụng voucher thành công"
}
```

---

## 📊 **7. STATISTICS (For buyer)**

### **7.1. Thống kê đơn hàng**

```java
GET /api/statistics/orders
```

**Response:**
```json
{
  "total": 50,
  "pending": 3,
  "processing": 5,
  "completed": 40,
  "cancelled": 2,
  "totalSpent": 15000000
}
```

---

## 📝 **SUMMARY - APIs cần bổ sung**

### **Ưu tiên cao (Critical):**

1. ✅ **Product Reviews** (5 APIs)
   - GET `/api/products/{id}/reviews` - Xem reviews
   - POST `/api/products/{id}/reviews` - Tạo review
   - PUT `/api/reviews/{id}` - Sửa review
   - DELETE `/api/reviews/{id}` - Xóa review
   - GET `/api/my-reviews` - Reviews của mình

2. ✅ **Checkout & Payment** (3 APIs)
   - POST `/api/checkout/calculate-shipping` - Tính phí ship
   - POST `/api/orders` - Tạo đơn hàng
   - POST `/api/payment/momo/callback` - MOMO callback

3. ✅ **Order Management** (4 APIs)
   - GET `/api/orders` - Danh sách đơn
   - GET `/api/orders/{id}` - Chi tiết đơn
   - POST `/api/orders/{id}/cancel` - Hủy đơn
   - POST `/api/orders/{id}/complete` - Xác nhận nhận hàng

### **Ưu tiên trung bình:**

4. ✅ **Shop Info** (3 APIs)
   - GET `/api/shops/{id}` - Thông tin shop
   - GET `/api/shops/{id}/products` - Sản phẩm của shop
   - GET `/api/shops/{id}/reviews` - Reviews của shop

5. ✅ **Wallet** (2 APIs)
   - GET `/api/wallet` - Số dư
   - GET `/api/wallet/transactions` - Lịch sử

6. ✅ **Vouchers** (2 APIs)
   - GET `/api/vouchers/available` - Vouchers khả dụng
   - POST `/api/vouchers/apply` - Áp dụng voucher

### **Ưu tiên thấp:**

7. ✅ **Statistics** (1 API)
   - GET `/api/statistics/orders` - Thống kê

---

**Tổng cộng: ~20 APIs cần implement**

**Thời gian ước tính:**
- Reviews: 2-3 ngày
- Checkout/Payment: 3-4 ngày
- Order Management: 2-3 ngày
- Shop/Wallet/Vouchers: 1-2 ngày
- Total: **8-12 ngày làm việc**
