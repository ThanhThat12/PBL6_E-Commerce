// ...existing code...
package com.PBL6.Ecommerce.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.entity.cart.Cart;
import com.PBL6.Ecommerce.domain.entity.cart.CartItem;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.product.ProductVariant;
import com.PBL6.Ecommerce.domain.entity.user.User;
import com.PBL6.Ecommerce.domain.dto.CartDTO;
import com.PBL6.Ecommerce.domain.dto.CartItemDTO;
import com.PBL6.Ecommerce.exception.CartItemNotFoundException;
import com.PBL6.Ecommerce.exception.ProductNotFoundException;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserService userService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductVariantRepository productVariantRepository,
                       UserService userService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userService = userService;
    }

    public Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
    }

    public CartDTO getCartDtoForUser(User user) {
        Cart cart = getOrCreateCartForUser(user);
        return toDto(cart);
    }

    // NOTE: use variantId (not productId) because stock stored in ProductVariant
    public CartDTO addItem(User user, Long variantId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductNotFoundException("Product variant not found with ID: " + variantId));
        
        // Prevent shop owner from buying their own products
        Product product = variant.getProduct();
        if (product.getShop() != null && product.getShop().getOwner() != null) {
            if (product.getShop().getOwner().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Bạn không thể mua sản phẩm của chính shop bạn");
            }
        }
        
        if (!Boolean.TRUE.equals(variant.getProduct().getIsActive())) {
            throw new IllegalStateException("Product is not active");
        }
        if (variant.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock. Available: " + variant.getStock());
        }

        Cart cart = getOrCreateCartForUser(user);
        Optional<CartItem> opt = cartItemRepository.findByCartAndProductVariant(cart, variant);
        CartItem item;
        if (opt.isPresent()) {
            item = opt.get();
            int newQty = item.getQuantity() + quantity;
            if (variant.getStock() < newQty) {
                throw new IllegalArgumentException("Not enough stock. Available: " + variant.getStock() + ", requested: " + newQty);
            }
            item.setQuantity(newQty);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartDTO updateItem(User user, Long itemId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Cart cart = getOrCreateCartForUser(user);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId() != null && ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with ID: " + itemId));
        ProductVariant variant = item.getProductVariant();
        if (variant.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock. Available: " + variant.getStock());
        }
        item.setQuantity(quantity);
        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartDTO removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCartForUser(user);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId() != null && ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with ID: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartDTO clearCart(User user) {
        Cart cart = getOrCreateCartForUser(user);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toDto(cart);
    }

    private CartDTO toDto(Cart cart) {
        var items = cart.getItems().stream().map(ci -> {
            ProductVariant variant = ci.getProductVariant();
            Product p = variant.getProduct();
            
            // Calculate unit price
            BigDecimal unit = variant.getPrice() != null ? variant.getPrice() :
                             (p.getBasePrice() != null ? p.getBasePrice() : BigDecimal.ZERO);
            
            // Calculate subtotal
            BigDecimal sub = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));
            
            // Get variant attributes
            java.util.List<com.PBL6.Ecommerce.domain.dto.AttributeDTO> attributes = new java.util.ArrayList<>();
            if (variant.getProductVariantValues() != null && !variant.getProductVariantValues().isEmpty()) {
                attributes = variant.getProductVariantValues().stream()
                        .map(pvv -> {
                            com.PBL6.Ecommerce.domain.dto.AttributeDTO attr = new com.PBL6.Ecommerce.domain.dto.AttributeDTO();
                            if (pvv.getProductAttribute() != null) {
                                attr.setName(pvv.getProductAttribute().getName() + ": " + pvv.getValue());
                            } else {
                                attr.setName(pvv.getValue());
                            }
                            return attr;
                        })
                        .collect(Collectors.toList());
            }
            
            // Get shop ID and name from product
            Long shopId = (p.getShop() != null) ? p.getShop().getId() : null;
            String shopName = (p.getShop() != null) ? p.getShop().getName() : null;
            
            // Build full CartItemDTO with all information
            return new CartItemDTO(
                ci.getId(),                    // Cart item ID
                variant.getId(),               // Variant ID
                p.getId(),                     // Product ID
                shopId,                        // Shop ID
                shopName,                      // Shop name
                p.getName(),                   // Product name
                p.getMainImage(),              // Product image URL
                variant.getSku(),              // SKU
                attributes,                    // Variant attributes (Size: L, Color: Red)
                unit,                          // Unit price
                ci.getQuantity(),              // Quantity
                variant.getStock(),            // Stock available
                sub                            // Subtotal
            );
        }).collect(Collectors.toList());
        
        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDTO(cart.getId(), items, total);
    }
    
    // ===== Authentication-based methods =====
    
    /**
     * Get cart for authenticated user
     */
    public CartDTO getCartDtoForUser(Authentication authentication) {
        User user = userService.resolveCurrentUser(authentication);
        return getCartDtoForUser(user);
    }
    
    /**
     * Add item to cart for authenticated user
     */
    public CartDTO addItem(Authentication authentication, Long variantId, int quantity) {
        User user = userService.resolveCurrentUser(authentication);
        return addItem(user, variantId, quantity);
    }
    
    /**
     * Update cart item for authenticated user
     */
    public CartDTO updateItem(Authentication authentication, Long itemId, int quantity) {
        User user = userService.resolveCurrentUser(authentication);
        return updateItem(user, itemId, quantity);
    }
    
    /**
     * Remove cart item for authenticated user
     */
    public CartDTO removeItem(Authentication authentication, Long itemId) {
        User user = userService.resolveCurrentUser(authentication);
        return removeItem(user, itemId);
    }
    
    /**
     * Clear cart for authenticated user
     */
    public CartDTO clearCart(Authentication authentication) {
        User user = userService.resolveCurrentUser(authentication);
        return clearCart(user);
    }
    
}