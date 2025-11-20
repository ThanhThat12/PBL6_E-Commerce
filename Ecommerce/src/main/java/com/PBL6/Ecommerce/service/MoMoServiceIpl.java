package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.constant.PaymentTransactionStatus;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of CheckoutService for payment processing
 */
@Service
@Transactional
public class MoMoServiceIpl implements MoMoService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoServiceIpl.class);
    
    private final MoMoPaymentService momoPaymentService;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    public MoMoServiceIpl(MoMoPaymentService momoPaymentService,
                              OrderRepository orderRepository,
                              PaymentTransactionRepository paymentTransactionRepository,
                              OrderService orderService,
                              SimpMessagingTemplate messagingTemplate) {
        this.momoPaymentService = momoPaymentService;
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
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
            transaction.setStatus(PaymentTransactionStatus.PENDING);
            
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
            
            // Update status based on response
            if (response.isSuccess()) {
                transaction.setStatus(PaymentTransactionStatus.PENDING);
                logger.info("Web Payment created successfully with momoOrderId: {}, user will be redirected to pay.momo.vn", momoOrderId);
            } else {
                transaction.setStatus(PaymentTransactionStatus.FAILED);
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
            
            // Update payment status based on result code
            if (momoPaymentService.isPaymentSuccessful(callback)) {
                transaction.setStatus(PaymentTransactionStatus.SUCCESS);
                logger.info("Payment successful for order: {}", transaction.getOrder().getId());
                
                // Update order payment status
                updateOrderPaymentStatus(transaction.getOrder(), transaction);
                
                // Send WebSocket notification to user
                sendPaymentNotification(transaction.getOrder(), transaction);
                
            } else {
                transaction.setStatus(PaymentTransactionStatus.FAILED);
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
            if (transaction.getStatus() == PaymentTransactionStatus.SUCCESS) {
                // Update order status and payment status
                // Sau khi thanh toán thành công, đơn hàng chuyển sang trạng thái PENDING (chờ xác nhận)
                order.setStatus(Order.OrderStatus.PENDING);
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setMethod("MOMO");
                order.setMomoTransId(transaction.getTransId());
                order.setPaidAt(new java.util.Date());
                orderRepository.save(order);
                logger.info("Order payment status updated successfully for order: {} (status=PENDING, paymentStatus=PAID)", order.getId());
                
                // ✅ XÓA CÁC SẢN PHẨM ĐÃ THANH TOÁN KHỊI CART
                try {
                    orderService.clearCartAfterSuccessfulPayment(order.getUser().getId(), order.getId());
                    logger.info("✅ Cart items cleared after successful payment for order: {}", order.getId());
                } catch (Exception e) {
                    logger.error("❌ Error clearing cart after payment: {}", e.getMessage(), e);
                }
                
                // ✅ TẠO SHIPMENT SAU KHI THANH TOÁN THÀNH CÔNG
                try {
                    orderService.createShipmentAfterPayment(order.getId());
                    logger.info("✅ Shipment creation initiated after payment for order: {}", order.getId());
                } catch (Exception e) {
                    logger.error("❌ Error creating shipment after payment: {}", e.getMessage(), e);
                    // Không throw exception, order vẫn hợp lệ
                }
            }
        } catch (Exception e) {
            logger.error("Error updating order payment status: {}", e.getMessage(), e);
        }
    }

    /**
     * Send WebSocket notification to user after successful payment
     */
    private void sendPaymentNotification(Order order, PaymentTransaction transaction) {
        try {
            Long userId = order.getUser().getId();
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("orderId", order.getId());
            notification.put("orderStatus", order.getStatus().name());
            notification.put("paymentStatus", order.getPaymentStatus().name());
            notification.put("transactionId", transaction.getId());
            notification.put("amount", order.getTotalAmount());
            notification.put("message", "Payment successful for order #" + order.getId());
            notification.put("timestamp", System.currentTimeMillis());
            
            String destination = "/topic/orderws/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("WebSocket notification sent to {} for order: {}", destination, order.getId());
        } catch (Exception e) {
            logger.error("Error sending WebSocket notification for order: {}, error: {}", 
                        order.getId(), e.getMessage(), e);
            // Don't throw exception, payment already successful
        }
    }
}
