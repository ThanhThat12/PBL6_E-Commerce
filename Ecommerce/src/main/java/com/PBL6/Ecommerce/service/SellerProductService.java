package com.PBL6.Ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Category;
import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductAttribute;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.ProductVariantValue;
import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.VariantImage;
import com.PBL6.Ecommerce.domain.dto.ProductCreateDTO;
import com.PBL6.Ecommerce.domain.dto.ProductDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantDTO;
import com.PBL6.Ecommerce.domain.dto.ProductVariantValueDTO;
import com.PBL6.Ecommerce.repository.CategoryRepository;
import com.PBL6.Ecommerce.repository.ProductAttributeRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.ProductVariantValueRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VariantImageRepository;

/**
 * Seller Product Service - Phase 2
 * Service methods for seller product management with filtering
 */
@Service
public class SellerProductService {
    
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantValueRepository variantValueRepository;
    private final ProductAttributeRepository attributeRepository;
    private final VariantImageRepository variantImageRepository;
    
    public SellerProductService(ProductRepository productRepository,
                               UserRepository userRepository,
                               ShopRepository shopRepository,
                               CategoryRepository categoryRepository,
                               ProductVariantRepository variantRepository,
                               ProductVariantValueRepository variantValueRepository,
                               ProductAttributeRepository attributeRepository,
                               VariantImageRepository variantImageRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.variantValueRepository = variantValueRepository;
        this.attributeRepository = attributeRepository;
        this.variantImageRepository = variantImageRepository;
    }
    
    /**
     * Create new product for seller (status = false, pending approval)
     */
    @Transactional
    public ProductDTO createSellerProduct(ProductCreateDTO request, Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        // Validate category
        if (request.getCategoryId() == null) {
            throw new RuntimeException("Category ID không được để trống");
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
        
        // Create product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setMainImage(request.getMainImage());
        product.setCategory(category);
        product.setShop(shop);
        
        // Set shipping dimensions
        product.setWeightGrams(request.getWeightGrams());
        product.setLengthCm(request.getLengthCm());
        product.setWidthCm(request.getWidthCm());
        product.setHeightCm(request.getHeightCm());
        
        // ✅ AUTO-APPROVAL: Tạm thời tất cả sản phẩm được duyệt tự động
        product.setIsActive(true); // Changed from false - auto approval enabled
        
        Product savedProduct = productRepository.save(product);
        
        // ✅ Create product variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantDTO variantDTO : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSku(variantDTO.getSku());
                variant.setPrice(variantDTO.getPrice());
                variant.setStock(variantDTO.getStock());
                
                ProductVariant savedVariant = variantRepository.save(variant);
                
                // Create variant values (color, size, material, etc.)
                if (variantDTO.getVariantValues() != null) {
                    for (ProductVariantValueDTO valueDTO : variantDTO.getVariantValues()) {
                        ProductAttribute attribute = attributeRepository.findById(valueDTO.getProductAttributeId())
                            .orElseThrow(() -> new RuntimeException("Attribute not found: " + valueDTO.getProductAttributeId()));
                        
                        ProductVariantValue variantValue = new ProductVariantValue();
                        variantValue.setVariant(savedVariant);
                        variantValue.setProductAttribute(attribute);
                        variantValue.setValue(valueDTO.getValue());
                        
                        variantValueRepository.save(variantValue);
                    }
                }
                
                // ✅ Save variant image if provided
                if (variantDTO.getImageUrl() != null && !variantDTO.getImageUrl().isEmpty()) {
                    VariantImage variantImage = new VariantImage();
                    variantImage.setVariant(savedVariant);
                    variantImage.setImageUrl(variantDTO.getImageUrl());
                    variantImage.setDisplayOrder(0); // Main image for this variant
                    variantImage.setIsActive(true);
                    
                    variantImageRepository.save(variantImage);
                }
            }
        }
        
        return convertToDTO(savedProduct);
    }
    
    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setMainImage(product.getMainImage());
        dto.setIsActive(product.getIsActive());
        
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getShop() != null) {
            dto.setShopName(product.getShop().getName());
        }
        
        return dto;
    }
    
    /**
     * Get seller products with filters (manual filtering using streams)
     */
    public Page<ProductDTO> getSellerProductsWithFilters(
            Authentication authentication, 
            String keyword, 
            Long categoryId, 
            Boolean status,
            Pageable pageable) {
        
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        // Get all products of the shop
        List<Product> allProducts = productRepository.findByShopId(shop.getId());
        
        // Apply filters using stream
        List<Product> filtered = allProducts.stream()
            .filter(product -> {
                // Keyword filter (search in name and description)
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase();
                    boolean matchName = product.getName().toLowerCase().contains(lowerKeyword);
                    boolean matchDesc = product.getDescription() != null && 
                                      product.getDescription().toLowerCase().contains(lowerKeyword);
                    if (!matchName && !matchDesc) return false;
                }
                
                // Category filter
                if (categoryId != null) {
                    if (product.getCategory() == null || 
                        !product.getCategory().getId().equals(categoryId)) return false;
                }
                
                // Status filter
                if (status != null) {
                    if (!product.getIsActive().equals(status)) return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<Product> pagedProducts = start < filtered.size() 
            ? filtered.subList(start, end) 
            : List.of();
        
        // Convert to DTOs
        List<ProductDTO> productDTOs = pagedProducts.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(productDTOs, pageable, filtered.size());
    }
    
    /**
     * Get seller product by ID with ownership verification
     */
    public ProductDTO getSellerProductById(Long productId, Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Verify ownership
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You don't have permission to access this product");
        }
        
        return convertToDTO(product);
    }
    
    /**
     * Update seller product with ownership verification
     */
    @Transactional
    public ProductDTO updateSellerProduct(
            Long productId, 
            ProductCreateDTO request, 
            Authentication authentication) {
        
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Verify ownership
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You don't have permission to update this product");
        }
        
        // Update product fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }
    
    /**
     * Toggle product active status
     */
    @Transactional
    public ProductDTO toggleSellerProductStatus(
            Long productId, 
            Boolean status, 
            Authentication authentication) {
        
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Verify ownership
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You don't have permission to update this product");
        }
        
        product.setIsActive(status);
        
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Delete seller product with ownership verification
     */
    @Transactional
    public void deleteSellerProduct(Long productId, Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        Shop shop = shopRepository.findByOwnerId(seller.getId())
            .orElseThrow(() -> new RuntimeException("Shop not found for seller"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Verify ownership
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("You don't have permission to delete this product");
        }
        
        // Delete the product (cascading should handle variants and variant values)
        productRepository.delete(product);
    }
}
