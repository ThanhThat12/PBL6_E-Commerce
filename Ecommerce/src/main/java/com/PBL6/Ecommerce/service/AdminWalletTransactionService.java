package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.admin.AdminWalletTransactionDTO;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.domain.entity.payment.Wallet;
import com.PBL6.Ecommerce.domain.entity.payment.WalletTransaction;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
public class AdminWalletTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminWalletTransactionService.class);
    
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get admin wallet transactions with filter and pagination
     * @param page Page number (0-based)
     * @param size Items per page
     * @param period Filter period: TODAY, WEEKLY, MONTHLY
     * @param type Transaction type filter (optional)
     * @return Paginated transactions
     */
    public Page<AdminWalletTransactionDTO> getAdminTransactions(
            int page, 
            int size, 
            String period, 
            String type) {
        
        logger.info("Getting admin transactions - page: {}, size: {}, period: {}, type: {}", 
                   page, size, period, type);
        
        try {
            // Get admin user and wallet
            User admin = userRepository.findByRole(Role.ADMIN)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new UserNotFoundException("Admin user not found"));
            Wallet adminWallet = walletService.getOrCreateWallet(admin);
            
            // Calculate date range based on period
            LocalDateTime startDate = calculateStartDate(period);
            LocalDateTime endDate = LocalDateTime.now();
            
            logger.info("Date range: {} to {}", startDate, endDate);
            
            // Build pageable - Sort by ID descending (newest first)
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            
            Page<WalletTransaction> transactions;
            
            // Apply filters
            if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
                // Filter by type AND date range
                WalletTransaction.TransactionType transactionType = 
                    WalletTransaction.TransactionType.valueOf(type.toUpperCase());
                
                transactions = walletTransactionRepository
                    .findByWalletIdAndTypeAndCreatedAtBetween(
                        adminWallet.getId(), 
                        transactionType,
                        startDate,
                        endDate,
                        pageable
                    );
                
                logger.info("Filtered by type: {} - Found {} transactions", 
                           type, transactions.getTotalElements());
            } else {
                // Filter by date range only
                transactions = walletTransactionRepository
                    .findByWalletIdAndCreatedAtBetween(
                        adminWallet.getId(), 
                        startDate,
                        endDate,
                        pageable
                    );
                
                logger.info("Filtered by date only - Found {} transactions", 
                           transactions.getTotalElements());
            }
            
            // Convert to DTO
            Page<AdminWalletTransactionDTO> dtos = transactions.map(this::convertToDTO);
            
            logger.info("Successfully retrieved {} transactions (page {}/{})", 
                       dtos.getNumberOfElements(), page + 1, dtos.getTotalPages());
            
            return dtos;
            
        } catch (Exception e) {
            logger.error("Error getting admin transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get admin transactions: " + e.getMessage());
        }
    }
    
    /**
     * Calculate start date based on period filter
     */
    private LocalDateTime calculateStartDate(String period) {
        if (period == null || period.isEmpty()) {
            // Default: last 30 days
            return LocalDateTime.now().minusDays(30);
        }
        
        switch (period.toUpperCase()) {
            case "TODAY":
                return LocalDateTime.now().with(LocalTime.MIN);
            case "WEEKLY":
                return LocalDateTime.now().minusWeeks(1);
            case "MONTHLY":
                return LocalDateTime.now().minusMonths(1);
            default:
                return LocalDateTime.now().minusDays(30);
        }
    }
    
    /**
     * Convert WalletTransaction entity to DTO
     */
    private AdminWalletTransactionDTO convertToDTO(WalletTransaction transaction) {
        return new AdminWalletTransactionDTO(
            transaction.getId(),
            transaction.getType().name(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getRelatedOrder() != null ? transaction.getRelatedOrder().getId() : null,
            transaction.getCreatedAt()
        );
    }
    
    /**
     * Get admin wallet balance
     * @return Map with balance information
     */
    public java.util.Map<String, Object> getAdminBalance() {
        logger.info("Getting admin wallet balance");
        
        try {
            // Get admin user (ID = 1)
            User admin = userRepository.findById(1L)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found"));
            
            // Get admin wallet
            Wallet adminWallet = walletService.getOrCreateWallet(admin);
            
            // Prepare result
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("balance", adminWallet.getBalance());
            result.put("currency", "VND");
            result.put("userId", admin.getId());
            result.put("username", admin.getUsername());
            result.put("lastUpdated", LocalDateTime.now());
            
            logger.info("✅ Admin balance: {} VND", adminWallet.getBalance());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Error getting admin balance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get admin balance: " + e.getMessage());
        }
    }
}
