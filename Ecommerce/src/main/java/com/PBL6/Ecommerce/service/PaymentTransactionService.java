package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.payment.PaymentTransaction;
import com.PBL6.Ecommerce.constant.PaymentTransactionStatus;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing payment transactions
 */
@Service
@Transactional
public class PaymentTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);
    
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    /**
     * Get payment transaction by ID
     */
    public PaymentTransaction getById(Long id) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Payment transaction not found with ID: " + id));
    }

    /**
     * Get payment transaction by request ID
     */
    public PaymentTransaction getByRequestId(String requestId) {
        return paymentTransactionRepository.findByRequestId(requestId)
                .orElseThrow(() -> new OrderNotFoundException("Payment transaction not found for requestId: " + requestId));
    }

    /**
     * Get all payment transactions for an order
     */
    public List<PaymentTransaction> getByOrderId(Long orderId) {
        return paymentTransactionRepository.findByOrderId(orderId);
    }

    /**
     * Get latest payment transaction for an order
     */
    public PaymentTransaction getLatestByOrderId(Long orderId) {
        return paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElse(null);
    }

    /**
     * Get payment transactions by user ID
     */
    public List<PaymentTransaction> getByUserId(Long userId) {
        return paymentTransactionRepository.findByUserId(userId);
    }

    /**
     * Get payment transactions by shop ID
     */
    public List<PaymentTransaction> getByShopId(Long shopId) {
        return paymentTransactionRepository.findByShopId(shopId);
    }

    /**
     * Get payment transactions by status
     */
    public List<PaymentTransaction> getByStatus(PaymentTransactionStatus status) {
        return paymentTransactionRepository.findByStatus(status);
    }

    /**
     * Check if order has successful payment
     */
    public boolean hasSuccessfulPayment(Long orderId) {
        return paymentTransactionRepository.existsSuccessfulTransactionForOrder(orderId);
    }

    /**
     * Save payment transaction
     */
    public PaymentTransaction save(PaymentTransaction transaction) {
        return paymentTransactionRepository.save(transaction);
    }

    /**
     * Update payment transaction status
     */
    public PaymentTransaction updateStatus(Long transactionId, PaymentTransactionStatus status) {
        PaymentTransaction transaction = getById(transactionId);
        transaction.setStatus(status);
        return paymentTransactionRepository.save(transaction);
    }

    /**
     * Mark expired pending transactions
     * Should be run periodically (e.g., via scheduled job)
     */
    public int markExpiredTransactions(int timeoutMinutes) {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<PaymentTransaction> expiredTransactions = paymentTransactionRepository
                .findExpiredPendingTransactions(expiredTime);
        
        int count = 0;
        for (PaymentTransaction transaction : expiredTransactions) {
            transaction.setStatus(PaymentTransactionStatus.FAILED);
            transaction.setMessage("Transaction expired after " + timeoutMinutes + " minutes");
            paymentTransactionRepository.save(transaction);
            count++;
        }
        
        logger.info("Marked {} pending transactions as expired/failed", count);
        return count;
    }

    /**
     * Get payment statistics
     */
    public PaymentStatistics getStatistics() {
        PaymentStatistics stats = new PaymentStatistics();
        
    stats.totalPending = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.PENDING);
    stats.totalSuccess = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.SUCCESS);
    stats.totalFailed = paymentTransactionRepository.countByStatus(PaymentTransactionStatus.FAILED);
        
        stats.totalSuccessAmount = paymentTransactionRepository.calculateTotalSuccessfulAmount();
        
        return stats;
    }

    /**
     * Inner class for payment statistics
     */
    public static class PaymentStatistics {
        public Long totalPending;
        public Long totalSuccess;
        public Long totalFailed;
        public Long totalSuccessAmount;

        @Override
        public String toString() {
            return "PaymentStatistics{" +
                    "pending=" + totalPending +
                    ", success=" + totalSuccess +
                    ", failed=" + totalFailed +
                    ", totalAmount=" + totalSuccessAmount +
                    '}';
        }
    }
}
