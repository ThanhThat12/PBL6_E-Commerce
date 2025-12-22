package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.dto.payment.PaymentResponseDTO;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import com.PBL6.Ecommerce.domain.entity.payment.WalletTransaction;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.WalletRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;

/**
 * Service for managing user wallets
 */
@Service
@Transactional
public class WalletService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository,
                        WalletTransactionRepository walletTransactionRepository,
                        UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get or create wallet for user
     */
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> {
                    logger.info("Creating new wallet for user ID: {}", user.getId());
                    Wallet wallet = new Wallet(user);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Get wallet by user ID
     * Auto-create wallet if not exists
     */
    public Wallet getByUserId(Long userId) {
        logger.info("💰 [WalletService] Finding wallet for user ID: {}", userId);
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        
        if (walletOpt.isPresent()) {
            Wallet wallet = walletOpt.get();
            logger.info("💰 [WalletService] Wallet found in DB - Wallet ID: {}, User ID: {}, Balance: {}", 
                       wallet.getId(), wallet.getUser().getId(), wallet.getBalance());
            return wallet;
        } else {
            logger.warn("⚠️ [WalletService] Wallet not found for user ID: {}, creating new wallet with balance = 0", userId);
            // Auto-create wallet if not exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            Wallet newWallet = new Wallet();
            newWallet.setUser(user);
            newWallet.setBalance(BigDecimal.ZERO);
            Wallet saved = walletRepository.save(newWallet);
            logger.info("💰 [WalletService] New wallet created - Wallet ID: {}, User ID: {}, Balance: 0", 
                       saved.getId(), saved.getUser().getId());
            return saved;
        }
    }

    /**
     * Check if user has wallet
     */
    public boolean hasWallet(Long userId) {
        return walletRepository.existsByUserId(userId);
    }

    /**
     * Get wallet balance
     */
    public BigDecimal getBalance(Long userId) {
        logger.info("💰 [WalletService] Getting balance for user ID: {}", userId);
        Wallet wallet = getByUserId(userId);
        logger.info("💰 [WalletService] Wallet found - ID: {}, Balance: {}", wallet.getId(), wallet.getBalance());
        return wallet.getBalance();
    }

    /**
     * Deposit money to wallet
     */
    public Wallet deposit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        
        logger.info("Depositing {} to wallet ID: {}", amount, wallet.getId());
        
        // Update wallet balance
        wallet.deposit(amount);
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = new WalletTransaction(
            wallet,
            WalletTransaction.TransactionType.DEPOSIT,
            amount,
            description != null ? description : "Nạp tiền vào ví"
        );
        walletTransactionRepository.save(transaction);
        
        logger.info("Deposit successful. New balance: {}", wallet.getBalance());
        
        return wallet;
    }

    /**
     * Withdraw money from wallet
     */
    public Wallet withdraw(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        
        logger.info("Withdrawing {} from wallet ID: {}", amount, wallet.getId());
        
        // Update wallet balance (will throw exception if insufficient)
        wallet.withdraw(amount);
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = new WalletTransaction(
            wallet,
            WalletTransaction.TransactionType.WITHDRAWAL,
            amount,
            description != null ? description : "Rút tiền từ ví"
        );
        walletTransactionRepository.save(transaction);
        
        logger.info("Withdrawal successful. New balance: {}", wallet.getBalance());
        
        return wallet;
    }

    /**
     * Process refund to wallet
     */
    public Wallet refund(Long userId, BigDecimal amount, Order order, String description) {
        Wallet wallet = getByUserId(userId);
        
        logger.info("Refunding {} to wallet ID: {} for order: {}", amount, wallet.getId(), order.getId());
        
        // Update wallet balance
        wallet.deposit(amount);
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = new WalletTransaction(
            wallet,
            WalletTransaction.TransactionType.REFUND,
            amount,
            description != null ? description : "Hoàn tiền đơn hàng #" + order.getId()
        );
        transaction.setRelatedOrder(order);
        walletTransactionRepository.save(transaction);
        
        logger.info("Refund successful. New balance: {}", wallet.getBalance());
        
        return wallet;
    }

    /**
     * Process order payment from wallet
     */
    public Wallet processOrderPayment(Long userId, BigDecimal amount, Order order) {
        Wallet wallet = getByUserId(userId);
        
        logger.info("Processing order payment {} from wallet ID: {} for order: {}", 
                   amount, wallet.getId(), order.getId());
        
        // Check if wallet has enough balance
        if (!wallet.hasEnoughBalance(amount)) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }
        
        // Update wallet balance
        wallet.withdraw(amount);
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        WalletTransaction transaction = new WalletTransaction(
            wallet,
            WalletTransaction.TransactionType.ORDER_PAYMENT,
            amount,
            "Thanh toán đơn hàng #" + order.getId()
        );
        transaction.setRelatedOrder(order);
        walletTransactionRepository.save(transaction);
        
        logger.info("Order payment successful. New balance: {}", wallet.getBalance());
        
        return wallet;
    }

    /**
     * Pay for order using SportyPay wallet
     * This method handles the complete wallet payment flow:
     * 1. Withdraw from buyer's wallet
     * 2. Deposit to admin wallet
     * 3. Update order status
     * 4. Create transaction records
     */
    public Map<String, Object> payOrderWithWallet(Long userId, Long orderId, BigDecimal amount) {
        try {
            logger.info("💰 [START] payOrderWithWallet - userId: {}, orderId: {}, amount: {}", 
                       userId, orderId, amount);
            
            // 1. Get buyer wallet
            Wallet buyerWallet = getByUserId(userId);
            BigDecimal buyerOldBalance = buyerWallet.getBalance();
            logger.info("📊 Buyer wallet ID: {}, Current balance: {}", buyerWallet.getId(), buyerOldBalance);
            
            // 2. Check if buyer has enough balance
            if (!buyerWallet.hasEnoughBalance(amount)) {
                logger.error("❌ Insufficient balance. Required: {}, Available: {}", amount, buyerOldBalance);
                throw new IllegalArgumentException("Số dư ví không đủ. Cần: " + amount + " VNĐ, Có: " + buyerOldBalance + " VNĐ");
            }
            
            // 3. Withdraw from buyer wallet
            logger.info("💸 Withdrawing {} from buyer wallet...", amount);
            buyerWallet.withdraw(amount);
            buyerWallet = walletRepository.saveAndFlush(buyerWallet);
            BigDecimal buyerNewBalance = buyerWallet.getBalance();
            logger.info("✅ Buyer withdrawal successful. New balance: {} (old: {}, withdrawn: {})", 
                       buyerNewBalance, buyerOldBalance, amount);
            
            // 4. Create buyer transaction record
            logger.info("📝 Creating buyer transaction record...");
            WalletTransaction buyerTransaction = new WalletTransaction(
                buyerWallet,
                WalletTransaction.TransactionType.ORDER_PAYMENT,
                amount,
                "Thanh toán đơn hàng #" + orderId + " qua SportyPay"
            );
            // Note: Will set relatedOrder later when we have the Order entity
            WalletTransaction savedBuyerTx = walletTransactionRepository.save(buyerTransaction);
            logger.info("✅ Buyer transaction saved with ID: {}", savedBuyerTx.getId());
            
            // 5. Prepare result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderId", orderId);
            result.put("amount", amount);
            result.put("buyerWalletId", buyerWallet.getId());
            result.put("buyerOldBalance", buyerOldBalance);
            result.put("buyerNewBalance", buyerNewBalance);
            result.put("transactionId", savedBuyerTx.getId());
            result.put("message", "Thanh toán thành công qua ví SportyPay");
            
            logger.info("✅ [SUCCESS] payOrderWithWallet completed. Buyer new balance: {}", buyerNewBalance);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ [ERROR] Validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] payOrderWithWallet failed for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Thanh toán ví thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Get wallet transaction history
     */
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        return walletTransactionRepository.findByUserId(userId);
    }

    /**
     * Get wallet transactions by type
     */
    public List<WalletTransaction> getTransactionsByType(Long userId, WalletTransaction.TransactionType type) {
        Wallet wallet = getByUserId(userId);
        return walletTransactionRepository.findByWalletAndType(wallet, type);
    }

    /**
     * Calculate total deposit amount
     */
    public BigDecimal getTotalDeposit(Long userId) {
        Wallet wallet = getByUserId(userId);
        return walletTransactionRepository.calculateTotalDeposit(wallet.getId());
    }

    /**
     * Calculate total withdrawal amount
     */
    public BigDecimal getTotalWithdrawal(Long userId) {
        Wallet wallet = getByUserId(userId);
        return walletTransactionRepository.calculateTotalWithdrawal(wallet.getId());
    }

    /**
     * Calculate total payment amount
     */
    public BigDecimal getTotalPayment(Long userId) {
        Wallet wallet = getByUserId(userId);
        return walletTransactionRepository.calculateTotalPayment(wallet.getId());
    }

    /**
     * Get admin user (first user with ADMIN role)
     */
    private User getAdminUser() {
        return userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Admin user not found"));
    }

    /**
     * Deposit to admin wallet from buyer payment (MoMo, SportyPay, COD)
     */
    public Wallet depositToAdminWallet(BigDecimal amount, Order order, String paymentMethod) {
        try {
            logger.info("🔵 [START] depositToAdminWallet - amount: {}, order: {}, method: {}", 
                       amount, order.getId(), paymentMethod);
            
            // 1. Get admin user
            User adminUser = getAdminUser();
            logger.info("✅ Found admin user: {} (ID: {})", adminUser.getUsername(), adminUser.getId());
            
            // 2. Get or create admin wallet
            Wallet adminWallet = getOrCreateWallet(adminUser);
            BigDecimal oldBalance = adminWallet.getBalance();
            logger.info("📊 Admin wallet ID: {}, Current balance: {}", adminWallet.getId(), oldBalance);
            
            // 3. Update balance
            logger.info("💸 Calling deposit({}) on wallet...", amount);
            adminWallet.deposit(amount);
            BigDecimal newBalance = adminWallet.getBalance();
            logger.info("💰 Balance after deposit call: {} (old: {}, diff: {})", 
                       newBalance, oldBalance, newBalance.subtract(oldBalance));
            
            // 4. Save wallet with flush to ensure DB write
            logger.info("💾 Saving wallet to database...");
            adminWallet = walletRepository.saveAndFlush(adminWallet);
            logger.info("✅ Wallet saved. Balance in entity: {}", adminWallet.getBalance());
            
            // 5. Create transaction record
            logger.info("📝 Creating wallet transaction record...");
            WalletTransaction transaction = new WalletTransaction(
                adminWallet,
                WalletTransaction.TransactionType.ORDER_PAYMENT,
                amount,
                String.format("Nhận thanh toán từ đơn hàng #%d qua %s", order.getId(), paymentMethod)
            );
            transaction.setRelatedOrder(order);
            WalletTransaction savedTx = walletTransactionRepository.save(transaction);
            logger.info("✅ Transaction saved with ID: {}", savedTx.getId());
            
            // 6. Verify by re-fetching from DB
            Wallet verifyWallet = walletRepository.findById(adminWallet.getId()).orElse(null);
            if (verifyWallet != null) {
                logger.info("🔍 VERIFICATION: Wallet balance in DB = {}", verifyWallet.getBalance());
                if (!verifyWallet.getBalance().equals(newBalance)) {
                    logger.error("⚠️ WARNING: Balance mismatch! Entity={}, DB={}", 
                               newBalance, verifyWallet.getBalance());
                }
            } else {
                logger.error("❌ ERROR: Could not verify wallet - not found in DB!");
            }
            
            logger.info("✅ [SUCCESS] depositToAdminWallet completed. Final balance: {}", 
                       adminWallet.getBalance());
            
            return adminWallet;
            
        } catch (Exception e) {
            logger.error("❌ [ERROR] depositToAdminWallet failed for order {}: {}", 
                        order.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Transfer money from admin wallet to seller wallet (overloaded for scheduler)
     * Called automatically by scheduler after 2 minutes
     */
    public void transferFromAdminToSeller(Long sellerId, BigDecimal amount, Order order, String description) {
        User adminUser = getAdminUser();
        Wallet adminWallet = getOrCreateWallet(adminUser);
        
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller not found with ID: " + sellerId));
        Wallet sellerWallet = getOrCreateWallet(seller);
        
        logger.info("💸 Transferring {} from admin wallet to seller {} for order: {}", 
                   amount, sellerId, order.getId());
        
        // Check if admin has enough balance
        if (!adminWallet.hasEnoughBalance(amount)) {
            throw new IllegalArgumentException("Admin wallet has insufficient balance");
        }
        
        // Withdraw from admin wallet
        adminWallet.withdraw(amount);
        walletRepository.save(adminWallet);
        
        // Create admin withdrawal transaction
        WalletTransaction adminTransaction = new WalletTransaction(
            adminWallet,
            WalletTransaction.TransactionType.PAYMENT_TO_SELLER,
            amount,
            String.format("Chuyển tiền cho seller #%d - đơn hàng #%d", sellerId, order.getId())
        );
        adminTransaction.setRelatedOrder(order);
        walletTransactionRepository.save(adminTransaction);
        
        // Deposit to seller wallet
        sellerWallet.deposit(amount);
        walletRepository.save(sellerWallet);
        
        // Create seller deposit transaction
        WalletTransaction sellerTransaction = new WalletTransaction(
            sellerWallet,
            WalletTransaction.TransactionType.PAYMENT_TO_SELLER,
            amount,
            description
        );
        sellerTransaction.setRelatedOrder(order);
        walletTransactionRepository.save(sellerTransaction);
        
        logger.info("✅ Transfer successful. Admin balance: {}, Seller balance: {}", 
                   adminWallet.getBalance(), sellerWallet.getBalance());
    }

    /**
     * Transfer money from admin wallet to seller wallet
     * Called after order is completed and return period expired
     */
    public void transferFromAdminToSeller(Long sellerId, BigDecimal amount, Order order, BigDecimal platformFee) {
        User adminUser = getAdminUser();
        Wallet adminWallet = getOrCreateWallet(adminUser);
        
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller not found with ID: " + sellerId));
        Wallet sellerWallet = getOrCreateWallet(seller);
        
        logger.info("Transferring {} from admin wallet to seller {} for order: {}", 
                   amount, sellerId, order.getId());
        
        // Check if admin has enough balance
        if (!adminWallet.hasEnoughBalance(amount)) {
            throw new IllegalArgumentException("Admin wallet has insufficient balance");
        }
        
        // Withdraw from admin wallet
        adminWallet.withdraw(amount);
        walletRepository.save(adminWallet);
        
        // Create admin withdrawal transaction
        WalletTransaction adminTransaction = new WalletTransaction(
            adminWallet,
            WalletTransaction.TransactionType.PAYMENT_TO_SELLER,
            amount,
            String.format("Chuyển tiền cho seller #%d - đơn hàng #%d", sellerId, order.getId())
        );
        adminTransaction.setRelatedOrder(order);
        walletTransactionRepository.save(adminTransaction);
        
        // Deposit to seller wallet
        sellerWallet.deposit(amount);
        walletRepository.save(sellerWallet);
        
        // Create seller deposit transaction
        WalletTransaction sellerTransaction = new WalletTransaction(
            sellerWallet,
            WalletTransaction.TransactionType.PAYMENT_TO_SELLER,
            amount,
            String.format("Nhận tiền từ đơn hàng #%d (trừ phí nền tảng: %s)", 
                         order.getId(), platformFee.toString())
        );
        sellerTransaction.setRelatedOrder(order);
        walletTransactionRepository.save(sellerTransaction);
        
        logger.info("Transfer successful. Admin balance: {}, Seller balance: {}", 
                   adminWallet.getBalance(), sellerWallet.getBalance());
    }

    /**
     * Get admin wallet balance
     */
    public BigDecimal getAdminBalance() {
        User adminUser = getAdminUser();
        Wallet adminWallet = getOrCreateWallet(adminUser);
        return adminWallet.getBalance();
    }

    /**
     * Create MoMo payment for wallet deposit
     */
    public PaymentResponseDTO createDepositPayment(
            Long userId, BigDecimal amount, String description, String walletOrderId) {
        try {
            logger.info("Creating MoMo payment for wallet deposit: userId={}, amount={}", userId, amount);
            
            // Inject beans
            MoMoPaymentService momoPaymentService = com.PBL6.Ecommerce.config.SpringContext.getBean(MoMoPaymentService.class);
            com.PBL6.Ecommerce.config.MoMoConfig momoConfig = com.PBL6.Ecommerce.config.SpringContext.getBean(com.PBL6.Ecommerce.config.MoMoConfig.class);
            
            String requestId = momoPaymentService.generateRequestId(walletOrderId);
            String walletIpnUrl = momoConfig.getWalletIpnUrl();
            String mobileRedirectUrl = momoConfig.getMobileRedirectUrl();
            
            logger.info("Using wallet IPN URL: {}, mobile redirect: {}", walletIpnUrl, mobileRedirectUrl);
            
            // Use custom redirect and IPN URLs for mobile wallet deposits
            PaymentResponseDTO response = momoPaymentService.createPaymentWithCustomUrls(
                walletOrderId,
                amount,
                description,
                requestId,
                mobileRedirectUrl,  // Mobile deep link
                walletIpnUrl
            );
            
            logger.info("MoMo payment created for wallet deposit: orderId={}, payUrl={}", 
                       walletOrderId, response.getPayUrl());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating deposit payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create deposit payment: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển tiền từ tài khoản quản trị viên cho người bán sau khi đơn hàng hoàn tất.
     * Creates two transactions:
     * 1. PAYMENT_TO_SELLER: Transfers 90% to seller
     * 2. PLATFORM_FEE: Records 10% platform fee (tracking only, no balance change)
     * 
     * @param sellerId The seller's user ID
     * @param totalAmount Total order amount
     * @param order The completed order
     */
    public void transferFromAdminToSeller(Long sellerId, BigDecimal totalAmount, Order order) {
        try {
            logger.info("🔄 [WalletService] Starting transfer from admin to seller - " +
                       "Order #{}, Seller ID: {}, Amount: {}", 
                       order.getId(), sellerId, totalAmount);
            
            // Get admin wallet (user_id = 1)
            Wallet adminWallet = getByUserId(1L);
            logger.info("💰 [Admin Wallet] Current balance: {}", adminWallet.getBalance());
            
            // Get seller wallet
            Wallet sellerWallet = getByUserId(sellerId);
            logger.info("💰 [Seller Wallet] Current balance: {}", sellerWallet.getBalance());
            
            // Calculate amounts (90% to seller, 10% platform fee)
            BigDecimal sellerAmount = totalAmount.multiply(new BigDecimal("0.90"));
            BigDecimal platformFee = totalAmount.multiply(new BigDecimal("0.10"));
            
            logger.info("💵 [Transfer] Seller amount (90%): {}, Platform fee (10%): {}", 
                       sellerAmount, platformFee);
            
            // Check admin wallet has enough balance
            if (adminWallet.getBalance().compareTo(sellerAmount) < 0) {
                throw new RuntimeException(
                    String.format("Admin wallet insufficient balance. Required: %s, Available: %s",
                                sellerAmount, adminWallet.getBalance())
                );
            }
            
            // 1. Create PAYMENT_TO_SELLER transaction (decreases admin balance, increases seller balance)
            // Withdraw from admin wallet
            adminWallet.setBalance(adminWallet.getBalance().subtract(sellerAmount));
            walletRepository.save(adminWallet);
            
            WalletTransaction adminPaymentTransaction = new WalletTransaction();
            adminPaymentTransaction.setWallet(adminWallet);
            adminPaymentTransaction.setAmount(sellerAmount.negate()); // Negative for withdrawal
            adminPaymentTransaction.setType(WalletTransaction.TransactionType.PAYMENT_TO_SELLER);
            adminPaymentTransaction.setDescription(
                String.format("Chuyển tiền cho seller #%d - đơn hàng #%d (đã trừ 10%% phí dịch vụ)", 
                             sellerId, order.getId())
            );  
            adminPaymentTransaction.setRelatedOrder(order);
            walletTransactionRepository.save(adminPaymentTransaction);
            
            logger.info("✅ [Admin Transaction] PAYMENT_TO_SELLER created - Amount: {}, New balance: {}",
                       sellerAmount.negate(), adminWallet.getBalance());
            
            // Deposit to seller wallet
            sellerWallet.setBalance(sellerWallet.getBalance().add(sellerAmount));
            walletRepository.save(sellerWallet);
            
            WalletTransaction sellerDepositTransaction = new WalletTransaction();
            sellerDepositTransaction.setWallet(sellerWallet);
            sellerDepositTransaction.setAmount(sellerAmount); // Positive for deposit
            sellerDepositTransaction.setType(WalletTransaction.TransactionType.PAYMENT_TO_SELLER);
            sellerDepositTransaction.setDescription(
                String.format("Nhận tiền từ đơn hàng #%d (90%% sau khi trừ phí nền tảng)", 
                             order.getId())
            );
            sellerDepositTransaction.setRelatedOrder(order);
            walletTransactionRepository.save(sellerDepositTransaction);
            
            logger.info("✅ [Seller Transaction] PAYMENT_TO_SELLER created - Amount: {}, New balance: {}",
                       sellerAmount, sellerWallet.getBalance());
            
            // 2. Create PLATFORM_FEE transaction (tracking only, no balance change)
            WalletTransaction platformFeeTransaction = new WalletTransaction();
            platformFeeTransaction.setWallet(adminWallet);
            platformFeeTransaction.setAmount(platformFee); // Positive to track fee earned
            platformFeeTransaction.setType(WalletTransaction.TransactionType.PLATFORM_FEE);
            platformFeeTransaction.setDescription(
                String.format("Phí nền tảng từ đơn hàng #%d (10%%)", order.getId())
            );
            platformFeeTransaction.setRelatedOrder(order);
            walletTransactionRepository.save(platformFeeTransaction);
            
            logger.info("✅ [Platform Fee] Transaction created - Amount: {}", platformFee);
            
            logger.info("🎉 [Transfer Complete] Order #{} - Seller received: {}, Platform fee: {}, " +
                       "Admin balance: {}, Seller balance: {}",
                       order.getId(), sellerAmount, platformFee, 
                       adminWallet.getBalance(), sellerWallet.getBalance());
            
        } catch (Exception e) {
            logger.error("❌ [Transfer Failed] Order #{}: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to transfer funds to seller: " + e.getMessage(), e);
        }
    }

    /**
     * Process withdrawal from wallet to MoMo account
     * Note: In production, this would use MoMo's disbursement/transfer API
     * For UAT/sandbox, we simulate the withdrawal
     */
    public Map<String, Object> processWithdrawal(
            Long userId, BigDecimal amount, String momoPhone, String description) {
        try {
            logger.info("Processing withdrawal: userId={}, amount={}, momoPhone={}", 
                       userId, amount, momoPhone);
            
            // First, deduct from wallet
            Wallet wallet = withdraw(userId, amount, description + " - " + momoPhone);
            
            // In production, you would call MoMo disbursement API here
            // For now, we'll create a pending transaction
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "PROCESSING");
            result.put("amount", amount);
            result.put("momoPhone", momoPhone);
            result.put("walletBalance", wallet.getBalance());
            result.put("message", "Withdrawal request submitted. Money will be transferred to MoMo phone: " + momoPhone);
            result.put("estimatedTime", "1-3 business days");
            
            logger.info("Withdrawal processed successfully: userId={}, newBalance={}", 
                       userId, wallet.getBalance());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing withdrawal: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process withdrawal: " + e.getMessage(), e);
        }
    }
}