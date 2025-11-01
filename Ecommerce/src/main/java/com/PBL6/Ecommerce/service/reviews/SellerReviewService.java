package com.PBL6.Ecommerce.service.reviews;

import com.PBL6.Ecommerce.domain.ProductReview;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.dto.ProductReviewDTO;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.ForbiddenException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Extended ProductReviewService with Seller Response capabilities
 * 
 * Additional features:
 * - Seller can reply to reviews
 * - Shop can view all reviews for their products
 */
@Service
@Transactional
public class SellerReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Seller response to a review
     * 
     * Only shop owner can reply
     * One response per review
     */
    public ProductReviewDTO addSellerResponse(
            Long reviewId,
            String response,
            String username) {

        // 1. Get review
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review không tồn tại"));

        // 2. Check if already has response
        if (review.getSellerResponse() != null && !review.getSellerResponse().isEmpty()) {
            throw new BadRequestException("Review này đã có response từ shop");
        }

        // 3. Get product's shop
        Long shopId = review.getProduct().getShop().getId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop không tồn tại"));

        // 4. Check if user is shop owner
        if (!shop.getOwner().getUsername().equals(username)) {
            throw new ForbiddenException("Bạn không phải chủ shop này");
        }

        // 5. Validate response length
        if (response == null || response.trim().isEmpty()) {
            throw new BadRequestException("Response không được để trống");
        }

        if (response.length() > 1000) {
            throw new BadRequestException("Response không được vượt quá 1000 ký tự");
        }

        // 6. Add response
        review.setSellerResponse(response);
        review.setSellerResponseDate(LocalDateTime.now());

        // 7. Save
        reviewRepository.save(review);

        return mapToDTO(review);
    }

    /**
     * Get all reviews for a shop's products
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getShopReviews(
            Long shopId,
            int page,
            int size) {

        // 1. Validate shop exists
        shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop không tồn tại"));

        // 2. Query reviews
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductReview> reviews = reviewRepository.findByProductShopId(shopId, pageable);

        // 3. Map to DTO
        return reviews.map(this::mapToDTO);
    }

    /**
     * Get shop review statistics
     */
    @Transactional(readOnly = true)
    public ShopReviewStats getShopReviewStats(Long shopId) {

        // 1. Validate shop exists
        shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop không tồn tại"));

        // 2. Count total reviews
        long totalReviews = reviewRepository.countByProductShopId(shopId);

        // 3. Get average rating
        Double avgRating = reviewRepository.getAverageRatingByProductShopId(shopId);

        // 4. Count reviews by rating
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = reviewRepository.countByProductShopIdAndRating(shopId, i);
            ratingCounts.put(i, count);
        }

        // 5. Build stats
        ShopReviewStats stats = new ShopReviewStats();
        stats.setShopId(shopId);
        stats.setTotalReviews(totalReviews);
        stats.setAverageRating(avgRating != null ? avgRating : 0.0);
        stats.setRatingCounts(ratingCounts);

        return stats;
    }

    /**
     * Helper: Convert ProductReview entity to ProductReviewDTO
     */
    private ProductReviewDTO mapToDTO(ProductReview review) {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());

        // Deserialize images from JSON
        try {
            if (review.getImages() != null) {
                dto.setImages(objectMapper.readValue(
                        review.getImages(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                ));
            } else {
                dto.setImages(new ArrayList<>());
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            dto.setImages(new ArrayList<>());
        }

        dto.setVerifiedPurchase(review.getVerifiedPurchase());

        // Map user info
        if (review.getUser() != null) {
            ProductReviewDTO.UserSimpleDTO userDto = new ProductReviewDTO.UserSimpleDTO();
            userDto.setId(review.getUser().getId());
            userDto.setUsername(review.getUser().getUsername());
            userDto.setFullName(review.getUser().getFullName());
            userDto.setAvatarUrl(review.getUser().getAvatarUrl());
            dto.setUser(userDto);
        }

        dto.setSellerResponse(review.getSellerResponse());
        dto.setSellerResponseDate(review.getSellerResponseDate());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }

    /**
     * DTO for shop review statistics
     */
    public static class ShopReviewStats {
        private Long shopId;
        private long totalReviews;
        private Double averageRating;
        private Map<Integer, Long> ratingCounts; // Rating 1-5 counts

        // Constructors
        public ShopReviewStats() {}

        public ShopReviewStats(Long shopId, long totalReviews, Double averageRating, Map<Integer, Long> ratingCounts) {
            this.shopId = shopId;
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.ratingCounts = ratingCounts;
        }

        // Getters & Setters
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }

        public long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Map<Integer, Long> getRatingCounts() { return ratingCounts; }
        public void setRatingCounts(Map<Integer, Long> ratingCounts) { this.ratingCounts = ratingCounts; }
    }
}
