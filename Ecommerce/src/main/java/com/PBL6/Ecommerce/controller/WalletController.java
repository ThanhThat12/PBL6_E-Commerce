package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.domain.WalletTransaction;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.PBL6.Ecommerce.service.UserService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Controller for Wallet operations
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final UserService userService;

    public WalletController(WalletService walletService, UserRepository userRepository, UserService userService) {
        this.walletService = walletService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Get user's wallet information
     * GET /api/wallet
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Wallet>> getWallet(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            Wallet wallet = walletService.getOrCreateWallet(user);
            
            return ResponseDTO.success(wallet, "Wallet retrieved successfully");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving wallet: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve wallet");
        }
    }

    /**
     * Get wallet balance
     * GET /api/wallet/balance
     */
    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getBalance(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            BigDecimal balance = walletService.getBalance(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            
            return ResponseDTO.success(response, "Balance retrieved successfully");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving balance: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve balance");
        }
    }

    /**
     * Create MoMo payment for wallet deposit
     * POST /api/wallet/deposit
     * 
     * Request body:
     * {
     *   "amount": 100000,
     *   "description": "Nạp tiền vào ví SportyPay"
     * }
     * 
     * Response: MoMo payment URL
     */
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> deposit(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.getOrDefault("description", "Nạp tiền vào ví SportyPay").toString();
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseDTO.error(400, "BAD_REQUEST", "Amount must be positive");
            }
            
            // Create MoMo payment for wallet deposit
            // Use negative wallet ID to distinguish from order payments
            String walletOrderId = "WALLET-" + userId + "-" + System.currentTimeMillis();
            
            com.PBL6.Ecommerce.dto.PaymentResponseDTO momoResponse = 
                walletService.createDepositPayment(user.getId(), amount, description, walletOrderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("payUrl", momoResponse.getPayUrl());
            response.put("orderId", momoResponse.getOrderId());
            response.put("requestId", momoResponse.getRequestId());
            response.put("amount", amount);
            response.put("message", "Redirect to MoMo to complete payment");
            
            logger.info("User {} initiated wallet deposit of {} via MoMo", user.getUsername(), amount);
            
            return ResponseDTO.success(response, "MoMo payment created. Redirect user to payUrl");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(400, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating deposit payment: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to create deposit payment");
        }
    }
    
    /**
     * Handle MoMo callback for wallet deposit
     * This is called by MoMo IPN after user completes payment
     */
    @PostMapping("/deposit/callback")
    public ResponseEntity<Map<String, Object>> handleDepositCallback(
            @RequestBody com.PBL6.Ecommerce.dto.PaymentCallbackRequest callback) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("========== WALLET DEPOSIT CALLBACK ==========");
            logger.info("OrderId: {}", callback.getOrderId());
            logger.info("ResultCode: {}", callback.getResultCode());
            logger.info("Amount: {}", callback.getAmount());
            logger.info("TransId: {}", callback.getTransId());
            logger.info("Message: {}", callback.getMessage());
            
            if (callback.getResultCode() == 0) {
                // Payment successful - credit wallet
                String orderId = callback.getOrderId();
                logger.info("Processing successful payment for orderId: {}", orderId);
                
                if (orderId != null && orderId.startsWith("WALLET-")) {
                    String[] parts = orderId.split("-");
                    logger.info("OrderId parts: {}", String.join(", ", parts));
                    
                    if (parts.length >= 3) {
                        Long userId = Long.parseLong(parts[1]);
                        BigDecimal amount = new BigDecimal(callback.getAmount());
                        
                        logger.info("Crediting wallet - userId: {}, amount: {}", userId, amount);
                        
                        Wallet wallet = walletService.deposit(userId, amount, 
                            String.format("Nạp tiền qua MoMo - TransId: %s, OrderId: %s", 
                                         callback.getTransId(), orderId));
                        
                        logger.info("✅ Wallet credited successfully!");
                        logger.info("UserId: {}, NewBalance: {}, Amount: {}", 
                                   userId, wallet.getBalance(), amount);
                        
                        response.put("status", "success");
                        response.put("userId", userId);
                        response.put("newBalance", wallet.getBalance());
                    } else {
                        logger.error("Invalid orderId format - not enough parts: {}", orderId);
                        response.put("status", "error");
                        response.put("message", "Invalid orderId format");
                    }
                } else {
                    logger.error("OrderId does not start with WALLET-: {}", orderId);
                    response.put("status", "error");
                    response.put("message", "Invalid wallet orderId");
                }
            } else {
                logger.warn("❌ Wallet deposit failed - resultCode: {}, message: {}", 
                           callback.getResultCode(), callback.getMessage());
                response.put("status", "failed");
                response.put("resultCode", callback.getResultCode());
            }
            
            logger.info("=============================================");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 Error processing wallet deposit callback: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Request withdrawal from wallet to MoMo account
     * POST /api/wallet/withdraw
     * 
     * Request body:
     * {
     *   "amount": 50000,
     *   "momoPhone": "0123456789",
     *   "description": "Rút tiền từ ví SportyPay"
     * }
     * 
     * Note: This creates a withdrawal request that will be processed via MoMo refund API
     * Money will be transferred to user's MoMo account
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Wallet>> withdraw(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.getOrDefault("description", "Rút tiền từ ví").toString();
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseDTO.error(400, "BAD_REQUEST", "Amount must be positive");
            }
            
            Wallet wallet = walletService.withdraw(user.getId(), amount, description);
            
            logger.info("Raw withdraw request: {}", request);
            logger.info("User {} withdrew {} from wallet", user.getUsername(), amount);
            
            return ResponseDTO.success(wallet, "Withdrawal successful");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(400, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            logger.error("Withdraw error. Request: {}", request);
            logger.error("Error processing withdrawal: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to process withdrawal");
        }
    }

    /**
     * Get wallet transaction history
     * GET /api/wallet/transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<List<WalletTransaction>>> getTransactions(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            List<WalletTransaction> transactions = walletService.getTransactionHistory(user.getId());
            
            return ResponseDTO.success(transactions, "Transactions retrieved successfully");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving transactions: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve transactions");
        }
    }

    /**
     * Get wallet statistics
     * GET /api/wallet/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getStatistics(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("balance", walletService.getBalance(user.getId()));
            stats.put("totalDeposit", walletService.getTotalDeposit(user.getId()));
            stats.put("totalWithdrawal", walletService.getTotalWithdrawal(user.getId()));
            stats.put("totalPayment", walletService.getTotalPayment(user.getId()));
            
            return ResponseDTO.success(stats, "Statistics retrieved successfully");
            
        } catch (UserNotFoundException e) {
            return ResponseDTO.error(404, "NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseDTO.error(500, "INTERNAL_SERVER_ERROR", "Failed to retrieve statistics");
        }
    }
}
