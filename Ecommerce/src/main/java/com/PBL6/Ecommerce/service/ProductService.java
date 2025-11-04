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

import com.PBL6.Ecommerce.domain.Category;
import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductAttribute;
import com.PBL6.Ecommerce.domain.ProductImage;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.ProductVariantValue;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AttributeDTO;
import com.PBL6.Ecommerce.domain.dto.CategoryDTO;
import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ProductImageDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantValueDTO;
import com.PBL6.Ecommerce.exception.CategoryNotFoundException;
import com.PBL6.Ecommerce.exception.DuplicateSKUException;
import com.PBL6.Ecommerce.exception.InvalidProductDataException;
import com.PBL6.Ecommerce.exception.ProductHasReferencesException;
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

    // Lấy tất cả sản phẩm
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToProductDTO(product);
    }

// Thay thế method createProduct (dòng 61-104)
public ProductDTO createProduct(ProductCreateDTO request, Authentication authentication) {
    System.out.println("🔍 DEBUG - Starting createProduct");
    System.out.println("🔍 DEBUG - Request: " + request.getName());
    System.out.println("🔍 DEBUG - Has variants: " + (request.getVariants() != null ? request.getVariants().size() : 0));
    System.out.println("🔍 DEBUG - Has images: " + (request.getImageUrls() != null ? request.getImageUrls().size() : 0));
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
    product.setMainImage(request.getMainImage());
    product.setCategory(category);
    product.setShop(shop);
    
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
    
    // 🔧 3. Handle images SAU KHI product đã có ID
    if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
        handleProductImages(product, request.getImageUrls());
    }
    
    // 🔧 4. Handle variants SAU KHI product đã có ID  
    if (request.getVariants() != null && !request.getVariants().isEmpty()) {
        handleProductVariants(product, request.getVariants());
    }
    
    // 🔧 5. Reload product để có relationships mới
    product = productRepository.findById(product.getId()).orElse(product);
    
    return convertToProductDTO(product);
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
                                               Pageable pageable) {
        Page<Product> products = productRepository.findProductsWithFilters(
            name, categoryId, shopId, true, minPrice, maxPrice, pageable);
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
        
        // Convert category
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }
        
        // Set shop name (theo ProductDTO hiện tại)
        if (product.getShop() != null) {
            dto.setShopName(product.getShop().getName());
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
        dto.setColor(image.getColor());
        return dto;
    }

    // 🆕 Handle product images
private void handleProductImages(Product product, List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Clear existing images
            product.getProductImages().clear();
            
            // Add new images
            for (String imageUrl : imageUrls) {
                ProductImage image = new ProductImage();
                image.setImageUrl(imageUrl);
                image.setColor(null); // Hoặc lấy từ request nếu có
                product.addProductImage(image);
            }
        }
    }

// 🆕 Handle product variants
private void handleProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
    if (variantDTOs == null || variantDTOs.isEmpty()) return;
    
    try {
        for (ProductVariantDTO variantDTO : variantDTOs) {
            // Null checks
            if (variantDTO.getSku() == null || variantDTO.getSku().trim().isEmpty()) {
                throw new RuntimeException("SKU không được để trống");
            }
            
            if (variantDTO.getPrice() == null) {
                throw new RuntimeException("Giá variant không được để trống");
            }
            
            if (variantDTO.getStock() == null) {
                throw new RuntimeException("Số lượng tồn kho không được để trống");
            }
            
            // Check if SKU already exists
            if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                throw new RuntimeException("SKU đã tồn tại: " + variantDTO.getSku());
            }
            
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);  
            variant.setSku(variantDTO.getSku());
            variant.setPrice(variantDTO.getPrice());
            variant.setStock(variantDTO.getStock());
            
            // Save variant trước
            variant = productVariantRepository.save(variant);
            System.out.println("🔍 DEBUG - Saved variant: " + variantDTO.getSku());
            
            // Handle variant values if exists
            if (variantDTO.getVariantValues() != null && !variantDTO.getVariantValues().isEmpty()) {
                handleVariantValues(variant, variantDTO.getVariantValues());
            }
            
            // Add variant to product
            product.addVariant(variant);
        }
    } catch (Exception e) {
        System.err.println("❌ Error in handleProductVariants: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi thêm biến thể sản phẩm: " + e.getMessage());
    }
}

// Sửa method handleVariantValues (dòng 169-183)
private void handleVariantValues(ProductVariant variant, List<ProductVariantValueDTO> valueDTOs) {
    try {
        for (ProductVariantValueDTO valueDTO : valueDTOs) {
            // Null checks
            if (valueDTO.getProductAttributeId() == null) {
                throw new RuntimeException("Product Attribute ID không được để trống");
            }
            
            if (valueDTO.getValue() == null || valueDTO.getValue().trim().isEmpty()) {
                throw new RuntimeException("Giá trị thuộc tính không được để trống");
            }
            
            // Validate product attribute exists
            ProductAttribute attribute = productAttributeRepository.findById(valueDTO.getProductAttributeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + valueDTO.getProductAttributeId()));
            
            ProductVariantValue variantValue = new ProductVariantValue();
            variantValue.setVariant(variant);
            variantValue.setProductAttribute(attribute);
            variantValue.setValue(valueDTO.getValue().trim());

            // Kiểm tra repository có tồn tại không
            if (productVariantValueRepository == null) {
                throw new RuntimeException("ProductVariantValueRepository chưa được inject");
            }
            
            productVariantValueRepository.save(variantValue);
            System.out.println("🔍 DEBUG - Saved variant value: " + valueDTO.getValue());
        }
    } catch (Exception e) {
        System.err.println("❌ Error in handleVariantValues: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi thêm giá trị thuộc tính: " + e.getMessage());
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

     // Lấy sản phẩm đã duyệt của shop (có phân trang)
    public Page<ProductDTO> getMyShopApprovedProducts(Authentication authentication, Pageable pageable) {
        if (authentication == null) {
            throw new RuntimeException("Authentication required");
        }

        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Shop shop = shopRepository.findByOwnerId(user.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for user: " + username));

        Page<Product> products = productRepository.findByShopIdAndIsActive(shop.getId(), true, pageable);
        return products.map(this::convertToProductDTO);
    }
}