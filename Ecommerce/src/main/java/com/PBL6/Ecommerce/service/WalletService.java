package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Wallet;
import com.PBL6.Ecommerce.domain.WalletTransaction;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.WalletRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;
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

    public WalletService(WalletRepository walletRepository,
                        WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
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
     */
    public Wallet getByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Wallet not found for user ID: " + userId));
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
}
