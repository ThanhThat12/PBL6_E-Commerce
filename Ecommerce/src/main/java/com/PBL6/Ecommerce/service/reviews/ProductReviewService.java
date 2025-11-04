package com.PBL6.Ecommerce.service.reviews;

import java.util.List;

import org.springframework.data.domain.Page;

import com.PBL6.Ecommerce.dto.review.ProductReviewDTO;
import com.PBL6.Ecommerce.dto.review.request.CreateReviewRequest;
import com.PBL6.Ecommerce.dto.review.request.UpdateReviewRequest;

/**
 * Service interface for Product Reviews
 * 
 * Defines business logic for:
 * - Get product reviews (with pagination and filtering)
 * - Create review (with validation: purchase, order completed, one review per product)
 * - Update review (with 7-day time limit)
 * - Delete review
 * - Get my reviews
 * 
 * Validation rules:
 * - User must own the order
 * - Order must be COMPLETED
 * - Order must contain the product
 * - One review per user per product (unique constraint)
 * - Can only update/delete within 7 days
 */
public interface ProductReviewService {

    /**
     * Get all reviews for a product with pagination and optional filtering
     * 
     * @param productId Product ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param rating Optional: filter by rating (1-5)
     * @param sortBy Sort order: "newest", "oldest", "highest", "lowest"
     * @return Page of ProductReviewDTO
     */
    Page<ProductReviewDTO> getProductReviews(
        Long productId,
        int page,
        int size,
        Integer rating,
        String sortBy
    );

    /**
     * Create a new review for a product
     * 
     * Validation:
     * - User must own the order
     * - Order status must be COMPLETED
     * - Order must contain the product
     * - User cannot have reviewed this product before
     * 
     * @param productId Product ID
     * @param request CreateReviewRequest with rating, comment, images
     * @param username Current user username
     * @return Created ProductReviewDTO
     * @throws NotFoundException if product or order not found
     * @throws ForbiddenException if order doesn't belong to user
     * @throws BadRequestException if order not completed, product not in order, or already reviewed
     */
    ProductReviewDTO createReview(
        Long productId,
        CreateReviewRequest request,
        String username
    );

    /**
     * Update an existing review
     * 
     * Validation:
     * - User must own the review
     * - Can only update within 7 days of creation
     * 
     * @param reviewId Review ID
     * @param request UpdateReviewRequest with new rating, comment, images
     * @param username Current user username
     * @return Updated ProductReviewDTO
     * @throws NotFoundException if review not found
     * @throws ForbiddenException if user doesn't own the review
     * @throws BadRequestException if 7-day limit exceeded
     */
    ProductReviewDTO updateReview(
        Long reviewId,
        UpdateReviewRequest request,
        String username
    );

    /**
     * Delete a review
     * 
     * Validation:
     * - User must own the review
     * 
     * @param reviewId Review ID
     * @param username Current user username
     * @throws NotFoundException if review not found
     * @throws ForbiddenException if user doesn't own the review
     */
    void deleteReview(Long reviewId, String username);

    /**
     * Get reviews written by the current user
     * 
     * @param username Current user username
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of ProductReviewDTO
     */
    Page<ProductReviewDTO> getMyReviews(String username, int page, int size);

    /**
     * Remove specific images from a review (Shopee-style)
     * 
     * Validation:
     * - User must own the review
     * - Can only update within 7 days of creation
     * 
     * @param reviewId Review ID
     * @param imageUrls List of image URLs to remove
     * @param username Current user username
     * @return Updated ProductReviewDTO
     * @throws NotFoundException if review not found
     * @throws ForbiddenException if user doesn't own the review
     * @throws BadRequestException if 7-day limit exceeded or image not found
     */
    ProductReviewDTO removeReviewImages(Long reviewId, List<String> imageUrls, String username);
}
