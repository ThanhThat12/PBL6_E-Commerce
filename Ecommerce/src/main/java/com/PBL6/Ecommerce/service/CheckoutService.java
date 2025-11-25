package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.repository.*;
import com.PBL6.Ecommerce.constant.TypeAddress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final GhnService ghnService;

    public CheckoutService(
            UserRepository userRepository,
            AddressRepository addressRepository,
            ShopRepository shopRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ShipmentRepository shipmentRepository,
            GhnService ghnService) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shopRepository = shopRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.ghnService = ghnService;
    }

    // ============================================
    // PUBLIC METHODS
    // ============================================

    /**
     * Lấy user từ email (từ JWT token)
     */
    private User getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email không hợp lệ");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User không tồn tại: " + email));
    }

    /**
     * Lấy danh sách dịch vụ vận chuyển khả dụng
     */
    public List<Map<String,Object>> getAvailableShippingServices(String email, CheckoutInitRequestDTO req) {
        User user = getUserByEmail(email);
        
        System.out.println("========== GET AVAILABLE SERVICES ==========");
        System.out.println("User: " + user.getEmail());
        System.out.println("Shop ID: " + req.getShopId());
        System.out.println("Address ID: " + req.getAddressId());
        System.out.println("===========================================");
        
        // Validate address ownership
        Address buyerAddress = validateAndGetBuyerAddress(user, req.getAddressId());
        
        // Get shop and shop address
        Shop shop = shopRepository.findById(req.getShopId())
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        
        Address shopAddress = getShopStoreAddress(shop);
        
        // Validate cart items
        List<CartItem> cartItems = validateAndGetCartItems(user, req.getCartItemIds(), req.getShopId());
        
        // Calculate weight and dimensions
        Map<String, Integer> dimensions = calculateDimensions(cartItems);
        
        // Call GHN API
        Map<String, Object> payload = new HashMap<>();
        payload.put("from_district", shopAddress.getDistrictId());
        payload.put("from_ward_code", shopAddress.getWardCode());
        payload.put("to_district", buyerAddress.getDistrictId());
        payload.put("to_ward_code", buyerAddress.getWardCode());
        payload.put("weight", dimensions.get("totalWeight"));
        payload.put("length", dimensions.get("maxLength"));
        payload.put("width", dimensions.get("maxWidth"));
        payload.put("height", dimensions.get("maxHeight"));

        List<Map<String,Object>> services = ghnService.getAvailableServices(payload, req.getShopId());
        
        // Build response
        List<Map<String,Object>> result = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put("services", services);
        data.put("totalWeight", dimensions.get("totalWeight"));
        data.put("shopAddress", Map.of(
            "districtId", shopAddress.getDistrictId(),
            "wardCode", shopAddress.getWardCode()
        ));
        data.put("buyerAddress", Map.of(
            "districtId", buyerAddress.getDistrictId(),
            "wardCode", buyerAddress.getWardCode()
        ));
        result.add(data);
        
        return result;
    }

    /**
     * Tính phí vận chuyển
     */
    public Map<String,Object> calculateShippingFee(String email, CheckoutCalculateFeeRequestDTO req) {
        User user = getUserByEmail(email);
        
        System.out.println("========== CALCULATE SHIPPING FEE ==========");
        System.out.println("User: " + user.getEmail());
        System.out.println("Shop ID: " + req.getShopId());
        System.out.println("Service ID: " + req.getServiceId());
        System.out.println("===========================================");
        
        Address buyerAddress = validateAndGetBuyerAddress(user, req.getAddressId());
        Shop shop = shopRepository.findById(req.getShopId())
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        Address shopAddress = getShopStoreAddress(shop);
        
        List<CartItem> cartItems = validateAndGetCartItems(user, req.getCartItemIds(), req.getShopId());
        Map<String, Integer> dimensions = calculateDimensions(cartItems);
        
        // Build items array
        List<Map<String, Object>> items = buildGhnItems(cartItems);
        
        // Call GHN API
        Map<String, Object> payload = new HashMap<>();
        payload.put("from_district_id", shopAddress.getDistrictId());
        payload.put("from_ward_code", shopAddress.getWardCode());
        payload.put("to_district_id", buyerAddress.getDistrictId());
        payload.put("to_ward_code", buyerAddress.getWardCode());
        payload.put("service_id", req.getServiceId());
        payload.put("service_type_id", req.getServiceTypeId());
        payload.put("weight", dimensions.get("totalWeight"));
        payload.put("length", dimensions.get("maxLength"));
        payload.put("width", dimensions.get("maxWidth"));
        payload.put("height", dimensions.get("maxHeight"));
        payload.put("items", items);
        payload.put("insurance_value", 0);
        payload.put("coupon", null);

        return ghnService.calculateFee(payload, req.getShopId());
    }

    /**
     * Confirm checkout - Main business logic
     * - Validate ownership
     * - Create Order + OrderItems
     * - Create GHN shipping order
     * - Save Shipment
     * - Clear cart
     */
    @Transactional
    public Map<String,Object> confirmCheckout(String email, CheckoutConfirmRequestDTO req) {
        System.out.println("========== CONFIRM CHECKOUT ==========");
        System.out.println("User email: " + email);
        System.out.println("Shop ID: " + req.getShopId());
        System.out.println("Address ID: " + req.getAddressId());
        System.out.println("Payment Method: " + req.getPaymentMethod());
        System.out.println("Cart Items: " + Arrays.toString(req.getCartItemIds()));
        System.out.println("======================================");
        
        // 1. Get user
        User user = getUserByEmail(email);
        
        // 2. Validate address ownership
        Address buyerAddress = validateAndGetBuyerAddress(user, req.getAddressId());
        
        // 3. Get shop and shop address
        Shop shop = shopRepository.findById(req.getShopId())
            .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        Address shopAddress = getShopStoreAddress(shop);
        
        // 4. Validate cart items ownership and shop consistency
        List<CartItem> cartItems = validateAndGetCartItems(user, req.getCartItemIds(), req.getShopId());
        
        // 5. Calculate dimensions and amounts
        Map<String, Integer> dimensions = calculateDimensions(cartItems);
        int codAmount = calculateCodAmount(cartItems, req.getPaymentMethod());
        List<Map<String, Object>> ghnItems = buildGhnItems(cartItems);
        
        // 6. Create Order first (to get order ID)
        Order order = createOrderFromCheckout(user, shop, cartItems, req.getPaymentMethod(), codAmount);
        
        // 7. Create OrderItems
        createOrderItemsFromCart(order, cartItems);
        
        // 8. Build GHN payload
        Map<String, Object> ghnPayload = buildGhnPayload(
            shop, shopAddress, user, buyerAddress, 
            dimensions, codAmount, ghnItems, req, order.getId()
        );
        
        // 9. Call GHN API
        Map<String,Object> ghnResponse = ghnService.createShippingOrder(ghnPayload, req.getShopId());
        
        // 10. Save Shipment
        Shipment shipment = saveShipmentFromGhn(ghnResponse, ghnPayload, buyerAddress);
        
        // 11. Link shipment to order
        order.setShipment(shipment);
        orderRepository.save(order);
        
        // 12. Clear cart items
        cartItemRepository.deleteAll(cartItems);
        
        System.out.println("✅ Checkout completed successfully!");
        System.out.println("  - Order ID: " + order.getId());
        System.out.println("  - Shipment ID: " + shipment.getId());
        System.out.println("  - GHN Order Code: " + shipment.getGhnOrderCode());
        System.out.println("  - Deleted cart items: " + cartItems.size());
        
        // 13. Build response
        Map<String, Object> result = new HashMap<>();
        result.put("order_id", order.getId());
        result.put("ghn_order_code", shipment.getGhnOrderCode());
        result.put("shipping_fee", shipment.getShippingFee());
        result.put("total_amount", order.getTotalAmount());
        result.put("status", order.getStatus().name());
        result.put("expected_delivery_time", shipment.getExpectedDeliveryTime());
        
        return result;
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    /**
     * Validate address thuộc về user
     */
    private Address validateAndGetBuyerAddress(User user, Long addressId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        if (!address.getUser().getId().equals(user.getId())) {
            System.err.println("❌ SECURITY: User " + user.getId() + 
                " tried to use address " + addressId + 
                " owned by user " + address.getUser().getId());
            throw new RuntimeException("Địa chỉ không thuộc về bạn");
        }
        
        if (address.getTypeAddress() != TypeAddress.HOME) {
            throw new RuntimeException("Địa chỉ phải là loại HOME");
        }
        
        System.out.println("✅ Address validated: " + address.getId() + " - " + address.getFullAddress());
        return address;
    }

    /**
     * Get shop STORE address
     */
    private Address getShopStoreAddress(Shop shop) {
        return addressRepository.findByUserAndTypeAddress(shop.getOwner(), TypeAddress.STORE)
            .stream().findFirst()
            .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ STORE của shop"));
    }

    /**
     * Validate cart items thuộc về user và cùng shop
     */
    private List<CartItem> validateAndGetCartItems(User user, Long[] cartItemIds, Long shopId) {
        if (cartItemIds == null || cartItemIds.length == 0) {
            throw new RuntimeException("Giỏ hàng trống");
        }
        
        List<Long> ids = Arrays.asList(cartItemIds);
        List<CartItem> cartItems = cartItemRepository.findAllById(ids);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
        }
        
        if (cartItems.size() != cartItemIds.length) {
            throw new RuntimeException("Một số cart items không tồn tại");
        }
        
        // Validate ownership
        for (CartItem item : cartItems) {
            Long cartOwnerId = item.getCart().getUser().getId();
            if (!cartOwnerId.equals(user.getId())) {
                System.err.println("❌ SECURITY: User " + user.getId() + 
                    " tried to checkout cart item " + item.getId() + 
                    " owned by user " + cartOwnerId);
                throw new RuntimeException("Cart item ID " + item.getId() + " không thuộc về bạn");
            }
        }
        
        // Validate same shop
        Set<Long> shopIds = cartItems.stream()
            .map(item -> item.getProductVariant().getProduct().getShop().getId())
            .collect(Collectors.toSet());
        
        if (shopIds.size() > 1) {
            throw new RuntimeException("Không thể checkout items từ nhiều shop cùng lúc");
        }
        
        if (!shopIds.isEmpty() && !shopIds.contains(shopId)) {
            throw new RuntimeException("Cart items không thuộc shop " + shopId);
        }
        
        System.out.println("✅ Validated " + cartItems.size() + " cart items from shop " + shopId);
        return cartItems;
    }

    /**
     * Calculate dimensions and weight
     */
    private Map<String, Integer> calculateDimensions(List<CartItem> cartItems) {
        int totalWeight = 0;
        int maxLength = 0, maxWidth = 0, maxHeight = 0;
        
        for (CartItem item : cartItems) {
            Product product = item.getProductVariant().getProduct();
            int quantity = item.getQuantity();
            
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
        }
        
        // Default values if not set
        if (totalWeight == 0) totalWeight = 200;
        if (maxLength == 0) maxLength = 20;
        if (maxWidth == 0) maxWidth = 20;
        if (maxHeight == 0) maxHeight = 10;
        
        Map<String, Integer> result = new HashMap<>();
        result.put("totalWeight", totalWeight);
        result.put("maxLength", maxLength);
        result.put("maxWidth", maxWidth);
        result.put("maxHeight", maxHeight);
        
        System.out.println("📦 Dimensions: " + totalWeight + "g, " + 
            maxLength + "x" + maxWidth + "x" + maxHeight + " cm");
        
        return result;
    }

    /**
     * Calculate COD amount
     */
    private int calculateCodAmount(List<CartItem> cartItems, String paymentMethod) {
        if (!"COD".equalsIgnoreCase(paymentMethod)) {
            return 0;
        }
        
        int codAmount = 0;
        for (CartItem item : cartItems) {
            BigDecimal price = item.getProductVariant().getPrice();
            if (price != null) {
                codAmount += price.intValue() * item.getQuantity();
            }
        }
        
        System.out.println("💰 COD Amount: " + codAmount + " VND");
        return codAmount;
    }

    /**
     * Build GHN items array
     */
    private List<Map<String, Object>> buildGhnItems(List<CartItem> cartItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Product product = item.getProductVariant().getProduct();
            ProductVariant variant = item.getProductVariant();
            
            Map<String, Object> ghnItem = new HashMap<>();
            ghnItem.put("name", product.getName());
            ghnItem.put("code", variant.getSku() != null ? variant.getSku() : "");
            ghnItem.put("quantity", item.getQuantity());
            ghnItem.put("price", variant.getPrice() != null ? variant.getPrice().intValue() : 0);
            ghnItem.put("length", product.getLengthCm() != null ? product.getLengthCm() : 12);
            ghnItem.put("width", product.getWidthCm() != null ? product.getWidthCm() : 12);
            ghnItem.put("height", product.getHeightCm() != null ? product.getHeightCm() : 12);
            ghnItem.put("weight", product.getWeightGrams() != null ? product.getWeightGrams() : 200);
            
            Map<String, String> category = new HashMap<>();
            category.put("level1", product.getCategory() != null ? 
                product.getCategory().getName() : "Khác");
            ghnItem.put("category", category);
            
            items.add(ghnItem);
        }
        
        System.out.println("📦 Built " + items.size() + " GHN items");
        return items;
    }

    /**
     * Create Order entity from checkout
     */
    private Order createOrderFromCheckout(User user, Shop shop, List<CartItem> cartItems, 
                                          String paymentMethod, int codAmount) {
        Order order = new Order();
        order.setUser(user);
        order.setShop(shop);
        order.setMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(codAmount));
        
        Order saved = orderRepository.save(order);
        System.out.println("✅ Order created: ID = " + saved.getId());
        
        return saved;
    }

    /**
     * Create OrderItems from CartItems
     */
    private void createOrderItemsFromCart(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();
            Product product = variant.getProduct();
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setProductId(product.getId());
            orderItem.setVariantName(variant.getSku() != null ? variant.getSku() : product.getName());
            orderItem.setPrice(variant.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            
            orderItems.add(orderItem);
        }
        
        orderItemRepository.saveAll(orderItems);
        System.out.println("✅ Created " + orderItems.size() + " order items");
    }

    /**
     * Build GHN payload for creating shipping order
     */
    private Map<String, Object> buildGhnPayload(
            Shop shop, Address shopAddress, User user, Address buyerAddress,
            Map<String, Integer> dimensions, int codAmount, 
            List<Map<String, Object>> items, CheckoutConfirmRequestDTO req,
            Long orderId) {
        
        Map<String, Object> payload = new HashMap<>();
        
        // Sender info (Shop)
        payload.put("from_name", shopAddress.getContactName() != null ? 
            shopAddress.getContactName() : shop.getName());
        payload.put("from_phone", shopAddress.getContactPhone() != null ? 
            shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
        payload.put("from_address", shopAddress.getFullAddress());
        payload.put("from_ward_name", shopAddress.getWardName());
        payload.put("from_district_name", shopAddress.getDistrictName());
        payload.put("from_province_name", shopAddress.getProvinceName());
        
        // Return address (Shop)
        payload.put("return_phone", shopAddress.getContactPhone() != null ? 
            shopAddress.getContactPhone() : shop.getOwner().getPhoneNumber());
        payload.put("return_address", shopAddress.getFullAddress());
        payload.put("return_district_id", null);
        payload.put("return_ward_code", "");
        
        // Receiver info (Buyer)
        payload.put("to_name", buyerAddress.getContactName() != null ? 
            buyerAddress.getContactName() : user.getFullName());
        payload.put("to_phone", buyerAddress.getContactPhone() != null ? 
            buyerAddress.getContactPhone() : user.getPhoneNumber());
        payload.put("to_address", buyerAddress.getFullAddress());
        payload.put("to_ward_code", buyerAddress.getWardCode());
        payload.put("to_district_id", buyerAddress.getDistrictId());
        
        // Shipment info
        payload.put("weight", dimensions.get("totalWeight"));
        payload.put("length", dimensions.get("maxLength"));
        payload.put("width", dimensions.get("maxWidth"));
        payload.put("height", dimensions.get("maxHeight"));
        payload.put("service_id", req.getServiceId());
        payload.put("service_type_id", req.getServiceTypeId());
        payload.put("payment_type_id", 2); // Người bán trả phí
        payload.put("required_note", "KHONGCHOXEMHANG");
        payload.put("cod_amount", codAmount);
        payload.put("insurance_value", codAmount > 0 ? (int)(codAmount * 0.1) : 0);
        payload.put("items", items);
        payload.put("client_order_code", "ORDER_" + orderId);
        payload.put("note", req.getNote() != null ? req.getNote() : "");
        payload.put("content", "Đơn hàng từ " + shop.getName());
        payload.put("coupon", null);
        payload.put("pick_shift", new int[]{2});
        payload.put("pick_station_id", shopAddress.getDistrictId());
        payload.put("deliver_station_id", null);
        
        System.out.println("📦 Built GHN payload for order: ORDER_" + orderId);
        return payload;
    }

    /**
     * Save Shipment entity from GHN response
     */
    private Shipment saveShipmentFromGhn(Map<String,Object> ghnResponse, 
                                         Map<String,Object> ghnPayload,
                                         Address buyerAddress) {
        if (ghnResponse == null || ghnResponse.get("code") == null) {
            throw new RuntimeException("GHN response không hợp lệ");
        }
        
        int code = Integer.parseInt(String.valueOf(ghnResponse.get("code")));
        if (code != 200) {
            String message = ghnResponse.get("message") != null ? 
                String.valueOf(ghnResponse.get("message")) : "Unknown error";
            throw new RuntimeException("GHN trả về lỗi: " + message);
        }
        
        Shipment shipment = new Shipment();
        shipment.setReceiverName(String.valueOf(ghnPayload.get("to_name")));
        shipment.setReceiverPhone(String.valueOf(ghnPayload.get("to_phone")));
        shipment.setReceiverAddress(String.valueOf(ghnPayload.get("to_address")));
        shipment.setProvince(buyerAddress.getProvinceName());
        shipment.setDistrict(buyerAddress.getDistrictName());
        shipment.setWard(buyerAddress.getWardName());
        
        // Parse GHN response data
        Object data = ghnResponse.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            
            // GHN order code
            Object orderCode = dataMap.get("order_code");
            if (orderCode != null) {
                shipment.setGhnOrderCode(String.valueOf(orderCode));
            }
            
            // Shipping fee
            Object totalFee = dataMap.get("total_fee");
            if (totalFee instanceof Number) {
                shipment.setShippingFee(BigDecimal.valueOf(((Number) totalFee).doubleValue()));
            } else if (totalFee != null) {
                try {
                    shipment.setShippingFee(new BigDecimal(String.valueOf(totalFee)));
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Cannot parse shipping fee: " + totalFee);
                }
            }
            
            // Expected delivery time
            Object expectedDeliveryTime = dataMap.get("expected_delivery_time");
            if (expectedDeliveryTime != null) {
                shipment.setExpectedDeliveryTime(String.valueOf(expectedDeliveryTime));
            }
        }
        
        // Save full GHN response as JSON
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                new com.fasterxml.jackson.databind.ObjectMapper();
            shipment.setGhnPayload(mapper.writeValueAsString(ghnResponse));
        } catch (Exception e) {
            shipment.setGhnPayload(ghnResponse.toString());
        }
        
        shipment.setStatus("CREATED");
        
        Shipment saved = shipmentRepository.save(shipment);
        System.out.println("✅ Shipment saved: ID = " + saved.getId() + 
            ", GHN Code = " + saved.getGhnOrderCode());
        
        return saved;
    }
}