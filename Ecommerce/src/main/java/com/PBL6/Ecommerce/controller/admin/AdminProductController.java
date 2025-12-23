package com.PBL6.Ecommerce.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminListProductDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminProductDetailDTO;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.service.AdminProductService;

/**
 * Controller for admin product management
 * Admin can: view all products with pagination
 */
@Tag(name = "Admin Products", description = "Admin product management")
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    
    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * API lấy danh sách sản phẩm với phân trang (Admin only)
     * GET /api/admin/products/page?page=0&size=10
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminListProductDTO>> - Danh sách sản phẩm với thông tin tổng hợp
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminListProductDTO>>> getProductsWithPaging(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminListProductDTO> products = adminProductService.getProductsWithPaging(pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Products retrieved successfully", products));
    }

    /**
     * API lấy thống kê sản phẩm (Admin only)
     * GET /api/admin/products/stats
     * @return ResponseDTO<AdminProductStats> - Thống kê sản phẩm
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<com.PBL6.Ecommerce.domain.dto.admin.AdminProductStats>> getProductStats() {
        com.PBL6.Ecommerce.domain.dto.admin.AdminProductStats stats = adminProductService.getProductStats();
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Product stats retrieved successfully", stats));
    }

    /**
     * API lọc sản phẩm theo category (Admin only)
     * GET /api/admin/products/category?categoryName=Electronics&page=0&size=10
     * @param categoryName - Tên category
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminListProductDTO>> - Danh sách sản phẩm theo category
     */
    @GetMapping("/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminListProductDTO>>> getProductsByCategory(
            @RequestParam(value = "categoryName") String categoryName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminListProductDTO> products = adminProductService.getProductsByCategory(categoryName, pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Products filtered by category successfully", products));
    }

    /**
     * API lọc sản phẩm theo status (Admin only)
     * GET /api/admin/products/status?status=Active&page=0&size=10
     * @param status - Status ("Active" hoặc "Pending")
     * @param page - Trang hiện tại (bắt đầu từ 0)
     * @param size - Số lượng items trên mỗi trang (mặc định 10)
     * @return ResponseDTO<Page<AdminListProductDTO>> - Danh sách sản phẩm theo status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminListProductDTO>>> getProductsByStatus(
            @RequestParam(value = "status") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminListProductDTO> products = adminProductService.getProductsByStatus(status, pageable);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Products filtered by status successfully", products));
    }

    /**
     * API xóa sản phẩm (Admin only)
     * DELETE /api/admin/products/{productId}/delete
     * @param productId - ID sản phẩm cần xóa
     * @return ResponseDTO<Void> - Thông báo xóa thành công
     * @throws ProductNotFoundException - Nếu sản phẩm không tồn tại
     * @throws BadRequestException - Nếu sản phẩm đang có trong giỏ hàng
     */
    @DeleteMapping("/{productId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Product deleted successfully", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Page<AdminListProductDTO>>> searchProducts(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<AdminListProductDTO> products = adminProductService.searchProducts(name, page, size);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Products searched successfully", products));
    }

    /**
     * API lấy chi tiết sản phẩm theo ID (Admin only)
     * GET /api/admin/products/{productId}/detail
     * @param productId - ID sản phẩm cần xem chi tiết
     * @return ResponseDTO<AdminProductDetailDTO> - Thông tin chi tiết đầy đủ của sản phẩm
     * @throws ProductNotFoundException - Nếu sản phẩm không tồn tại
     */
    @GetMapping("/{productId}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminProductDetailDTO>> getProductDetail(@PathVariable Long productId) {
        AdminProductDetailDTO productDetail = adminProductService.getProductDetail(productId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Product detail retrieved successfully", productDetail));
    }

    /**
     * API cập nhật trạng thái sản phẩm (Admin only)
     * PUT /api/admin/products/{productId}/status?isActive=true
     * @param productId - ID sản phẩm cần cập nhật
     * @param isActive - Trạng thái mới (true = Active, false = Inactive)
     * @return ResponseDTO<Void> - Thông báo cập nhật thành công
     * @throws ProductNotFoundException - Nếu sản phẩm không tồn tại
     */
    @PutMapping("/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam(value = "isActive") Boolean isActive) {
        adminProductService.updateProductStatus(productId, isActive);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Product status updated successfully", null));
    }
}
