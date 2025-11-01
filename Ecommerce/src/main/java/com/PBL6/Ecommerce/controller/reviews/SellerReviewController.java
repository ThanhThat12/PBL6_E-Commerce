package com.PBL6.Ecommerce.controller.reviews;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.dto.ProductReviewDTO;
import com.PBL6.Ecommerce.service.reviews.SellerReviewService;

/**
 * REST Controller for Seller Review Operations
 * 
 * Handles:
 * - Seller responses to customer reviews
 * - Shop review statistics and listing
 */
@RestController
@RequestMapping("/api/seller/reviews")
public class SellerReviewController {

    @Autowired
    private SellerReviewService sellerReviewService;

    /**
     * POST /api/seller/reviews/{reviewId}/response
     * Seller adds response to a review
     * 
     * @param reviewId Review ID
     * @param request Response object with "response" field
     * @param authentication Current seller
     * @return Updated review with seller response
     */
    @PostMapping("/{reviewId}/response")
    public ResponseEntity<ProductReviewDTO> addResponse(
            @PathVariable Long reviewId,
            @RequestBody SellerResponseRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaim("username");  // ✅ Lấy từ claim "username"
        ProductReviewDTO dto = sellerReviewService.addSellerResponse(
                reviewId,
                request.getResponse(),
                username
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/seller/reviews?page=0&size=10
     * Get all reviews for seller's products
     * 
     * @param shopId Shop ID (from context)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of reviews for seller's shop
     */
    @GetMapping
    public ResponseEntity<Page<ProductReviewDTO>> getShopReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @AuthenticationPrincipal Jwt jwt) {

        // In practice, shopId should be retrieved from authenticated seller
        // For now, assume seller info contains shop ID
        if (shopId == null) {
            // This should be fetched from seller's profile
            // For demo: throw exception
            throw new IllegalArgumentException("Shop ID is required");
        }

        Page<ProductReviewDTO> reviews = sellerReviewService.getShopReviews(shopId, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/seller/reviews/stats
     * Get review statistics for seller's shop
     * 
     * @param shopId Shop ID (from context)
     * @return Shop review statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<SellerReviewService.ShopReviewStats> getReviewStats(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId) {

        if (shopId == null) {
            throw new IllegalArgumentException("Shop ID is required");
        }

        SellerReviewService.ShopReviewStats stats = sellerReviewService.getShopReviewStats(shopId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Request body for seller response
     */
    public static class SellerResponseRequest {
        private String response;

        public SellerResponseRequest() {}
        public SellerResponseRequest(String response) {
            this.response = response;
        }

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }
}
