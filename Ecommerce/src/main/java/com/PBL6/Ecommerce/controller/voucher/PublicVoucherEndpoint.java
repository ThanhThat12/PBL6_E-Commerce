package com.PBL6.Ecommerce.controller.voucher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.VoucherDTO;
import com.PBL6.Ecommerce.service.VoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Public Voucher Endpoints - APIs công khai
 * GET /api/vouchers/platform - Lấy platform vouchers cho homepage
 */
@RestController
@RequestMapping("/api/vouchers")
@Tag(name = "Public Vouchers", description = "Public APIs for vouchers")
public class PublicVoucherEndpoint {
    
    private static final Logger log = LoggerFactory.getLogger(PublicVoucherEndpoint.class);
    
    private final VoucherService voucherService;

    public PublicVoucherEndpoint(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * Lấy voucher do sàn phát hành (platform vouchers) cho homepage
     * GET /api/vouchers/platform?page=0&size=8
     * PUBLIC endpoint - không cần authentication
     */
    @Operation(
        summary = "Get platform vouchers for homepage",
        description = "Lấy danh sách voucher do sàn phát hành (admin) đang hoạt động. " +
                     "Endpoint public, không cần đăng nhập. Trả về vouchers đang ACTIVE, " +
                     "chưa hết hạn, còn lượt sử dụng."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách voucher thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Lỗi khi lấy vouchers"
        )
    })
    @GetMapping("/platform")
    public ResponseEntity<ResponseDTO<Page<VoucherDTO>>> getPlatformVouchers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số voucher mỗi trang", example = "8")
            @RequestParam(defaultValue = "8") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VoucherDTO> vouchers = voucherService.getPlatformVouchers(pageable);
            
            log.info("Found {} platform vouchers for homepage", vouchers.getTotalElements());
            
            return ResponseDTO.success(vouchers, "Lấy danh sách voucher sàn thành công");
        } catch (Exception e) {
            log.error("Error getting platform vouchers", e);
            return ResponseDTO.error(400, "GET_PLATFORM_VOUCHERS_ERROR", e.getMessage());
        }
    }
}
