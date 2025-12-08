package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.constant.PaymentTransactionStatus;
import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.PaymentTransaction;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.dto.PaymentCallbackRequest;
import com.PBL6.Ecommerce.dto.PaymentResponseDTO;
import com.PBL6.Ecommerce.exception.MoMoPaymentException;
import com.PBL6.Ecommerce.exception.OrderNotFoundException;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.service.MoMoService;
import com.PBL6.Ecommerce.service.PaymentTransactionService;
import com.PBL6.Ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for MoMo payment operations
 */
@Tag(name = "MoMo Payment", description = "MoMo payment integration")
@RestController
@RequestMapping("/api/payment/momo")
public class MoMoPaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoPaymentController.class);
    
    private final MoMoService moMoService;
    private final PaymentTransactionService paymentTransactionService;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public MoMoPaymentController(MoMoService moMoService,
                            PaymentTransactionService paymentTransactionService,
                            OrderRepository orderRepository,
                            UserService userService) {
        this.moMoService = moMoService;
        this.paymentTransactionService = paymentTransactionService;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    /**
     * Create MoMo Web Payment for an order
     * POST /api/payment/momo/create
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "orderInfo": "Thanh toán đơn hàng #123"
     * }
     * 
     * Response:
     * {
     *   "payUrl": "httpss://pay.momo.vn/web/...",
     *   "orderId": "ORD-123-UUID",
     *   "requestId": "REQ-...",
     *   "amount": 100000,
     *   "message": "Web Payment URL created successfully. Please redirect user to payUrl to complete payment."
     * }
     * 
     * Frontend should redirect user to the payUrl returned in response.
     * After user completes payment on MoMo, they will be redirected to the return URL with result.
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> createPayment(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            logger.info("User {} requesting MoMo payment creation", userId);
            
            // LOG TOÀN BỘ REQUEST ĐỂ DEBUG
            logger.info("🔍 Received request map: {}", request);
            logger.info("🔍 Request keys: {}", request.keySet());
            logger.info("🔍 Request values: {}", request.values());

            // Parse request with validation
            Object orderIdObj = request.get("orderId");
            logger.info("🔍 orderIdObj from request: {} (type: {})", orderIdObj, orderIdObj != null ? orderIdObj.getClass().getName() : "null");
            
            if (orderIdObj == null) {
                return ResponseDTO.error(400, "BAD_REQUEST", "orderId is required");
            }
            
            Long orderId;
            try {
                String orderIdStr = orderIdObj.toString();
                logger.info("🔍 orderIdStr: '{}'", orderIdStr);
                orderId = Long.valueOf(orderIdStr);
                logger.info("✅ Successfully parsed orderId: {}", orderId);
            } catch (NumberFormatException e) {
                logger.error("❌ Invalid orderId format: '{}' - {}", orderIdObj, e.getMessage());
                return ResponseDTO.error(400, "BAD_REQUEST", "orderId must be a valid number, got: " + orderIdObj);
            }
            
            String orderInfo = request.getOrDefault("orderInfo", "Thanh toán đơn hàng #" + orderId).toString();

            // Validate order exists and belongs to user
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

            // Security check: verify user owns the order
            if (!order.getUser().getId().equals(userId)) {
                return ResponseDTO.error(403, "FORBIDDEN", "You don't have permission to pay for this order");
            }

            // Tự động lấy amount từ order (không cần gõ trong request)
            BigDecimal amount = order.getTotalAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseDTO.error(400, "BAD_REQUEST", "Order has invalid total amount");
            }

            // Sinh orderId duy nhất cho MoMo (UUID, hoặc nối thêm timestamp)
            String momoOrderId = "ORD-" + orderId + "-" + java.util.UUID.randomUUID();

            // Create payment (truyền momoOrderId thay vì orderId tự tăng)
            PaymentResponseDTO paymentResponse = moMoService.createMomoPayment(orderId, amount, orderInfo);

            // Prepare response - Web Payment only (payUrl)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("payUrl", paymentResponse.getPayUrl());
            responseData.put("orderId", paymentResponse.getOrderId());
            responseData.put("requestId", paymentResponse.getRequestId());
            responseData.put("amount", paymentResponse.getAmount());
            responseData.put("message", paymentResponse.getMessage());

            logger.info("MoMo Web Payment created successfully for order: {} (momoOrderId: {})", orderId, momoOrderId);

            return ResponseDTO.success(responseData, "Web Payment URL created successfully. Please redirect user to payUrl to complete payment.");

        } catch (OrderNotFoundException | MoMoPaymentException e) {
            logger.error("Payment creation error: {}", e.getMessage());
            return ResponseDTO.error(400, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating payment: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to create payment: " + e.getMessage());
        }
    }

    /**
     * MoMo IPN (Instant Payment Notification) callback endpoint
     * POST /api/payment/momo/callback
     * 
     * This endpoint receives payment notifications from MoMo
     * Must return https 204 or 200 to acknowledge receipt
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestBody(required = false) PaymentCallbackRequest callback,
            @RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Parse callback data (support both JSON and form-data)
            if (callback == null) {
                callback = new PaymentCallbackRequest();
                callback.setPartnerCode(params.get("partnerCode"));
                callback.setOrderId(params.get("orderId"));
                callback.setRequestId(params.get("requestId"));
                if (params.get("amount") != null) callback.setAmount(Long.valueOf(params.get("amount")));
                callback.setOrderInfo(params.get("orderInfo"));
                callback.setOrderType(params.get("orderType"));
                if (params.get("transId") != null) callback.setTransId(Long.valueOf(params.get("transId")));
                if (params.get("resultCode") != null) callback.setResultCode(Integer.valueOf(params.get("resultCode")));
                callback.setMessage(params.get("message"));
                callback.setPayType(params.get("payType"));
                if (params.get("responseTime") != null) callback.setResponseTime(Long.valueOf(params.get("responseTime")));
                callback.setExtraData(params.get("extraData"));
                callback.setSignature(params.get("signature"));
            }
            logger.info("[MoMo] Callback received - orderId: {}, transId: {}, resultCode: {}", 
                       callback.getOrderId(), callback.getTransId(), callback.getResultCode());

            // Idempotent: check if transaction already SUCCESS
            boolean alreadySuccess = false;
            try {
                if (callback.getRequestId() != null) {
                    PaymentTransaction tx = paymentTransactionService.getByRequestId(callback.getRequestId());
                    if (tx != null && tx.getStatus() == PaymentTransactionStatus.SUCCESS) {
                        alreadySuccess = true;
                    }
                }
            } catch (Exception ex) {
                // ignore, will process as normal
            }

            boolean success = false;
            if (!alreadySuccess) {
                success = moMoService.processMomoCallback(callback);
            } else {
                logger.info("[MoMo] Callback ignored: transaction already SUCCESS for requestId {}", callback.getRequestId());
                success = true;
            }

            response.put("status", success ? "success" : "error");
            response.put("message", success ? "Callback processed successfully" : "Failed to process callback");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[MoMo] Callback error: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Internal error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * MoMo return URL endpoint (user redirected here after payment)
     * GET /api/payment/momo/return
     * 
     * Query params: orderId, requestId, resultCode, message, etc.
     */
    @GetMapping("/return")
    public RedirectView handleReturn(
            @RequestParam(name = "orderId", required = false) String orderId,
            @RequestParam(name = "requestId", required = false) String requestId,
            @RequestParam(name = "resultCode", required = false) Integer resultCode,
            @RequestParam(name = "message", required = false) String message,
            @RequestParam(name = "transId", required = false) String transId) {
        
        try {
            logger.info("User returned from MoMo - orderId: {}, requestId: {}, resultCode: {}", 
                       orderId, requestId, resultCode);
            
            // Update payment status if not already updated by IPN
            if (requestId != null && resultCode != null) {
                try {
                    PaymentTransaction transaction = paymentTransactionService.getByRequestId(requestId);
                    if (transaction != null && transaction.getStatus() == PaymentTransactionStatus.PENDING) {
                        logger.info("Updating payment status from return URL - requestId: {}, resultCode: {}", 
                                   requestId, resultCode);
                        
                        transaction.setResultCode(resultCode);
                        transaction.setMessage(message);
                        
                        if (resultCode == 0) {
                            transaction.setStatus(PaymentTransactionStatus.SUCCESS);
                            if (transId != null) {
                                transaction.setTransId(transId);
                            }
                            
                            // Update order status
                            Order order = transaction.getOrder();
                            moMoService.updateOrderPaymentStatus(order, transaction);
                            
                            logger.info("Payment marked as SUCCESS from return URL");
                        } else {
                            transaction.setStatus(PaymentTransactionStatus.FAILED);
                            logger.info("Payment marked as FAILED from return URL - resultCode: {}", resultCode);
                        }
                        
                        paymentTransactionService.save(transaction);
                    } else if (transaction != null) {
                        logger.info("Payment already processed (status: {}), skipping update from return URL", 
                                   transaction.getStatus());
                    }
                } catch (Exception e) {
                    logger.error("Failed to update payment from return URL: {}", e.getMessage());
                    // Continue to redirect even if update fails
                }
            }
            
            // Determine redirect URL based on result
            String redirectUrl;
            
            if (resultCode != null && resultCode == 0) {
                // Payment successful - redirect to success page
                redirectUrl = "https://localhost:3000/payment/success?orderId=" + orderId + 
                             "&transId=" + transId + "&message=Payment successful";
                logger.info("Payment successful, redirecting to success page");
            } else {
                // Payment failed - redirect to failure page
                redirectUrl = "https://localhost:3000/payment/failed?orderId=" + orderId + 
                             "&resultCode=" + resultCode +
                             "&message=" + (message != null ? message : "Payment failed");
                logger.warn("Payment failed with result code: {}, redirecting to failure page", resultCode);
            }
            
            return new RedirectView(redirectUrl);
            
        } catch (Exception e) {
            logger.error("Error handling return URL: {}", e.getMessage(), e);
            return new RedirectView("https://localhost:3000/payment/error?message=Error processing payment");
        }
    }

    /**
     * Get payment transaction by order ID
     * GET /api/payment/momo/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<PaymentTransaction>> getPaymentByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            // Validate order and ownership
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
            
            if (!order.getUser().getId().equals(userId)) {
                return ResponseDTO.error(403, "FORBIDDEN", "You don't have permission to view this payment");
            }
            
            PaymentTransaction payment = paymentTransactionService.getLatestByOrderId(orderId);
            
            if (payment == null) {
                return ResponseDTO.error(404, "NOT_FOUND", "No payment found for this order");
            }
            
            return ResponseDTO.success(payment, "Payment retrieved successfully");
            
        } catch (OrderNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving payment: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve payment");
        }
    }

    /**
     * Get payment transaction by request ID
     * GET /api/payment/momo/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<PaymentTransaction>> getPaymentByRequestId(
            @PathVariable String requestId,
            Authentication authentication) {
        try {
            PaymentTransaction payment = paymentTransactionService.getByRequestId(requestId);
            
            // Security check
            Long userId = Long.parseLong(authentication.getName());
            if (!payment.getOrder().getUser().getId().equals(userId)) {
                return ResponseDTO.error(403, "FORBIDDEN", "You don't have permission to view this payment");
            }
            
            return ResponseDTO.success(payment, "Payment retrieved successfully");
            
        } catch (OrderNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving payment: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve payment");
        }
    }

    /**
     * Get all payments for current user
     * GET /api/payment/momo/my-payments
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<PaymentTransaction>>> getMyPayments(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            // Get payments for current user
            List<PaymentTransaction> payments = paymentTransactionService.getByUserId(userId);
            
            return ResponseDTO.success(payments, "Payments retrieved successfully");
            
        } catch (Exception e) {
            logger.error("Error retrieving payments: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve payments");
        }
    }

    /**
     * Check payment status for an order
     * GET /api/payment/momo/status/{orderId}
     */
    @GetMapping("/status/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkPaymentStatus(
            @PathVariable Long orderId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            // Validate order and ownership
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
            
            if (!order.getUser().getId().equals(userId)) {
                return ResponseDTO.error(403, "FORBIDDEN", "You don't have permission to view this order");
            }
            
            boolean hasSuccessfulPayment = paymentTransactionService.hasSuccessfulPayment(orderId);
            PaymentTransaction latestPayment = paymentTransactionService.getLatestByOrderId(orderId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("orderId", orderId);
            status.put("hasSuccessfulPayment", hasSuccessfulPayment);
            status.put("latestPayment", latestPayment);
            
            if (latestPayment != null) {
                status.put("paymentStatus", latestPayment.getStatus().toString());
                status.put("transId", latestPayment.getTransId());
                status.put("amount", latestPayment.getAmount());
            }
            
            return ResponseDTO.success(status, "Payment status retrieved successfully");
            
        } catch (OrderNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error checking payment status: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to check payment status");
        }
    }

    /**
     * Get payment statistics (Admin only)
     * GET /api/payment/momo/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<PaymentTransactionService.PaymentStatistics>> getStatistics() {
        try {
            PaymentTransactionService.PaymentStatistics stats = paymentTransactionService.getStatistics();
            return ResponseDTO.success(stats, "Statistics retrieved successfully");
            
        } catch (Exception e) {
            logger.error("Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve statistics");
        }
    }

    /**
     * TEST ONLY - Simulate successful MoMo callback (SKIP signature validation)
     * POST /api/payment/momo/test-callback
     * 
     * Manually trigger callback to update payment status
     * Use this to test database update without real MoMo callback
     */
    @PostMapping("/test-callback")
    public ResponseEntity<ResponseDTO<String>> testCallback(@RequestBody Map<String, Object> request) {
        try {
            String requestId = request.get("requestId").toString();
            Long orderId = Long.valueOf(request.get("orderId").toString().replaceAll("ORD-", "").split("-")[0]);
            
            logger.info("TEST: Simulating MoMo callback for requestId: {} (SKIP signature validation)", requestId);
            
            // Find transaction
            PaymentTransaction transaction = paymentTransactionService.getByRequestId(requestId);
            if (transaction == null) {
                return ResponseDTO.error(404, "NOT_FOUND", "Transaction not found");
            }
            
            // Update directly without signature check
            transaction.setTransId(request.getOrDefault("transId", System.currentTimeMillis()).toString());
            transaction.setResultCode(0);
            transaction.setMessage("TEST: Payment successful");
            transaction.setStatus(PaymentTransactionStatus.SUCCESS);
            
            paymentTransactionService.save(transaction);
            
            logger.info("TEST: Transaction updated successfully - ID: {}, Status: SUCCESS", transaction.getId());
            
            return ResponseDTO.success("Test callback processed successfully", "Database updated - Status: SUCCESS");
            
        } catch (Exception e) {
            logger.error("TEST: Error processing test callback: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Error: " + e.getMessage());
        }
    }

    /**
     * Refund MoMo payment
     * POST /api/payment/momo/refund
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "amount": 50000,
     *   "description": "Refund for damaged product"
     * }
     */
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.dto.MoMoRefundResponseDTO>> refundPayment(
            @RequestBody com.PBL6.Ecommerce.dto.MoMoRefundRequestDTO request,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            logger.info("User {} requesting MoMo refund for order: {}", userId, request.getOrderId());
            
            // Validate request
            if (request.getOrderId() == null) {
                return ResponseDTO.error(400, "BAD_REQUEST", "orderId is required");
            }
            
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseDTO.error(400, "BAD_REQUEST", "amount must be greater than 0");
            }
            
            // Verify order exists and user has permission
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.getOrderId()));
            
            // Process refund
            com.PBL6.Ecommerce.dto.MoMoRefundResponseDTO refundResponse = moMoService.refundPayment(
                request.getOrderId(),
                request.getAmount(),
                request.getDescription() != null ? request.getDescription() : "Refund for order #" + request.getOrderId()
            );
            
            if (refundResponse.getResultCode() == 0) {
                logger.info("MoMo refund successful for order: {}, amount: {}", 
                           request.getOrderId(), request.getAmount());
                return ResponseDTO.success(refundResponse, "Refund processed successfully");
            } else {
                logger.warn("MoMo refund failed for order: {}, code: {}, message: {}", 
                           request.getOrderId(), refundResponse.getResultCode(), refundResponse.getMessage());
                return ResponseDTO.error(
                    400,
                    "REFUND_FAILED", 
                    refundResponse.getMessage()
                );
            }
            
        } catch (OrderNotFoundException e) {
            logger.error("Order not found: {}", e.getMessage());
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (MoMoPaymentException e) {
            logger.error("MoMo refund error: {}", e.getMessage());
            return ResponseDTO.error(400, "PAYMENT_ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during refund: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to process refund");
        }
    }
}
