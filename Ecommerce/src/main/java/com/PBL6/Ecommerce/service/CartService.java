package com.PBL6.Ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.ProductVariant;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.repository.CartItemRepository;
import com.PBL6.Ecommerce.repository.CartRepository;
import com.PBL6.Ecommerce.repository.ProductVariantRepository;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Lấy hoặc tạo cart mới cho user
     */
    public Cart getCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Thêm product variant vào giỏ hàng
     */
    @Transactional
    public Cart addToCart(User user, Long productVariantId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getCart(user);
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new RuntimeException("Product variant không tồn tại"));

        // Kiểm tra tồn kho
        if (productVariant.getStock() < quantity) {
            throw new RuntimeException("Sản phẩm không đủ tồn kho. Tồn kho hiện tại: " + productVariant.getStock());
        }

        // Tìm hoặc tạo mới cart item
        CartItem cartItem = cartItemRepository.findByCartAndProductVariant(cart, productVariant)
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProductVariant(productVariant);
                    item.setProductId(productVariant.getProduct().getId());
                    item.setQuantity(0);
                    return item;
                });

        // Cập nhật số lượng
        int newQuantity = cartItem.getQuantity() + quantity;
        
        // Kiểm tra lại tồn kho với số lượng mới
        if (productVariant.getStock() < newQuantity) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + productVariant.getStock());
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        
        return cart;
    }

    /**
     * Cập nhật số lượng product variant trong giỏ hàng
     */
    @Transactional
    public Cart updateQuantity(User user, Long productVariantId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getCart(user);
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new RuntimeException("Product variant không tồn tại"));

        CartItem item = cartItemRepository.findByCartAndProductVariant(cart, productVariant)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        // Kiểm tra tồn kho
        if (productVariant.getStock() < quantity) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + productVariant.getStock());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        
        return cart;
    }

    /**
     * Xóa product variant khỏi giỏ hàng
     */
    @Transactional
    public Cart removeFromCart(User user, Long productVariantId) {
        Cart cart = getCart(user);
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new RuntimeException("Product variant không tồn tại"));

        CartItem item = cartItemRepository.findByCartAndProductVariant(cart, productVariant)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        cartItemRepository.delete(item);
        
        return cart;
    }

    /**
     * Cập nhật số lượng theo cart item id
     */
    @Transactional
    public Cart updateQuantityByCartItemId(User user, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item không tồn tại"));

        // Verify cart belongs to user
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Không có quyền truy cập cart item này");
        }

        // Kiểm tra tồn kho
        if (item.getProductVariant().getStock() < quantity) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + item.getProductVariant().getStock());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        
        return cart;
    }

    /**
     * Xóa cart item theo id
     */
    @Transactional
    public Cart removeFromCartByCartItemId(User user, Long cartItemId) {
        Cart cart = getCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item không tồn tại"));

        // Verify cart belongs to user
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Không có quyền truy cập cart item này");
        }

        cartItemRepository.delete(item);
        
        return cart;
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCart(user);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    /**
     * Lấy tổng số sản phẩm trong giỏ hàng
     */
    public int getCartItemCount(User user) {
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return 0;
        }
        return cartItemRepository.findByCartId(cart.getId())
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}