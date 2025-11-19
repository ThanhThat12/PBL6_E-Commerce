package com.PBL6.Ecommerce.service;


import com.PBL6.Ecommerce.constant.OrderStatus;
import com.PBL6.Ecommerce.constant.OrderItemStatus;
import com.PBL6.Ecommerce.constant.PaymentMethod;
import com.PBL6.Ecommerce.constant.PaymentStatus;
import com.PBL6.Ecommerce.constant.RefundStatus;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Refund;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.PaymentTransactionRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import com.PBL6.Ecommerce.repository.ShopRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ItemReturnRequestDTO;
import com.PBL6.Ecommerce.domain.dto.MultiShopOrderResult;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.OrderItemDTO;
import com.PBL6.Ecommerce.exception.InvalidOrderStatusException;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.exception.ShopNotFoundException;
import com.PBL6.Ecommerce.exception.UnauthorizedOrderAccessException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.domain.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final GhnService ghnService;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private MoMoPaymentService momoPaymentService;
    public OrderService(ProductRepository productRepository,
                        ProductVariantRepository productVariantRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        ShopRepository shopRepository,
                        GhnService ghnService) {
        // ...existing code...
        this.productVariantRepository = productVariantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.ghnService = ghnService;
    }
@Transactional
    public Order createOrder(CreateOrderRequestDTO req) {
        var user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Long> variantIds = req.getItems().stream()
                .map(CreateOrderRequestDTO.Item::getVariantId)
                .collect(Collectors.toList());

        Map<Long, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // Calculate subtotal (product prices only)
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (var it : req.getItems()) {
            ProductVariant v = variantMap.get(it.getVariantId());
            if (v == null) throw new IllegalArgumentException("Variant not found: " + it.getVariantId());
            if (v.getStock() == null || v.getStock() < it.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for variant " + v.getId());
            }
            v.setStock(v.getStock() - it.getQuantity());
            productVariantRepository.save(v);

            BigDecimal unitPrice = v.getPrice();
            BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(it.getQuantity()));
            subtotal = subtotal.add(line);

            OrderItem oi = new OrderItem();
            oi.setVariant(v);
            oi.setProductId(v.getProduct().getId());
            oi.setVariantName(v.getSku() != null ? v.getSku() : v.getProduct().getName());
            oi.setPrice(unitPrice);
            oi.setQuantity(it.getQuantity());
            items.add(oi);
        }

        // Get shipping fee and voucher from request (calculated by frontend)
        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : BigDecimal.ZERO;
        BigDecimal voucherDiscount = req.getVoucherDiscount() != null ? req.getVoucherDiscount() : BigDecimal.ZERO;
        
        // Calculate final total: subtotal + shipping - voucher
        BigDecimal finalTotal = subtotal.add(shippingFee).subtract(voucherDiscount);
        
        // Ensure final total is not negative
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        // Get shop from first product variant's shop
        Shop shop = null;
        if (!items.isEmpty() && variantMap.size() > 0) {
            ProductVariant firstVariant = variantMap.values().iterator().next();
            shop = firstVariant.getProduct().getShop();
        }

    Order order = new Order();
    order.setUser(user);
    order.setShop(shop);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(finalTotal); // Use finalTotal (subtotal + shipping - voucher)
    // Set payment method from request (fix missing method field)
        // Bắt buộc frontend phải truyền method (COD, MOMO, BANK_TRANSFER...)
        if (req.getMethod() == null || req.getMethod().isBlank()) {
            throw new IllegalArgumentException("Phải chọn phương thức thanh toán (method)!");
        }
        order.setMethod(req.getMethod());
    // Order does not expose setItems(List<OrderItem>); associate items after saving the order.
    // order.setItems(items);

        Order saved = orderRepository.save(order);
        for (OrderItem oi : items) oi.setOrder(saved);
        orderItemRepository.saveAll(items);

        // Validate required GHN fields before creating payload
        if (req.getReceiverName() == null || req.getReceiverName().isBlank()) {
            throw new IllegalArgumentException("Tên người nhận không được để trống");
        }
        if (req.getReceiverPhone() == null || req.getReceiverPhone().isBlank()) {
            throw new IllegalArgumentException("Số điện thoại người nhận không được để trống");
        }
        if (req.getReceiverAddress() == null || req.getReceiverAddress().isBlank()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống");
        }
        if (req.getToDistrictId() == null || req.getToDistrictId().isBlank()) {
            throw new IllegalArgumentException("Mã quận/huyện không hợp lệ");
        }
        if (req.getToWardCode() == null || req.getToWardCode().isBlank()) {
            throw new IllegalArgumentException("Mã phường/xã không hợp lệ");
        }
        
        // prepare GHN payload
        Map<String,Object> ghnPayload = new HashMap<>();
        ghnPayload.put("to_name", req.getReceiverName());
        ghnPayload.put("to_phone", req.getReceiverPhone());
        
        // Parse district ID as integer (GHN requires integer)
        try {
            ghnPayload.put("to_district_id", Integer.parseInt(req.getToDistrictId()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã quận/huyện phải là số: " + req.getToDistrictId());
        }
        
        ghnPayload.put("to_ward_code", req.getToWardCode());
        ghnPayload.put("to_address", req.getReceiverAddress());
        
        // Store province/district/ward names for reference (not sent to GHN API)
        ghnPayload.put("province", req.getProvince());
        ghnPayload.put("district", req.getDistrict());
        ghnPayload.put("ward", req.getWard());
        
        ghnPayload.put("weight", req.getWeightGrams() != null ? req.getWeightGrams() : 200); // Default 200g
        ghnPayload.put("length", 15); // Default dimensions (cm)
        ghnPayload.put("width", 15);
        ghnPayload.put("height", 15);
        ghnPayload.put("client_order_code", "ORDER_" + saved.getId());
        ghnPayload.put("cod_amount", req.getCodAmount() != null ? req.getCodAmount().intValue() : 0);
        ghnPayload.put("shipping_fee", shippingFee); // Pass frontend-calculated shipping fee
        ghnPayload.put("items", req.getItems().stream().map(i -> {
            ProductVariant pv = variantMap.get(i.getVariantId());
            Map<String,Object> m = new HashMap<>();
            m.put("name", pv.getProduct().getName());
            m.put("quantity", i.getQuantity());
            m.put("price", pv.getPrice().intValue());
            return m;
        }).collect(Collectors.toList()));
        
        logger.info("📦 GHN Payload for order #{}: to_district_id={}, to_ward_code={}, to_address={}", 
            saved.getId(), ghnPayload.get("to_district_id"), ghnPayload.get("to_ward_code"), ghnPayload.get("to_address"));

        // Create shipment based on payment method
        // COD: Tạo shipment ngay, nếu GHN fail thì rollback toàn bộ order
        // Online payment (MOMO/SPORTYPAY): Chỉ tạo shipment sau khi thanh toán thành công
        if ("COD".equalsIgnoreCase(saved.getMethod())) {
            try {
                var shipment = ghnService.createShippingOrderAsync(saved.getId(), ghnPayload);
                if (shipment != null && shipment.getGhnOrderCode() != null) {
                    saved.setShipment(shipment);
                    orderRepository.save(saved);
                    logger.info("✅ Shipment created successfully for COD order: {}", saved.getId());
                } else {
                    // GHN trả về null hoặc không có order code → rollback
                    throw new RuntimeException("GHN không tạo được vận đơn. Vui lòng kiểm tra địa chỉ giao hàng.");
                }
            } catch (Exception ex) {
                // Với COD, GHN fail là không thể tiếp tục → rollback order
                logger.error("❌ GHN shipment creation failed for COD order: {}", ex.getMessage());
                logger.error("GHN Payload: to_district_id={}, to_ward_code={}, to_address={}", 
                    ghnPayload.get("to_district_id"), ghnPayload.get("to_ward_code"), ghnPayload.get("to_address"));
                throw new RuntimeException("Không thể tạo vận đơn giao hàng. Vui lòng kiểm tra: " +
                    "1) Địa chỉ giao hàng đầy đủ và chính xác, " +
                    "2) Mã quận/huyện và phường/xã hợp lệ. " +
                    "Chi tiết lỗi: " + ex.getMessage());
            }
            
            // Xóa cart cho COD ngay sau khi tạo order thành công
            try {
                clearCartAfterSuccessfulPayment(user.getId(), saved.getId());
                logger.info("✅ Cart cleared for COD order #{}", saved.getId());
            } catch (Exception ex) {
                logger.error("❌ Error clearing cart for COD: {}", ex.getMessage());
            }
        } else {
            // Online payment: Chưa tạo shipment, chờ thanh toán xong mới tạo
            logger.info("⏳ Shipment will be created after payment confirmation for method: {}", saved.getMethod());
            logger.info("⏳ Cart will be cleared after payment confirmation");
        }

        return saved;
    }

    /**
     * Tạo shipment cho order sau khi thanh toán thành công (với online payment)
     * Được gọi từ CheckoutService sau khi xác nhận thanh toán
     */
    @Transactional
    public void createShipmentAfterPayment(Long orderId) {
        logger.info("Creating shipment after payment for order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        // Kiểm tra nếu đã có shipment thì không tạo nữa
        if (order.getShipment() != null && order.getShipment().getGhnOrderCode() != null) {
            logger.info("Shipment already exists for order: {}", orderId);
            return;
        }
        
        // Kiểm tra order đã thanh toán chưa
        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            logger.warn("Order {} is not paid yet, cannot create shipment", orderId);
            return;
        }
        
        // Lấy shipment cũ (có thể là GHN_ERROR) để lấy thông tin
        var existingShipment = order.getShipment();
        if (existingShipment == null) {
            logger.warn("No shipment record found for order: {}, cannot retry", orderId);
            return;
        }
        
        // Chuẩn bị GHN payload từ thông tin shipment cũ
        Map<String, Object> ghnPayload = new HashMap<>();
        ghnPayload.put("to_name", existingShipment.getReceiverName());
        ghnPayload.put("to_phone", existingShipment.getReceiverPhone());
        ghnPayload.put("to_address", existingShipment.getReceiverAddress());
        ghnPayload.put("province", existingShipment.getProvince());
        ghnPayload.put("district", existingShipment.getDistrict());
        ghnPayload.put("ward", existingShipment.getWard());
        
        // Parse GHN payload cũ nếu có
        if (existingShipment.getGhnPayload() != null) {
            try {
                var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> oldPayload = objectMapper.readValue(
                    existingShipment.getGhnPayload(), 
                    Map.class
                );
                
                // Lấy các thông tin quan trọng từ payload cũ
                if (oldPayload.containsKey("request")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> request = (Map<String, Object>) oldPayload.get("request");
                    ghnPayload.put("to_district_id", request.get("to_district_id"));
                    ghnPayload.put("to_ward_code", request.get("to_ward_code"));
                    ghnPayload.put("weight", request.get("weight"));
                    ghnPayload.put("cod_amount", request.get("cod_amount"));
                    ghnPayload.put("shipping_fee", request.get("shipping_fee"));
                    ghnPayload.put("items", request.get("items"));
                }
            } catch (Exception e) {
                logger.error("Error parsing old GHN payload: {}", e.getMessage());
            }
        }
        
        ghnPayload.put("client_order_code", "ORDER_" + orderId);
        
        // Gọi GHN API để tạo vận đơn
        try {
            var shipment = ghnService.createShippingOrderAsync(orderId, ghnPayload);
            if (shipment != null && shipment.getGhnOrderCode() != null) {
                order.setShipment(shipment);
                orderRepository.save(order);
                logger.info("✅ Shipment created successfully after payment for order: {}", orderId);
            } else {
                logger.warn("⚠️ GHN returned null or invalid shipment for order: {}", orderId);
            }
        } catch (Exception ex) {
            logger.error("❌ Failed to create shipment after payment for order {}: {}", orderId, ex.getMessage());
            // Không throw exception, để order vẫn có thể tiếp tục
            // Seller có thể tạo lại shipment sau
        }
    }

    /**
     * Xóa các sản phẩm vừa thanh toán khỏi cart
     * @param userId - ID của user
     * @param items - Danh sách sản phẩm vừa đặt order
     */
    private void clearCartAfterOrder(Long userId, List<CreateOrderRequestDTO.Item> items) {
        try {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isEmpty()) {
                return;
            }

            Cart cart = cartOpt.get();
            List<Long> variantIds = items.stream()
                    .map(CreateOrderRequestDTO.Item::getVariantId)
                    .collect(Collectors.toList());

            // Xóa các cart items có variant trong danh sách vừa thanh toán
            cartItemRepository.deleteByCartIdAndVariantIdIn(cart.getId(), variantIds);
        } catch (Exception ex) {
            // log only; không ảnh hưởng đến quy trình thanh toán
            System.err.println("Error clearing cart after order: " + ex.getMessage());
        }
    }

    /**
     * Public method: Xóa sản phẩm khỏi cart sau khi thanh toán thành công
     * @param userId - ID của user
     * @param orderId - ID của order vừa tạo
     */
    public void clearCartAfterSuccessfulPayment(Long userId, Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));
            
            // Lấy danh sách order items
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            
            // Tạo danh sách variant IDs từ order items
            List<Long> variantIds = orderItems.stream()
                    .map(item -> item.getVariant().getId())
                    .collect(Collectors.toList());
            
            // Xóa các cart items tương ứng
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                cartItemRepository.deleteByCartIdAndVariantIdIn(cartOpt.get().getId(), variantIds);
                System.out.println("✅ Cart cleared for user " + userId + " after order #" + orderId);
            }
        } catch (Exception ex) {
            System.err.println("❌ Error clearing cart after successful payment: " + ex.getMessage());
        }
    }

    /**
     * Update order status after successful wallet payment (SPORTYPAY)
     */
    @Transactional
    public Order updateOrderAfterWalletPayment(Long orderId) {
        System.out.println("🔄 [SportyPay] Updating order #" + orderId + " after wallet payment");

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        System.out.println("✅ Found order #" + orderId);
        System.out.println("  - Current status: " + order.getStatus());
        System.out.println("  - Current payment status: " + order.getPaymentStatus());

        // Mark as PAID, giữ nguyên status (PENDING)
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaidAt(java.time.LocalDateTime.now());

        System.out.println("  - Updated status: " + order.getStatus());
        System.out.println("  - Updated payment status: " + order.getPaymentStatus());

        Order saved = orderRepository.save(order);
        System.out.println("✅ Order #" + orderId + " updated successfully!");
        System.out.println("  - Saved status: " + saved.getStatus());
        System.out.println("  - Saved payment status: " + saved.getPaymentStatus());

        // ✅ XÓA CÁC SẢN PHẨM ĐÃ THANH TOÁN KHỎI CART
        try {
            clearCartAfterSuccessfulPayment(order.getUser().getId(), orderId);
            System.out.println("✅ Cart items cleared after successful SportyPay payment for order #" + orderId);
        } catch (Exception e) {
            System.err.println("❌ Error clearing cart after SportyPay payment: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Create multiple orders for multi-shop checkout
     * Groups items by shop and creates separate orders
     */
    @Transactional
    public MultiShopOrderResult createMultiShopOrders(CreateOrderRequestDTO req) {
        // Validate user
        var user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get all variants
        List<Long> variantIds = req.getItems().stream()
                .map(CreateOrderRequestDTO.Item::getVariantId)
                .collect(Collectors.toList());

        Map<Long, ProductVariant> variantMap = productVariantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // Group items by shop
        Map<Long, List<CreateOrderRequestDTO.Item>> itemsByShop = new HashMap<>();
        for (var item : req.getItems()) {
            ProductVariant variant = variantMap.get(item.getVariantId());
            if (variant == null) {
                throw new IllegalArgumentException("Variant not found: " + item.getVariantId());
            }
            Long shopId = variant.getProduct().getShop().getId();
            itemsByShop.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
        }

        // Create separate orders for each shop
        List<Long> orderIds = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, List<CreateOrderRequestDTO.Item>> entry : itemsByShop.entrySet()) {
            // Create a new request DTO for this shop's items
            CreateOrderRequestDTO shopOrderReq = new CreateOrderRequestDTO();
            shopOrderReq.setUserId(req.getUserId());
            shopOrderReq.setItems(entry.getValue());
            shopOrderReq.setReceiverName(req.getReceiverName());
            shopOrderReq.setReceiverPhone(req.getReceiverPhone());
            shopOrderReq.setReceiverAddress(req.getReceiverAddress());
            shopOrderReq.setProvince(req.getProvince());
            shopOrderReq.setDistrict(req.getDistrict());
            shopOrderReq.setWard(req.getWard());
            shopOrderReq.setToDistrictId(req.getToDistrictId());
            shopOrderReq.setToWardCode(req.getToWardCode());
            shopOrderReq.setMethod(req.getMethod());
            
            // Calculate shipping fee for this shop's items
            // For now, divide shipping proportionally by item count
            // TODO: Implement proper per-shop shipping calculation
            int totalItems = req.getItems().size();
            int shopItems = entry.getValue().size();
            BigDecimal shopShippingFee = req.getShippingFee() != null 
                ? req.getShippingFee().multiply(BigDecimal.valueOf(shopItems))
                    .divide(BigDecimal.valueOf(totalItems), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
            
            shopOrderReq.setShippingFee(shopShippingFee);
            
            // Calculate weight for this shop's items
            int shopWeight = entry.getValue().stream()
                .mapToInt(CreateOrderRequestDTO.Item::getQuantity)
                .sum() * 200; // Approximate 200g per item
            shopOrderReq.setWeightGrams(shopWeight);
            
            // Voucher discount only applied to first shop (or split it)
            // For now, no voucher for individual shop orders
            shopOrderReq.setVoucherDiscount(BigDecimal.ZERO);
            shopOrderReq.setCodAmount(BigDecimal.ZERO);

            // Create order for this shop
            Order order = createOrder(shopOrderReq);
            orderIds.add(order.getId());
            grandTotal = grandTotal.add(order.getTotalAmount());
        }

        MultiShopOrderResult result = new MultiShopOrderResult();
        result.setOrderIds(orderIds);
        result.setTotalAmount(grandTotal);
        result.setMethod(req.getMethod());

        return result;
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Get order by ID and verify user ownership (for buyer refund requests)
     */
    public Order getOrderByIdAndUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("Bạn không có quyền truy cập đơn hàng này");
        }
        
        return order;
    }

    /**
     * Lấy danh sách đơn hàng của seller theo username
     * Lấy theo shop của seller
     * @param username - Tên đăng nhập của seller
     * @return List<OrderDTO> - Danh sách đơn hàng
     */
    public List<OrderDTO> getSellerOrders(String username) {
        // Lấy thông tin user
        User seller = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        // Kiểm tra user có phải seller không
        if (seller.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new UnauthorizedOrderAccessException("Người dùng không phải là seller");
        }

        // Lấy shop của seller
        Shop shop = shopRepository.findByOwner(seller)
            .orElseThrow(() -> new ShopNotFoundException(seller.getId()));

        // Lấy tất cả orders của shop này
        List<Order> orders = orderRepository.findOrdersByShopId(shop.getId());

        // Convert sang DTO
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * @param orderId - ID của đơn hàng
     * @param username - Username của seller (để verify quyền)
     * @return OrderDetailDTO - Chi tiết đơn hàng
     */
    public OrderDetailDTO getOrderDetail(Long orderId, String username) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        // Kiểm tra role SELLER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new UnauthorizedOrderAccessException("Người dùng không phải là seller");
        }

        // Tìm shop của seller
        Shop shop = shopRepository.findByOwnerId(user.getId())
            .orElseThrow(() -> new ShopNotFoundException(user.getId()));

        // Tìm order theo ID
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify order thuộc shop của seller
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        // Convert sang DTO
        return convertToDetailDTO(order);
    }

    /**
     * Lấy danh sách đơn hàng theo shop_id
     * @param shopId - ID của shop
     * @return List<OrderDTO> - Danh sách đơn hàng
     */
    public List<OrderDTO> getOrdersByShopId(Long shopId) {
        List<Order> orders = orderRepository.findOrdersByShopId(shopId);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn hàng của buyer theo username
     * @param username - Username của buyer
     * @return List<OrderDTO> - Danh sách đơn hàng
     */
    public List<OrderDTO> getBuyerOrders(String username) {
        // Lấy thông tin user
        User buyer = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        // Lấy tất cả orders của user này
        List<Order> orders = orderRepository.findByUser(buyer);

        // Chỉ hiển thị:
        // - Đơn COD
        // - Đơn MOMO đã thanh toán (paymentStatus = PAID)
        return orders.stream()
            .filter(o ->
                "COD".equalsIgnoreCase(o.getMethod()) ||
                ("MOMO".equalsIgnoreCase(o.getMethod()) && o.getPaymentStatus() == Order.PaymentStatus.PAID)
            )
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn hàng của buyer theo userId
     * @param userId - User ID của buyer
     * @return List<OrderDTO> - Danh sách đơn hàng
     */
    public List<OrderDTO> getBuyerOrdersByUserId(Long userId) {
        // Lấy thông tin user
        User buyer = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Lấy tất cả orders của user này
        List<Order> orders = orderRepository.findByUser(buyer);

        // Chỉ hiển thị:
        // - Đơn COD
        // - Đơn MOMO đã thanh toán (paymentStatus = PAID)
        // - Đơn SPORTYPAY đã thanh toán (paymentStatus = PAID)
        return orders.stream()
            .filter(o ->
                "COD".equalsIgnoreCase(o.getMethod()) ||
                ("MOMO".equalsIgnoreCase(o.getMethod()) && o.getPaymentStatus() == Order.PaymentStatus.PAID) ||
                ("SPORTYPAY".equalsIgnoreCase(o.getMethod()) && o.getPaymentStatus() == Order.PaymentStatus.PAID)
            )
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đơn hàng của buyer theo ID
     * @param orderId - ID của đơn hàng
     * @param username - Username của buyer (để verify quyền)
     * @return OrderDetailDTO - Chi tiết đơn hàng
     */
    public OrderDetailDTO getBuyerOrderDetail(Long orderId, String username) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        // Tìm order theo ID
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify order thuộc user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        // Convert sang DTO
        return convertToDetailDTO(order);
    }

    /**
     * Lấy chi tiết đơn hàng của buyer theo userId
     * @param orderId - ID của đơn hàng
     * @param userId - User ID của buyer (để verify quyền)
     * @return OrderDetailDTO - Chi tiết đơn hàng
     */
    public OrderDetailDTO getBuyerOrderDetailByUserId(Long orderId, Long userId) {
        // Tìm user theo userId
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Tìm order theo ID
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify order thuộc user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        // Convert sang DTO
        return convertToDetailDTO(order);
    }

    /**
     * Lấy tất cả đơn hàng (Admin only)
     * @return List<OrderDTO> - Danh sách tất cả đơn hàng
     */
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đơn hàng (Admin only - không cần verify ownership)
     * @param orderId - ID của đơn hàng
     * @return OrderDetailDTO - Chi tiết đơn hàng
     */
    public OrderDetailDTO getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        return convertToDetailDTO(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng (Admin only - không cần verify ownership)
     * @param orderId - ID của đơn hàng
     * @param newStatus - Trạng thái mới
     * @return OrderDetailDTO - Thông tin đơn hàng sau khi cập nhật
     */
    public OrderDetailDTO updateOrderStatusByAdmin(Long orderId, String newStatus) {
        // Tìm order theo ID
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate và convert status string sang enum
        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException(newStatus);
        }

        // Cập nhật status
        order.setStatus(orderStatus);
        
        // Lưu vào database
        Order updatedOrder = orderRepository.save(order);

        // Convert sang DTO và trả về
        return convertToDetailDTO(updatedOrder);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId - ID của đơn hàng
     * @param newStatus - Trạng thái mới (PENDING, PROCESSING, COMPLETED, CANCELLED)
     * @param username - Username của seller (để verify quyền)
     * @return OrderDetailDTO - Thông tin đơn hàng sau khi cập nhật
     */
    public OrderDetailDTO updateOrderStatus(Long orderId, String newStatus, String username) {
        // Tìm user theo username
        User user = userRepository.findOneByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        // Kiểm tra role SELLER
        if (user.getRole() != com.PBL6.Ecommerce.domain.Role.SELLER) {
            throw new UnauthorizedOrderAccessException("Người dùng không phải là seller");
        }

        // Tìm shop của seller
        Shop shop = shopRepository.findByOwner(user)
            .orElseThrow(() -> new ShopNotFoundException(user.getId()));

        // Tìm order theo ID
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify order thuộc shop của seller
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new UnauthorizedOrderAccessException(orderId);
        }

        // Validate và convert status string sang enum
        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException(newStatus);
        }

        // ⚠️ BUSINESS RULE: Seller không được tự ý chuyển sang COMPLETED hoặc CANCELLED
        // COMPLETED: Chỉ buyer hoặc system (auto after 1 day) mới set được
        // CANCELLED: Chỉ được phép từ PENDING hoặc PROCESSING (đã check ở controller)
        Order.OrderStatus currentStatus = order.getStatus();
        
        // Kiểm tra luồng chuyển trạng thái hợp lệ
        if (orderStatus == Order.OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusException("Seller không được phép tự đặt trạng thái Hoàn thành. Chỉ buyer xác nhận hoặc hệ thống tự động.");
        }
        
        // Validate flow: PENDING → PROCESSING → SHIPPING → (COMPLETED by buyer/system)
        if (currentStatus == Order.OrderStatus.PENDING && orderStatus != Order.OrderStatus.PROCESSING && orderStatus != Order.OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Từ trạng thái Chờ xác nhận, chỉ có thể chuyển sang Đang xử lý hoặc Hủy đơn");
        }
        
        if (currentStatus == Order.OrderStatus.PROCESSING && orderStatus != Order.OrderStatus.SHIPPING && orderStatus != Order.OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Từ trạng thái Đang xử lý, chỉ có thể chuyển sang Đang giao hàng hoặc Hủy đơn");
        }
        
        if (currentStatus == Order.OrderStatus.SHIPPING && orderStatus != Order.OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusException("Từ trạng thái Đang giao hàng, không thể thay đổi. Chỉ buyer hoặc hệ thống tự động chuyển sang Hoàn thành");
        }

        // Cập nhật status
        order.setStatus(orderStatus);
        
        // Lưu vào database
        Order updatedOrder = orderRepository.save(order);

        // Convert sang DTO và trả về
        return convertToDetailDTO(updatedOrder);
    }

    /**
     * Convert Order entity sang OrderDTO (cho danh sách)
     * Chỉ lấy các trường: id, created_at, method, status, total_amount, user_id
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setMethod(order.getMethod());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        
        // Thêm items để frontend có thể filter theo item status
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }

    /**
     * Convert Order entity sang OrderDetailDTO (chi tiết đầy đủ)
     * Lấy tất cả các trường: id, created_at, method, status, total_amount, updated_at, shop_id, user_id, items
     */
    private OrderDetailDTO convertToDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setMethod(order.getMethod());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setShopId(order.getShop() != null ? order.getShop().getId() : null);
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);

        // Thêm đoạn này để lấy thông tin giao hàng từ shipment
        if (order.getShipment() != null) {
            dto.setReceiverName(order.getShipment().getReceiverName());
            dto.setReceiverPhone(order.getShipment().getReceiverPhone());
            dto.setReceiverAddress(order.getShipment().getReceiverAddress());
        }

        // Convert order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }
    
    /**
     * Convert OrderItem entity sang OrderItemDTO
     */
    private OrderItemDTO convertToOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
        dto.setVariantName(item.getVariantName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setStatus(item.getStatus() != null ? item.getStatus().name() : OrderItemStatus.COMPLETED.name());
        
        // Calculate subtotal
        if (item.getPrice() != null && item.getQuantity() != null) {
            dto.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        // Add product details if variant is available
        if (item.getVariant() != null && item.getVariant().getProduct() != null) {
            dto.setProductName(item.getVariant().getProduct().getName());
            dto.setProductImage(item.getVariant().getProduct().getMainImage());
        }
        
        return dto;
    }
     
    @Transactional
    public void cancelOrderAndRefund(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancel this order");
        }
        // Dùng đúng enum inner class của entity
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled in current status");
        }

        // Nếu COD thì chỉ hủy đơn, không hoàn tiền
        if ("COD".equalsIgnoreCase(order.getMethod())) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);
            return;
        }

        // Nếu đã thanh toán bằng MOMO hoặc SPORTYPAY thì hoàn tiền vào ví
        if (("MOMO".equalsIgnoreCase(order.getMethod()) || "SPORTYPAY".equalsIgnoreCase(order.getMethod()))
                && order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            // Tìm giao dịch thanh toán thành công của đơn này
            PaymentTransaction transaction = paymentTransactionRepository
                    .findFirstByOrderIdAndStatus(order.getId(), com.PBL6.Ecommerce.constant.PaymentTransactionStatus.SUCCESS)
                    .orElse(null);

            // Hoàn tiền Momo nếu là thanh toán Momo
            if ("MOMO".equalsIgnoreCase(order.getMethod()) && transaction != null) {
                try {
                    // Gọi API refund Momo UAT
                    momoPaymentService.refundMomoPayment(
                        transaction.getOrderIdMomo(),
                        order.getTotalAmount(),
                        transaction.getTransId()
                    );
                    logger.info("Momo refund successful for order: {}", order.getId());
                } catch (Exception e) {
                    logger.error("Momo refund failed for order: {}, error: {}", order.getId(), e.getMessage());
                    // Vẫn tiếp tục hoàn tiền vào ví ngay cả khi refund Momo thất bại
                }
            }

            // Hoàn tiền vào ví (dùng method deposit của walletService)
            walletService.deposit(order.getUser().getId(), order.getTotalAmount(), "Refund for cancelled order #" + order.getId());

            // Tạo bản ghi refund
            Refund refund = new Refund();
            refund.setOrder(order); // Gán entity Order
            refund.setAmount(order.getTotalAmount());
            refund.setReason(reason != null ? reason : "User cancelled order");
            refund.setStatus(Refund.RefundStatus.COMPLETED); // Dùng enum inner class của Refund
            refundRepository.save(refund);

            // Cập nhật trạng thái đơn hàng
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);
            return;
        }

        // Trường hợp khác (chưa thanh toán), chỉ hủy đơn
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    /**
     * Seller cập nhật trạng thái đơn hàng sang SHIPPING (Đang giao hàng)
     * Shipment record sẽ được tạo từ GHN service
     */
    @Transactional
    public Order markAsShipping(Long orderId) {
        logger.info("Marking order as shipping: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        // Chỉ có thể chuyển từ PENDING hoặc PROCESSING sang SHIPPING
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException("Order must be in PENDING or PROCESSING status");
        }
        
        order.setStatus(Order.OrderStatus.SHIPPING);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * Buyer xác nhận đã nhận hàng → chuyển sang COMPLETED
     */
    @Transactional
    public Order confirmReceived(Long orderId, Long userId) {
        logger.info("Buyer {} confirming received for order: {}", userId, orderId);
        
        Order order = getOrderByIdAndUser(orderId, userId);
        
        if (order.getStatus() != Order.OrderStatus.SHIPPING) {
            throw new InvalidOrderStatusException("Order must be in SHIPPING status");
        }
        
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * Tự động hoàn thành đơn hàng sau 1 ngày kể từ khi chuyển sang SHIPPING
     * Dựa vào Shipment.created_at để tính thời gian
     * Scheduled task sẽ gọi method này
     */
    @Transactional
    public void autoCompleteShippingOrders() {
        logger.info("Running auto-complete for shipping orders");
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        // Tìm tất cả đơn hàng đang ở trạng thái SHIPPING
        List<Order> shippingOrders = orderRepository.findByStatus(Order.OrderStatus.SHIPPING);
        
        int completedCount = 0;
        for (Order order : shippingOrders) {
            // Kiểm tra xem có shipment và đã quá 1 ngày chưa
            if (order.getShipment() != null && 
                order.getShipment().getCreatedAt() != null &&
                order.getShipment().getCreatedAt().isBefore(oneDayAgo)) {
                
                order.setStatus(Order.OrderStatus.COMPLETED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                logger.info("Auto-completed order: {} (shipment created: {})", 
                    order.getId(), order.getShipment().getCreatedAt());
                completedCount++;
            }
        }
        
        logger.info("Auto-completed {} orders out of {} shipping orders", 
            completedCount, shippingOrders.size());
    }

    /**
     * Buyer yêu cầu trả hàng cho một sản phẩm cụ thể trong đơn hàng
     */
    @Transactional
    public Refund requestItemReturn(ItemReturnRequestDTO dto, Long userId) {
        logger.info("Buyer {} requesting return for order item: {}", userId, dto.getOrderItemId());
        
        // Tìm order item
        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
            .orElseThrow(() -> new RuntimeException("Order item not found"));
        
        Order order = orderItem.getOrder();
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You don't have permission to return this item");
        }
        
        // Chỉ cho phép trả hàng khi đơn đã COMPLETED
        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusException("Can only return items from completed orders");
        }
        
        // Kiểm tra item chưa được yêu cầu trả hàng
        if (orderItem.getStatus() != OrderItemStatus.COMPLETED) {
            throw new RuntimeException("Item already has a return request");
        }
        
        // Cập nhật status của order item
        orderItem.setStatus(OrderItemStatus.RETURN_REQUESTED);
        orderItemRepository.save(orderItem);
        
        // Tạo refund request (sử dụng lại bảng refunds)
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setStatus(Refund.RefundStatus.PENDING);
        refund.setReason(dto.getReason());
        
        // Lưu thông tin return method và images vào reason (có thể tạo bảng riêng sau)
        String detailedReason = String.format(
            "Return Request - Product: %s, Quantity: %d, Method: %s, Reason: %s, Images: %s",
            orderItem.getVariantName(),
            dto.getQuantity(),
            dto.getReturnMethod(),
            dto.getReason(),
            dto.getImageUrls() != null ? String.join(",", dto.getImageUrls()) : ""
        );
        refund.setReason(detailedReason);
        
        // Tính số tiền hoàn lại
        BigDecimal refundAmount = orderItem.getPrice().multiply(new BigDecimal(dto.getQuantity()));
        refund.setAmount(refundAmount);
        
        refund.setRequiresReturn(true); // Yêu cầu trả hàng
        refund.setCreatedAt(LocalDateTime.now());
        
        return refundRepository.save(refund);
    }
}

