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
    
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    
    @Autowired
    private ReviewReportRepository reviewReportRepository;

    /**
     * 1. Tạo đánh giá sản phẩm (POST /api/reviews)
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
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
     * - Chỉ được chỉnh sửa 1 lần duy nhất
     * - Trong vòng 30 ngày kể từ ngày gửi đánh giá ban đầu
     * - Không thể xóa đánh giá
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
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
            
            // 3. Check if already edited (only 1 edit allowed)
            if (review.getEditCount() != null && review.getEditCount() >= 1) {
                throw new RuntimeException("Bạn chỉ có thể chỉnh sửa đánh giá 1 lần duy nhất");
            }
            
            // 4. Check time limit (30 days from review creation)
            LocalDateTime createdAt = review.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();
            long daysSinceCreation = Duration.between(createdAt, now).toDays();
            
            if (daysSinceCreation > 30) {
                throw new RuntimeException("Chỉ có thể sửa đánh giá trong vòng 30 ngày kể từ ngày gửi đánh giá");
            }
            
            // 5. Update review
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setImages(convertImagesToJson(request.getImages()));
            review.setEditCount((review.getEditCount() == null ? 0 : review.getEditCount()) + 1);
            review.setUpdatedAt(LocalDateTime.now());
            
            review = productReviewRepository.save(review);
            
            log.info("Updated review {} by user {} (edit count: {})", reviewId, user.getUsername(), review.getEditCount());
            
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
     * ⚠️ DISABLED: Không cho phép xóa đánh giá theo chính sách mới.
     * Chỉ ADMIN mới có thể xóa review trong trường hợp vi phạm.
     */
    public void deleteReview(Long reviewId, Authentication authentication) {
        try {
            // 1. Find review
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // 2. Only ADMIN can delete reviews
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                throw new RuntimeException("Không thể xóa đánh giá. Vui lòng liên hệ Admin nếu cần hỗ trợ.");
            }
            
            User user = getCurrentUser(authentication);
            
            // 3. Delete review (Admin only)
            productReviewRepository.delete(review);
            
            log.info("Admin {} deleted review {}", user.getUsername(), reviewId);
            
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
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
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

    /**
     * Kiểm tra trạng thái review của user cho một product
     * GET /api/products/{productId}/review-eligibility
     * - Đã mua hàng chưa?
     * - Đã review chưa?
     * - Còn thời hạn review không (30 ngày từ khi order COMPLETED)?
     * - Có thể edit review không (1 lần trong 30 ngày)?
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @Transactional(readOnly = true)
    public ReviewEligibilityDTO checkReviewEligibility(Long productId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            ReviewEligibilityDTO dto = new ReviewEligibilityDTO();
            dto.setProductId(productId);
            
            // 1. Check if product exists
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            // 2. Check if SELLER is trying to review their own shop's product
            if (product.getShop() != null && product.getShop().getOwner() != null) {
                if (product.getShop().getOwner().getId().equals(user.getId())) {
                    dto.setCanReview(false);
                    dto.setHasPurchased(false);
                    dto.setHasReviewed(false);
                    dto.setMessage("Bạn không thể đánh giá sản phẩm của chính shop mình");
                    return dto;
                }
            }
            
            // 3. Check if already reviewed
            Optional<ProductReview> existingReview = productReviewRepository.findByUserIdAndProductId(user.getId(), productId);
            if (existingReview.isPresent()) {
                ProductReview review = existingReview.get();
                dto.setHasReviewed(true);
                dto.setHasPurchased(true);
                dto.setCanReview(false);
                dto.setExistingReviewId(review.getId());
                
                // Check edit eligibility
                Integer editCount = review.getEditCount() != null ? review.getEditCount() : 0;
                dto.setEditCount(editCount);
                
                long daysSinceCreation = Duration.between(review.getCreatedAt(), LocalDateTime.now()).toDays();
                int daysRemainingToEdit = Math.max(0, 30 - (int) daysSinceCreation);
                dto.setDaysRemainingToEdit(daysRemainingToEdit);
                
                boolean canEdit = editCount < 1 && daysRemainingToEdit > 0;
                dto.setCanEditReview(canEdit);
                
                if (canEdit) {
                    dto.setMessage("Bạn có thể chỉnh sửa đánh giá 1 lần trong " + daysRemainingToEdit + " ngày tới");
                } else if (editCount >= 1) {
                    dto.setMessage("Bạn đã sử dụng hết lượt chỉnh sửa đánh giá");
                } else {
                    dto.setMessage("Đã hết thời hạn chỉnh sửa đánh giá");
                }
                
                return dto;
            }
            
            // 4. Check for COMPLETED order containing this product
            java.util.List<Order> orders = orderRepository.findCompletedOrdersByUserAndProduct(user.getId(), productId);
            if (orders == null || orders.isEmpty()) {
                dto.setHasPurchased(false);
                dto.setHasReviewed(false);
                dto.setCanReview(false);
                dto.setMessage("Bạn cần mua sản phẩm này trước khi đánh giá");
                return dto;
            }
            
            Order order = orders.get(0);
            dto.setHasPurchased(true);
            dto.setHasReviewed(false);
            dto.setOrderId(order.getId());
            
            // 5. Check review deadline (30 days from order completion - use updatedAt as completion time)
            Date completedAt = order.getUpdatedAt();
            
            if (completedAt != null) {
                LocalDateTime completedDateTime = completedAt.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime deadline = completedDateTime.plusDays(30);
                long daysSinceCompleted = Duration.between(completedDateTime, LocalDateTime.now()).toDays();
                int daysRemaining = Math.max(0, 30 - (int) daysSinceCompleted);
                
                dto.setReviewDeadline(deadline);
                dto.setDaysRemainingToReview(daysRemaining);
                
                if (daysRemaining > 0) {
                    dto.setCanReview(true);
                    dto.setMessage("Bạn có " + daysRemaining + " ngày để đánh giá sản phẩm này");
                } else {
                    dto.setCanReview(false);
                    dto.setMessage("Đã hết thời hạn đánh giá (30 ngày kể từ khi nhận hàng)");
                }
            } else {
                // Fallback if no date available
                dto.setCanReview(true);
                dto.setDaysRemainingToReview(30);
                dto.setMessage("Bạn có thể đánh giá sản phẩm này");
            }
            
            return dto;
            
        } catch (Exception e) {
            log.error("Error checking review eligibility for product {}", productId, e);
            throw new RuntimeException("Lỗi khi kiểm tra trạng thái đánh giá: " + e.getMessage());
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
            dto.setProductImage(review.getProduct().getMainImage());
        }
        
        // Order info with variant and purchase date
        if (review.getOrder() != null) {
            dto.setOrderId(review.getOrder().getId());
            
            // Use updatedAt as purchase/completion date (when order status changed to COMPLETED)
            if (review.getOrder().getUpdatedAt() != null) {
                dto.setPurchaseDate(review.getOrder().getUpdatedAt().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            }
            
            // Try to get variant info from order items - use variantName which is already stored
            try {
                review.getOrder().getOrderItems().stream()
                    .filter(item -> item.getProductId() != null && 
                                   item.getProductId().equals(review.getProduct().getId()))
                    .findFirst()
                    .ifPresent(item -> {
                        // Use the pre-stored variantName field
                        if (item.getVariantName() != null && !item.getVariantName().isEmpty()) {
                            dto.setVariantInfo(item.getVariantName());
                        }
                    });
            } catch (Exception e) {
                // Ignore variant info errors - non-critical
                log.debug("Could not get variant info for review: {}", e.getMessage());
            }
        }
        
        // Like info - include count for all reviews
        dto.setLikesCount(reviewLikeRepository.countByReviewId(review.getId()));
        // isLikedByCurrentUser will be set separately when user context is available
        dto.setIsLikedByCurrentUser(false);
        
        // Edit constraints info
        Integer editCount = review.getEditCount() != null ? review.getEditCount() : 0;
        dto.setEditCount(editCount);
        
        // Calculate days remaining to edit (30 days from review creation)
        LocalDateTime createdAt = review.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long daysSinceCreation = Duration.between(createdAt, now).toDays();
        int daysRemaining = Math.max(0, 30 - (int) daysSinceCreation);
        dto.setDaysRemainingToEdit(daysRemaining);
        
        // Can edit: only if editCount < 1 AND within 30 days
        boolean canEdit = editCount < 1 && daysRemaining > 0;
        dto.setCanEdit(canEdit);
        
        return dto;
    }

    
    /**
     * Tạo đánh giá sản phẩm từ trang chi tiết sản phẩm hoặc trang đơn hàng
     * - Thời hạn đánh giá: 30 ngày kể từ khi đơn hàng chuyển sang COMPLETED
     * - SELLER không được đánh giá sản phẩm của chính shop mình
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ProductReviewDTO createReviewForProduct(Long productId, CreateReviewRequestDTO request, Authentication authentication) {
            try {
                // 1. Get current user
                User user = getCurrentUser(authentication);

                // 2. Validate product exists
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                
                // 3. Check if SELLER is trying to review their own shop's product
                if (product.getShop() != null && product.getShop().getOwner() != null) {
                    if (product.getShop().getOwner().getId().equals(user.getId())) {
                        throw new RuntimeException("Bạn không thể đánh giá sản phẩm của chính shop mình");
                    }
                }

                // 4. Check if already reviewed
                if (productReviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
                    throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
                }

                // 5. Find a COMPLETED order of the user that contains this product
                java.util.List<Order> orders = orderRepository.findCompletedOrdersByUserAndProduct(user.getId(), productId);
                if (orders == null || orders.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy đơn hàng hoàn tất chứa sản phẩm này");
                }
                Order order = orders.get(0); // most recent due to ORDER BY in query
                
                // 6. Check review deadline: 30 days from order completion (use updatedAt as completion time)
                Date completedAt = order.getUpdatedAt();
                
                if (completedAt != null) {
                    long daysSinceCompleted = Duration.between(
                        completedAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                        LocalDateTime.now()
                    ).toDays();
                    
                    if (daysSinceCompleted > 30) {
                        throw new RuntimeException("Đã hết thời hạn đánh giá. Bạn chỉ có thể đánh giá trong vòng 30 ngày kể từ khi đơn hàng hoàn thành.");
                    }
                }

                // 7. Create review
                ProductReview review = new ProductReview();
                review.setProduct(product);
                review.setUser(user);
                review.setOrder(order);
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setImages(convertImagesToJson(request.getImages()));
                review.setVerifiedPurchase(true);
                review.setEditCount(0);

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
    
    // ==================== LIKE METHODS ====================
    
    /**
     * Toggle like/unlike một review
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ReviewLikeResponseDTO toggleReviewLike(Long reviewId, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            // Check review exists
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // Check if already liked
            Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserIdAndReviewId(user.getId(), reviewId);
            
            boolean liked;
            if (existingLike.isPresent()) {
                // Unlike
                reviewLikeRepository.delete(existingLike.get());
                liked = false;
                log.info("User {} unliked review {}", user.getUsername(), reviewId);
            } else {
                // Like
                ReviewLike newLike = new ReviewLike();
                newLike.setUser(user);
                newLike.setReview(review);
                reviewLikeRepository.save(newLike);
                liked = true;
                log.info("User {} liked review {}", user.getUsername(), reviewId);
            }
            
            Long likesCount = reviewLikeRepository.countByReviewId(reviewId);
            
            return new ReviewLikeResponseDTO(reviewId, liked, likesCount);
            
        } catch (Exception e) {
            log.error("Error toggling review like", e);
            throw new RuntimeException("Lỗi khi like/unlike đánh giá: " + e.getMessage());
        }
    }
    
    /**
     * Get like status cho một review
     */
    @Transactional(readOnly = true)
    public ReviewLikeResponseDTO getReviewLikeStatus(Long reviewId, Authentication authentication) {
        try {
            // Check review exists
            if (!productReviewRepository.existsById(reviewId)) {
                throw new RuntimeException("Không tìm thấy đánh giá");
            }
            
            Long likesCount = reviewLikeRepository.countByReviewId(reviewId);
            boolean liked = false;
            
            if (authentication != null) {
                User user = getCurrentUser(authentication);
                liked = reviewLikeRepository.existsByUserIdAndReviewId(user.getId(), reviewId);
            }
            
            return new ReviewLikeResponseDTO(reviewId, liked, likesCount);
            
        } catch (Exception e) {
            log.error("Error getting review like status", e);
            throw new RuntimeException("Lỗi khi lấy trạng thái like: " + e.getMessage());
        }
    }
    
    // ==================== REPORT METHODS ====================
    
    /**
     * Report một review
     */
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ReviewReportDTO reportReview(Long reviewId, ReportReviewRequestDTO request, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            
            // Check review exists
            ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
            
            // Check if already reported by this user
            if (reviewReportRepository.existsByReportedByIdAndReviewId(user.getId(), reviewId)) {
                throw new RuntimeException("Bạn đã báo cáo đánh giá này rồi");
            }
            
            // Don't allow reporting own review
            if (review.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bạn không thể báo cáo đánh giá của chính mình");
            }
            
            // Create report
            ReviewReport report = new ReviewReport();
            report.setReview(review);
            report.setReportedBy(user);
            report.setReportType(ReviewReport.ReportType.valueOf(request.getReportType()));
            report.setReason(request.getReason());
            report.setStatus(ReviewReport.ReportStatus.PENDING);
            
            report = reviewReportRepository.save(report);
            
            log.info("User {} reported review {} for {}", user.getUsername(), reviewId, request.getReportType());
            
            return convertReportToDTO(report);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Loại báo cáo không hợp lệ: " + request.getReportType());
        } catch (Exception e) {
            log.error("Error reporting review", e);
            throw new RuntimeException("Lỗi khi báo cáo đánh giá: " + e.getMessage());
        }
    }
    
    /**
     * Get pending reports (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ReviewReportDTO> getPendingReports(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewReport> reports = reviewReportRepository.findByStatus(
                ReviewReport.ReportStatus.PENDING, pageable);
            
            return reports.map(this::convertReportToDTO);
            
        } catch (Exception e) {
            log.error("Error getting pending reports", e);
            throw new RuntimeException("Lỗi khi lấy danh sách báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Get all reports with status filter (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ReviewReportDTO> getReportsByStatus(String status, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<ReviewReport> reports = reviewReportRepository.findByStatus(
                ReviewReport.ReportStatus.valueOf(status), pageable);
            
            return reports.map(this::convertReportToDTO);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status);
        } catch (Exception e) {
            log.error("Error getting reports by status", e);
            throw new RuntimeException("Lỗi khi lấy danh sách báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Update report status (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewReportDTO updateReportStatus(Long reportId, String status, String adminNote, Authentication authentication) {
        try {
            User admin = getCurrentUser(authentication);
            
            ReviewReport report = reviewReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
            
            report.setStatus(ReviewReport.ReportStatus.valueOf(status));
            report.setAdminNote(adminNote);
            report.setReviewedBy(admin);
            report.setReviewedAt(LocalDateTime.now());
            
            report = reviewReportRepository.save(report);
            
            log.info("Admin {} updated report {} status to {}", admin.getUsername(), reportId, status);
            
            return convertReportToDTO(report);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status);
        } catch (Exception e) {
            log.error("Error updating report status", e);
            throw new RuntimeException("Lỗi khi cập nhật báo cáo: " + e.getMessage());
        }
    }
    
    /**
     * Get report counts by status (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Long> getReportCounts() {
        try {
            Map<String, Long> counts = new HashMap<>();
            for (ReviewReport.ReportStatus status : ReviewReport.ReportStatus.values()) {
                counts.put(status.name(), reviewReportRepository.countByStatus(status));
            }
            return counts;
            
        } catch (Exception e) {
            log.error("Error getting report counts", e);
            throw new RuntimeException("Lỗi khi lấy thống kê báo cáo: " + e.getMessage());
        }
    }
    
    // Helper method to convert ReviewReport to DTO
    private ReviewReportDTO convertReportToDTO(ReviewReport report) {
        ReviewReportDTO dto = new ReviewReportDTO();
        dto.setId(report.getId());
        dto.setReviewId(report.getReview().getId());
        dto.setReviewRating(report.getReview().getRating());
        dto.setReviewComment(report.getReview().getComment());
        dto.setReviewerName(report.getReview().getUser().getUsername());
        dto.setProductId(report.getReview().getProduct().getId());
        dto.setProductName(report.getReview().getProduct().getName());
        dto.setReportedByName(report.getReportedBy().getUsername());
        dto.setReportType(report.getReportType().name());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus().name());
        dto.setAdminNote(report.getAdminNote());
        if (report.getReviewedBy() != null) {
            dto.setReviewedByName(report.getReviewedBy().getUsername());
        }
        dto.setReviewedAt(report.getReviewedAt());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}