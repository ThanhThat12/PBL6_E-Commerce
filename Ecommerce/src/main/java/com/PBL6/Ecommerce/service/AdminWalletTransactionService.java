package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.admin.AdminWalletTransactionDTO;
import com.PBL6.Ecommerce.domain.dto.admin.RecentInvoiceDTO;
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
import java.util.List;
import java.util.stream.Collectors;

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
     * Get admin wallet transactions with pagination (simple version)
     * @param page Page number (0-based)
     * @param size Items per page
     * @return Paginated transactions
     */
    public Page<AdminWalletTransactionDTO> getAdminTransactions(int page, int size) {
        
        logger.info("Getting all wallet transactions - page: {}, size: {}", page, size);
        
        try {
            // Build pageable - Sort by ID descending (newest first)
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            
            // Get ALL transactions (không filter theo wallet)
            Page<WalletTransaction> transactions = walletTransactionRepository
                .findAllTransactions(pageable);
                
            logger.info("Found {} transactions", transactions.getTotalElements());
            
            // Convert to DTO
            Page<AdminWalletTransactionDTO> dtos = transactions.map(this::convertToDTO);
            
            logger.info("Successfully retrieved {} transactions (page {}/{})", 
                       dtos.getNumberOfElements(), page + 1, dtos.getTotalPages());
            
            return dtos;
            
        } catch (Exception e) {
            logger.error("Error getting all transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get all transactions: " + e.getMessage());
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
     * Search admin wallet transactions
     * @param keyword Search keyword (id, description, orderId, date)
     * @param page Page number (0-based)
     * @param size Items per page
     * @return Paginated search results
     */
    public Page<AdminWalletTransactionDTO> searchAdminTransactions(String keyword, int page, int size) {
        logger.info("Searching all wallet transactions - keyword: {}, page: {}, size: {}", keyword, page, size);
        
        try {
            // Build pageable - Sort by ID descending
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            
            // Search ALL transactions (không filter theo wallet)
            Page<WalletTransaction> transactions = walletTransactionRepository
                .searchAllTransactions(keyword, pageable);
                
            logger.info("Found {} transactions matching keyword: {}", transactions.getTotalElements(), keyword);
            
            // Convert to DTO
            Page<AdminWalletTransactionDTO> dtos = transactions.map(this::convertToDTO);
            
            logger.info("Successfully searched {} transactions (page {}/{})", 
                       dtos.getNumberOfElements(), page + 1, dtos.getTotalPages());
            
            return dtos;
            
        } catch (Exception e) {
            logger.error("Error searching all transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search all transactions: " + e.getMessage());
        }
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
    
    /**
     * Get recent invoices (ORDER_PAYMENT transactions)
     * @param limit Number of recent invoices to return (default: 5)
     * @return List of RecentInvoiceDTO
     */
    public List<RecentInvoiceDTO> getRecentInvoices(int limit) {
        logger.info("Getting recent invoices - limit: {}", limit);
        
        try {
            // Get admin user and wallet
            User admin = userRepository.findByRole(Role.ADMIN)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new UserNotFoundException("Admin user not found"));
            Wallet adminWallet = walletService.getOrCreateWallet(admin);
            
            // Get recent ORDER_PAYMENT transactions
            Pageable pageable = PageRequest.of(0, limit, Sort.by("id").descending());
            Page<WalletTransaction> transactions = walletTransactionRepository
                .findByWalletIdAndType(
                    adminWallet.getId(), 
                    WalletTransaction.TransactionType.ORDER_PAYMENT,
                    pageable
                );
            
            // Convert to DTO
            List<RecentInvoiceDTO> invoices = transactions.getContent().stream()
                .map(this::convertToInvoiceDTO)
                .collect(Collectors.toList());
            
            logger.info("✅ Retrieved {} recent invoices", invoices.size());
            
            return invoices;
            
        } catch (Exception e) {
            logger.error("❌ Error getting recent invoices: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get recent invoices: " + e.getMessage());
        }
    }
    
    /**
     * Convert WalletTransaction to RecentInvoiceDTO
     */
    private RecentInvoiceDTO convertToInvoiceDTO(WalletTransaction transaction) {
        String name = "Unknown";
        String avatar = null;
        
        // For ORDER_PAYMENT, show buyer information (who paid)
        if (transaction.getRelatedOrder() != null) {
            var order = transaction.getRelatedOrder();
            
            // Get buyer name and avatar
            if (order.getUser() != null) {
                name = order.getUser().getUsername();
                avatar = order.getUser().getAvatarUrl();
            }
        }
        
        return new RecentInvoiceDTO(
            transaction.getId(),
            name,
            avatar,
            transaction.getAmount(),
            transaction.getCreatedAt(),
            transaction.getRelatedOrder() != null ? transaction.getRelatedOrder().getId() : null
        );
    }
}
