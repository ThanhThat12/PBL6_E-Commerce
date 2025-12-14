package com.PBL6.Ecommerce.controller.admin;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminWalletTransactionDTO;
import com.PBL6.Ecommerce.service.AdminWalletTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/wallet")
public class AdminWalletTransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminWalletTransactionController.class);
    
    @Autowired
    private AdminWalletTransactionService adminWalletTransactionService;
    
    /**
     * Get admin wallet transactions with filter and pagination
     * 
     * @param page Page number (default: 0)
     * @param size Items per page (default: 15)
     * @param period Filter period: TODAY, WEEKLY, MONTHLY (optional)
     * @param type Transaction type filter (optional)
     * @return Paginated transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminWalletTransactionDTO>>> getAdminTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String type
    ) {
        logger.info("📊 GET /api/admin/wallet/transactions - page: {}, size: {}, period: {}, type: {}", 
                   page, size, period, type);
        
        try {
            Page<AdminWalletTransactionDTO> transactions = adminWalletTransactionService
                .getAdminTransactions(page, size, period, type);
            
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
}
