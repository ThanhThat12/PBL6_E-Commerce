package com.PBL6.Ecommerce.service.reviews;

import com.PBL6.Ecommerce.domain.Order;
import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductReview;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.dto.review.ProductReviewDTO;
import com.PBL6.Ecommerce.dto.review.request.CreateReviewRequest;
import com.PBL6.Ecommerce.dto.review.request.UpdateReviewRequest;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.ForbiddenException;
import com.PBL6.Ecommerce.exception.NotFoundException;
import com.PBL6.Ecommerce.repository.OrderRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ProductReviewService
 * 
 * Business logic for product reviews:
 * - Get product reviews with pagination and filtering
 * - Create review with full validation (purchase, order completed, one review per product)
 * - Update review within 7-day time limit
 * - Delete review
 * - Get user's reviews
 * 
 * Validation rules:
 * - User must own the order
 * - Order must be COMPLETED
 * - Order must contain the product
 * - One review per user per product (unique constraint)
 * - Can only update/delete within 7 days
 */
@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    // Giới hạn số ảnh tối đa trong 1 review (giống Shopee)
    private static final int MAX_REVIEW_IMAGES = 5;

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Get all reviews for a product with pagination and optional filtering
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getProductReviews(
            Long productId,
            int page,
            int size,
            Integer rating,
            String sortBy) {

        // 1. Validate product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

        // 2. Build sort order
        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Query reviews
        Page<ProductReview> reviews;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        } else {
            reviews = reviewRepository.findByProductId(productId, pageable);
        }

        // 4. Map to DTO
        return reviews.map(this::mapToDTO);
    }

    /**
     * Create a new review for a product with full validation
     */
    @Override
    public ProductReviewDTO createReview(
            Long productId,
            CreateReviewRequest request,
            String username) {

        // 1. Get current user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 2. Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

        // 3. Get order and validate
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Đơn hàng không tồn tại"));

        // 4. Check order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Đơn hàng không thuộc về bạn");
        }

        // 5. Check order status = COMPLETED
        if (!order.getStatus().equals(Order.OrderStatus.COMPLETED)) {
            throw new BadRequestException("Chỉ có thể review khi đơn hàng đã hoàn thành");
        }

        // 6. Check order contains this product
        boolean hasProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));

        if (!hasProduct) {
            throw new BadRequestException("Đơn hàng không chứa sản phẩm này");
        }

        // 7. Check if already reviewed (unique constraint)
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("Bạn đã review sản phẩm này rồi");
        }

        // 8. Create review
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // 9. Validate và serialize images as JSON (giới hạn 5 ảnh)
        try {
            if (request.getImages() != null && request.getImages().size() > MAX_REVIEW_IMAGES) {
                throw new BadRequestException("Chỉ được upload tối đa " + MAX_REVIEW_IMAGES + " ảnh");
            }
            
            String imagesJson = request.getImages() != null && !request.getImages().isEmpty()
                    ? objectMapper.writeValueAsString(request.getImages())
                    : null;
            review.setImages(imagesJson);
        } catch (BadRequestException e) {
            throw e; // Re-throw validation error
        } catch (Exception e) {
            throw new BadRequestException("Lỗi xử lý images: " + e.getMessage());
        }

        review.setVerifiedPurchase(true);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // 10. Save (trigger sẽ tự động update product.rating)
        reviewRepository.save(review);

        // 11. Return DTO
        return mapToDTO(review);
    }

    /**
     * Update an existing review (within 7 days)
     */
    @Override
    public ProductReviewDTO updateReview(
            Long reviewId,
            UpdateReviewRequest request,
            String username) {

        // 1. Get review
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review không tồn tại"));

        // 2. Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 3. Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa review này");
        }

        // 4. Check 7-day time limit
        LocalDateTime createdAt = review.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long daysElapsed = ChronoUnit.DAYS.between(createdAt, now);

        if (daysElapsed > 7) {
            throw new BadRequestException("Chỉ có thể sửa review trong vòng 7 ngày");
        }

        // 5. Update fields
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // 6. Update images (MERGE logic giống Shopee)
        try {
            List<String> finalImages = new ArrayList<>();
            
            // 6.1. Load ảnh cũ (nếu có)
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                List<String> existingImages = objectMapper.readValue(
                    review.getImages(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
                finalImages.addAll(existingImages);
            }
            
            // 6.2. Thêm ảnh mới (nếu có)
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                for (String newImage : request.getImages()) {
                    // Chỉ thêm nếu chưa tồn tại (tránh duplicate)
                    if (!finalImages.contains(newImage)) {
                        finalImages.add(newImage);
                    }
                }
            }
            
            // 6.3. Validate số lượng ảnh tối đa
            if (finalImages.size() > MAX_REVIEW_IMAGES) {
                throw new BadRequestException(
                    "Tổng số ảnh không được vượt quá " + MAX_REVIEW_IMAGES + " ảnh. " +
                    "Hiện có " + (finalImages.size() - request.getImages().size()) + " ảnh cũ, " +
                    "bạn chỉ có thể thêm tối đa " + 
                    (MAX_REVIEW_IMAGES - (finalImages.size() - request.getImages().size())) + " ảnh mới."
                );
            }
            
            // 6.4. Serialize danh sách cuối cùng
            String imagesJson = !finalImages.isEmpty()
                    ? objectMapper.writeValueAsString(finalImages)
                    : null;
            review.setImages(imagesJson);
            
        } catch (BadRequestException e) {
            throw e; // Re-throw validation error
        } catch (Exception e) {
            throw new BadRequestException("Lỗi xử lý images: " + e.getMessage());
        }

        review.setUpdatedAt(LocalDateTime.now());

        // 7. Save (trigger sẽ tự động update product.rating)
        reviewRepository.save(review);

        // 8. Return DTO
        return mapToDTO(review);
    }

    /**
     * Delete a review
     */
    @Override
    public void deleteReview(Long reviewId, String username) {

        // 1. Get review
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review không tồn tại"));

        // 2. Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 3. Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Bạn không có quyền xóa review này");
        }

        // 4. Delete (trigger sẽ tự động update product.rating)
        reviewRepository.deleteById(reviewId);
    }

    /**
     * Get reviews written by the current user
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getMyReviews(String username, int page, int size) {

        // 1. Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 2. Query reviews
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductReview> reviews = reviewRepository.findByUserId(user.getId(), pageable);

        // 3. Map to DTO
        return reviews.map(this::mapToDTO);
    }

    /**
     * Remove specific images from a review (Shopee-style)
     * Allows user to delete individual images from their review
     */
    @Override
    public ProductReviewDTO removeReviewImages(Long reviewId, List<String> imageUrls, String username) {
        
        // 1. Get review
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review không tồn tại"));

        // 2. Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        // 3. Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa review này");
        }

        // 4. Check 7-day time limit
        LocalDateTime createdAt = review.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long daysElapsed = ChronoUnit.DAYS.between(createdAt, now);

        if (daysElapsed > 7) {
            throw new BadRequestException("Chỉ có thể sửa review trong vòng 7 ngày");
        }

        // 5. Remove specified images
        try {
            List<String> currentImages = new ArrayList<>();
            
            // Load existing images
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                currentImages = objectMapper.readValue(
                    review.getImages(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                );
            }
            
            // Remove specified URLs
            if (imageUrls != null && !imageUrls.isEmpty()) {
                currentImages.removeAll(imageUrls);
            }
            
            // Save updated list
            String imagesJson = !currentImages.isEmpty()
                    ? objectMapper.writeValueAsString(currentImages)
                    : null;
            review.setImages(imagesJson);
            review.setUpdatedAt(LocalDateTime.now());
            
            reviewRepository.save(review);
            return mapToDTO(review);
            
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Lỗi xử lý xóa images: " + e.getMessage());
        }
    }

    /**
     * Helper: Build sort from sortBy parameter
     */
    private Sort buildSort(String sortBy) {
        return switch(sortBy) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "highest" -> Sort.by("rating").descending();
            case "lowest" -> Sort.by("rating").ascending();
            default -> Sort.by("createdAt").descending(); // newest
        };
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
        } catch (Exception e) {
            dto.setImages(new ArrayList<>()); // Empty list if error
        }

        dto.setVerifiedPurchase(review.getVerifiedPurchase());

        // Map user info (hide sensitive data)
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
}
