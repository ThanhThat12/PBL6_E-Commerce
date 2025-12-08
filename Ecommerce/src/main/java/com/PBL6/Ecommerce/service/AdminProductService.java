package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductImage;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductAttributeDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductDetailDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductDimensionsDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductImageDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductStats;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductVariantDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminShopDetailDTO;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.ProductImageRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;

    public AdminProductService(ProductRepository productRepository, 
                              CartItemRepository cartItemRepository,
                              OrderItemRepository orderItemRepository,
                              ProductReviewRepository productReviewRepository,
                              ProductVariantRepository productVariantRepository,
                              ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productReviewRepository = productReviewRepository;
        this.productVariantRepository = productVariantRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * Lấy danh sách sản phẩm với thông tin tổng hợp cho Admin (có phân trang)
     * Logic xử lý ở Service layer thay vì Repository
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm với thông tin tổng hợp
     */
    public Page<AdminListProductDTO> getProductsWithPaging(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllWithCategory(pageable);
        
        List<AdminListProductDTO> dtoList = productPage.getContent().stream()
            .map(this::convertToAdminListProductDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    public Page<AdminListProductDTO> searchProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByNameContaining(name, pageable);
        
        List<AdminListProductDTO> dtoList = productPage.getContent().stream()
            .map(this::convertToAdminListProductDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    /**
     * Lấy thống kê sản phẩm cho Admin
     * @return AdminProductStats - Thống kê sản phẩm
     */
    public AdminProductStats getProductStats() {
        Long totalProducts = productRepository.count();
        Long activeProducts = productRepository.countByIsActiveFalse();
        Long pendingProducts = productRepository.countByIsActiveFalse();
        
        // Tính tổng sản phẩm đã bán từ tất cả COMPLETED orders
        Long totalProductsSold = orderItemRepository.findAll().stream()
            .filter(oi -> oi.getOrder() != null && "COMPLETED".equals(oi.getOrder().getStatus()))
            .mapToLong(oi -> oi.getQuantity())
            .sum();
        
        return new AdminProductStats(totalProducts, activeProducts, pendingProducts, totalProductsSold);
    }

    /**
     * Lọc sản phẩm theo category
     * @param categoryName - Tên category
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm theo category
     */
    public Page<AdminListProductDTO> getProductsByCategory(String categoryName, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryName(categoryName, pageable);
        
        List<AdminListProductDTO> dtoList = productPage.getContent().stream()
            .map(this::convertToAdminListProductDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    /**
     * Lọc sản phẩm theo status
     * @param status - Status ("Active" hoặc "Pending")
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm theo status
     */
    public Page<AdminListProductDTO> getProductsByStatus(String status, Pageable pageable) {
        Boolean isActive = "Active".equalsIgnoreCase(status);
        Page<Product> productPage = productRepository.findByIsActive(isActive, pageable);
        
        List<AdminListProductDTO> dtoList = productPage.getContent().stream()
            .map(this::convertToAdminListProductDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    /**
     * Chuyển đổi Product entity sang AdminListProductDTO
     * Xử lý logic tính toán các trường: totalStock, sales, rating
     */
    private AdminListProductDTO convertToAdminListProductDTO(Product product) {
        // Lấy tổng stock từ tất cả variants
        Long totalStock = productVariantRepository.getTotalStockByProductId(product.getId());
        
        // Lấy tổng số lượng đã bán (chỉ COMPLETED orders)
        Long totalSold = orderItemRepository.getTotalSoldByProductId(product.getId());
        
        // Lấy rating trung bình
        Double avgRating = productReviewRepository.getAverageRatingByProductId(product.getId());
        
        return new AdminListProductDTO(
            product.getId(),
            product.getName(),
            product.getMainImage(),
            product.getCategory() != null ? product.getCategory().getName() : "Uncategorized",
            product.getBasePrice(),
            totalStock != null ? totalStock : 0L,
            product.getIsActive(),
            totalSold != null ? totalSold : 0L,
            avgRating != null ? avgRating : 0.0
        );
    }

    /**
     * Xóa sản phẩm (chỉ được xóa nếu sản phẩm không có trong cart_items)
     * @param productId - ID sản phẩm cần xóa
     * @throws ProductNotFoundException - Nếu sản phẩm không tồn tại
     * @throws BadRequestException - Nếu sản phẩm đang có trong giỏ hàng, đơn hàng, hoặc có reviews
     */
    @Transactional
    public void deleteProduct(Long productId) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Kiểm tra sản phẩm có trong cart_items không
        long cartItemCount = cartItemRepository.countByProductVariant_ProductId(productId);
        if (cartItemCount > 0) {
            throw new BadRequestException("Cannot delete product. Product exists in " + cartItemCount + " cart item(s)");
        }

        // Kiểm tra sản phẩm có trong order_items không
        long orderItemCount = orderItemRepository.countByProductVariant_ProductId(productId);
        if (orderItemCount > 0) {
            throw new BadRequestException("Cannot delete product. Product has been ordered " + orderItemCount + " time(s)");
        }

        // Kiểm tra sản phẩm có reviews không
        long reviewCount = productReviewRepository.countByProductId(productId);
        if (reviewCount > 0) {
            throw new BadRequestException("Cannot delete product. Product has " + reviewCount + " review(s)");
        }

        // Xóa sản phẩm (cascade sẽ tự động xóa product_variants và product_images)
        productRepository.delete(product);
    }

    /**
     * Lấy chi tiết đầy đủ của sản phẩm theo ID (Admin only)
     * Bao gồm: product info, variants, images, category, shop, statistics
     * @param productId - ID sản phẩm
     * @return AdminProductDetailDTO - Thông tin chi tiết đầy đủ
     * @throws ProductNotFoundException - Nếu sản phẩm không tồn tại
     */
    @Transactional(readOnly = true)
    public AdminProductDetailDTO getProductDetail(Long productId) {
        // 1. Lấy Product entity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // 2. Tạo basic info
        AdminProductDetailDTO dto = new AdminProductDetailDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBasePrice(product.getBasePrice());
        dto.setDescription(product.getDescription());
        dto.setMainImage(product.getMainImage());
        dto.setIsActive(product.getIsActive());
        dto.setWeightGrams(product.getWeightGrams());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // 3. Set category
        if (product.getCategory() != null) {
            dto.setCategory(new AdminCategoryDetailDTO(
                product.getCategory().getId(),
                product.getCategory().getName()
            ));
        }

        // 4. Set shop
        if (product.getShop() != null) {
            dto.setShop(new AdminShopDetailDTO(
                product.getShop().getId(),
                product.getShop().getName()
            ));
        }

        // 5. Set dimensions
        dto.setDimensions(new AdminProductDimensionsDTO(
            product.getHeightCm(),
            product.getLengthCm(),
            product.getWidthCm()
        ));

        // 6. Tính thống kê
        Long totalStock = productVariantRepository.getTotalStockByProductId(productId);
        Long totalSold = orderItemRepository.getTotalSoldByProductId(productId);
        Double averageRating = productReviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = productReviewRepository.countByProductId(productId);

        dto.setTotalStock(totalStock != null ? totalStock : 0L);
        dto.setTotalSold(totalSold != null ? totalSold : 0L);
        dto.setAverageRating(averageRating != null ? averageRating : 0.0);
        dto.setReviewCount(reviewCount);

        // 7. Lấy tất cả images của product
        List<ProductImage> productImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        List<AdminProductImageDTO> imageDTOs = productImages.stream()
            .map(img -> new AdminProductImageDTO(
                img.getId(),
                img.getImageUrl(),
                "MAIN".equals(img.getImageType()) // isMain = true nếu imageType = "MAIN"
            ))
            .collect(Collectors.toList());
        dto.setImages(imageDTOs);

        // 8. Lấy tất cả variants với attributes và images
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        List<AdminProductVariantDTO> variantDTOs = new ArrayList<>();

        for (ProductVariant variant : variants) {
            // Tính số lượng đã bán của variant này
            Long variantSold = orderItemRepository.getTotalSoldByVariantId(variant.getId());

            AdminProductVariantDTO variantDTO = new AdminProductVariantDTO(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStock(),
                variantSold != null ? variantSold : 0L
            );

            // Lấy attributes của variant từ product_variant_values
            List<AdminProductAttributeDTO> attributeDTOs = variant.getProductVariantValues().stream()
                .map(pvv -> new AdminProductAttributeDTO(
                    pvv.getProductAttribute().getName(),
                    pvv.getValue()
                ))
                .collect(Collectors.toList());
            variantDTO.setAttributes(attributeDTOs);

            // Lấy images của variant (nếu có)
            // Images của variant thường được lưu với imageType = "VARIANT" và variantAttributeValue
            // Lấy primary attribute value để tìm ảnh
            if (!variant.getProductVariantValues().isEmpty()) {
                String primaryAttributeValue = variant.getProductVariantValues().get(0).getValue();
                List<ProductImage> variantImages = productImageRepository
                    .findByProductIdAndVariantAttributeValue(productId, primaryAttributeValue);
                
                List<String> variantImageUrls = variantImages.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
                variantDTO.setImages(variantImageUrls);
            }

            variantDTOs.add(variantDTO);
        }
        dto.setVariants(variantDTOs);

        return dto;
    }
}