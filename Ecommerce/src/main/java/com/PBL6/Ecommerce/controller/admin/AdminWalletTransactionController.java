package com.PBL6.Ecommerce.controller.admin;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminWalletTransactionDTO;
import com.PBL6.Ecommerce.domain.dto.admin.RecentInvoiceDTO;
import com.PBL6.Ecommerce.service.AdminWalletTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/wallet")
public class AdminWalletTransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminWalletTransactionController.class);
    
    @Autowired
    private AdminWalletTransactionService adminWalletTransactionService;
    
    /**
     * Get admin wallet transactions with pagination
     * Similar to customer/seller listing APIs
     * 
     * GET /api/admin/wallet/transactions?page=0&size=15
     * 
     * @param page Page number (default: 0)
     * @param size Items per page (default: 15)
     * @return Paginated transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminWalletTransactionDTO>>> getAdminTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        logger.info("📊 GET /api/admin/wallet/transactions - page: {}, size: {}", page, size);
        
        try {
            Page<AdminWalletTransactionDTO> transactions = adminWalletTransactionService
                .getAdminTransactions(page, size);
            
            logger.info("✅ Successfully retrieved {} transactions (page {}/{})", 
                       transactions.getNumberOfElements(), 
                       page + 1, 
                       transactions.getTotalPages());
            
            return ResponseDTO.success(
                transactions, 
                "Transactions retrieved successfully"
            );
            
        } catch (Exception e) {
            logger.error("❌ Error getting admin transactions: {}", e.getMessage(), e);
            return ResponseDTO.error(
                500,
                "TRANSACTION_ERROR",
                "Failed to get transactions: " + e.getMessage()
            );
        }
    }
    
    /**
     * Search admin wallet transactions
     * 
     * GET /api/admin/wallet/transactions/search?keyword=123&page=0&size=15
     * 
     * @param keyword Search keyword (id, description, orderId, date)
     * @param page Page number (default: 0)
     * @param size Items per page (default: 15)
     * @return Paginated search results
     */
    @GetMapping("/transactions/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminWalletTransactionDTO>>> searchTransactions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        logger.info("🔍 GET /api/admin/wallet/transactions/search - keyword: {}, page: {}, size: {}", 
                   keyword, page, size);
        
        try {
            Page<AdminWalletTransactionDTO> transactions = adminWalletTransactionService
                .searchAdminTransactions(keyword, page, size);
            
            logger.info("✅ Found {} transactions matching '{}' (page {}/{})", 
                       transactions.getNumberOfElements(), 
                       keyword,
                       page + 1, 
                       transactions.getTotalPages());
            
            return ResponseDTO.success(
                transactions, 
                "Search completed successfully"
            );
            
        } catch (Exception e) {
            logger.error("❌ Error searching transactions: {}", e.getMessage(), e);
            return ResponseDTO.error(
                500,
                "SEARCH_ERROR",
                "Failed to search transactions: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get admin wallet balance
     * 
     * @return Admin wallet balance information
     */
    @GetMapping("/balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<java.util.Map<String, Object>>> getAdminBalance() {
        logger.info("💰 GET /api/admin/wallet/balance");
        
        try {
            java.util.Map<String, Object> balance = adminWalletTransactionService.getAdminBalance();
            
            logger.info("✅ Successfully retrieved admin balance: {} VND", balance.get("balance"));
            
            return ResponseDTO.success(
                balance,
                "Admin balance retrieved successfully"
            );
            
        } catch (Exception e) {
            logger.error("❌ Error getting admin balance: {}", e.getMessage(), e);
            return ResponseDTO.error(
                500,
                "BALANCE_ERROR",
                "Failed to get admin balance: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get recent invoices (ORDER_PAYMENT transactions)
     * 
     * GET /api/admin/wallet/recent-invoices?limit=5
     * 
     * @param limit Number of recent invoices (default: 5)
     * @return List of recent invoices with shop/buyer info
     */
    @GetMapping("/recent-invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<RecentInvoiceDTO>>> getRecentInvoices(
            @RequestParam(defaultValue = "5") int limit
    ) {
        logger.info("📋 GET /api/admin/wallet/recent-invoices - limit: {}", limit);
        
        try {
            List<RecentInvoiceDTO> invoices = adminWalletTransactionService
                .getRecentInvoices(limit);
            
            logger.info("✅ Successfully retrieved {} recent invoices", invoices.size());
            
            return ResponseDTO.success(
                invoices,
                "Recent invoices retrieved successfully"
            );
            
        } catch (Exception e) {
            logger.error("❌ Error getting recent invoices: {}", e.getMessage(), e);
            return ResponseDTO.error(
                500,
                "INVOICE_ERROR",
                "Failed to get recent invoices: " + e.getMessage()
            );
        }
    }
}
