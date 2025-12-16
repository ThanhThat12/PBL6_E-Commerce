package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.dto.AttributeDTO;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ProductImageDTO;
import com.PBL6.Ecommerce.domain.dto.ProductUpdateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantValueDTO;
import com.PBL6.Ecommerce.domain.entity.product.Category;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.product.ProductAttribute;
import com.PBL6.Ecommerce.domain.entity.product.ProductImage;
import com.PBL6.Ecommerce.domain.entity.product.ProductPrimaryAttribute;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariant;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariantValue;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.domain.entity.user.Address;
import com.PBL6.Ecommerce.domain.entity.user.Role;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.exception.DuplicateSKUException;
import com.PBL6.Ecommerce.exception.InvalidProductDataException;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.exception.ShopNotFoundException;
import com.PBL6.Ecommerce.exception.UnauthorizedProductAccessException;
import com.PBL6.Ecommerce.exception.UserNotFoundException;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.OrderItemRepository;
import com.PBL6.Ecommerce.repository.ProductAttributeRepository;
import com.PBL6.Ecommerce.repository.ProductImageRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.ProductVariantValueRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;

@Service
@Transactional
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;
    
    @Autowired
    private ProductAttributeRepository productAttributeRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantValueRepository productVariantValueRepository;
    
    @Autowired
    private com.PBL6.Ecommerce.repository.ProductPrimaryAttributeRepository productPrimaryAttributeRepository;

    @Autowired
    private com.PBL6.Ecommerce.repository.ProductReviewRepository productReviewRepository;

    // Lấy tất cả sản phẩm
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID
    @Transactional
public ProductDTO getProductById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    
    // Sync rating from reviews if not yet synced or outdated
    syncProductRatingIfNeeded(product);
    
    // Convert to DTO and set additional fields
    ProductDTO dto = convertToProductDTO(product);
    dto.setShopId(product.getShop().getId());
    
    return dto;
}


// Thay thế method createProduct (dòng 61-104)
public ProductDTO createProduct(ProductCreateDTO request, Authentication authentication) {
    System.out.println("🔍 DEBUG - Starting createProduct");
    System.out.println("🔍 DEBUG - Request: " + request.getName());
    System.out.println("🔍 DEBUG - Has variants: " + (request.getVariants() != null ? request.getVariants().size() : 0));
    System.out.println("🔍 DEBUG - Images will be handled separately");
    User currentUser = getCurrentUser(authentication);
    
    // Null checks
    if (request.getCategoryId() == null) {
        throw new RuntimeException("Category ID không được để trống");
    }
    
    // Kiểm tra category tồn tại
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
    
    Shop shop;
    
    // Get shop logic
    if (isAdmin(authentication)) {
        if (request.getShopId() == null) {
            throw new RuntimeException("Admin phải chỉ định Shop ID");

        }
        shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + request.getShopId()));
    } 
    else if (isSeller(authentication)) {
        Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
        if (shopOpt.isEmpty()) {
            throw new RuntimeException("Seller chưa có cửa hàng");
        }
        shop = shopOpt.get();
        request.setShopId(shop.getId());
    } else {
        throw new RuntimeException("Không có quyền tạo sản phẩm");
    }
    
    // 🔧 1. Tạo và lưu product TRƯỚC (không có relationships)
    Product product = new Product();
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setBasePrice(request.getBasePrice());
    product.setProductCondition(request.getProductCondition());
    product.setCategory(category);
    product.setShop(shop);
    
    // Set shipping dimensions if provided
    product.setWeightGrams(request.getWeightGrams());
    product.setLengthCm(request.getLengthCm());
    product.setWidthCm(request.getWidthCm());
    product.setHeightCm(request.getHeightCm());
    
    // Set active status
    if (isAdmin(authentication)) {
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
    } else {
        product.setIsActive(false);
        System.out.println("🔍 DEBUG - Seller tạo sản phẩm, is_active = false");
    }
    
    // 🔧 2. SAVE product trước khi add relationships
    product = productRepository.save(product);
    System.out.println("🔍 DEBUG - Product saved with ID: " + product.getId());
    
    // 🔧 3. Images are now handled separately via /api/products/{id}/images/* endpoints
    // This separates concerns and allows for better image management
    System.out.println("ℹ️ Images will be handled by separate endpoints after product creation");
    
    // 🔧 4. Handle variants SAU KHI product đã có ID  
    if (request.getVariants() != null && !request.getVariants().isEmpty()) {
        System.out.println("🔍 DEBUG - Starting to handle " + request.getVariants().size() + " variants for product ID: " + product.getId());
        handleProductVariants(product, request.getVariants());
        System.out.println("✅ DEBUG - Completed handling all variants");
    }
    
    // 🆕 5. Handle primary attribute if provided
    if (request.getPrimaryAttributeId() != null) {
        System.out.println("🔍 DEBUG - Setting primary attribute ID: " + request.getPrimaryAttributeId());
        handlePrimaryAttribute(product.getId(), request.getPrimaryAttributeId());
    }
    
    // 🔧 6. Reload product để có relationships mới
    product = productRepository.findById(product.getId()).orElse(product);
    System.out.println("🔍 DEBUG - Final product has " + 
        (product.getProductVariants() != null ? product.getProductVariants().size() : 0) + " variants");
    
    return convertToProductDTO(product);
}

