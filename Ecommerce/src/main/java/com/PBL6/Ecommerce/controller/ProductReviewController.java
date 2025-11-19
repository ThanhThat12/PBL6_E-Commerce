package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Product Reviews
 * 
 * Endpoints:
 * POST /api/reviews - Create review (BUYER only)
 * GET /api/products/{productId}/reviews - Get product reviews (Public)
 * PUT /api/reviews/{reviewId} - Update review (BUYER only)
 * POST /api/reviews/{reviewId}/reply - Seller reply (SELLER only)
 * DELETE /api/reviews/{reviewId} - Delete review (ADMIN or owner)
 * GET /api/products/{productId}/rating-summary - Get rating summary (Public)
 * GET /api/users/{userId}/reviews - Get user reviews (Public)
 * GET /api/my-reviews - Get my reviews (BUYER only)
 */
@RestController
@RequestMapping("/api")
public class ProductReviewController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductReviewController.class);
    
    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 1️⃣ Tạo đánh giá sản phẩm
     * POST /api/reviews
     * ➡️ Buyer tạo review cho sản phẩm đã mua.
     * Body: { productId, orderId, rating, comment, images }
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ResponseDTO<ProductReviewDTO>> createReview(
            @Valid @RequestBody CreateReviewRequestDTO request,
            Authentication authentication) {
        try {
            ProductReviewDTO review = productReviewService.createReview(request, authentication);
            
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                201, null, "Tạo đánh giá thành công", review
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating review", e);
            ResponseDTO<ProductReviewDTO> response = new ResponseDTO<>(
                400, "CREATE_REVIEW_ERROR", e.getMessage(), null
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
     * ➡️ Buyer chỉnh sửa review của chính mình.
     * Body: { rating, comment, images }
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
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
     * ➡️ Xem tất cả review mà mình đã viết.
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('BUYER')")
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
}