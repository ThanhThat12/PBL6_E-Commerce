package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductStats;
import com.PBL6.Ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;

    public AdminProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lấy danh sách sản phẩm với thông tin tổng hợp cho Admin (có phân trang)
     * @param pageable - Thông tin phân trang
     * @return Page<AdminListProductDTO> - Danh sách sản phẩm với thông tin tổng hợp
     */
    public Page<AdminListProductDTO> getProductsWithPaging(Pageable pageable) {
        return productRepository.findAllProductsForAdmin(pageable);
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
}