// 🆕 Cập nhật sản phẩm
@Transactional
public ProductDTO updateProduct(Long productId, ProductUpdateDTO request, Authentication authentication) {
    // Tìm sản phẩm
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    
    // Validate ownership
    validateProductOwnership(product, authentication);
    
    // Update only non-null fields
    if (request.getName() != null) {
        product.setName(request.getName());
    }
    if (request.getDescription() != null) {
        product.setDescription(request.getDescription());
    }
    if (request.getBasePrice() != null) {
        product.setBasePrice(request.getBasePrice());
    }
    if (request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
    }
    if (request.getProductCondition() != null) {
        product.setProductCondition(request.getProductCondition());
    }
    if (request.getIsActive() != null) {
        product.setIsActive(request.getIsActive());
    }
    
    // Update shipping dimensions
    if (request.getWeightGrams() != null) product.setWeightGrams(request.getWeightGrams());
    if (request.getLengthCm() != null) product.setLengthCm(request.getLengthCm());
    if (request.getWidthCm() != null) product.setWidthCm(request.getWidthCm());
    if (request.getHeightCm() != null) product.setHeightCm(request.getHeightCm());
    
    // Handle variants if provided
    if (request.getVariants() != null) {
        handleProductVariants(product, request.getVariants());
    }
    
    // Handle primary attribute if provided
    if (request.getPrimaryAttributeId() != null) {
        System.out.println("🔍 DEBUG - Updating primary attribute ID: " + request.getPrimaryAttributeId());
        handlePrimaryAttribute(product.getId(), request.getPrimaryAttributeId());
    }
    
    productRepository.save(product);
    return convertToProductDTO(product);
}

