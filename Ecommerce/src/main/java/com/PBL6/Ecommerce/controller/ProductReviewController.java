package com.PBL6.Ecommerce.controller;

// imports cleaned: ApiResponse and ProductReview not needed in this controller
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.CreateReviewRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ProductRatingSummaryDTO;
import com.PBL6.Ecommerce.domain.dto.ProductReviewDTO;
import com.PBL6.Ecommerce.domain.dto.ReportReviewRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ReviewEligibilityDTO;
import com.PBL6.Ecommerce.domain.dto.ReviewLikeResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ReviewReportDTO;
import com.PBL6.Ecommerce.domain.dto.SellerReplyRequestDTO;
import com.PBL6.Ecommerce.domain.dto.ShopReviewsGroupedDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateReviewRequestDTO;
import com.PBL6.Ecommerce.service.ProductReviewService;

import jakarta.validation.Valid;

/**
 * REST Controller for Product Reviews
 * 
 * Endpoints:
 * POST /api/products/{productId}/reviews - Create review (BUYER or SELLER who bought from other shops)
 * GET /api/products/{productId}/reviews - Get product reviews (Public)
 * PUT /api/reviews/{reviewId} - Update review (BUYER or SELLER owner)
 * POST /api/reviews/{reviewId}/reply - Seller reply (SELLER only)
 * DELETE /api/reviews/{reviewId} - Delete review (ADMIN or owner)
 * GET /api/products/{productId}/rating-summary - Get rating summary (Public)
 * GET /api/users/{userId}/reviews - Get user reviews (Public)
 * GET /api/my-reviews - Get my reviews (BUYER or SELLER)
 * 
 * LIKE/REPORT Endpoints:
 * POST /api/reviews/{reviewId}/like - Toggle like (BUYER or SELLER)
 * GET /api/reviews/{reviewId}/like - Get like status (Public with optional auth)
 * POST /api/reviews/{reviewId}/report - Report review (BUYER or SELLER)
 * GET /api/admin/reviews/reports - Get reports (ADMIN only)
 * PUT /api/admin/reviews/reports/{reportId} - Update report status (ADMIN only)
 * GET /api/admin/reviews/reports/counts - Get report counts (ADMIN only)
 */
