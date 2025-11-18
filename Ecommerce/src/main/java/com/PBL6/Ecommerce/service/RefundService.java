package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Refund;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.repository.RefundRepository;
import com.PBL6.Ecommerce.repository.WalletRepository;
import com.PBL6.Ecommerce.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RefundService {
    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    
    private final RefundRepository refundRepository;
    private final WalletRepository walletRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private MoMoPaymentService momoPaymentService;

    public RefundService(RefundRepository refundRepository, 
                        WalletRepository walletRepository,
                        PaymentTransactionRepository paymentTransactionRepository) {
        this.refundRepository = refundRepository;
        this.walletRepository = walletRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
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
        refund.setStatus(Refund.RefundStatus.PENDING);
        refund.setRequiresReturn(false); // Mặc định không cần trả hàng
        
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
        
        if (requiresReturn) {
            // Yêu cầu khách trả hàng trước
            refund.setStatus(Refund.RefundStatus.APPROVED_WAITING_RETURN);
            logger.info("Refund {} waiting for return", refundId);
        } else {
            // Không cần trả hàng → nhảy thẳng sang hoàn tiền
            refund.setStatus(Refund.RefundStatus.APPROVED_REFUNDING);
            logger.info("Refund {} ready for refunding (no return needed)", refundId);
            
            // Tự động xử lý hoàn tiền luôn
            processRefund(refund);
        }
        
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
        
        if (refund.getStatus() != Refund.RefundStatus.APPROVED_WAITING_RETURN) {
            throw new RuntimeException("Refund must be in APPROVED_WAITING_RETURN status");
        }
        
        refund.setStatus(Refund.RefundStatus.RETURNING);
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
        
        if (refund.getStatus() != Refund.RefundStatus.RETURNING) {
            throw new RuntimeException("Refund must be in RETURNING status");
        }
        
        if (isAccepted) {
            // Hàng OK → chuyển sang hoàn tiền
            refund.setStatus(Refund.RefundStatus.APPROVED_REFUNDING);
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
        
        // Cho phép từ APPROVED_WAITING_RETURN hoặc RETURNING
        if (refund.getStatus() != Refund.RefundStatus.APPROVED_WAITING_RETURN 
                && refund.getStatus() != Refund.RefundStatus.RETURNING) {
            throw new RuntimeException("Refund must be in APPROVED_WAITING_RETURN or RETURNING status");
        }
        
        // Chuyển sang APPROVED_REFUNDING
        refund.setStatus(Refund.RefundStatus.APPROVED_REFUNDING);
        refund.setReason(refund.getReason() + "\n[Kết quả kiểm tra]: Hàng đã nhận và kiểm tra OK");
        refundRepository.save(refund);
        
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
            
            // Cập nhật trạng thái order
            order.setStatus(Order.OrderStatus.CANCELLED);
            
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
        refund.setStatus(Refund.RefundStatus.APPROVED_REFUNDING);
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
     * Convert Refund entity to DTO
     */
    public com.PBL6.Ecommerce.dto.RefundDTO convertToDTO(Refund refund) {
        com.PBL6.Ecommerce.dto.RefundDTO dto = new com.PBL6.Ecommerce.dto.RefundDTO(refund);
        
        // Get shop address for return shipping
        if (refund.getOrder() != null && refund.getOrder().getShop() != null) {
            dto.setShopAddress(refund.getOrder().getShop().getAddress());
        }
        
        // Get first order item for display
        if (refund.getOrder() != null && refund.getOrder().getOrderItems() != null 
                && !refund.getOrder().getOrderItems().isEmpty()) {
            var orderItem = refund.getOrder().getOrderItems().get(0);
            var variant = orderItem.getVariant();
            var product = variant != null ? variant.getProduct() : null;
            
            if (product != null) {
                com.PBL6.Ecommerce.dto.RefundDTO.OrderItemDTO itemDTO = new com.PBL6.Ecommerce.dto.RefundDTO.OrderItemDTO(
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
}
