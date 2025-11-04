package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.OrderItem;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
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
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
@Transactional
public class OrderService {
    
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final GhnService ghnService;
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

        // prepare GHN payload
        Map<String,Object> ghnPayload = new HashMap<>();
        ghnPayload.put("to_name", req.getReceiverName());
        ghnPayload.put("to_phone", req.getReceiverPhone());
        ghnPayload.put("to_district_id", Integer.parseInt(req.getToDistrictId()));
        ghnPayload.put("to_ward_code", req.getToWardCode());
        ghnPayload.put("to_address", req.getReceiverAddress());
        ghnPayload.put("province", req.getProvince());
        ghnPayload.put("district", req.getDistrict());
        ghnPayload.put("ward", req.getWard());
        ghnPayload.put("weight", req.getWeightGrams());
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

        // Create shipment and set shipment_id
        try {
            var shipment = ghnService.createShippingOrderAsync(saved.getId(), ghnPayload);
            if (shipment != null) {
                saved.setShipment(shipment);
                orderRepository.save(saved);
            }
        } catch (Exception ex) {
            // log only; order has been created
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

