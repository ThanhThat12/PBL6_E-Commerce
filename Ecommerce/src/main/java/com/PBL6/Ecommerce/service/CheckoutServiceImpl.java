package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.dto.PaymentCallbackRequest;
import com.PBL6.Ecommerce.dto.PaymentResponseDTO;
import com.PBL6.Ecommerce.exception.MoMoPaymentException;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Implementation of CheckoutService for payment processing
 */
@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    
    private final MoMoPaymentService momoPaymentService;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public CheckoutServiceImpl(MoMoPaymentService momoPaymentService,
                              OrderRepository orderRepository,
                              PaymentTransactionRepository paymentTransactionRepository) {
        this.momoPaymentService = momoPaymentService;
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public PaymentResponseDTO createMomoPayment(Long orderId, BigDecimal amount, String orderInfo) {
        // Sinh orderId duy nhất cho MoMo
        String momoOrderId = "ORD-" + orderId + "-" + java.util.UUID.randomUUID();
        // Gọi hàm createMomoPaymentWithCustomOrderId
        return createMomoPaymentWithCustomOrderId(orderId, amount, orderInfo, momoOrderId);
    }

    @Override
    public PaymentResponseDTO createMomoPaymentWithCustomOrderId(Long orderId, BigDecimal amount, String orderInfo, String momoOrderId) {
        try {
            logger.info("Creating MoMo payment for order ID: {}, momoOrderId: {}, amount: {}", orderId, momoOrderId, amount);
            
            // Validate order exists
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
            
            // Check if order already has successful payment
            if (hasSuccessfulPayment(orderId)) {
                throw new MoMoPaymentException("Order already has successful payment");
            }
            
            // Generate unique request ID
            String requestId = momoPaymentService.generateRequestId(momoOrderId);
            
            // Create initial payment transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrder(order);
            transaction.setRequestId(requestId);
            transaction.setOrderIdMomo(momoOrderId); // Sử dụng momoOrderId duy nhất thay vì orderId
            transaction.setAmount(amount);
            transaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);
            transaction.setPaymentMethod("MOMO");
            
            // Save transaction
            transaction = paymentTransactionRepository.save(transaction);
            logger.info("Created payment transaction with ID: {}, requestId: {}, momoOrderId: {}", 
                       transaction.getId(), requestId, momoOrderId);
            
            // Call MoMo API to create payment (với momoOrderId duy nhất)
            PaymentResponseDTO response = momoPaymentService.createPaymentWithCustomOrderId(
                momoOrderId,  // Gửi momoOrderId duy nhất lên MoMo
                amount, 
                orderInfo, 
                requestId
            );
            
            // Update transaction with response data (Web Payment only)
            transaction.setPayUrl(response.getPayUrl());
            transaction.setResultCode(response.getResultCode());
            transaction.setMessage(response.getMessage());
            transaction.setSignature(response.getSignature());
            
            if (response.getResponseTime() != null) {
                LocalDateTime responseTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(response.getResponseTime()), 
                    ZoneId.systemDefault()
                );
                transaction.setResponseTime(responseTime);
            }
            
            // Update status based on response
            if (response.isSuccess()) {
                transaction.setStatus(PaymentTransaction.PaymentStatus.PROCESSING);
                logger.info("Web Payment created successfully with momoOrderId: {}, user will be redirected to pay.momo.vn", momoOrderId);
            } else {
                transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
                logger.warn("Web Payment creation failed with result code: {}, momoOrderId: {}", response.getResultCode(), momoOrderId);
            }
            
            paymentTransactionRepository.save(transaction);
            
            return response;
            
        } catch (OrderNotFoundException | MoMoPaymentException e) {
            logger.error("Payment creation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating payment: {}", e.getMessage(), e);
            throw new MoMoPaymentException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean processMomoCallback(PaymentCallbackRequest callback) {
        try {
            logger.info("Processing MoMo callback for requestId: {}, transId: {}, resultCode: {}", 
                       callback.getRequestId(), callback.getTransId(), callback.getResultCode());
            
            // Verify callback signature
            if (!momoPaymentService.verifyCallback(callback)) {
                logger.error("Invalid callback signature for requestId: {}", callback.getRequestId());
                return false;
            }
            
            // Find payment transaction by requestId
            PaymentTransaction transaction = paymentTransactionRepository
                    .findByRequestId(callback.getRequestId())
                    .orElseThrow(() -> new OrderNotFoundException(
                        "Payment transaction not found for requestId: " + callback.getRequestId()
                    ));
            
            logger.info("Found payment transaction ID: {} for order: {}", 
                       transaction.getId(), transaction.getOrder().getId());
            
            // Update transaction with callback data
            transaction.setTransId(String.valueOf(callback.getTransId()));
            transaction.setResultCode(callback.getResultCode());
            transaction.setMessage(callback.getMessage());
            transaction.setPayType(callback.getPayType());
            
            if (callback.getResponseTime() != null) {
                LocalDateTime responseTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(callback.getResponseTime()), 
                    ZoneId.systemDefault()
                );
                transaction.setResponseTime(responseTime);
            }
            
            // Update payment status based on result code
            if (momoPaymentService.isPaymentSuccessful(callback)) {
                transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
                logger.info("Payment successful for order: {}", transaction.getOrder().getId());
                
                // Update order payment status
                updateOrderPaymentStatus(transaction.getOrder(), transaction);
                
            } else {
                transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
                logger.warn("Payment failed for order: {}, resultCode: {}", 
                           transaction.getOrder().getId(), callback.getResultCode());
            }
            
            paymentTransactionRepository.save(transaction);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing MoMo callback: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PaymentTransaction getPaymentByRequestId(String requestId) {
        return paymentTransactionRepository.findByRequestId(requestId)
                .orElseThrow(() -> new OrderNotFoundException(
                    "Payment transaction not found for requestId: " + requestId
                ));
    }

    @Override
    public PaymentTransaction getPaymentByOrderId(Long orderId) {
        return paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElse(null);
    }

    @Override
    public boolean hasSuccessfulPayment(Long orderId) {
        return paymentTransactionRepository.existsSuccessfulTransactionForOrder(orderId);
    }

    @Override
    public void updateOrderPaymentStatus(Order order, PaymentTransaction transaction) {
        try {
            logger.info("Updating order payment status for order ID: {}", order.getId());
            if (transaction.getStatus() == PaymentTransaction.PaymentStatus.SUCCESS) {
                // Update order status and payment status
                order.setStatus(Order.OrderStatus.PROCESSING);
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setMethod("MOMO");
                order.setMomoTransId(transaction.getTransId());
                order.setPaidAt(LocalDateTime.now());
                orderRepository.save(order);
                logger.info("Order payment status updated successfully for order: {} (status=PROCESSING, paymentStatus=PAID)", order.getId());
            }
        } catch (Exception e) {
            logger.error("Error updating order payment status: {}", e.getMessage(), e);
        }
    }
}