private void validateProductOwnership(Product product, Authentication authentication) {
    String userEmail = authentication.getName();
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Admin can modify any product
    if (user.getRole() == Role.ADMIN) {
        return;
    }
    
    // Seller can only modify their own shop's products
    if (user.getRole() == Role.SELLER) {
        if (user.getShop() == null) {
            throw new UnauthorizedProductAccessException("Seller must have a shop to modify products");
        }
        if (!product.getShop().getId().equals(user.getShop().getId())) {
            throw new UnauthorizedProductAccessException("You can only modify your own shop's products");
        }
        return;
    }
    
    throw new UnauthorizedProductAccessException("Insufficient permissions to modify product");
}

     // 🆕 Admin duyệt/từ chối sản phẩm
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO approveProduct(Long productId, Boolean approved) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        product.setIsActive(approved);
        product = productRepository.save(product);
        
        // TODO: Gửi notification cho seller về kết quả duyệt
        // notificationService.notifyProductApproval(product.getShop().getOwner(), product, approved);
        
        System.out.println("🔍 DEBUG - Admin " + (approved ? "duyệt" : "từ chối") + " sản phẩm ID: " + productId);
        
        return convertToProductDTO(product);
    }

      // 🆕 Lấy danh sách sản phẩm chờ duyệt (chỉ admin)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<ProductDTO> getPendingProducts(Pageable pageable) {
        Page<Product> pendingProducts = productRepository.findByIsActiveFalse(pageable);
        return pendingProducts.map(this::convertToProductDTO);
    }

    // 🆕 Đếm số sản phẩm chờ duyệt
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public long countPendingProducts() {
        return productRepository.countByIsActiveFalse();
    }
    
    
    // Lấy tất cả sản phẩm cho quản lý (Admin xem tất cả, Seller xem của mình)
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProductsForManagement(Pageable pageable, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        log.debug("Get products for management - User: {}, Role: {}", currentUser.getUsername(), currentUser.getRole());
        
        if (currentUser.getRole() == Role.ADMIN) {
            log.debug("User is ADMIN, getting all products");
            Page<Product> products = productRepository.findAll(pageable);
            return products.map(this::convertToProductDTO);
        } 
        else if (currentUser.getRole() == Role.SELLER) {
            log.debug("User is SELLER, finding shop by owner_id");
            
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
            
            if (shopOpt.isEmpty()) {
                log.warn("Seller has no shop - owner_id: {}", currentUser.getId());
                throw new ShopNotFoundException("Seller chưa có cửa hàng");
            }
            
            Shop shop = shopOpt.get();
            log.debug("Shop found: {}, ID: {}", shop.getName(), shop.getId());
            
            Page<Product> products = productRepository.findByShopId(shop.getId(), pageable);
            log.debug("Found {} products for shop", products.getTotalElements());
            
            return products.map(this::convertToProductDTO);
        } 
        else {
            log.error("Invalid user role: {}", currentUser.getRole());
            throw new UnauthorizedProductAccessException("Không có quyền truy cập");
        }
    }
    
    // Lấy tất cả sản phẩm đang hoạt động (cho khách hàng)
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToProductDTO);
    }
    
    // Tìm kiếm sản phẩm đang hoạt động
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchActiveProducts(String name, Long categoryId, Long shopId, 
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               BigDecimal minRating,
                                               Pageable pageable) {
        Page<Product> products = productRepository.findProductsWithFilters(
            name, categoryId, shopId, true, minPrice, maxPrice, minRating, pageable);
        return products.map(this::convertToProductDTO);
    }
    
    // Lấy sản phẩm đang hoạt động theo category
    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return products.map(this::convertToProductDTO);
    }
    
    // Lấy sản phẩm theo category (tất cả trạng thái)
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }
    

   
    // Xóa sản phẩm với kiểm tra quyền
     public void deleteProduct(Long productId, Authentication authentication) {
        // 🔧 THÊM NULL CHECK CHO AUTHENTICATION
        if (authentication == null) {
            throw new RuntimeException("Không tìm thấy thông tin xác thực. Vui lòng đăng nhập lại.");
        }
        
        String username = authentication.getName();
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username không hợp lệ");
        }
        
        // Tìm sản phẩm
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        System.out.println("🔍 DEBUG - Deleting product ID: " + productId + ", User: " + username);
        
        // Kiểm tra quyền
        if (isAdmin(authentication)) {
            System.out.println("🔍 DEBUG - Admin deleting product");
            productRepository.delete(product);
        } else if (isSeller(authentication)) {
            System.out.println("🔍 DEBUG - Seller attempting to delete product");
            
            // Seller chỉ có thể xóa sản phẩm của shop mình
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với username: " + username));
            // Kiểm tra ownership
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
            if (shopOpt.isEmpty()) {
                throw new RuntimeException("Seller chưa có cửa hàng");
            }
            
            Shop userShop = shopOpt.get();
            if (!product.getShop().getId().equals(userShop.getId())) {
                throw new RuntimeException("Bạn không có quyền xóa sản phẩm này");
            }
            
            System.out.println("🔍 DEBUG - Ownership verified, deleting product");
            productRepository.delete(product);
        } else {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm");
        }
        
        System.out.println("✅ DEBUG - Product deleted successfully");

    }
    
    // Lấy sản phẩm của seller hiện tại
    @Transactional(readOnly = true)
    public Page<ProductDTO> getSellerProducts(Pageable pageable, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
        if (shopOpt.isEmpty()) {
            throw new ShopNotFoundException("Seller chưa có cửa hàng");
        }
        
        Shop shop = shopOpt.get();
        Page<Product> products = productRepository.findByShopId(shop.getId(), pageable);
        return products.map(this::convertToProductDTO);
    }
    
    // Lấy danh sách sản phẩm của seller hiện tại (không phân trang)
    @Transactional(readOnly = true)
    public List<ProductDTO> getSellerProductsList(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
        if (shopOpt.isEmpty()) {
            throw new ShopNotFoundException("Seller chưa có cửa hàng");
        }
        
        Shop shop = shopOpt.get();
        return productRepository.findByShopId(shop.getId())
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }
    
     
    // 🔧 SỬA: Chỉ admin mới được thay đổi trạng thái sản phẩm
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO toggleProductStatus(Long id, Authentication authentication) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        
        product.setIsActive(!product.getIsActive());
        product = productRepository.save(product);
        
        return convertToProductDTO(product);
    }

    
    // Kiểm tra quyền sở hữu sản phẩm (để dùng trong @PreAuthorize)
    public boolean isProductOwner(Long productId, String username) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) return false;
            
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(user.getId());
            if (shopOpt.isEmpty()) return false;
            
            Shop userShop = shopOpt.get();
            return product.getShop().getId().equals(userShop.getId());
        } catch (Exception e) {
            log.error("Error checking product ownership", e);
            return false;
        }
    }
    
    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        
        log.debug("Looking for user with username: {}", username);
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với username: " + username));
    }
    
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    
    private boolean isSeller(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELLER"));
    }
    
    private boolean canModifyProduct(Product product, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true; // Admin có thể sửa tất cả
        }
        
        if (isSeller(authentication)) {
            User currentUser = getCurrentUser(authentication);
            
            Optional<Shop> shopOpt = shopRepository.findByOwnerId(currentUser.getId());
            if (shopOpt.isEmpty()) {
                return false;
            }
            
            Shop userShop = shopOpt.get();
            return product.getShop().getId().equals(userShop.getId());
        }
        
        return false;
    }
    
    // Tạo product variants
    private List<ProductVariant> createProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
        List<ProductVariant> variants = new ArrayList<>();
        
        for (ProductVariantDTO variantDTO : variantDTOs) {
            // Kiểm tra SKU đã tồn tại
            if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                throw new DuplicateSKUException("SKU đã tồn tại: " + variantDTO.getSku());
            }
            
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(variantDTO.getSku());
            variant.setPrice(variantDTO.getPrice());
            variant.setStock(variantDTO.getStock());
            
            variant = productVariantRepository.save(variant);
            
            // Tạo variant values nếu có
            if (variantDTO.getVariantValues() != null && !variantDTO.getVariantValues().isEmpty()) {
                List<ProductVariantValue> variantValues = createProductVariantValues(variant, variantDTO.getVariantValues());
                variant.setProductVariantValues(variantValues);
            }
            
            variants.add(variant);
        }
        
        return variants;
    }
    
    // Tạo product variant values
    private List<ProductVariantValue> createProductVariantValues(ProductVariant variant, List<ProductVariantValueDTO> valuesDTOs) {
        List<ProductVariantValue> values = new ArrayList<>();
        
        for (ProductVariantValueDTO valueDTO : valuesDTOs) {
            ProductAttribute attribute = productAttributeRepository.findById(valueDTO.getProductAttributeId())
                .orElseThrow(() -> new InvalidProductDataException("Không tìm thấy thuộc tính với ID: " + valueDTO.getProductAttributeId()));
            
            ProductVariantValue value = new ProductVariantValue();
            value.setVariant(variant);
            value.setProductAttribute(attribute);
            value.setValue(valueDTO.getValue());
            
            values.add(value);
        }
        
        return values;
    }
    
    // Tạo product images
    private List<ProductImage> createProductImages(Product product, List<String> imageUrls) {
        return imageUrls.stream().map(url -> {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(url);
            return productImageRepository.save(image);
        }).collect(Collectors.toList());
    }
    
    // Convert Product entity to ProductDTO
    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setIsActive(product.getIsActive());
        dto.setMainImage(product.getMainImage());
        dto.setMainImagePublicId(product.getMainImagePublicId());
        
        // Set product condition with default if null
        dto.setProductCondition(product.getProductCondition() != null ? product.getProductCondition() : "NEW");
        
        // Calculate real-time rating from reviews instead of using cached DB value
        Double averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
        long actualReviewCount = productReviewRepository.countByProductId(product.getId());
        
        if (averageRating != null && averageRating > 0) {
            dto.setRating(BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP));
        } else {
            dto.setRating(BigDecimal.ZERO);
        }
        dto.setReviewCount((int) actualReviewCount);
        dto.setSoldCount(product.getSoldCount() != null ? product.getSoldCount() : 0);
        
        // Set timestamps
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Set shipping dimensions
        dto.setWeightGrams(product.getWeightGrams());
        dto.setLengthCm(product.getLengthCm());
        dto.setWidthCm(product.getWidthCm());
        dto.setHeightCm(product.getHeightCm());
        
        // Convert category
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }
        
        // Set shop info (both name and ID)
        if (product.getShop() != null) {
            dto.setShopId(product.getShop().getId());
            dto.setShopName(product.getShop().getName());
            
            // Get shop province from owner's primary address
            User owner = product.getShop().getOwner();
            if (owner != null && owner.getAddresses() != null && !owner.getAddresses().isEmpty()) {
                // Find primary address or use first address
                String provinceName = owner.getAddresses().stream()
                    .filter(Address::isPrimaryAddress)
                    .findFirst()
                    .map(Address::getProvinceName)
                    .orElse(owner.getAddresses().get(0).getProvinceName());
                dto.setShopProvince(provinceName);
            }
        }
        
        // Convert variants
        if (product.getProductVariants() != null && !product.getProductVariants().isEmpty()) {
            List<ProductVariantDTO> variantDTOs = product.getProductVariants().stream()
                .map(this::convertToProductVariantDTO)
                .collect(Collectors.toList());
            dto.setVariants(variantDTOs);
        }
        
        // Convert images
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            List<ProductImageDTO> imageDTOs = product.getProductImages().stream()
                .map(this::convertToProductImageDTO)
                .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        }
        
        return dto;
    }
    
    // Handle primary attribute for product
    private void handlePrimaryAttribute(Long productId, Long attributeId) {
        try {
            System.out.println("🔍 DEBUG - Creating primary attribute: productId=" + productId + ", attributeId=" + attributeId);
            
            // Check if primary attribute already exists
            Optional<ProductPrimaryAttribute> existingPrimary = productPrimaryAttributeRepository.findByProductId(productId);
            
            if (existingPrimary.isPresent()) {
                // Update existing
                ProductPrimaryAttribute primary = existingPrimary.get();
                primary.setAttribute(productAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeId)));
                productPrimaryAttributeRepository.save(primary);
                System.out.println("✅ DEBUG - Updated existing primary attribute");
            } else {
                // Create new
                ProductPrimaryAttribute primaryAttribute = new ProductPrimaryAttribute();
                primaryAttribute.setProductId(productId);
                primaryAttribute.setAttribute(productAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new RuntimeException("Attribute not found: " + attributeId)));
                productPrimaryAttributeRepository.save(primaryAttribute);
                System.out.println("✅ DEBUG - Created new primary attribute");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR - Failed to handle primary attribute: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    // Convert ProductVariant to ProductVariantDTO
    private ProductVariantDTO convertToProductVariantDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setStock(variant.getStock());
        
        // Convert variant values
        if (variant.getProductVariantValues() != null && !variant.getProductVariantValues().isEmpty()) {
            List<ProductVariantValueDTO> valueDTOs = variant.getProductVariantValues().stream()
                .map(this::convertToProductVariantValueDTO)
                .collect(Collectors.toList());
            dto.setVariantValues(valueDTOs);
        }
        
        return dto;
    }
    
    // Convert ProductVariantValue to ProductVariantValueDTO
    private ProductVariantValueDTO convertToProductVariantValueDTO(ProductVariantValue value) {
        ProductVariantValueDTO dto = new ProductVariantValueDTO();
        dto.setId(value.getId());
        dto.setValue(value.getValue());
        dto.setProductAttributeId(value.getProductAttribute().getId());
        
        // Convert product attribute
        if (value.getProductAttribute() != null) {
            AttributeDTO attributeDTO = new AttributeDTO();
            attributeDTO.setId(value.getProductAttribute().getId());
            attributeDTO.setName(value.getProductAttribute().getName());
            dto.setProductAttribute(attributeDTO);
        }
        
        return dto;
    }
    
    // Convert ProductImage to ProductImageDTO
    private ProductImageDTO convertToProductImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setVariantValueName(image.getVariantAttributeValue());
        return dto;
    }

    // 🆕 Handle product images with variant value information
