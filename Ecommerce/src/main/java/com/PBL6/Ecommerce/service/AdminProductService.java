package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductStats;
import com.PBL6.Ecommerce.exception.BadRequestException;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductReviewRepository productReviewRepository;

    public AdminProductService(ProductRepository productRepository, 
                              CartItemRepository cartItemRepository,
                              OrderItemRepository orderItemRepository,
                              ProductReviewRepository productReviewRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productReviewRepository = productReviewRepository;
    }

    /**
     * Lấy danh sách sản phẩm với thông tin tổng hợp cho Admin (có phân trang)
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm với thông tin tổng hợp
     */
    public Page<AdminListProductDTO> getProductsWithPaging(Pageable pageable) {
        return productRepository.findAllProductsForAdmin(pageable);
    }

    public Page<AdminListProductDTO> searchProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAllProductsForAdminWithSearch(name, pageable);
    }

    /**
     * Lấy thống kê sản phẩm cho Admin
     * @return AdminProductStats - Thống kê sản phẩm
     */
    public AdminProductStats getProductStats() {
        Long totalProducts = productRepository.countTotalProducts();
        Long activeProducts = productRepository.countActiveProducts();
        Long pendingProducts = productRepository.countPendingProducts();
        Long totalProductsSold = productRepository.countTotalProductsSold();
        
        return new AdminProductStats(totalProducts, activeProducts, pendingProducts, totalProductsSold);
    }

    /**
     * Lọc sản phẩm theo category
     * @param categoryName - Tên category
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm theo category
     */
    public Page<AdminListProductDTO> getProductsByCategory(String categoryName, Pageable pageable) {
        return productRepository.findProductsByCategory(categoryName, pageable);
    }

    /**
     * Lọc sản phẩm theo status
     * @param status - Status ("Active" hoặc "Pending")
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm theo status
     */
    public Page<AdminListProductDTO> getProductsByStatus(String status, Pageable pageable) {
        Boolean isActive = "Active".equalsIgnoreCase(status);
        return productRepository.findProductsByStatus(isActive, pageable);
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

    
}
