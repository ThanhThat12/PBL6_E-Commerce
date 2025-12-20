package com.PBL6.Ecommerce.controller.order;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.order.RefundDTO;
import com.PBL6.Ecommerce.domain.dto.order.RefundRequestDTO;
import com.PBL6.Ecommerce.domain.entity.order.Order;
import com.PBL6.Ecommerce.domain.entity.order.Refund;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.service.OrderService;
import com.PBL6.Ecommerce.service.RefundService;
import com.PBL6.Ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Refund Management", description = "API quản lý yêu cầu hoàn tiền và trả hàng. Hỗ trợ đầy đủ quy trình: Buyer gửi yêu cầu (có ảnh minh chứng) → Seller duyệt → Buyer trả hàng → Seller xác nhận → Hoàn tiền tự động.")
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
     * Bước 1: Buyer gửi yêu cầu hoàn tiền (có lý do + ảnh minh chứng)
     */
    @Operation(
        summary = "Tạo yêu cầu hoàn tiền",
        description = "Buyer gửi yêu cầu hoàn tiền cho đơn hàng đã hoàn thành (trong vòng 15 ngày). "
            + "Hỗ trợ upload nhiều ảnh minh chứng (sản phẩm lỗi, không đúng mô tả, v.v.). "
            + "\n\n**Điều kiện:**\n"
            + "- Đơn hàng phải ở trạng thái COMPLETED\n"
            + "- Trong vòng 15 ngày kể từ ngày hoàn thành\n"
            + "- Chưa có yêu cầu hoàn tiền trước đó\n\n"
            + "**Lưu ý:** imageUrl có thể là JSON array chứa nhiều URLs ảnh: `[\"url1\", \"url2\", \"url3\"]`",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Yêu cầu hoàn tiền đã được gửi thành công",
            content = @Content(schema = @Schema(implementation = Refund.class))),
        @ApiResponse(responseCode = "400", description = "Đơn hàng không hợp lệ hoặc đã quá thời hạn hoàn tiền (15 ngày)",
            content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ BUYER)"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PostMapping("/request/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<Refund>> requestRefund(
            @Parameter(description = "ID của đơn hàng cần hoàn tiền", required = true, example = "12345")
            @PathVariable Long orderId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin yêu cầu hoàn tiền",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RefundRequestDTO.class),
                    examples = {
                        @ExampleObject(
                            name = "Single Image",
                            summary = "Yêu cầu hoàn tiền với 1 ảnh",
                            value = "{\"amount\": 500000, \"description\": \"Sản phẩm bị lỗi, không hoạt động\", \"imageUrl\": \"https://cloudinary.com/image1.jpg\"}"
                        ),
                        @ExampleObject(
                            name = "Multiple Images",
                            summary = "Yêu cầu hoàn tiền với nhiều ảnh",
                            value = "{\"amount\": 500000, \"description\": \"Sản phẩm không đúng mô tả, bị trầy xước\", \"imageUrl\": \"[\\\"https://cloudinary.com/image1.jpg\\\", \\\"https://cloudinary.com/image2.jpg\\\", \\\"https://cloudinary.com/image3.jpg\\\"]\"}"
                        )
                    }
                )
            )
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
        
        return ResponseDTO.success(refund, "Yêu cầu hoàn tiền đã được gửi");
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
    @Operation(
        summary = "Duyệt hoặc từ chối yêu cầu hoàn tiền",
        description = "Seller xem xét yêu cầu hoàn tiền từ buyer (có ảnh minh chứng).\n\n"
            + "**Nếu approve=true:**\n"
            + "- Chấp nhận yêu cầu\n"
            + "- Status → APPROVED (luôn yêu cầu khách trả hàng)\n"
            + "- Buyer cần gửi hàng về\n\n"
            + "**Nếu approve=false:**\n"
            + "- Từ chối yêu cầu\n"
            + "- Status → REJECTED\n"
            + "- Bắt buộc nhập lý do từ chối (rejectReason)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đã xử lý yêu cầu hoàn tiền thành công",
            content = @Content(schema = @Schema(implementation = RefundDTO.class))),
        @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ hoặc thiếu lý do từ chối"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ SELLER)"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu hoàn tiền")
    })
    @PostMapping("/review/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> reviewRefund(
            @Parameter(description = "ID của yêu cầu hoàn tiền", required = true, example = "101")
            @PathVariable Long refundId,
            
            @Parameter(description = "true = Chấp nhận, false = Từ chối", required = true, example = "true")
            @RequestParam boolean approve,
            
            @Parameter(description = "Lý do từ chối (bắt buộc nếu approve=false)", example = "Sản phẩm không có vấn đề gì")
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
     * Có thể gọi từ status APPROVED hoặc RETURNING
     */
    @Operation(
        summary = "Xác nhận đã nhận hàng trả về và hoàn tiền tự động",
        description = "Seller xác nhận đã nhận được hàng trả về từ buyer. \n\n"
            + "**Quy trình tự động:**\n"
            + "1. Seller nhận hàng và kiểm tra\n"
            + "2. Xác nhận qua API này\n"
            + "3. Hệ thống tự động hoàn tiền vào ví buyer\n"
            + "4. Status → COMPLETED\n\n"
            + "**Phương thức hoàn tiền:**\n"
            + "- MoMo → Hoàn về tài khoản MoMo\n"
            + "- SportyPay → Hoàn vào ví SportyPay\n"
            + "- COD → Hoàn vào ví SportyPay",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đã xác nhận và hoàn tiền thành công",
            content = @Content(schema = @Schema(implementation = RefundDTO.class))),
        @ApiResponse(responseCode = "400", description = "Trạng thái yêu cầu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ SELLER)"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy yêu cầu hoàn tiền")
    })
    @PostMapping("/confirm-receipt/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<RefundDTO>> confirmReceipt(
            @Parameter(description = "ID của yêu cầu hoàn tiền", required = true, example = "101")
            @PathVariable Long refundId) {
        
        Refund refund = refundService.confirmReceiptAndRefund(refundId);
        RefundDTO dto = refundService.convertToDTO(refund);
        return ResponseDTO.success(dto, "Đã xác nhận nhận hàng - Hoàn tiền thành công");
    }

    /**
     * Bước 3: Khách đánh dấu đã gửi hàng về → RETURNING
     */
    @Operation(
        summary = "Buyer đánh dấu đã gửi hàng trả về",
        description = "Buyer thông báo đã gửi hàng trả về cho seller sau khi yêu cầu được chấp nhận.\n\n"
            + "**Điều kiện:** Yêu cầu phải ở trạng thái APPROVED\n\n"
            + "**Sau khi đánh dấu:** Status không thay đổi, chờ seller xác nhận nhận hàng",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đã đánh dấu đang trả hàng thành công"),
        @ApiResponse(responseCode = "400", description = "Yêu cầu chưa được duyệt hoặc không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền (chỉ BUYER)")
    })
    @PostMapping("/{refundId}/mark-returning")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<Refund>> markAsReturning(
            @Parameter(description = "ID của yêu cầu hoàn tiền", required = true)
            @PathVariable Long refundId) {
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
    @Operation(
        summary = "Lấy danh sách yêu cầu hoàn tiền của shop",
        description = "Seller xem tất cả yêu cầu hoàn tiền cho các đơn hàng của shop.\n\n"
            + "**Bao gồm:**\n"
            + "- Yêu cầu chờ duyệt (REQUESTED)\n"
            + "- Đã chấp nhận (APPROVED)\n"
            + "- Đã từ chối (REJECTED)\n"
            + "- Đã hoàn thành (COMPLETED)\n\n"
            + "**Mỗi yêu cầu có:** thông tin đơn hàng, buyer, số tiền, lý do, ảnh minh chứng, trạng thái",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "400", description = "Seller chưa có shop"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền (chỉ SELLER)")
    })
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
    @Operation(
        summary = "Lấy danh sách yêu cầu hoàn tiền của buyer",
        description = "Buyer xem tất cả yêu cầu hoàn tiền đã gửi cho các đơn hàng của mình.\n\n"
            + "**Thông tin bao gồm:**\n"
            + "- Thông tin đơn hàng\n"
            + "- Số tiền yêu cầu hoàn\n"
            + "- Lý do và ảnh minh chứng đã gửi\n"
            + "- Trạng thái xử lý (REQUESTED/APPROVED/REJECTED/COMPLETED)\n"
            + "- Lý do từ chối (nếu có)\n"
            + "- Thời gian tạo và cập nhật",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền (chỉ BUYER)")
    })
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
}
