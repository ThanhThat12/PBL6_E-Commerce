package com.PBL6.Ecommerce.controller.reviews;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.dto.ProductReviewDTO;
import com.PBL6.Ecommerce.dto.request.CreateReviewRequest;
import com.PBL6.Ecommerce.dto.request.UpdateReviewRequest;
import com.PBL6.Ecommerce.service.reviews.ProductReviewService;

import jakarta.validation.Valid;

/**
 * REST Controller for Product Reviews
 * 
 * Endpoints:
 * - GET /api/products/{productId}/reviews - Get all reviews for a product
 * - POST /api/products/{productId}/reviews - Create a review (BUYER only)
 * - PUT /api/reviews/{reviewId} - Update a review (BUYER only)
 * - DELETE /api/reviews/{reviewId} - Delete a review (BUYER only)
 * - GET /api/reviews/my - Get my reviews (BUYER only)
 * 
 * All endpoints with @PreAuthorize("hasRole('BUYER')") require authentication and BUYER role
 */
@RestController
@RequestMapping("/api")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * GET /api/products/{productId}/reviews
     * 
     * Get all reviews for a product with pagination and filtering
     * 
     * @param productId Product ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param rating Optional: filter by rating (1-5)
     * @param sortBy Sort order (default: "newest")
     *               Options: "newest", "oldest", "highest", "lowest"
     * @return Page of reviews
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "newest") String sortBy
    ) {
        Page<ProductReviewDTO> reviews = productReviewService.getProductReviews(
            productId, page, size, rating, sortBy
        );
        return ResponseEntity.ok(reviews);
    }

    /**
     * POST /api/products/{productId}/reviews
     * 
     * Create a new review for a product
     * 
     * Request body:
     * {
     *   "orderId": 123,
     *   "rating": 5,
     *   "comment": "Great product!",
     *   "images": ["https://image1.jpg"]
     * }
     * 
     * Validation:
     * - User must own the order
     * - Order must be COMPLETED
     * - Order must contain the product
     * - One review per product per user
     * 
     * @param productId Product ID
     * @param request CreateReviewRequest (validated with @Valid)
     * @param authentication Current user authentication
     * @return Created ProductReviewDTO with 201 status
     */
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProductReviewDTO> createReview(
            @PathVariable Long productId,
            @RequestBody @Valid CreateReviewRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("username");  // ✅ Lấy từ claim "username" thay vì subject
        ProductReviewDTO review = productReviewService.createReview(productId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * PUT /api/reviews/{reviewId}
     * 
     * Update an existing review (within 7 days)
     * 
     * Request body:
     * {
     *   "rating": 4,
     *   "comment": "Updated: After 1 week, still good",
     *   "images": ["https://new-image.jpg"]
     * }
     * 
     * Validation:
     * - User must own the review
     * - Can only update within 7 days
     * 
     * @param reviewId Review ID
     * @param request UpdateReviewRequest
     * @param authentication Current user authentication
     * @return Updated ProductReviewDTO
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProductReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("username");  // ✅ Lấy từ claim "username"
        ProductReviewDTO review = productReviewService.updateReview(reviewId, request, username);
        return ResponseEntity.ok(review);
    }

    /**
     * DELETE /api/reviews/{reviewId}
     * 
     * Delete a review
     * 
     * Validation:
     * - User must own the review
     * 
     * @param reviewId Review ID
     * @param authentication Current user authentication
     * @return 204 No Content
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("username");  // ✅ Lấy từ claim "username"
        productReviewService.deleteReview(reviewId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/reviews/my
     * 
     * Get all reviews written by the current user
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param authentication Current user authentication
     * @return Page of user's reviews
     */
    @GetMapping("/reviews/my")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Page<ProductReviewDTO>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("username");  // ✅ Lấy từ claim "username"
        Page<ProductReviewDTO> reviews = productReviewService.getMyReviews(username, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * DELETE /api/reviews/{reviewId}/images
     * 
     * Remove specific images from a review (Shopee-style)
     * 
     * Request body:
     * {
     *   "imageUrls": ["https://image1.jpg", "https://image2.jpg"]
     * }
     * 
     * Validation:
     * - User must own the review
     * - Can only update within 7 days
     * 
     * @param reviewId Review ID
     * @param imageUrls List of image URLs to remove
     * @param jwt Current user JWT
     * @return Updated ProductReviewDTO
     */
    @DeleteMapping("/reviews/{reviewId}/images")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProductReviewDTO> removeReviewImages(
            @PathVariable Long reviewId,
            @RequestBody java.util.Map<String, java.util.List<String>> body,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = jwt.getClaim("username");
        java.util.List<String> imageUrls = body.get("imageUrls");
        
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new com.PBL6.Ecommerce.exception.BadRequestException("Danh sách imageUrls không được rỗng");
        }
        
        ProductReviewDTO review = productReviewService.removeReviewImages(reviewId, imageUrls, username);
        return ResponseEntity.ok(review);
    }
}
