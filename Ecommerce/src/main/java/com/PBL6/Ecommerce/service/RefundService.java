package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.domain.dto.order.RefundDTO;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.order.OrderItem;
import com.PBL6.Ecommerce.domain.entity.order.Refund;
import com.PBL6.Ecommerce.domain.entity.order.RefundItem;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import com.PBL6.Ecommerce.domain.entity.payment.PaymentTransaction;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.RefundItemRepository;
import com.PBL6.Ecommerce.repository.AddressRepository;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.constant.TypeAddress;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.WalletRepository;
import com.PBL6.Ecommerce.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.PBL6.Ecommerce.domain.dto.order.RefundDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RefundService {
    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalletRepository walletRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AddressRepository addressRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private MoMoPaymentService momoPaymentService;
    
    @Autowired
    private com.PBL6.Ecommerce.repository.ProductVariantRepository productVariantRepository;
    
    @Autowired
    private com.PBL6.Ecommerce.repository.ProductRepository productRepository;

    public RefundService(RefundRepository refundRepository,
                        RefundItemRepository refundItemRepository,
                        OrderItemRepository orderItemRepository,
                        WalletRepository walletRepository,
                        PaymentTransactionRepository paymentTransactionRepository,
                        AddressRepository addressRepository) {
        this.refundRepository = refundRepository;
        this.refundItemRepository = refundItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.walletRepository = walletRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Bước 1: Khách mở yêu cầu hoàn tiền/trả hàng (có lý do + ảnh)
     * Tạo refund_request (status: PENDING)
     */
    @Transactional
    public Refund createRefundRequest(Order order, BigDecimal amount, String reason, String imageUrl) {
        logger.info("Creating refund request for order: {}", order.getId());

        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setImageUrl(imageUrl); // Ảnh bằng chứng từ khách
        // Bước 1: Tạo refund_request (status: REQUESTED)
        refund.setStatus(Refund.RefundStatus.REQUESTED);
        refund.setRequiresReturn(false); // Mặc định không cần trả hàng
        
        return refundRepository.save(refund);
    }
    
    /**
     * Bước 1 (Mới): Tạo refund request với danh sách món hàng cụ thể
     * @param order Đơn hàng
     * @param refundItemsData Map<OrderItemId, Quantity> - Danh sách món và số lượng cần refund
     * @param reason Lý do refund
     * @param imageUrl Ảnh bằng chứng
     * @return Refund object đã tạo
     */
    @Transactional
    public Refund createRefundRequestWithItems(Order order, Map<Long, Integer> refundItemsData, 
                                                String reason, String imageUrl) {
        logger.info("Creating refund request with specific items for order: {}", order.getId());

        // Tính tổng số tiền refund
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setReason(reason);
        refund.setImageUrl(imageUrl);
        // Bước 1: Tạo refund_request (status: REQUESTED)
        refund.setStatus(Refund.RefundStatus.REQUESTED);
        refund.setRequiresReturn(false);
        
        // Tạo refund items
        for (Map.Entry<Long, Integer> entry : refundItemsData.entrySet()) {
            Long orderItemId = entry.getKey();
            Integer refundQuantity = entry.getValue();
            
            // Lấy OrderItem
            OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("OrderItem not found: " + orderItemId));
            
            // Validate số lượng
            if (refundQuantity > orderItem.getQuantity()) {
                throw new IllegalArgumentException(
                    "Refund quantity cannot exceed ordered quantity for item: " + orderItemId);
            }
            
            // Tính số tiền refund cho món này
            BigDecimal itemRefundAmount = orderItem.getPrice()
                .multiply(BigDecimal.valueOf(refundQuantity));
            
            // Tạo RefundItem
            RefundItem refundItem = new RefundItem();
            refundItem.setRefund(refund);
            refundItem.setOrderItem(orderItem);
            refundItem.setQuantity(refundQuantity);
            refundItem.setRefundAmount(itemRefundAmount);
            
            refund.addRefundItem(refundItem);
            totalRefundAmount = totalRefundAmount.add(itemRefundAmount);
        }
        
        refund.setAmount(totalRefundAmount);
        
        logger.info("Refund request created with {} items, total amount: {}", 
            refund.getRefundItems().size(), totalRefundAmount);
        
        return refundRepository.save(refund);
    }
    
    /**
     * Bước 2a: Shop/Admin từ chối → REJECTED
     */
    @Transactional
    public Refund rejectRefund(Long refundId, String rejectReason) {
        logger.info("Rejecting refund: {}", refundId);
        
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found"));
            
        refund.setStatus(Refund.RefundStatus.REJECTED);
        // Lưu lý do từ chối vào reason (có thể append vào reason cũ)
        String fullReason = refund.getReason() + "\n[Lý do từ chối]: " + rejectReason;
        refund.setReason(fullReason);
        
        return refundRepository.save(refund);
    }

    /**
     * Bước 2b: Shop/Admin chấp nhận
     * - Nếu yêu cầu trả hàng → APPROVED_WAITING_RETURN
     * - Nếu không cần trả hàng → APPROVED_REFUNDING (nhảy thẳng)
     */
    @Transactional
    public Refund approveRefund(Long refundId, boolean requiresReturn) {
        logger.info("Approving refund: {}, requiresReturn: {}", refundId, requiresReturn);
        
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found"));
        
        refund.setRequiresReturn(requiresReturn);
        
        // Bước 2b: Shop/Admin chấp nhận
        refund.setStatus(Refund.RefundStatus.APPROVED);
        logger.info("Refund {} approved", refundId);
        
        // Không cần xử lý hoàn tiền tự động ở đây nữa, sẽ xử lý trong bước 5
        
        return refundRepository.save(refund);
    }

    /**
     * Bước 3: Khách đã gửi hàng về → RETURNING
     */
    @Transactional
    public Refund markAsReturning(Long refundId) {
        logger.info("Marking refund as returning: {}", refundId);
        
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found"));
        
        if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
            throw new RuntimeException("Refund must be in APPROVED status");
        }
        
        // Keep status as APPROVED during return process
        return refundRepository.save(refund);
    }

    /**
     * Bước 4: Shop kiểm tra hàng trả về → OK → APPROVED_REFUNDING
     */
    @Transactional
    public Refund confirmReturnReceived(Long refundId, boolean isAccepted, String checkNote) {
        logger.info("Confirming return for refund: {}, accepted: {}", refundId, isAccepted);
        
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found"));
        
        if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
            throw new RuntimeException("Refund must be in APPROVED status");
        }
        
        if (isAccepted) {
            // Hàng OK → hoàn tiền
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            // Lưu ghi chú kiểm tra vào reason
            refund.setReason(refund.getReason() + "\n[Kết quả kiểm tra]: " + checkNote);
            refundRepository.save(refund);
            
            // Tự động xử lý hoàn tiền
            processRefund(refund);
        } else {
            // Hàng không đạt → từ chối
            refund.setStatus(Refund.RefundStatus.REJECTED);
            refund.setReason(refund.getReason() + "\n[Từ chối do]: " + checkNote);
            return refundRepository.save(refund);
        }
        
        return refund;
    }
    
    /**
     * Seller xác nhận đã nhận hàng và tự động hoàn tiền
     * Có thể gọi từ APPROVED_WAITING_RETURN hoặc RETURNING
     */
    @Transactional
    public Refund confirmReceiptAndRefund(Long refundId) {
        logger.info("Confirming receipt and processing refund: {}", refundId);
        
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found"));
        
        // Must be in APPROVED status
        if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
            throw new RuntimeException("Refund must be in APPROVED status");
        }
        
        // Mark as completed after inspection
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund.setReason(refund.getReason() + "\n[Kết quả kiểm tra]: Hàng đã nhận và kiểm tra OK");
        refundRepository.save(refund);
        
        // ✅ Restore stock khi seller confirm đã nhận hàng trả
        restoreStockForRefund(refund);
        
        // Tự động xử lý hoàn tiền
        processRefund(refund);
        
        return refund;
    }

    /**
     * Bước 5: Backend gọi API thanh toán để refund (MoMo...)
     * Cập nhật trạng thái order + refund + payment
     */
    @Transactional
    public void processRefund(Refund refund) {
        logger.info("Processing refund: {}", refund.getId());
        
        Order order = refund.getOrder();
        String paymentMethod = order.getMethod();
        
        try {
            if ("MOMO".equalsIgnoreCase(paymentMethod)) {
                // Gọi API Momo để hoàn tiền
                List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByOrder(order);
                    
                PaymentTransaction transaction = !transactions.isEmpty() ? transactions.get(0) : null;
                
                if (transaction != null) {
                    try {
                        momoPaymentService.refundMomoPayment(
                            transaction.getOrderIdMomo(),
                            refund.getAmount(),
                            transaction.getTransId()
                        );
                        logger.info("Momo refund successful for refund: {}", refund.getId());
                    } catch (Exception e) {
                        logger.error("Momo refund failed: {}", e.getMessage());
                        // Vẫn tiếp tục hoàn vào ví
                    }
                }
            }
            
            // Hoàn tiền vào ví SportyPay (luôn thực hiện)
            walletService.deposit(
                order.getUser().getId(), 
                refund.getAmount(), 
                "Hoàn tiền đơn hàng #" + order.getId()
            );
            
            // Cập nhật trạng thái refund → COMPLETED
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            refundRepository.save(refund);
            
            // Chỉ hủy đơn hàng nếu refund toàn bộ giá trị đơn hàng
            // Kiểm tra 2 cách:
            // 1. So sánh refund.amount với order.totalAmount
            // 2. Kiểm tra xem có refund items không và tất cả items có được refund hết không
            
            boolean isFullRefund = false;
            
            if (refund.getRefundItems() != null && !refund.getRefundItems().isEmpty()) {
                // Có refund items cụ thể → kiểm tra xem tất cả items có được refund hết không
                List<OrderItem> allOrderItems = order.getOrderItems();
                int totalRefundedItems = 0;
                int totalRefundedQuantity = 0;
                
                for (RefundItem refundItem : refund.getRefundItems()) {
                    totalRefundedItems++;
                    totalRefundedQuantity += refundItem.getQuantity();
                }
                
                // Tính tổng số lượng trong đơn hàng
                int totalOrderQuantity = 0;
                for (OrderItem oi : allOrderItems) {
                    totalOrderQuantity += oi.getQuantity();
                }
                
                // Nếu refund hết tất cả items và số lượng → full refund
                isFullRefund = (totalRefundedItems == allOrderItems.size()) && 
                               (totalRefundedQuantity >= totalOrderQuantity);
                
                logger.info("Refund items check: refunded {}/{} items, {}/{} quantity", 
                    totalRefundedItems, allOrderItems.size(), 
                    totalRefundedQuantity, totalOrderQuantity);
            } else {
                // Không có refund items → so sánh số tiền
                BigDecimal orderTotal = order.getTotalAmount();
                BigDecimal refundAmount = refund.getAmount();
                
                // Cho phép sai số nhỏ (± 1000 VND) do làm tròn
                isFullRefund = refundAmount.compareTo(orderTotal) >= 0 || 
                               orderTotal.subtract(refundAmount).abs().compareTo(new BigDecimal("1000")) <= 0;
                
                logger.info("Amount check: refund {}/{}", refundAmount, orderTotal);
            }
            
            if (isFullRefund) {
                // Hoàn tiền toàn bộ → Hủy đơn hàng
                order.setStatus(Order.OrderStatus.CANCELLED);
                logger.info("Full refund - Order {} cancelled", order.getId());
            } else {
                // Hoàn tiền một phần → Giữ nguyên trạng thái đơn hàng
                // Đánh dấu là PARTIALLY_REFUNDED nếu đã COMPLETED
                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                    // Có thể thêm enum PARTIALLY_REFUNDED vào OrderStatus nếu muốn
                    // order.setStatus(Order.OrderStatus.PARTIALLY_REFUNDED);
                }
                logger.info("Partial refund - Order {} remains in current status. " +
                           "Refunded {} items with total amount: {}", 
                    order.getId(), 
                    refund.getRefundItems() != null ? refund.getRefundItems().size() : 0,
                    refund.getAmount());
            }
            
            logger.info("Refund completed successfully: {}", refund.getId());
            
        } catch (Exception e) {
            logger.error("Error processing refund: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - để backward compatibility
     */
    @Transactional
    public Refund createAndCompleteRefund(Order order, BigDecimal amount, String reason) {
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund = refundRepository.save(refund);
        
        processRefund(refund);
        return refund;
    }
    
    /**
     * Get all refunds for a specific shop
     */
    public List<Refund> getRefundsByShop(Long shopId) {
        logger.info("Getting refunds for shop: {}", shopId);
        return refundRepository.findByShopId(shopId);
    }
    
    /**
     * Get all refunds for a specific user (buyer)
     */
    public List<Refund> getRefundsByUserId(Long userId) {
        logger.info("Getting refunds for user: {}", userId);
        return refundRepository.findByUserId(userId);
    }
    
    /**
     * Get refund by ID
     */
    public Refund getRefundById(Long refundId) {
        logger.info("Getting refund by id: {}", refundId);
        return refundRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found with id: " + refundId));
    }

    /**
     * Convert Refund entity to DTO
     */
    public RefundDTO convertToDTO(Refund refund) {
        RefundDTO dto = new RefundDTO(refund);

        // Lấy địa chỉ shop từ bảng addresses theo type_address = STORE
        if (refund.getOrder() != null && refund.getOrder().getShop() != null) {
            var shop = refund.getOrder().getShop();
            var shopOwner = shop.getOwner();
            if (shopOwner != null) {
                var shopAddress = addressRepository.findFirstByUserIdAndTypeAddress(shopOwner.getId(), TypeAddress.STORE)
                    .or(() -> addressRepository.findByUserIdAndPrimaryAddressTrue(shopOwner.getId()))
                    .orElse(null);
                if (shopAddress != null) {
                    dto.setShopAddress(shopAddress.getFullAddress());
                } else {
                    dto.setShopAddress("Chưa cập nhật");
                }
            } else {
                dto.setShopAddress("Chưa cập nhật");
            }
        }

        // Get first order item for display
        if (refund.getOrder() != null && refund.getOrder().getOrderItems() != null 
                && !refund.getOrder().getOrderItems().isEmpty()) {
            var orderItem = refund.getOrder().getOrderItems().get(0);
            var variant = orderItem.getVariant();
            var product = variant != null ? variant.getProduct() : null;
            
            if (product != null) {
                RefundDTO.OrderItemDTO itemDTO = new RefundDTO.OrderItemDTO(
                    orderItem.getId(),
                    product.getName(),
                    product.getMainImage(),
                    orderItem.getVariantName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice()
                );
                dto.setOrderItem(itemDTO);
            }
        }
        
        return dto;
    }
    
    /**
     * Helper method: Restore stock khi refund được confirm
     * @param refund Refund object
     */
    @Transactional
    public void restoreStockForRefund(Refund refund) {
        logger.info("🔄 [RESTORE_STOCK] Restoring stock for refund #{}", refund.getId());
        
        if (refund.getRefundItems() != null && !refund.getRefundItems().isEmpty()) {
            // Có refund items cụ thể → restore từng item
            for (com.PBL6.Ecommerce.domain.entity.order.RefundItem refundItem : refund.getRefundItems()) {
                OrderItem orderItem = refundItem.getOrderItem();
                if (orderItem == null || orderItem.getVariant() == null) {
                    logger.warn("⚠️ OrderItem or Variant not found for RefundItem #{}", refundItem.getId());
                    continue;
                }
                
                com.PBL6.Ecommerce.domain.entity.product.ProductVariant variant = orderItem.getVariant();
                Integer refundQuantity = refundItem.getQuantity();
                
                // Restore stock
                Integer currentStock = variant.getStock() != null ? variant.getStock() : 0;
                Integer newStock = currentStock + refundQuantity;
                variant.setStock(newStock);
                productVariantRepository.save(variant);
                logger.info("📈 [STOCK_RESTORED] Variant #{}: {} → {} (+{})", 
                    variant.getId(), currentStock, newStock, refundQuantity);
                
                // Decrease sold_count
                com.PBL6.Ecommerce.domain.entity.product.Product product = variant.getProduct();
                if (product != null) {
                    Integer currentSoldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
                    Integer newSoldCount = Math.max(0, currentSoldCount - refundQuantity);
                    product.setSoldCount(newSoldCount);
                    productRepository.save(product);
                    logger.info("📉 [SOLD_COUNT_DECREASED] Product #{}: {} → {} (-{})", 
                        product.getId(), currentSoldCount, newSoldCount, refundQuantity);
                }
            }
        } else {
            // Không có refund items cụ thể → restore toàn bộ order
            Order order = refund.getOrder();
            if (order != null && order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    com.PBL6.Ecommerce.domain.entity.product.ProductVariant variant = item.getVariant();
                    if (variant == null) {
                        logger.warn("⚠️ Variant not found for OrderItem #{}", item.getId());
                        continue;
                    }
                    
                    // Restore stock
                    Integer currentStock = variant.getStock() != null ? variant.getStock() : 0;
                    Integer newStock = currentStock + item.getQuantity();
                    variant.setStock(newStock);
                    productVariantRepository.save(variant);
                    logger.info("📈 [STOCK_RESTORED] Variant #{}: {} → {} (+{})", 
                        variant.getId(), currentStock, newStock, item.getQuantity());
                    
                    // Decrease sold_count
                    com.PBL6.Ecommerce.domain.entity.product.Product product = variant.getProduct();
                    if (product != null) {
                        Integer currentSoldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
                        Integer newSoldCount = Math.max(0, currentSoldCount - item.getQuantity());
                        product.setSoldCount(newSoldCount);
                        productRepository.save(product);
                        logger.info("📉 [SOLD_COUNT_DECREASED] Product #{}: {} → {} (-{})", 
                            product.getId(), currentSoldCount, newSoldCount, item.getQuantity());
                    }
                }
            }
        }
        
        logger.info("✅ [RESTORE_STOCK] Stock restored successfully for refund #{}", refund.getId());
    }

    /**
     * Tạo refund request cho một sản phẩm cụ thể (từ frontend mới)
     * @param orderItemId ID của order item
     * @param userId ID của buyer
     * @param reason Lý do refund
     * @param description Mô tả chi tiết
     * @param quantity Số lượng muốn refund
     * @param imageUrlsJson JSON string chứa mảng URLs ảnh bằng chứng
     * @param requestedAmount Số tiền yêu cầu hoàn
     * @return Refund object đã tạo
     */
    @Transactional
    public Refund createRefundRequestByItem(Long orderItemId, Long userId, String reason,
                                           String description, Integer quantity,
                                           String imageUrlsJson, BigDecimal requestedAmount) {
        logger.info("Creating refund request for orderItemId: {}, quantity: {}, amount: {}",
            orderItemId, quantity, requestedAmount);

        // Lấy OrderItem
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new RuntimeException("OrderItem not found: " + orderItemId));

        // Validate user ownership
        Order order = orderItem.getOrder();
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Order does not belong to user");
        }

        // Validate quantity
        if (quantity > orderItem.getQuantity()) {
            throw new IllegalArgumentException(
                "Refund quantity (" + quantity + ") cannot exceed ordered quantity (" + orderItem.getQuantity() + ")");
        }

        // Tạo Refund
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(requestedAmount);
        refund.setReason(reason);
        refund.setImageUrl(imageUrlsJson); // JSON array of image URLs
        refund.setStatus(Refund.RefundStatus.REQUESTED);
        refund.setRequiresReturn(false); // Seller sẽ quyết định sau

        // Tạo RefundItem
        RefundItem refundItem = new RefundItem();
        refundItem.setRefund(refund);
        refundItem.setOrderItem(orderItem);
        refundItem.setQuantity(quantity);
        refundItem.setRefundAmount(requestedAmount);

        refund.addRefundItem(refundItem);

        // Lưu description vào reason field (kết hợp)
        String fullReason = reason + "\n" + description;
        refund.setReason(fullReason);

        logger.info("Refund request created for item #{}, quantity: {}, amount: {}",
            orderItemId, quantity, requestedAmount);

        return refundRepository.save(refund);
    }
}