private void handleProductImagesWithVariantValue(Product product, List<ProductImageDTO> imageDTOs) {
    if (imageDTOs == null || imageDTOs.isEmpty()) {
        System.out.println("⚠️ DEBUG - No images to process");
        return;
    }
    
    System.out.println("🔍 DEBUG - Processing " + imageDTOs.size() + " images with variant value info");
    
    try {
        List<ProductImage> savedImages = new ArrayList<>();
        
        for (int i = 0; i < imageDTOs.size(); i++) {
            ProductImageDTO imageDTO = imageDTOs.get(i);
            
            System.out.println("🔍 DEBUG - Processing image " + (i+1) + ": " + imageDTO.getImageUrl() + 
                " (variant value: " + imageDTO.getVariantValueName() + ")");
            
            // Validation
            if (imageDTO.getImageUrl() == null || imageDTO.getImageUrl().trim().isEmpty()) {
                throw new RuntimeException("Image URL không được để trống cho image " + (i+1));
            }
            
            // Tạo ProductImage entity
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(imageDTO.getImageUrl().trim());
            productImage.setImageType(imageDTO.getVariantValueName() != null ? "VARIANT" : "GALLERY");
            productImage.setVariantAttributeValue(imageDTO.getVariantValueName() != null ? imageDTO.getVariantValueName().trim() : null);
            productImage.setUploadedAt(java.time.LocalDateTime.now());
            
            // 🔧 QUAN TRỌNG: Save vào database
            try {
                productImage = productImageRepository.save(productImage);
                savedImages.add(productImage);
                
                System.out.println("✅ DEBUG - Saved product image to database: ID=" + productImage.getId() + 
                    ", URL=" + productImage.getImageUrl() + 
                    ", VariantValue=" + productImage.getVariantAttributeValue() +
                    ", ProductId=" + product.getId());
                
            } catch (Exception saveEx) {
                System.err.println("❌ ERROR - Failed to save product image: " + saveEx.getMessage());
                throw new RuntimeException("Lỗi khi lưu product image vào database: " + saveEx.getMessage(), saveEx);
            }
        }
        
        // 🔧 Cập nhật relationship trong memory
        product.setProductImages(savedImages);
        
        System.out.println("✅ DEBUG - Successfully processed " + savedImages.size() + " product images");
        
    } catch (Exception e) {
        System.err.println("❌ ERROR in handleProductImagesWithVariantValue: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi xử lý product images: " + e.getMessage(), e);
    }
}

