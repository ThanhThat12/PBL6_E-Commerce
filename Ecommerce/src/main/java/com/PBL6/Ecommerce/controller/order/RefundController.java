package com.PBL6.Ecommerce.controller.order;

import com.PBL6.Ecommerce.domain.entity.order.Refund;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.order.RefundDTO;
import com.PBL6.Ecommerce.domain.dto.order.RefundRequestDTO;
import com.PBL6.Ecommerce.service.RefundService;
import com.PBL6.Ecommerce.service.OrderService;
import com.PBL6.Ecommerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;


import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Refunds", description = "Refund request and processing")
@RestController
@RequestMapping("/api/refund")
public class RefundController {
    private final RefundService refundService;
    private final OrderService orderService;
    private final UserService userService;

    public RefundController(RefundService refundService, OrderService orderService, UserService userService) {
        this.refundService = refundService;
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * Bước 1: Buyer gửi yêu cầu hoàn tiền (có lý do + ảnh)
     */
    @PostMapping("/request/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> requestRefund(
            @PathVariable Long orderId, 
            @RequestBody RefundRequestDTO dto, 
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        Order order = orderService.getOrderByIdAndUser(orderId, userId);
        
        // Convert Long amount to BigDecimal
        BigDecimal amount = dto.getAmount() != null ? BigDecimal.valueOf(dto.getAmount()) : order.getTotalAmount();
        
        Refund refund = refundService.createRefundRequest(
            order, 
            amount, 
            dto.getDescription(),
            dto.getImageUrl() // Ảnh bằng chứng từ khách
        );
        
        // Convert to DTO to avoid circular reference
        RefundDTO refundDTO = refundService.convertToDTO(refund);
        
        return ResponseDTO.success(refundDTO, "Yêu cầu hoàn tiền đã được gửi");
    }

    /**
     * Buyer gửi yêu cầu hoàn tiền cho một sản phẩm cụ thể (orderItemId)
     * POST /api/refund/create
     * Body: { orderItemId, reason, description, quantity, imageUrls[], requestedAmount }
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<Refund>> createItemRefund(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            
            // Extract data from request
            Long orderItemId = Long.valueOf(request.get("orderItemId").toString());
            String reason = (String) request.get("reason");
            String description = (String) request.get("description");
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            @SuppressWarnings("unchecked")
            java.util.List<String> imageUrls = (java.util.List<String>) request.get("imageUrls");
            Double requestedAmountDouble = Double.valueOf(request.get("requestedAmount").toString());
            BigDecimal requestedAmount = BigDecimal.valueOf(requestedAmountDouble);
            
            // Convert imageUrls list to JSON string
            String imageUrlsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(imageUrls);
            
            // Create refund request via service
            Refund refund = refundService.createRefundRequestByItem(
                orderItemId,
                userId,
                reason,
                description,
                quantity,
                imageUrlsJson,
                requestedAmount
            );
            
            return ResponseDTO.success(refund, "Yêu cầu hoàn tiền đã được gửi. Vui lòng chờ shop xét duyệt.");
            
        } catch (Exception e) {
            return ResponseDTO.error(400, "CREATE_REFUND_FAILED", 
                "Không thể tạo yêu cầu hoàn tiền: " + e.getMessage());
        }
    }

    /**
     * Bước 2: Seller duyệt/từ chối yêu cầu hoàn tiền
     * approve=true → APPROVED_WAITING_RETURN (luôn yêu cầu trả hàng)
     * approve=false → REJECTED
     */
    @PostMapping("/review/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> reviewRefund(
            @PathVariable Long refundId, 
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectReason) {
        
        if (approve) {
            // Luôn yêu cầu trả hàng
            Refund refund = refundService.approveRefund(refundId, true);
            RefundDTO dto = refundService.convertToDTO(refund);
            return ResponseDTO.success(dto, "Đã chấp nhận yêu cầu - Chờ khách trả hàng");
        } else {
            Refund refund = refundService.rejectRefund(refundId, rejectReason);
            RefundDTO dto = refundService.convertToDTO(refund);
            return ResponseDTO.success(dto, "Đã từ chối hoàn tiền");
        }
    }
    
    /**
     * Bước 3: Seller xác nhận đã nhận hàng trả về → Tự động hoàn tiền
     * Có thể gọi từ status APPROVED_WAITING_RETURN hoặc RETURNING
     */
    @PostMapping("/confirm-receipt/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> confirmReceipt(
            @PathVariable Long refundId) {
        
        Refund refund = refundService.confirmReceiptAndRefund(refundId);
        RefundDTO dto = refundService.convertToDTO(refund);
        return ResponseDTO.success(dto, "Đã xác nhận nhận hàng - Hoàn tiền thành công");
    }

    /**
     * Bước 3: Khách đánh dấu đã gửi hàng về → RETURNING
     */
    @PostMapping("/{refundId}/mark-returning")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<Refund>> markAsReturning(@PathVariable Long refundId) {
        Refund refund = refundService.markAsReturning(refundId);
        return ResponseDTO.success(refund, "Đã đánh dấu đang trả hàng");
    }

    /**
     * Bước 4: Shop xác nhận đã nhận hàng trả về và kiểm tra
     * isAccepted=true → APPROVED_REFUNDING → COMPLETED
     * isAccepted=false → REJECTED
     */
    @PostMapping("/{refundId}/confirm-return")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Refund>> confirmReturn(
            @PathVariable Long refundId,
            @RequestBody Map<String, Object> body) {
        
        boolean isAccepted = (boolean) body.getOrDefault("isAccepted", false);
        String checkNote = (String) body.getOrDefault("checkNote", "");
        
        Refund refund = refundService.confirmReturnReceived(refundId, isAccepted, checkNote);
        
        String message = isAccepted 
            ? "Đã xác nhận hàng trả về - Hoàn tiền thành công" 
            : "Hàng trả về không đạt - Từ chối hoàn tiền";
        
        return ResponseDTO.success(refund, message);
    }
    
    /**
     * Lấy danh sách refund requests (cho seller)
     */
    @GetMapping("/requests")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<java.util.List<RefundDTO>>> getRefundRequests(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        User seller = userService.resolveCurrentUser(authentication);
        
        if (seller.getShop() == null) {
            return ResponseDTO.error(400, "NO_SHOP", "Seller does not have a shop");
        }
        
        java.util.List<Refund> refunds = refundService.getRefundsByShop(seller.getShop().getId());
        java.util.List<RefundDTO> refundDTOs = refunds.stream()
            .map(refund -> refundService.convertToDTO(refund))
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseDTO.success(refundDTOs, "Success");
    }
    
    /**
     * Lấy danh sách refund requests của buyer (cho buyer xem trạng thái)
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<java.util.List<RefundDTO>>> getMyRefundRequests(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = userService.extractUserIdFromJwt(jwt);
        
        java.util.List<Refund> refunds = refundService.getRefundsByUserId(userId);
        java.util.List<RefundDTO> refundDTOs = refunds.stream()
            .map(refund -> refundService.convertToDTO(refund))
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseDTO.success(refundDTOs, "Success");
    }
    
    /**
     * Lấy chi tiết refund theo ID
     */
    @GetMapping("/{refundId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> getRefundById(
            @PathVariable Long refundId,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = userService.extractUserIdFromJwt(jwt);
            
            Refund refund = refundService.getRefundById(refundId);
            
            // Check authorization: buyer can see their own, seller can see their shop's
            User user = userService.resolveCurrentUser(authentication);
            boolean isAuthorized = false;
            
            if (refund.getOrder() != null) {
                // Check if buyer owns this refund
                if (refund.getOrder().getUser() != null 
                    && refund.getOrder().getUser().getId().equals(userId)) {
                    isAuthorized = true;
                }
                // Check if seller owns the shop
                if (user.getShop() != null && refund.getOrder().getShop() != null 
                    && refund.getOrder().getShop().getId().equals(user.getShop().getId())) {
                    isAuthorized = true;
                }
            }
            
            if (!isAuthorized) {
                return ResponseDTO.error(403, "FORBIDDEN", "Bạn không có quyền xem yêu cầu hoàn tiền này");
            }
            
            RefundDTO dto = refundService.convertToDTO(refund);
            return ResponseDTO.success(dto, "Success");
            
        } catch (Exception e) {
            return ResponseDTO.error(404, "NOT_FOUND", "Không tìm thấy yêu cầu hoàn tiền");
        }
    }
}