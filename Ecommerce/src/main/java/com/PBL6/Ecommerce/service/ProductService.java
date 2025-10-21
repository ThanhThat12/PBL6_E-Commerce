package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.*;
import com.PBL6.Ecommerce.domain.dto.*;
import com.PBL6.Ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
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
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToProductDTO(product);
    }

    // Tạo sản phẩm mới với kiểm tra quyền
    public ProductDTO createProduct(ProductCreateDTO request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Kiểm tra category tồn tại
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
        
        Shop shop;
        
        // Nếu là ADMIN, có thể tạo sản phẩm cho bất kỳ shop nào
        if (isAdmin(authentication)) {
            shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + request.getShopId()));
        } 
        // Nếu là SELLER, chỉ có thể tạo sản phẩm cho shop của mình
        else if (isSeller(authentication)) {
            shop = currentUser.getShop();
            if (shop == null) {
                throw new RuntimeException("Seller chưa có cửa hàng");
            }
            // Ghi đè shopId từ request bằng shop của seller
            request.setShopId(shop.getId());
        } else {
            throw new RuntimeException("Không có quyền tạo sản phẩm");
        }
        
        // Tạo sản phẩm
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setIsActive(request.getIsActive());
        product.setMainImage(request.getMainImage());
        product.setCategory(category);
        product.setShop(shop);
        
        // Lưu sản phẩm
        product = productRepository.save(product);
        
        // Tạo variants và images nếu có
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = createProductVariants(product, request.getVariants());
            product.setProductVariants(variants);
        }
        
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = createProductImages(product, request.getImageUrls());
            product.setProductImages(images);
        }
        
        return convertToProductDTO(product);
    }
    
    // Lấy tất cả sản phẩm cho quản lý (Admin xem tất cả, Seller xem của mình)
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProductsForManagement(Pageable pageable, Authentication authentication) {
        Page<Product> products;
        
        if (isAdmin(authentication)) {
            // Admin xem tất cả sản phẩm
            products = productRepository.findAll(pageable);
        } else if (isSeller(authentication)) {
            // Seller chỉ xem sản phẩm của shop mình
            User currentUser = getCurrentUser(authentication);
            Shop shop = currentUser.getShop();
            if (shop == null) {
                throw new RuntimeException("Seller chưa có cửa hàng");
            }
            products = productRepository.findByShopId(shop.getId(), pageable);
        } else {
            throw new RuntimeException("Không có quyền truy cập");
        }
        
        return products.map(this::convertToProductDTO);
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
    
    // Cập nhật sản phẩm với kiểm tra quyền
    public ProductDTO updateProduct(Long id, ProductCreateDTO request, Authentication authentication) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Kiểm tra quyền sở hữu
        if (!canModifyProduct(product, authentication)) {
            throw new RuntimeException("Không có quyền chỉnh sửa sản phẩm này");
        }
        
        // Cập nhật thông tin
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setIsActive(request.getIsActive());
        product.setMainImage(request.getMainImage());
        
        // Cập nhật category nếu thay đổi
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            product.setCategory(category);
        }
        
        product = productRepository.save(product);
        return convertToProductDTO(product);
    }
    
    // Xóa sản phẩm với kiểm tra quyền
    public void deleteProduct(Long id, Authentication authentication) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Kiểm tra quyền sở hữu
        if (!canModifyProduct(product, authentication)) {
            throw new RuntimeException("Không có quyền xóa sản phẩm này");
        }
        
        productRepository.delete(product);
    }
    
    // Lấy sản phẩm của seller hiện tại
    @Transactional(readOnly = true)
    public Page<ProductDTO> getSellerProducts(Pageable pageable, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Shop shop = currentUser.getShop();
        if (shop == null) {
            throw new RuntimeException("Seller chưa có cửa hàng");
        }
        
        Page<Product> products = productRepository.findByShopId(shop.getId(), pageable);
        return products.map(this::convertToProductDTO);
    }
    
    // Lấy danh sách sản phẩm của seller hiện tại (không phân trang)
    @Transactional(readOnly = true)
    public List<ProductDTO> getSellerProductsList(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Shop shop = currentUser.getShop();
        if (shop == null) {
            throw new RuntimeException("Seller chưa có cửa hàng");
        }
        
        return productRepository.findByShopId(shop.getId())
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }
    
    // Thay đổi trạng thái sản phẩm
    public ProductDTO toggleProductStatus(Long id, Authentication authentication) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Kiểm tra quyền sở hữu
        if (!canModifyProduct(product, authentication)) {
            throw new RuntimeException("Không có quyền thay đổi trạng thái sản phẩm này");
        }
        
        product.setIsActive(!product.getIsActive());
        product = productRepository.save(product);
        
        return convertToProductDTO(product);
    }
    
    // Thêm sản phẩm đơn giản (cho admin hoặc không cần authentication)
    public ProductDTO addProduct(ProductCreateDTO dto) {
        // Kiểm tra category tồn tại
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
        
        // Kiểm tra shop tồn tại
        Shop shop = shopRepository.findById(dto.getShopId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng với ID: " + dto.getShopId()));
        
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        product.setMainImage(dto.getMainImage());
        product.setCategory(category);
        product.setShop(shop);
        
        Product saved = productRepository.save(product);
        return convertToProductDTO(saved);
    }
    
    // Kiểm tra quyền sở hữu sản phẩm (để dùng trong @PreAuthorize)
    public boolean isProductOwner(Long productId, String username) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            
            User user = userRepository.findByEmail(username).orElse(null);
            if (user == null) return false;
            
            // Kiểm tra nếu user có shop và shop đó sở hữu sản phẩm
            if (user.getShop() == null) return false;
            
            return product.getShop().getId().equals(user.getShop().getId());
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
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
            Shop userShop = currentUser.getShop();
            return userShop != null && product.getShop().getId().equals(userShop.getId());
        }
        
        return false;
    }
    
    // Tạo product variants
    private List<ProductVariant> createProductVariants(Product product, List<ProductVariantDTO> variantDTOs) {
        List<ProductVariant> variants = new ArrayList<>();
        
        for (ProductVariantDTO variantDTO : variantDTOs) {
            // Kiểm tra SKU đã tồn tại
            if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                throw new RuntimeException("SKU đã tồn tại: " + variantDTO.getSku());
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + valueDTO.getProductAttributeId()));
            
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
}