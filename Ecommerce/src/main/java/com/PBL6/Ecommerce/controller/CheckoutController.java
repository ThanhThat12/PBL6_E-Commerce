package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.domain.Address;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.Shipment;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.service.GhnService;
import com.PBL6.Ecommerce.repository.*;

import java.util.*;
import java.math.BigDecimal;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.PBL6.Ecommerce.domain.User;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Checkout", description = "Checkout process, order preview, shipping fee calculation")
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final GhnService ghnService;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository;
    private final com.PBL6.Ecommerce.repository.CartItemRepository cartItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final com.PBL6.Ecommerce.repository.OrderRepository orderRepository;
    private final com.PBL6.Ecommerce.repository.OrderItemRepository orderItemRepository;
    private final com.PBL6.Ecommerce.service.WalletService walletService;
    private final com.PBL6.Ecommerce.service.NotificationService notificationService;
    private final com.PBL6.Ecommerce.service.OrderService orderService;

    public CheckoutController(GhnService ghnService, 
                            AddressRepository addressRepository,
                            ShopRepository shopRepository,
                            com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository,
                            com.PBL6.Ecommerce.repository.CartItemRepository cartItemRepository,
                            ShipmentRepository shipmentRepository,
                            UserRepository userRepository,
                            com.PBL6.Ecommerce.repository.OrderRepository orderRepository,
                            com.PBL6.Ecommerce.repository.OrderItemRepository orderItemRepository,
                            com.PBL6.Ecommerce.service.WalletService walletService,
                            com.PBL6.Ecommerce.service.NotificationService notificationService,
                            com.PBL6.Ecommerce.service.OrderService orderService) {
        this.ghnService = ghnService;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository;
        this.productVariantRepository = productVariantRepository;
        this.cartItemRepository = cartItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
        this.orderService = orderService;
    }

    /**
     * Bước 1-6: User ấn thanh toán → Backend trả danh sách dịch vụ GHN
     * POST /api/checkout/available-services
     */
    @PostMapping("/available-services")
    // @PreAuthorize("isAuthenticated()") // TODO: Re-enable after testing
    public ResponseEntity<ResponseDTO<List<Map<String,Object>>>> getAvailableServices(
            @Valid @RequestBody CheckoutInitRequestDTO req) {
        System.out.println("========== CHECKOUT AVAILABLE SERVICES REQUEST ==========");
        System.out.println("Request DTO: " + req);
        System.out.println("ShopId: " + req.getShopId());
        System.out.println("AddressId: " + req.getAddressId());
        System.out.println("CartItemIds: " + Arrays.toString(req.getCartItemIds()));
        System.out.println("========================================================");
        try {
            // 2. Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            System.out.println("Buyer Address: " + buyerAddress.getDistrictId() + ", " + buyerAddress.getWardCode());
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // 3. Tìm địa chỉ STORE của shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            System.out.println("Shop: " + shop.getName() + ", GhnShopId: " + shop.getGhnShopId() + ", GhnToken: " + shop.getGhnToken());
            
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));
            
            System.out.println("Shop Address: " + shopAddress.getDistrictId() + ", " + shopAddress.getWardCode());

            // 4. Tính tổng trọng lượng từ cartItems
            int totalWeight = 0;
            if (req.getCartItemIds() != null && req.getCartItemIds().length > 0) {
                var cartItems = cartItemRepository.findAllById(Arrays.asList(req.getCartItemIds()));
                for (var item : cartItems) {
                    Integer weight = item.getProductVariant().getProduct().getWeightGrams();
                    if (weight != null) {
                        totalWeight += weight * item.getQuantity();
                    }
                }
            }
            
            if (totalWeight == 0) {
                totalWeight = 200; // default 200g nếu không có weight
            }

            // 5. Gọi GHN /available-services
            Map<String, Object> payload = new HashMap<>();
            // ❌ Không cần put shop_id nữa - GhnService sẽ tự thêm
            // payload.put("shop_id", Integer.parseInt(shop.getGhnShopId()));
            
            payload.put("from_district", shopAddress.getDistrictId());
            payload.put("to_district", buyerAddress.getDistrictId());
            
            System.out.println("GHN Payload: " + payload);
            
            List<Map<String,Object>> services = ghnService.getAvailableServices(payload, req.getShopId());
            
            System.out.println("GHN Services Result: " + services);
            
            // 6. Trả về danh sách dịch vụ
            Map<String,Object> response = new HashMap<>();
            response.put("services", services);
            response.put("totalWeight", totalWeight);
            response.put("shopAddress", Map.of(
                "districtId", shopAddress.getDistrictId(),
                "wardCode", shopAddress.getWardCode()
            ));
            response.put("buyerAddress", Map.of(
                "districtId", buyerAddress.getDistrictId(),
                "wardCode", buyerAddress.getWardCode()
            ));
            
            return ResponseDTO.ok(List.of(response), "Lấy danh sách dịch vụ thành công");
        } catch (Exception e) {
            System.err.println("ERROR in getAvailableServices: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 7-10: User chọn service → Backend tính phí ship
     * POST /api/checkout/calculate-fee
     */
    @PostMapping("/calculate-fee")
    // @PreAuthorize("isAuthenticated()") // TODO: Re-enable after testing
    public ResponseEntity<ResponseDTO<Map<String,Object>>> calculateShippingFee(
            @Valid @RequestBody CheckoutCalculateFeeRequestDTO req) {
        try {
            // Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // Tìm địa chỉ STORE của shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // Tính tổng trọng lượng và lấy kích thước
            int totalWeight = 0;
            int maxLength = 0, maxWidth = 0, maxHeight = 0;
            List<Map<String, Object>> items = new ArrayList<>();
            
            if (req.getCartItemIds() != null && req.getCartItemIds().length > 0) {
                var cartItems = cartItemRepository.findAllById(Arrays.asList(req.getCartItemIds()));
                
                for (var cartItem : cartItems) {
                    var product = cartItem.getProductVariant().getProduct();
                    int quantity = cartItem.getQuantity();
                    
                    // Tính weight
                    Integer weight = product.getWeightGrams();
                    if (weight != null) {
                        totalWeight += weight * quantity;
                    }
                    
                    // Lấy kích thước lớn nhất
                    if (product.getLengthCm() != null) {
                        maxLength = Math.max(maxLength, product.getLengthCm());
                    }
                    if (product.getWidthCm() != null) {
                        maxWidth = Math.max(maxWidth, product.getWidthCm());
                    }
                    if (product.getHeightCm() != null) {
                        maxHeight = Math.max(maxHeight, product.getHeightCm());
                    }
                    
                    // Tạo items array theo format GHN
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", product.getName());
                    item.put("quantity", quantity);
                    item.put("weight", weight != null ? weight : 200);
                    item.put("length", product.getLengthCm() != null ? product.getLengthCm() : 20);
                    item.put("width", product.getWidthCm() != null ? product.getWidthCm() : 20);
                    item.put("height", product.getHeightCm() != null ? product.getHeightCm() : 10);
                    items.add(item);
                }
            }
            
            // Default values nếu không có dữ liệu
            if (totalWeight == 0) totalWeight = 200;
            if (maxLength == 0) maxLength = 20;
            if (maxWidth == 0) maxWidth = 20;
            if (maxHeight == 0) maxHeight = 10;
            if (items.isEmpty()) {
                items.add(Map.of(
                    "name", "Sản phẩm",
                    "quantity", 1,
                    "weight", totalWeight,
                    "length", maxLength,
                    "width", maxWidth,
                    "height", maxHeight
                ));
            }

            // Gọi GHN /shipping-order/fee
            Map<String, Object> payload = new HashMap<>();
            
            // Required fields
            payload.put("from_district_id", shopAddress.getDistrictId());
            payload.put("from_ward_code", shopAddress.getWardCode());
            payload.put("to_district_id", buyerAddress.getDistrictId());
            payload.put("to_ward_code", buyerAddress.getWardCode());
            payload.put("service_id", req.getServiceId());
            
            // service_type_id có thể null theo doc
            if (req.getServiceTypeId() != null) {
                payload.put("service_type_id", req.getServiceTypeId());
            }
            
            // Package dimensions
            payload.put("weight", totalWeight);
            payload.put("length", maxLength);
            payload.put("width", maxWidth);
            payload.put("height", maxHeight);
            
            // Items array
            payload.put("items", items);
            
            // Optional fields (có thể thêm sau)
            payload.put("insurance_value", 0); // Giá trị bảo hiểm
            payload.put("coupon", null);
            
            Map<String,Object> feeResponse = ghnService.calculateFee(payload, req.getShopId());
            
            return ResponseDTO.ok(feeResponse, "Tính phí thành công");
        } catch (RuntimeException e) {
            // Check if it's GHN route not found error
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("route not found")) {
                return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(400, 
                        "GHN không hỗ trợ dịch vụ vận chuyển này cho địa chỉ đã chọn. Vui lòng chọn dịch vụ khác.", 
                        "GHN_ROUTE_NOT_FOUND", 
                        null));
            }
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, "Lỗi tính phí: " + errorMsg, "ERROR", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi", null));
        }
    }

    /**
     * Bước 11-13: User xác nhận thanh toán → CHỈ TẠO ORDER (chưa tạo GHN shipment)
     * POST /api/checkout/confirm
     */
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ResponseDTO<Map<String,Object>>> confirmCheckout(
        @Valid @RequestBody CheckoutConfirmRequestDTO req,
        @AuthenticationPrincipal Jwt jwt) {
        try {
            // Lấy email từ token
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                throw new RuntimeException("Token không có email");
            }

            // Lấy User
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Lấy địa chỉ HOME của buyer
            Address buyerAddress = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
            
            if (buyerAddress.getTypeAddress() != TypeAddress.HOME) {
                throw new RuntimeException("Địa chỉ phải là loại HOME");
            }

            // Tìm shop và địa chỉ shop
            Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
            
            Address shopAddress = addressRepository.findByUserAndTypeAddress(
                shop.getOwner(), TypeAddress.STORE)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));

            // Lấy cart items và validate
            List<Long> requestedIds = Arrays.asList(req.getCartItemIds());
            var cartItems = cartItemRepository.findAllById(requestedIds);
            
            if (cartItems.isEmpty()) {
                throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
            }

            // Tính tổng tiền sản phẩm
            BigDecimal subtotal = BigDecimal.ZERO;
            int totalWeight = 0;
            int maxLength = 0, maxWidth = 0, maxHeight = 0;
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (var cartItem : cartItems) {
                var variant = cartItem.getProductVariant();
                var product = variant.getProduct();
                int quantity = cartItem.getQuantity();
                
                // Tính subtotal
                BigDecimal itemTotal = variant.getPrice().multiply(new BigDecimal(quantity));
                subtotal = subtotal.add(itemTotal);
                
                // Tính weight và dimensions cho GHN
                Integer weight = product.getWeightGrams();
                if (weight != null) {
                    totalWeight += weight * quantity;
                }
                
                if (product.getLengthCm() != null) {
                    maxLength = Math.max(maxLength, product.getLengthCm());
                }
                if (product.getWidthCm() != null) {
                    maxWidth = Math.max(maxWidth, product.getWidthCm());
                }
                if (product.getHeightCm() != null) {
                    maxHeight = Math.max(maxHeight, product.getHeightCm());
                }
                
                // Tạo items array cho GHN
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("quantity", quantity);
                item.put("weight", weight != null ? weight : 200);
                item.put("length", product.getLengthCm() != null ? product.getLengthCm() : 20);
                item.put("width", product.getWidthCm() != null ? product.getWidthCm() : 20);
                item.put("height", product.getHeightCm() != null ? product.getHeightCm() : 10);
                items.add(item);
            }
            
            // Default values
            if (totalWeight == 0) totalWeight = 200;
            if (maxLength == 0) maxLength = 20;
            if (maxWidth == 0) maxWidth = 20;
            if (maxHeight == 0) maxHeight = 10;
            
            // ✅ TÍNH SHIPPING FEE TỪ GHN
            BigDecimal shippingFee = BigDecimal.ZERO;
            try {
                Map<String, Object> feePayload = new HashMap<>();
                feePayload.put("from_district_id", shopAddress.getDistrictId());
                feePayload.put("from_ward_code", shopAddress.getWardCode());
                feePayload.put("to_district_id", buyerAddress.getDistrictId());
                feePayload.put("to_ward_code", buyerAddress.getWardCode());
                feePayload.put("service_id", req.getServiceId());
                if (req.getServiceTypeId() != null) {
                    feePayload.put("service_type_id", req.getServiceTypeId());
                }
                feePayload.put("weight", totalWeight);
                feePayload.put("length", maxLength);
                feePayload.put("width", maxWidth);
                feePayload.put("height", maxHeight);
                feePayload.put("items", items);
                feePayload.put("insurance_value", 0);
                
                Map<String, Object> feeResponse = ghnService.calculateFee(feePayload, req.getShopId());
                
                // Parse shipping fee từ response
                if (feeResponse != null && feeResponse.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) feeResponse.get("data");
                    Object totalFee = data.get("total");
                    if (totalFee instanceof Number) {
                        shippingFee = BigDecimal.valueOf(((Number) totalFee).doubleValue());
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Không thể tính shipping fee: " + e.getMessage());
                // Tiếp tục với shipping fee = 0
            }
            
            // Tính tổng cuối cùng
            BigDecimal totalAmount = subtotal.add(shippingFee);

            // ========== TẠO ORDER QUA OrderService (ĐẢM BẢO TRỪ STOCK VÀ CỘNG SOLD_COUNT) ==========
            com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO orderRequest = new com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO();
            orderRequest.setUserId(user.getId());
            // Note: CreateOrderRequestDTO không có setShopId() - OrderService sẽ tự lấy shop từ variant
            
            // Set items từ cart
            List<com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO.Item> orderItems = new ArrayList<>();
            for (var cartItem : cartItems) {
                com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO.Item item = 
                    new com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO.Item();
                item.setVariantId(cartItem.getProductVariant().getId());
                item.setQuantity(cartItem.getQuantity());
                // Note: Item không có setPrice() - OrderService sẽ lấy price từ variant
                orderItems.add(item);
            }
            orderRequest.setItems(orderItems);
            
            // Set địa chỉ giao hàng
            orderRequest.setReceiverName(buyerAddress.getContactName());
            orderRequest.setReceiverPhone(buyerAddress.getContactPhone());
            orderRequest.setReceiverAddress(buyerAddress.getFullAddress());
            orderRequest.setProvinceId(buyerAddress.getProvinceId());
            orderRequest.setDistrictId(buyerAddress.getDistrictId());
            orderRequest.setWardCode(buyerAddress.getWardCode());
            
            // Set GHN info
            orderRequest.setToDistrictId(String.valueOf(buyerAddress.getDistrictId()));
            orderRequest.setToWardCode(buyerAddress.getWardCode());
            orderRequest.setServiceId(req.getServiceId());
            orderRequest.setServiceTypeId(req.getServiceTypeId());
            orderRequest.setWeightGrams(totalWeight);
            
            // Set payment & shipping
            orderRequest.setMethod(req.getPaymentMethod());
            orderRequest.setShippingFee(shippingFee);
            orderRequest.setVoucherDiscount(BigDecimal.ZERO);
            orderRequest.setCodAmount("COD".equalsIgnoreCase(req.getPaymentMethod()) ? totalAmount : BigDecimal.ZERO);
            // Note: CreateOrderRequestDTO không có setNotes() - có thể bỏ qua hoặc thêm field vào DTO
            
            // ✅ GỌI OrderService.createOrder() - LOGIC TRỪ STOCK VÀ CỘNG SOLD_COUNT SẼ CHẠY Ở ĐÂY
            Order order = orderService.createOrder(orderRequest);

            // ========== XÓA CART ITEMS ==========
            // Logic:
            // - COD: Xóa cart ngay vì user đã xác nhận order (không có bước payment gateway)
            // - MoMo/SportyPay: GIỮ cart cho đến khi payment success (user có thể thoát khỏi payment page)
            if ("COD".equalsIgnoreCase(req.getPaymentMethod())) {
                cartItemRepository.deleteAll(cartItems);
                System.out.println("✅ Cart cleared for COD order #" + order.getId());
            } else {
                System.out.println("⏳ Cart kept for online payment order #" + order.getId() + " - will be cleared after payment success");
            }

            // ========== GỬI THÔNG BÁO CHO SELLER ==========
            try {
                Long sellerId = shop.getOwner().getId();
                String sellerMessage = "Bạn có đơn hàng mới #" + order.getId() + " từ " + user.getUsername();
                notificationService.sendSellerNotification(sellerId, "NEW_ORDER", sellerMessage, order.getId());
                System.out.println("✅ Sent notification to seller #" + sellerId + " for order #" + order.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send seller notification: " + e.getMessage());
                // Continue - notification failure should not block order creation
            }

            // ========== TRẢ VỀ KẾT QUẢ ==========
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("status", order.getStatus().name());
            response.put("message", "Đặt hàng thành công. Vui lòng chờ người bán xác nhận.");
            
            return ResponseDTO.ok(response, "Đặt hàng thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi tạo đơn", null));
        }
    }

    /**
     * Thanh toán đơn hàng bằng ví SportyPay
     * POST /api/checkout/pay-with-wallet
     */
    @PostMapping("/pay-with-wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> payWithWallet(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            System.out.println("💰 [SportyPay] Payment request received: " + request);
            System.out.println("💰 [SportyPay] JWT: " + jwt);
            System.out.println("💰 [SportyPay] JWT Claims: " + (jwt != null ? jwt.getClaims() : "null"));
            
            if (jwt == null) {
                System.err.println("❌ [SportyPay] JWT is null - authentication failed!");
                return ResponseEntity.status(401)
                    .body(new ResponseDTO<>(401, "Unauthorized", "Vui lòng đăng nhập để thanh toán", null));
            }
            
            String email = jwt.getClaimAsString("email");
            String username = jwt.getClaimAsString("sub");
            System.out.println("💰 [SportyPay] Email from JWT: " + email);
            System.out.println("💰 [SportyPay] Username from JWT: " + username);
            
            Long orderId = Long.valueOf(request.get("orderId").toString());
            
            // Get authenticated user by email (primary identifier in JWT)
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            // Get order
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to user
            if (!order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized: Order does not belong to user");
            }
            
            // Verify order status
            if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
                throw new RuntimeException("Order already paid");
            }
            
            BigDecimal amount = order.getTotalAmount();
            System.out.println("💰 [SportyPay] Processing payment for order #" + orderId + ", amount: " + amount);
            
            // Process wallet payment
            Map<String, Object> result = walletService.payOrderWithWallet(user.getId(), orderId, amount);
            
            // Update order status
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setMethod("SPORTYPAY");
            order.setPaidAt(new Date());
            orderRepository.save(order);
            
            System.out.println("✅ [SportyPay] Payment successful for order #" + orderId);
            
            // Deposit to admin wallet
            try {
                walletService.depositToAdminWallet(amount, order, "SPORTYPAY");
                System.out.println("✅ [SportyPay] Deposited to admin wallet");
            } catch (Exception e) {
                System.err.println("⚠️ [SportyPay] Failed to deposit to admin wallet: " + e.getMessage());
                // Continue even if admin deposit fails - can be retried manually
            }
            
            // ========== GỬI THÔNG BÁO CHO BUYER VÀ SELLER ==========
            try {
                // Notify buyer
                notificationService.sendOrderNotification(
                    user.getId(), 
                    "PAYMENT_SUCCESS", 
                    "Thanh toán đơn hàng #" + orderId + " thành công qua SportyPay"
                );
                
                // Notify seller
                Long sellerId = order.getShop().getOwner().getId();
                notificationService.sendSellerNotification(
                    sellerId, 
                    "ORDER_PAID", 
                    "Đơn hàng #" + orderId + " đã được thanh toán qua SportyPay", 
                    orderId
                );
                System.out.println("✅ [SportyPay] Sent payment notifications");
            } catch (Exception e) {
                System.err.println("⚠️ [SportyPay] Failed to send notifications: " + e.getMessage());
            }
            
            result.put("orderId", orderId);
            result.put("paymentStatus", "PAID");
            
            return ResponseDTO.ok(result, "Thanh toán thành công");
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ [SportyPay] Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(400, e.getMessage(), "Lỗi thanh toán", null));
        } catch (Exception e) {
            System.err.println("❌ [SportyPay] Payment failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(500, e.getMessage(), "Lỗi thanh toán", null));
        }
    }
    }
