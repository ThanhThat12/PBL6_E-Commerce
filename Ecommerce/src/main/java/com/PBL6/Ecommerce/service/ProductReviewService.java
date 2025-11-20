package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.domain.dto.*;

import com.PBL6.Ecommerce.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import com.PBL6.Ecommerce.domain.dto.ShopReviewsGroupedDTO;
import java.util.List;
import java.util.stream.Collectors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReviewService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductReviewService.class);
    
    @Autowired
    private ProductReviewRepository productReviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. Tạo đánh giá sản phẩm (POST /api/reviews)
     */
    @PreAuthorize("hasRole('BUYER')")
    public ProductReviewDTO createReview(CreateReviewRequestDTO request, Authentication authentication) {
        // Deprecated/compatibility stub
        throw new UnsupportedOperationException("Endpoint POST /api/reviews (body with productId/orderId) is deprecated. Use POST /api/products/{productId}/reviews with authenticated buyer instead.");
    }

    /**
     * 2. Xem danh sách đánh giá của 1 sản phẩm (GET /api/products/{productId}/reviews)
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getProductReviews(Long productId, int page, int size, 
                                                   Integer rating, String sortBy) {
        try {
            // 1. Validate product exists
            if (!productRepository.existsById(productId)) {
                throw new RuntimeException("Không tìm thấy sản phẩm");
            }
            
            // 2. Build sort
            Sort sort = switch(sortBy) {
                case "oldest" -> Sort.by("createdAt").ascending();
                case "highest" -> Sort.by("rating").descending();
                case "lowest" -> Sort.by("rating").ascending();
                default -> Sort.by("createdAt").descending(); // newest
            };
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 3. Query reviews
            Page<ProductReview> reviews;
            if (rating != null) {
                reviews = productReviewRepository.findByProductIdAndRating(productId, rating, pageable);
            } else {
                reviews = productReviewRepository.findByProductId(productId, pageable);
            }
            
            // 4. Convert to DTOs
            return reviews.map(this::convertToDTO);
            
        } catch (Exception e) {
            log.error("Error getting product reviews", e);
            throw new RuntimeException("Lỗi khi lấy danh sách đánh giá: " + e.getMessage());
        }
    }

    /**
     * 3. Cập nhật đánh giá (PUT /api/reviews/{reviewId})
     */
    @PreAuthorize("hasRole('BUYER')")
    public ProductReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request, Authentication authentication) {
        try {
            // 1. Find review
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // 2. Check ownership
            User user = getCurrentUser(authentication);
            if (!review.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
            }
            
            // 3. Check time limit (7 days)
            LocalDateTime createdAt = review.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();
            
            if (Duration.between(createdAt, now).toDays() > 7) {
                throw new RuntimeException("Chỉ có thể sửa đánh giá trong vòng 7 ngày");
            }
            
            // 4. Update
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setImages(convertImagesToJson(request.getImages()));
            review.setUpdatedAt(LocalDateTime.now());
            
            review = productReviewRepository.save(review);
            
            log.info("Updated review {} by user {}", reviewId, user.getUsername());
            
            return convertToDTO(review);
            
        } catch (Exception e) {
            log.error("Error updating review", e);
            throw new RuntimeException("Lỗi khi cập nhật đánh giá: " + e.getMessage());
        }
    }

    /**
     * 4. Seller phản hồi đánh giá (POST /api/reviews/{reviewId}/reply)
     */
    @PreAuthorize("hasRole('SELLER')")
    public ProductReviewDTO addSellerReply(Long reviewId, SellerReplyRequestDTO request, Authentication authentication) {
        try {
            // 1. Find review
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // 2. Check if seller owns the product's shop
            User seller = getCurrentUser(authentication);
            Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Bạn không có shop"));
            
            if (!review.getProduct().getShop().getId().equals(shop.getId())) {
                throw new RuntimeException("Bạn chỉ có thể phản hồi đánh giá của sản phẩm trong shop của mình");
            }
            
            // 3. Add seller response
            review.setSellerResponse(request.getSellerResponse());
            review.setSellerResponseDate(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
            
            review = productReviewRepository.save(review);
            
            log.info("Added seller reply to review {} by seller {}", reviewId, seller.getUsername());
            
            return convertToDTO(review);
            
        } catch (Exception e) {
            log.error("Error adding seller reply", e);
            throw new RuntimeException("Lỗi khi thêm phản hồi: " + e.getMessage());
        }
    }

    /**
     * 5. Xóa đánh giá (DELETE /api/reviews/{reviewId})
     */
    public void deleteReview(Long reviewId, Authentication authentication) {
        try {
            // 1. Find review
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // 2. Check permission (Admin or review owner)
            User user = getCurrentUser(authentication);
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = review.getUser().getId().equals(user.getId());
            
            if (!isAdmin && !isOwner) {
                throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
            }
            
            // 3. Delete review
            productReviewRepository.delete(review);
            
            log.info("Deleted review {} by {}", reviewId, user.getUsername());
            
        } catch (Exception e) {
            log.error("Error deleting review", e);
            throw new RuntimeException("Lỗi khi xóa đánh giá: " + e.getMessage());
        }
    }

    /**
     * 6. Xem thống kê & điểm trung bình sản phẩm (GET /api/products/{productId}/rating-summary)
     */
    @Transactional(readOnly = true)
    public ProductRatingSummaryDTO getProductRatingSummary(Long productId) {
        try {
            // 1. Validate product exists
            if (!productRepository.existsById(productId)) {
                throw new RuntimeException("Không tìm thấy sản phẩm");
            }
            
            // 2. Get average rating
            Double averageRating = productReviewRepository.getAverageRatingByProductId(productId);
            if (averageRating == null) {
                averageRating = 0.0;
            }
            
            // 3. Get total reviews
            Long totalReviews = productReviewRepository.countByProductId(productId);
            
            // 4. Get star counts
            Object[][] starCountsData = productReviewRepository.countByProductIdGroupByRating(productId);
            Map<Integer, Long> starCounts = new HashMap<>();
            
            // Initialize all star levels with 0
            for (int i = 1; i <= 5; i++) {
                starCounts.put(i, 0L);
            }
            
            // Fill actual counts
            for (Object[] row : starCountsData) {
                Integer rating = (Integer) row[0];
                Long count = (Long) row[1];
                starCounts.put(rating, count);
            }
            
            return new ProductRatingSummaryDTO(averageRating, totalReviews, starCounts);
            
        } catch (Exception e) {
            log.error("Error getting product rating summary", e);
            throw new RuntimeException("Lỗi khi lấy thống kê đánh giá: " + e.getMessage());
        }
    }

    /**
     * 7. Lấy danh sách review của 1 user (GET /api/users/{userId}/reviews)
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getUserReviews(Long userId, int page, int size) {
        try {
            // 1. Validate user exists
            if (!userRepository.existsById(userId)) {
                throw new RuntimeException("Không tìm thấy người dùng");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ProductReview> reviews = productReviewRepository.findByUserId(userId, pageable);
            
            return reviews.map(this::convertToDTO);
            
        } catch (Exception e) {
            log.error("Error getting user reviews", e);
            throw new RuntimeException("Lỗi khi lấy danh sách đánh giá của người dùng: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách review của mình (GET /api/my-reviews)
     */
    @PreAuthorize("hasRole('BUYER')")
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getMyReviews(int page, int size, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ProductReview> reviews = productReviewRepository.findByUserId(user.getId(), pageable);
            
            return reviews.map(this::convertToDTO);
            
        } catch (Exception e) {
            log.error("Error getting my reviews", e);
            throw new RuntimeException("Lỗi khi lấy danh sách đánh giá của bạn: " + e.getMessage());
        }
    }

    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    private String convertImagesToJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.error("Error converting images to JSON", e);
            return "[]";
        }
    }

    private List<String> convertImagesFromJson(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty() || imagesJson.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting images from JSON", e);
            return new ArrayList<>();
        }
    }

    private ProductReviewDTO convertToDTO(ProductReview review) {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImages(convertImagesFromJson(review.getImages()));
        dto.setVerifiedPurchase(review.getVerifiedPurchase());
        dto.setSellerResponse(review.getSellerResponse());
        dto.setSellerResponseDate(review.getSellerResponseDate());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        // User info
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getUsername());
            dto.setUserFullName(review.getUser().getFullName());
            dto.setUserAvatarUrl(review.getUser().getAvatarUrl());
        }
        
        // Product info
        if (review.getProduct() != null) {
            dto.setProductId(review.getProduct().getId());
            dto.setProductName(review.getProduct().getName());
        }
        
        // Order info
        if (review.getOrder() != null) {
            dto.setOrderId(review.getOrder().getId());
        }
        
        return dto;
    }

    
        public ProductReviewDTO createReviewForProduct(Long productId, CreateReviewRequestDTO request, Authentication authentication) {
            try {
                // 1. Get current user
                User user = getCurrentUser(authentication);

                // 2. Validate product exists
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                // 3. Check if already reviewed
                if (productReviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
                    throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
                }

                // 4. Find a COMPLETED order of the user that contains this product
                java.util.List<Order> orders = orderRepository.findCompletedOrdersByUserAndProduct(user.getId(), productId);
                if (orders == null || orders.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy đơn hàng hoàn tất chứa sản phẩm này");
                }
                Order order = orders.get(0); // most recent due to ORDER BY in query

                // 5. Create review
                ProductReview review = new ProductReview();
                review.setProduct(product);
                review.setUser(user);
                review.setOrder(order);
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setImages(convertImagesToJson(request.getImages()));
                review.setVerifiedPurchase(true);

                review = productReviewRepository.save(review);

                log.info("Created review {} for product {} by user {} via product-detail endpoint", review.getId(), productId, user.getUsername());

                return convertToDTO(review);
            } catch (Exception e) {
                log.error("Error creating review for product {}", productId, e);
                throw new RuntimeException("Lỗi khi tạo đánh giá: " + e.getMessage());
            }
        }

            /**
     * Lấy tất cả reviews của shop với filters (cho seller)
     */
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getShopReviews(Long shopId, Boolean replied, String ratingGroup, 
                                                int page, int size, Authentication authentication) {
        try {
            // 1. Check if seller owns the shop
            User seller = getCurrentUser(authentication);
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));
            
            if (!shop.getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn chỉ có thể xem đánh giá của shop của mình");
            }
            
            // 2. Get all reviews of the shop (without filters first)
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ProductReview> allReviews = productReviewRepository.findByProductShopId(shopId, pageable);
            
            // 3. Apply filters in memory (since we have small dataset)
            List<ProductReview> filteredReviews = allReviews.getContent().stream()
                .filter(review -> {
                    // Filter by replied
                    if (replied != null) {
                        boolean hasReply = review.getSellerResponse() != null;
                        if (replied && !hasReply) return false;
                        if (!replied && hasReply) return false;
                    }
                    
                    // Filter by rating group
                    if (ratingGroup != null) {
                        int rating = review.getRating();
                        switch (ratingGroup) {
                            case "1-2":
                                if (rating < 1 || rating > 2) return false;
                                break;
                            case "3-4":
                                if (rating < 3 || rating > 4) return false;
                                break;
                            case "5":
                                if (rating != 5) return false;
                                break;
                            default:
                                // Invalid ratingGroup, ignore filter
                                break;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // 4. Convert to Page (approximate, since we filtered in memory)
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredReviews.size());
            List<ProductReview> pageContent = filteredReviews.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, filteredReviews.size())
                .map(this::convertToDTO);
            
        } catch (Exception e) {
            log.error("Error getting shop reviews", e);
            throw new RuntimeException("Lỗi khi lấy danh sách đánh giá của shop: " + e.getMessage());
        }
    }


            /**
     * Lấy tất cả reviews của shop, phân loại theo đã/chưa phản hồi (không phân trang)
     */
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public ShopReviewsGroupedDTO getAllShopReviewsGrouped(Long shopId, Authentication authentication) {
        try {
            // 1. Lấy seller từ authentication
            User seller = getCurrentUser(authentication);
            
            // 2. Tìm shop theo shopId
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));
            
            // 3. Kiểm tra seller sở hữu shop
            if (!shop.getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn chỉ có thể xem đánh giá của shop của mình");
            }
            
            // 4. Get all reviews of the shop (no pagination)
            List<ProductReview> allReviews = productReviewRepository.findAllByProductShopId(shopId);
            
            // 5. Group by replied/unreplied
            List<ProductReviewDTO> replied = allReviews.stream()
                .filter(review -> review.getSellerResponse() != null)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            List<ProductReviewDTO> unreplied = allReviews.stream()
                .filter(review -> review.getSellerResponse() == null)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return new ShopReviewsGroupedDTO(replied, unreplied);
            
        } catch (Exception e) {
            log.error("Error getting all shop reviews grouped", e);
            throw new RuntimeException("Lỗi khi lấy tất cả đánh giá của shop: " + e.getMessage());
        }
    }

    /**
     * Lấy reviews chưa phản hồi của shop
     */
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getUnrepliedShopReviews(Long shopId, int page, int size, Authentication authentication) {
        try {
            // Check if seller owns the shop
            User seller = getCurrentUser(authentication);
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));
            
            if (!shop.getOwner().getId().equals(seller.getId())) {
                throw new RuntimeException("Bạn chỉ có thể xem đánh giá của shop của mình");
            }
            
            // Get all reviews, then filter unreplied in memory (for small dataset)
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ProductReview> allReviews = productReviewRepository.findByProductShopId(shopId, pageable);
            
            List<ProductReview> unrepliedReviews = allReviews.getContent().stream()
                .filter(review -> review.getSellerResponse() == null)
                .collect(Collectors.toList());
            
            // Convert to Page
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), unrepliedReviews.size());
            List<ProductReview> pageContent = unrepliedReviews.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, unrepliedReviews.size())
                .map(this::convertToDTO);
            
        } catch (Exception e) {
            log.error("Error getting unreplied shop reviews", e);
            throw new RuntimeException("Lỗi khi lấy đánh giá chưa phản hồi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả reviews của shop của seller hiện tại, grouped by replied/unreplied
     */
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public ShopReviewsGroupedDTO getMyShopReviewsGrouped(Authentication authentication) {
        try {
            User seller = getCurrentUser(authentication);
            Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop của bạn"));
            
            return getAllShopReviewsGrouped(shop.getId(), authentication);
            
        } catch (Exception e) {
            log.error("Error getting my shop reviews grouped", e);
            throw new RuntimeException("Lỗi khi lấy đánh giá của shop: " + e.getMessage());
        }
    }
}