// 🆕 Handle product images (backward compatibility - chỉ URL)
private void handleProductImages(Product product, List<String> imageUrls) {
    if (imageUrls != null && !imageUrls.isEmpty()) {
        for (String imageUrl : imageUrls) {
            ProductImage image = new ProductImage();
            image.setImageUrl(imageUrl);
            image.setImageType("GALLERY"); // Default to gallery image
            image.setVariantAttributeValue(null); // No variant attribute for simple images
            image.setUploadedAt(java.time.LocalDateTime.now());
            image.setProduct(product); // 🔧 THÊM DÒNG NÀY
            productImageRepository.save(image); // 🔧 THÊM DÒNG NÀY
        }
    }
}

// 🆕 Handle product variants
private void handleProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
    if (variantDTOs == null || variantDTOs.isEmpty()) return;
    
    System.out.println("🔍 DEBUG - handleProductVariants: Processing " + variantDTOs.size() + " variants");
    
    try {
        for (int i = 0; i < variantDTOs.size(); i++) {
            ProductVariantDTO variantDTO = variantDTOs.get(i);
            System.out.println("🔍 DEBUG - Processing variant " + (i+1) + "/" + variantDTOs.size() + ": " + variantDTO.getSku());
            
            // Null checks
            if (variantDTO.getSku() == null || variantDTO.getSku().trim().isEmpty()) {
                throw new RuntimeException("SKU không được để trống cho variant " + (i+1));
            }
            
            if (variantDTO.getPrice() == null) {
                throw new RuntimeException("Giá variant không được để trống cho variant " + (i+1));
            }
            
            if (variantDTO.getStock() == null) {
                throw new RuntimeException("Số lượng tồn kho không được để trống cho variant " + (i+1));
            }
            
            // Check if SKU already exists
            if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                throw new RuntimeException("SKU đã tồn tại: " + variantDTO.getSku());
            }
            
            try {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);  
                variant.setSku(variantDTO.getSku());
                variant.setPrice(variantDTO.getPrice());
                variant.setStock(variantDTO.getStock());
                
                // Save variant trước
                variant = productVariantRepository.save(variant);
                System.out.println("✅ DEBUG - Saved variant " + (i+1) + " with ID: " + variant.getId());
                
                // Handle variant values if exists
                if (variantDTO.getVariantValues() != null && !variantDTO.getVariantValues().isEmpty()) {
                    System.out.println("🔍 DEBUG - Processing " + variantDTO.getVariantValues().size() + " variant values for variant " + (i+1));
                    handleVariantValues(variant, variantDTO.getVariantValues());
                    System.out.println("✅ DEBUG - Completed variant values for variant " + (i+1));
                }
                
                System.out.println("✅ DEBUG - Successfully processed variant " + (i+1) + ": " + variantDTO.getSku());
                
            } catch (Exception variantEx) {
                System.err.println("❌ Error processing variant " + (i+1) + " (" + variantDTO.getSku() + "): " + variantEx.getMessage());
                variantEx.printStackTrace();
                throw new RuntimeException("Lỗi khi xử lý variant " + (i+1) + " (" + variantDTO.getSku() + "): " + variantEx.getMessage(), variantEx);
            }
        }
        
        System.out.println("✅ DEBUG - Successfully processed all " + variantDTOs.size() + " variants");
        
    } catch (Exception e) {
        System.err.println("❌ Error in handleProductVariants: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi thêm biến thể sản phẩm: " + e.getMessage());
    }
}

