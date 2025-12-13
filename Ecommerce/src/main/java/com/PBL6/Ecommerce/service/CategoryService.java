package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryDTO;
import com.PBL6.Ecommerce.domain.dto.admin.AdminCategoryStatsDTO;
import com.PBL6.Ecommerce.domain.entity.product.Category;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.PBL6.Ecommerce.exception.CategoryInUseException;
import com.PBL6.Ecommerce.exception.CategoryNotFoundException;
import com.PBL6.Ecommerce.exception.DuplicateCategoryException;


@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ShopRepository shopRepository,
                          UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .toList();
    }

    public CategoryDTO addCategory(CategoryDTO dto) {
        // 1. VALIDATION - Kiểm tra tên đã tồn tại chưa
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateCategoryException(dto.getName());
        }
        
        // 2. TẠO ENTITY MỚI
        Category category = new Category();
        category.setName(dto.getName());
        
        // 3. LƯU VÀO DATABASE
        Category saved = categoryRepository.save(category);
        // SQL: INSERT INTO categories (name) VALUES (?);
        
        // 4. CONVERT VÀ TRẢ VỀ
        return new CategoryDTO(saved.getId(), saved.getName());
    }

    /**
     * Sửa category - Chỉ admin
     */
    @Transactional
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto) {
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // Kiểm tra tên mới có bị trùng với category khác không
        if (categoryRepository.existsByName(dto.getName()) && !category.getName().equalsIgnoreCase(dto.getName())) {
            throw new DuplicateCategoryException(dto.getName());
        }

        // Cập nhật tên
        category.setName(dto.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getId(), saved.getName());
    }

    /**
     * Xóa category - Chỉ admin
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        
        // Kiểm tra có products nào đang sử dụng category này không
        long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new CategoryInUseException(categoryId, productCount);
        }
                
        // Xóa category
        categoryRepository.delete(category);
    }

    /**
     * API A: Get all categories with statistics for admin
     * Returns list of categories with totalProducts (active) and totalSoldProducts (COMPLETED orders)
     */
    @Transactional(readOnly = true)
    public List<AdminCategoryDTO> getAllCategoriesForAdmin() {
        return categoryRepository.findAllCategoriesForAdmin();
    }

    /**
     * API B: Get overall category statistics
     * Returns totalCategories, totalProducts (active), productsSold (from COMPLETED orders)
     */
    @Transactional(readOnly = true)
    public AdminCategoryStatsDTO getCategoryStats() {
        return categoryRepository.getCategoryStats();
    }

     /**
     * 🛍️ Lấy tất cả categories mà shop có sản phẩm
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByShop(String username) {
        // Tìm seller
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        // Tìm shop của seller
        Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        // Lấy tất cả categories có sản phẩm trong shop này
        List<Category> categories = categoryRepository.findCategoriesByShopId(shop.getId());
        
        return categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 🛍️ Lấy sản phẩm theo category và shop
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryAndShop(Long categoryId, String username) {
        // Tìm seller
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));
        
        // Tìm shop của seller
        Shop shop = shopRepository.findByOwnerId(seller.getId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));
        
        // Kiểm tra category tồn tại
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        // Lấy sản phẩm theo category và shop
        List<Product> products = productRepository.findByCategoryIdAndShopId(categoryId, shop.getId());
        
        return products.stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setIsActive(product.getIsActive());
        dto.setMainImage(product.getMainImage());
        
        // Set category information
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }
        
        // Set shop information
        if (product.getShop() != null) {
            dto.setShopName(product.getShop().getName());
        }
        
        return dto;
    }
}