@Tag(name = "Product Reviews", description = "Product review and rating system")
@RestController
@RequestMapping("/api")
public class ProductReviewController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductReviewController.class);
    
    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 1️⃣ Tạo đánh giá sản phẩm từ trang chi tiết sản phẩm
     * POST /api/products/{productId}/reviews
     * ➡️ Buyer hoặc Seller (mua từ shop khác) tạo review cho sản phẩm đã mua.
     * ⚠️ SELLER không được review sản phẩm của chính shop mình.
     * Body: { rating, comment, images }
     */
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProductReviewDTO>> createReviewForProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequestDTO request,
            Authentication authentication) {
        try {
            ProductReviewDTO review = productReviewService.createReviewForProduct(productId, request, authentication);

            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                201, null, "Tạo đánh giá thành công", review
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating review for product {}", productId, e);
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                400, "CREATE_REVIEW_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 🔍 Kiểm tra trạng thái đánh giá của user cho sản phẩm
     * GET /api/products/{productId}/review-eligibility
     * ➡️ Trả về: { canReview, hasReviewed, hasPurchased, daysRemainingToReview, canEditReview, ... }
     * Dùng để hiển thị nút "Đánh giá sản phẩm" hoặc "Xem đánh giá" trên frontend
     */
    @GetMapping("/products/{productId}/review-eligibility")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ReviewEligibilityDTO>> checkReviewEligibility(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            ReviewEligibilityDTO eligibility = productReviewService.checkReviewEligibility(productId, authentication);
            
            ResponseDTO<ReviewEligibilityDTO> response = new ResponseDTO<>(
                200, null, "Kiểm tra trạng thái đánh giá thành công", eligibility
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking review eligibility for product {}", productId, e);
            ResponseDTO<ReviewEligibilityDTO> response = new ResponseDTO<>(
                400, "CHECK_ELIGIBILITY_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 2️⃣ Xem danh sách đánh giá của 1 sản phẩm
     * GET /api/products/{productId}/reviews
     * ➡️ Lấy tất cả review + thông tin người dùng, ảnh, phản hồi shop.
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ResponseDTO<Page<ProductReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "newest") String sortBy) {
        try {
            Page<ProductReviewDTO> reviews = productReviewService.getProductReviews(
                productId, page, size, rating, sortBy
            );
            
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách đánh giá thành công", reviews
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting product reviews", e);
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                400, "GET_REVIEWS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 3️⃣ Cập nhật đánh giá
     * PUT /api/reviews/{reviewId}
     * ➡️ Buyer/Seller chỉnh sửa review của chính mình.
     * Body: { rating, comment, images }
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ProductReviewDTO>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequestDTO request,
            Authentication authentication) {
        try {
            ProductReviewDTO review = productReviewService.updateReview(reviewId, request, authentication);
            
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                200, null, "Cập nhật đánh giá thành công", review
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating review", e);
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                400, "UPDATE_REVIEW_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 4️⃣ Seller phản hồi đánh giá
     * POST /api/reviews/{reviewId}/reply
     * ➡️ Seller (chủ shop) trả lời review của khách.
     */
    @PostMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<ProductReviewDTO>> addSellerReply(
            @PathVariable Long reviewId,
            @Valid @RequestBody SellerReplyRequestDTO request,
            Authentication authentication) {
        try {
            ProductReviewDTO review = productReviewService.addSellerReply(reviewId, request, authentication);
            
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                200, null, "Thêm phản hồi thành công", review
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error adding seller reply", e);
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                400, "ADD_REPLY_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 5️⃣ Xóa đánh giá
     * DELETE /api/reviews/{reviewId}
     * ➡️ Admin hoặc chính người viết review được phép xóa.
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ResponseDTO<String>> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        try {
            productReviewService.deleteReview(reviewId, authentication);
            
            ResponseDTO<String> response = new ResponseDTO<>(
                200, null, "Xóa đánh giá thành công", "Review đã được xóa"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting review", e);
            ResponseDTO<String> response = new ResponseDTO<>(
                400, "DELETE_REVIEW_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 6️⃣ Xem thống kê & điểm trung bình sản phẩm
     * GET /api/products/{productId}/rating-summary
     * ➡️ Trả về: { average, totalReviews, starCounts }
     */
    @GetMapping("/products/{productId}/rating-summary")
    public ResponseEntity<ResponseDTO<ProductRatingSummaryDTO>> getProductRatingSummary(
            @PathVariable Long productId) {
        try {
            ProductRatingSummaryDTO summary = productReviewService.getProductRatingSummary(productId);
            
            ResponseDTO<ProductRatingSummaryDTO> response = new ResponseDTO<>(
                200, null, "Lấy thống kê đánh giá thành công", summary
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting rating summary", e);
            ResponseDTO<ProductRatingSummaryDTO> response = new ResponseDTO<>(
                400, "GET_SUMMARY_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 7️⃣ Lấy danh sách review của 1 user
     * GET /api/users/{userId}/reviews
     * ➡️ Xem tất cả review mà user đó đã viết.
     */
    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<ResponseDTO<Page<ProductReviewDTO>>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ProductReviewDTO> reviews = productReviewService.getUserReviews(userId, page, size);
            
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách đánh giá của người dùng thành công", reviews
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting user reviews", e);
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                400, "GET_USER_REVIEWS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 🔒 Lấy danh sách review của mình
     * GET /api/my-reviews
     * ➡️ Xem tất cả review mà mình đã viết (Buyer hoặc Seller).
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductReviewDTO>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Page<ProductReviewDTO> reviews = productReviewService.getMyReviews(page, size, authentication);
            
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách đánh giá của bạn thành công", reviews
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting my reviews", e);
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                400, "GET_MY_REVIEWS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

        /**
     * 🏪 Lấy tất cả đánh giá của shop (cho seller quản lý)
     * GET /api/shops/{shopId}/reviews
     * ➡️ Seller lấy tất cả review của shop mình, có thể lọc theo đã phản hồi/chưa và nhóm rating.
     * Query params: replied (true/false), ratingGroup ("1-2", "3-4", "5"), page, size
     */
    @GetMapping("/shops/{shopId}/reviews")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductReviewDTO>>> getShopReviews(
            @PathVariable Long shopId,
            @RequestParam(required = false) Boolean replied,
            @RequestParam(required = false) String ratingGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Page<ProductReviewDTO> reviews = productReviewService.getShopReviews(
                shopId, replied, ratingGroup, page, size, authentication
            );
            
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách đánh giá của shop thành công", reviews
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting shop reviews", e);
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                400, "GET_SHOP_REVIEWS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

     /**
     * 🏪 Lấy tất cả đánh giá của shop của mình (seller)
     * GET /api/my-shop/reviews/all
     * ➡️ Seller lấy tất cả review của shop mình, phân loại thành 2 nhóm: đã phản hồi và chưa phản hồi.
     * Không cần truyền shopId, server tự lấy từ JWT.
     */
    @GetMapping("/my-shop/reviews/all")
@PreAuthorize("hasRole('SELLER')")
public ResponseEntity<ResponseDTO<ShopReviewsGroupedDTO>> getAllShopReviewsGrouped(
        Authentication authentication) {
    try {
        // Sửa: gọi đúng method getMyShopReviewsGrouped(authentication)
        ShopReviewsGroupedDTO groupedReviews = productReviewService.getMyShopReviewsGrouped(authentication);
        
        ResponseDTO<ShopReviewsGroupedDTO> response = new ResponseDTO<>(
            200, null, "Lấy tất cả đánh giá của shop thành công", groupedReviews
        );
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        log.error("Error getting my shop reviews grouped", e);
        ResponseDTO<ShopReviewsGroupedDTO> response = new ResponseDTO<>(
            400, "GET_MY_SHOP_REVIEWS_ERROR", e.getMessage(), null
        );
        return ResponseEntity.badRequest().body(response);
    }
}

    /**
     * 🏪 Lấy đánh giá chưa phản hồi của shop
     * GET /api/shops/{shopId}/reviews/unreplied
     * ➡️ Seller lấy chỉ những review chưa có phản hồi của shop mình.
     * Query params: page, size
     */
    @GetMapping("/shops/{shopId}/reviews/unreplied")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ResponseDTO<Page<ProductReviewDTO>>> getUnrepliedShopReviews(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Page<ProductReviewDTO> reviews = productReviewService.getUnrepliedShopReviews(shopId, page, size, authentication);
            
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                200, null, "Lấy đánh giá chưa phản hồi thành công", reviews
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting unreplied shop reviews", e);
            ResponseDTO<Page<ProductReviewDTO>> response = new ResponseDTO<>(
                400, "GET_UNREPLIED_REVIEWS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== LIKE ENDPOINTS ====================

    /**
     * 👍 Toggle like/unlike một review
     * POST /api/reviews/{reviewId}/like
     * ➡️ Buyer hoặc Seller like/unlike một đánh giá.
     */
    @PostMapping("/reviews/{reviewId}/like")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ReviewLikeResponseDTO>> toggleReviewLike(
            @PathVariable Long reviewId,
            Authentication authentication) {
        try {
            ReviewLikeResponseDTO likeResponse = productReviewService.toggleReviewLike(reviewId, authentication);
            
            String message = likeResponse.getLiked() ? "Đã like đánh giá" : "Đã bỏ like đánh giá";
            ResponseDTO<ReviewLikeResponseDTO> response = new ResponseDTO<>(
                200, null, message, likeResponse
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error toggling review like", e);
            ResponseDTO<ReviewLikeResponseDTO> response = new ResponseDTO<>(
                400, "TOGGLE_LIKE_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 👍 Get like status của một review
     * GET /api/reviews/{reviewId}/like
     * ➡️ Lấy trạng thái like của review (count + isLikedByCurrentUser).
     */
    @GetMapping("/reviews/{reviewId}/like")
    public ResponseEntity<ResponseDTO<ReviewLikeResponseDTO>> getReviewLikeStatus(
            @PathVariable Long reviewId,
            Authentication authentication) {
        try {
            ReviewLikeResponseDTO likeStatus = productReviewService.getReviewLikeStatus(reviewId, authentication);
            
            ResponseDTO<ReviewLikeResponseDTO> response = new ResponseDTO<>(
                200, null, "Lấy trạng thái like thành công", likeStatus
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting review like status", e);
            ResponseDTO<ReviewLikeResponseDTO> response = new ResponseDTO<>(
                400, "GET_LIKE_STATUS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== REPORT ENDPOINTS ====================

    /**
     * 🚩 Report một review
     * POST /api/reviews/{reviewId}/report
     * ➡️ Buyer hoặc Seller báo cáo một đánh giá vi phạm.
     * Body: { reportType: "SPAM|INAPPROPRIATE|FAKE|OFFENSIVE|OTHER", reason: "..." }
     */
    @PostMapping("/reviews/{reviewId}/report")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<ResponseDTO<ReviewReportDTO>> reportReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReportReviewRequestDTO request,
            Authentication authentication) {
        try {
            ReviewReportDTO report = productReviewService.reportReview(reviewId, request, authentication);
            
            ResponseDTO<ReviewReportDTO> response = new ResponseDTO<>(
                201, null, "Báo cáo đánh giá thành công", report
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error reporting review", e);
            ResponseDTO<ReviewReportDTO> response = new ResponseDTO<>(
                400, "REPORT_REVIEW_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== ADMIN REPORT ENDPOINTS ====================

    /**
     * 📋 Get pending reports (Admin only)
     * GET /api/admin/reviews/reports
     * ➡️ Admin lấy danh sách các báo cáo cần xử lý.
     * Query params: status (optional), page, size
     */
    @GetMapping("/admin/reviews/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<ReviewReportDTO>>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ReviewReportDTO> reports;
            if (status != null && !status.isEmpty()) {
                reports = productReviewService.getReportsByStatus(status, page, size);
            } else {
                reports = productReviewService.getPendingReports(page, size);
            }
            
            ResponseDTO<Page<ReviewReportDTO>> response = new ResponseDTO<>(
                200, null, "Lấy danh sách báo cáo thành công", reports
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting reports", e);
            ResponseDTO<Page<ReviewReportDTO>> response = new ResponseDTO<>(
                400, "GET_REPORTS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * ✏️ Update report status (Admin only)
     * PUT /api/admin/reviews/reports/{reportId}
     * ➡️ Admin cập nhật trạng thái xử lý báo cáo.
     * Query params: status (PENDING|REVIEWED|RESOLVED|DISMISSED), adminNote (optional)
     */
    @PutMapping("/admin/reviews/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<ReviewReportDTO>> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam String status,
            @RequestParam(required = false) String adminNote,
            Authentication authentication) {
        try {
            ReviewReportDTO report = productReviewService.updateReportStatus(reportId, status, adminNote, authentication);
            
            ResponseDTO<ReviewReportDTO> response = new ResponseDTO<>(
                200, null, "Cập nhật báo cáo thành công", report
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating report status", e);
            ResponseDTO<ReviewReportDTO> response = new ResponseDTO<>(
                400, "UPDATE_REPORT_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 📊 Get report counts by status (Admin only)
     * GET /api/admin/reviews/reports/counts
     * ➡️ Admin lấy thống kê số lượng báo cáo theo trạng thái.
     */
    @GetMapping("/admin/reviews/reports/counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Long>>> getReportCounts() {
        try {
            Map<String, Long> counts = productReviewService.getReportCounts();
            
            ResponseDTO<Map<String, Long>> response = new ResponseDTO<>(
                200, null, "Lấy thống kê báo cáo thành công", counts
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting report counts", e);
            ResponseDTO<Map<String, Long>> response = new ResponseDTO<>(
                400, "GET_REPORT_COUNTS_ERROR", e.getMessage(), null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

}