// Method để xử lý variant values và lưu vào database
private void handleVariantValues(ProductVariant variant, List<ProductVariantValueDTO> valueDTOs) {
    if (valueDTOs == null || valueDTOs.isEmpty()) {
        System.out.println("⚠️ DEBUG - No variant values to process for variant: " + variant.getSku());
        return;
    }
    
    System.out.println("🔍 DEBUG - Processing " + valueDTOs.size() + " variant values for variant: " + variant.getSku());
    
    try {
        List<ProductVariantValue> savedVariantValues = new ArrayList<>();
        
        for (int i = 0; i < valueDTOs.size(); i++) {
            ProductVariantValueDTO valueDTO = valueDTOs.get(i);
            System.out.println("🔍 DEBUG - Processing variant value " + (i+1) + ": " + valueDTO.getValue() + 
                " (attributeId: " + valueDTO.getProductAttributeId() + ")");
            
            // Validation
            if (valueDTO.getProductAttributeId() == null) {
                throw new RuntimeException("Product Attribute ID không được để trống cho variant value " + (i+1));
            }
            
            if (valueDTO.getValue() == null || valueDTO.getValue().trim().isEmpty()) {
                throw new RuntimeException("Giá trị thuộc tính không được để trống cho variant value " + (i+1));
            }
            
            // Tìm product attribute
            ProductAttribute attribute = productAttributeRepository.findById(valueDTO.getProductAttributeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + valueDTO.getProductAttributeId()));
            
            System.out.println("✅ DEBUG - Found attribute: " + attribute.getName());
            
            // Tạo variant value
            ProductVariantValue variantValue = new ProductVariantValue();
            variantValue.setVariant(variant);
            variantValue.setProductAttribute(attribute);
            variantValue.setValue(valueDTO.getValue().trim());
            
            // 🔧 QUAN TRỌNG: Save vào database
            try {
                variantValue = productVariantValueRepository.save(variantValue);
                savedVariantValues.add(variantValue);
                
                System.out.println("✅ DEBUG - Saved variant value to database: ID=" + variantValue.getId() + 
                    ", Value=" + variantValue.getValue() + 
                    ", Attribute=" + attribute.getName() +
                    ", VariantId=" + variant.getId());
                
            } catch (Exception saveEx) {
                System.err.println("❌ ERROR - Failed to save variant value: " + saveEx.getMessage());
                throw new RuntimeException("Lỗi khi lưu variant value vào database: " + saveEx.getMessage(), saveEx);
            }
        }
        
        // 🔧 Cập nhật relationship trong memory
        variant.setProductVariantValues(savedVariantValues);
        
        System.out.println("✅ DEBUG - Successfully processed " + savedVariantValues.size() + 
            " variant values for variant: " + variant.getSku());
        
    } catch (Exception e) {
        System.err.println("❌ ERROR in handleVariantValues for variant " + variant.getSku() + ": " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi xử lý variant values cho variant " + variant.getSku() + ": " + e.getMessage(), e);
    }
}
 // Lấy sản phẩm của shop của user hiện tại (có phân trang)
    public Page<ProductDTO> getMyShopProducts(Authentication authentication, Boolean isActive, Pageable pageable) {
        if (authentication == null) {
            throw new RuntimeException("Authentication required");
        }

        String username = authentication.getName();
        
        // Tìm user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Tìm shop của user
        Shop shop = shopRepository.findByOwnerId(user.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for user: " + username));

        Page<Product> products;
        if (isActive != null) {
            products = productRepository.findByShopIdAndIsActive(shop.getId(), isActive, pageable);
        } else {
            products = productRepository.findByShopId(shop.getId(), pageable);
        }

        return products.map(this::convertToProductDTO);
    }

     // 🔧 Lấy sản phẩm đã duyệt với thông tin đơn giản
    public Page<ProductDTO> getMyShopApprovedProducts(Authentication authentication, Pageable pageable) {
        if (authentication == null) {
            throw new RuntimeException("Authentication required");
        }

        String username = authentication.getName();
        System.out.println("🔍 DEBUG - Getting approved products for: " + username);
        
        // Tìm user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Tìm shop của user  
        Shop shop = shopRepository.findByOwnerId(user.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for user: " + username));

        System.out.println("🔍 DEBUG - Shop found: " + shop.getName());

        // Chỉ lấy sản phẩm đã duyệt (is_active = true)
        Page<Product> products = productRepository.findByShopIdAndIsActive(shop.getId(), true, pageable);
        
        System.out.println("🔍 DEBUG - Found " + products.getTotalElements() + " approved products");

        // Convert sang ProductSimpleDTO
        return products.map(this::convertToProductDTO);
    }

    /**
     * Sync product rating if DB value doesn't match actual reviews
     */
    @Transactional
    private void syncProductRatingIfNeeded(Product product) {
        try {
            // Get actual rating from reviews
            Double averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
            long actualReviewCount = productReviewRepository.countByProductId(product.getId());
            
            BigDecimal currentRating = product.getRating() != null ? product.getRating() : BigDecimal.ZERO;
            int currentReviewCount = product.getReviewCount() != null ? product.getReviewCount() : 0;
            
            // Calculate expected rating
            BigDecimal expectedRating = (averageRating != null && averageRating > 0) 
                ? BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            // Check if sync needed (rating mismatch or review count mismatch)
            boolean needsSync = !currentRating.equals(expectedRating) || currentReviewCount != actualReviewCount;
            
            if (needsSync) {
                product.setRating(expectedRating);
                product.setReviewCount((int) actualReviewCount);
                product.setUpdatedAt(java.time.LocalDateTime.now());
                productRepository.save(product);
                
                log.info("🔄 Synced product {} rating: {} → {} (reviews: {} → {})", 
                    product.getId(), currentRating, expectedRating, currentReviewCount, actualReviewCount);
            }
        } catch (Exception e) {
            log.warn("Failed to sync rating for product {}: {}", product.getId(), e.getMessage());
        }
    }

    /**
     * Update product rating and review count based on actual reviews
     * Called automatically when reviews are created/updated/deleted
     */
    @Transactional
    public void updateProductRating(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
            
            // Get actual average rating from reviews
            Double averageRating = productReviewRepository.getAverageRatingByProductId(productId);
            long reviewCount = productReviewRepository.countByProductId(productId);
            
            // Update product with real data
            if (averageRating != null && averageRating > 0) {
                product.setRating(BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP));
            } else {
                product.setRating(BigDecimal.ZERO);
            }
            
            product.setReviewCount((int) reviewCount);
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            productRepository.save(product);
            
            log.info("Updated product {} rating to {} (from {} reviews)", 
                productId, product.getRating(), reviewCount);
            
        } catch (Exception e) {
            log.error("Error updating product rating for product {}", productId, e);
            // Don't throw exception to avoid breaking review operations
        }
    }
    
    /**
     * Sync all products ratings on application startup
     * This ensures existing data in DB is accurate
     */
    @org.springframework.context.event.EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
    @Transactional
    public void syncAllProductRatingsOnStartup() {
        try {
            log.info("🚀 Starting bulk product rating sync...");
            
            List<Product> allProducts = productRepository.findAll();
            int syncedCount = 0;
            
            for (Product product : allProducts) {
                Double averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
                long reviewCount = productReviewRepository.countByProductId(product.getId());
                
                BigDecimal expectedRating = (averageRating != null && averageRating > 0)
                    ? BigDecimal.valueOf(averageRating).setScale(2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                
                BigDecimal currentRating = product.getRating() != null ? product.getRating() : BigDecimal.ZERO;
                int currentReviewCount = product.getReviewCount() != null ? product.getReviewCount() : 0;
                
                // Update if mismatch
                if (!currentRating.equals(expectedRating) || currentReviewCount != reviewCount) {
                    product.setRating(expectedRating);
                    product.setReviewCount((int) reviewCount);
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    productRepository.save(product);
                    syncedCount++;
                    
                    log.debug("✅ Synced product {}: rating {} → {}, reviews {} → {}", 
                        product.getId(), currentRating, expectedRating, currentReviewCount, reviewCount);
                }
            }
            
            log.info("✅ Completed rating sync: {}/{} products updated", syncedCount, allProducts.size());
            
        } catch (Exception e) {
            log.error("❌ Error during bulk rating sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Lấy sản phẩm đánh giá cao (top-rated) cho homepage
     * Ưu tiên: reviewCount DESC, rating DESC
     * Filter: rating >= 4.0, reviewCount > 0
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getTopRatedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findTopRatedProducts(pageable);
        return products.map(this::convertToProductDTO);
    }

    /**
     * Lấy sản phẩm bán chạy (best-selling) cho homepage
     * Sort: soldCount DESC
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getBestSellingProducts(Pageable pageable) {
        Page<Product> products = productRepository.findBestSellingProducts(pageable);
        return products.map(this::convertToProductDTO);
    }

    
}