// ...existing code...
package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.Product;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.CartDTO;
import com.PBL6.Ecommerce.domain.dto.CartItemDTO;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductVariantRepository productVariantRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
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
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
        if (!Boolean.TRUE.equals(variant.getProduct().getIsActive())) throw new RuntimeException("Product is not active");
        if (variant.getStock() < quantity) throw new RuntimeException("Not enough stock");

        Cart cart = getOrCreateCartForUser(user);
        Optional<CartItem> opt = cartItemRepository.findByCartAndProductVariant(cart, variant);
        CartItem item;
        if (opt.isPresent()) {
            item = opt.get();
            int newQty = item.getQuantity() + quantity;
            if (variant.getStock() < newQty) throw new RuntimeException("Not enough stock");
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
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        ProductVariant variant = item.getProductVariant();
        if (variant.getStock() < quantity) throw new RuntimeException("Not enough stock");
        item.setQuantity(quantity);
        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartDTO removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCartForUser(user);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId() != null && ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
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
            BigDecimal unit = variant.getPrice() != null ? variant.getPrice() :
                             (p.getBasePrice() != null ? p.getBasePrice() : BigDecimal.ZERO);
            BigDecimal sub = unit.multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemDTO(ci.getId(), variant.getId(), p.getName(), unit, ci.getQuantity(), sub);
        }).collect(Collectors.toList());
        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDTO(cart.getId(), items, total);
    }
    
}