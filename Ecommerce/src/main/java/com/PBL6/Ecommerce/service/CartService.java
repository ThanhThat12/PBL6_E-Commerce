package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Verification;
import com.PBL6.Ecommerce.domain.Role;
import com.PBL6.Ecommerce.domain.dto.CheckContactDTO;
import com.PBL6.Ecommerce.domain.dto.VerifyOtpDTO;
import com.PBL6.Ecommerce.domain.dto.RegisterDTO;
import com.PBL6.Ecommerce.domain.dto.UserInfoDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.repository.VerificationRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.domain.Product;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(User user, Long productId, int quantity) {
        Cart cart = getCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(0);
                    return item;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);
        return cart;
    }

    public Cart updateQuantity(User user, Long productId, int quantity) {
        Cart cart = getCart(user);
        CartItem item = cartItemRepository.findByCartAndProduct(cart,
                        productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found")))
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return cart;
    }

    public Cart removeFromCart(User user, Long productId) {
        Cart cart = getCart(user);
        CartItem item = cartItemRepository.findByCartAndProduct(cart,
                        productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found")))
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        cartItemRepository.delete(item);
        return cart;
    }

    // Đã xoá code liên quan đến CartDTO, CartItemDTO
}