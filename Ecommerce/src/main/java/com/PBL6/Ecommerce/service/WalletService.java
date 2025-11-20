package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.domain.WalletTransaction;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.WalletRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Auto-create wallet if not exists
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });
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
        Wallet wallet = getByUserId(userId);
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
        return userRepository.findByRole(com.PBL6.Ecommerce.domain.Role.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Admin user not found"));
    }

    /**
     * Deposit to admin wallet from buyer payment (MoMo, SportyPay, COD)
     */
    @Transactional
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
                WalletTransaction.TransactionType.DEPOSIT,
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
}
