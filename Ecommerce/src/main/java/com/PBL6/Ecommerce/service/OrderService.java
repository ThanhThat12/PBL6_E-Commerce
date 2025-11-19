package com.PBL6.Ecommerce.service;


import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.dto.CreateOrderRequestDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDTO;
import com.PBL6.Ecommerce.domain.dto.OrderDetailDTO;
import com.PBL6.Ecommerce.domain.dto.OrderItemDTO;
import com.PBL6.Ecommerce.exception.InvalidOrderStatusException;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.exception.ShopNotFoundException;
import com.PBL6.Ecommerce.exception.UnauthorizedOrderAccessException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.constant.TypeAddress;


@Service
@Transactional
public class OrderService {
    
    // ...existing code...
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final GhnService ghnService;
    public OrderService(ProductRepository productRepository,
                        ProductVariantRepository productVariantRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        ShopRepository shopRepository,
                        AddressRepository addressRepository,
                        GhnService ghnService) {
        // ...existing code...
        this.productVariantRepository = productVariantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.ghnService = ghnService;
    }
// ...existing code...
    // ...existing code...
    public int calculateTotalChargeableWeightGrams(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return 0;
        final double divisor = 5000.0;
        int total = 0;
        for (OrderItem item : items) {
            if (item == null) continue;
            int qty = item.getQuantity() == null ? 1 : item.getQuantity();

            // Lấy product (từ variant->product hoặc trực tiếp từ productRepository)
            com.PBL6.Ecommerce.domain.Product product = null;
            ProductVariant variant = item.getVariant();
            if (variant != null && variant.getProduct() != null) {
                product = variant.getProduct();
            } else if (item.getProductId() != null) {
                try {
                    var op = productRepository.findById(item.getProductId());
                    if (op.isPresent()) product = op.get();
                } catch (Exception ignored) {}
            }

            int actual = 0;
            int volumetric = 0;

            // Chỉ sử dụng thông số từ Product; nếu không có product thì bỏ qua
            if (product != null) {
                if (product.getWeightGrams() != null) actual = product.getWeightGrams();
                if (product.getLengthCm() != null && product.getWidthCm() != null && product.getHeightCm() != null) {
                    double volKg = (product.getLengthCm() * (double) product.getWidthCm() * product.getHeightCm()) / divisor;
                    volumetric = (int) Math.ceil(volKg * 1000.0);
                }
            }

            int unit = Math.max(actual, volumetric);
            total += unit * qty;
        }
        return total;
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

            BigDecimal unitPrice = v.getPrice() == null ? BigDecimal.ZERO : v.getPrice();
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

        BigDecimal shippingFee = req.getShippingFee() != null ? req.getShippingFee() : null;
        BigDecimal voucherDiscount = req.getVoucherDiscount() != null ? req.getVoucherDiscount() : BigDecimal.ZERO;

        // If frontend did not provide shippingFee, call GHN /fee to estimate
        if (shippingFee == null) {
            try {
                int totalWeight = calculateTotalChargeableWeightGrams(items);
                Map<String, Object> feePayload = new HashMap<>();

                // Resolve seller pickup address (from shop -> owner -> addresses)
                Long shopId = null;
                if (!items.isEmpty() && !variantMap.isEmpty()) {
                    ProductVariant firstVariant = variantMap.values().iterator().next();
                    Shop s = firstVariant.getProduct().getShop();
                    if (s != null) {
                        shopId = s.getId();
                        // resolve pickup address from shop owner (Shop has no Address relation)
                        if (s.getOwner() != null && s.getOwner().getId() != null) {
                            var ownerPickup = addressRepository.findFirstByUserIdAndTypeAddress(s.getOwner().getId(), TypeAddress.STORE);
                            if (ownerPickup.isPresent()) {
                                var pa = ownerPickup.get();
                                if (pa.getDistrictId() != null) feePayload.put("from_district_id", pa.getDistrictId());
                                if (pa.getWardCode() != null) feePayload.put("from_ward_code", pa.getWardCode());
                            }
                        }
                    }
                }

                // Resolve buyer address: prefer DTO values, otherwise buyer primary address
                Integer toDistrict = null;
                String toWard = null;
                try {
                    if (req.getToDistrictId() != null) toDistrict = Integer.parseInt(req.getToDistrictId());
                } catch (Exception ignored) {}
                if (req.getToWardCode() != null && !req.getToWardCode().isBlank()) toWard = req.getToWardCode();

                if (toDistrict == null || toWard == null) {
                    var buyerAddr = addressRepository.findFirstByUserIdAndTypeAddress(user.getId(), TypeAddress.HOME);
                    if (buyerAddr.isPresent()) {
                        var ba = buyerAddr.get();
                        if (toDistrict == null && ba.getDistrictId() != null) toDistrict = ba.getDistrictId();
                        if (toWard == null && ba.getWardCode() != null) toWard = ba.getWardCode();
                    }
                }

                if (toDistrict != null) feePayload.put("to_district_id", toDistrict);
                if (toWard != null) feePayload.put("to_ward_code", toWard);

                feePayload.put("weight", totalWeight);
                feePayload.put("insurance_value", subtotal.intValue());
                feePayload.put("cod_amount", req.getCodAmount() != null ? req.getCodAmount().intValue() : 0);

                Map<String, Object> feeResp = ghnService.calculateFee(feePayload, shopId);
                if (feeResp != null && feeResp.get("code") != null && Integer.valueOf(String.valueOf(feeResp.get("code"))) == 200) {
                    Object data = feeResp.get("data");
                    if (data instanceof Map) {
                        Object totalFee = ((Map<?, ?>) data).get("total_fee");
                        if (totalFee instanceof Number) shippingFee = BigDecimal.valueOf(((Number) totalFee).doubleValue());
                        else if (totalFee != null) {
                            try { shippingFee = new BigDecimal(String.valueOf(totalFee)); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {
                // fallback to zero if GHN fee fails
            }
        }

        if (shippingFee == null) shippingFee = BigDecimal.ZERO;

        BigDecimal finalTotal = subtotal.add(shippingFee).subtract(voucherDiscount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        Shop shop = null;
        if (!items.isEmpty() && !variantMap.isEmpty()) {
            ProductVariant firstVariant = variantMap.values().iterator().next();
            shop = firstVariant.getProduct().getShop();
        }

        Order order = new Order();
        order.setUser(user);
        order.setShop(shop);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(finalTotal);
        if (req.getMethod() == null || req.getMethod().isBlank()) {
            throw new IllegalArgumentException("Phải chọn phương thức thanh toán (method)!");
        }
        order.setMethod(req.getMethod());

        Order saved = orderRepository.save(order);
        for (OrderItem oi : items) oi.setOrder(saved);
        orderItemRepository.saveAll(items);

        // prepare GHN payload for creating shipment (async best-effort)
        Map<String, Object> ghnPayload = new HashMap<>();
        ghnPayload.put("to_name", req.getReceiverName());
        ghnPayload.put("to_phone", req.getReceiverPhone());
        try { ghnPayload.put("to_district_id", Integer.parseInt(req.getToDistrictId())); } catch (Exception ignored) {}
        if (req.getToWardCode() != null) ghnPayload.put("to_ward_code", req.getToWardCode());
        ghnPayload.put("to_address", req.getReceiverAddress());
        ghnPayload.put("province", req.getProvince());
        ghnPayload.put("district", req.getDistrict());
        ghnPayload.put("ward", req.getWard());

    int weightToSend = calculateTotalChargeableWeightGrams(items);
        ghnPayload.put("weight", weightToSend);
        ghnPayload.put("client_order_code", "ORDER_" + saved.getId());
        ghnPayload.put("cod_amount", req.getCodAmount() != null ? req.getCodAmount().intValue() : 0);
    if (req.getServiceId() != null) ghnPayload.put("service_id", req.getServiceId());
    if (req.getServiceTypeId() != null) ghnPayload.put("service_type_id", req.getServiceTypeId());
        ghnPayload.put("shipping_fee", shippingFee);
        ghnPayload.put("items", req.getItems().stream().map(i -> {
            ProductVariant pv = variantMap.get(i.getVariantId());
            Map<String, Object> m = new HashMap<>();
            m.put("name", pv.getProduct().getName());
            m.put("quantity", i.getQuantity());
            m.put("price", pv.getPrice() != null ? pv.getPrice().intValue() : 0);
            return m;
        }).collect(Collectors.toList()));

        try {
            var shipment = ghnService.createShippingOrderAsync(saved.getId(), ghnPayload);
            if (shipment != null) {
                saved.setShipment(shipment);
                orderRepository.save(saved);
            }
        } catch (Exception ignored) {
            // Do not fail order creation if GHN fails
        }

        return saved;
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
        return orders.stream()
            .filter(o ->
                "COD".equalsIgnoreCase(o.getMethod()) ||
                ("MOMO".equalsIgnoreCase(o.getMethod()) && o.getPaymentStatus() == Order.PaymentStatus.PAID)
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
     
}

