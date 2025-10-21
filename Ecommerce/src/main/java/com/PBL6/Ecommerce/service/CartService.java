// package com.PBL6.Ecommerce.service;

// import com.PBL6.Ecommerce.repository.CartRepository;
// import com.PBL6.Ecommerce.repository.CartItemRepository;
// import com.PBL6.Ecommerce.repository.ProductRepository;
// import com.PBL6.Ecommerce.repository.UserRepository;
// import com.PBL6.Ecommerce.domain.User;
// import com.PBL6.Ecommerce.domain.Product;
// import com.PBL6.Ecommerce.domain.Cart;
// import com.PBL6.Ecommerce.domain.CartItem;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// @Service
// public class CartService {
//     private final CartRepository cartRepository;
//     private final CartItemRepository cartItemRepository;
//     private final ProductRepository productRepository;
//     private final UserRepository userRepository;

//     public CartService(CartRepository cartRepository,
//                        CartItemRepository cartItemRepository,
//                        ProductRepository productRepository,
//                        UserRepository userRepository) {
//         this.cartRepository = cartRepository;
//         this.cartItemRepository = cartItemRepository;
//         this.productRepository = productRepository;
//         this.userRepository = userRepository;
//     }

//     /**
//      * Lấy hoặc tạo cart mới cho user
//      */
//     public Cart getCart(User user) {
//         return cartRepository.findByUserId(user.getId())
//                 .orElseGet(() -> {
//                     Cart newCart = new Cart();
//                     newCart.setUser(user);
//                     return cartRepository.save(newCart);
//                 });
//     }

//     /**
//      * Thêm sản phẩm vào giỏ hàng
//      */
//     @Transactional
//     public Cart addToCart(User user, Long productId, int quantity) {
//         if (quantity <= 0) {
//             throw new RuntimeException("Số lượng phải lớn hơn 0");
//         }

//         Cart cart = getCart(user);
//         Product product = productRepository.findById(productId)
//                 .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

//         // Kiểm tra tồn kho
//         if (product.getStock() < quantity) {
//             throw new RuntimeException("Sản phẩm không đủ tồn kho");
//         }

//         // Tìm hoặc tạo mới cart item
//         CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
//                 .orElseGet(() -> {
//                     CartItem item = new CartItem();
//                     item.setCart(cart);
//                     item.setProduct(product);
//                     item.setQuantity(0);
//                     return item;
//                 });

//         // Cập nhật số lượng
//         int newQuantity = cartItem.getQuantity() + quantity;
        
//         // Kiểm tra lại tồn kho với số lượng mới
//         if (product.getStock() < newQuantity) {
//             throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + product.getStock());
//         }

//         cartItem.setQuantity(newQuantity);
//         cartItemRepository.save(cartItem);
        
//         return cart;
//     }

//     /**
//      * Cập nhật số lượng sản phẩm trong giỏ hàng
//      */
//     @Transactional
//     public Cart updateQuantity(User user, Long productId, int quantity) {
//         if (quantity <= 0) {
//             throw new RuntimeException("Số lượng phải lớn hơn 0");
//         }

//         Cart cart = getCart(user);
//         Product product = productRepository.findById(productId)
//                 .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

//         CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
//                 .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

//         // Kiểm tra tồn kho
//         if (product.getStock() < quantity) {
//             throw new RuntimeException("Số lượng vượt quá tồn kho. Tồn kho hiện tại: " + product.getStock());
//         }

//         item.setQuantity(quantity);
//         cartItemRepository.save(item);
        
//         return cart;
//     }

//     /**
//      * Xóa sản phẩm khỏi giỏ hàng
//      */
//     @Transactional
//     public Cart removeFromCart(User user, Long productId) {
//         Cart cart = getCart(user);
//         Product product = productRepository.findById(productId)
//                 .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

//         CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
//                 .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

//         cartItemRepository.delete(item);
        
//         return cart;
//     }

//     /**
//      * Xóa toàn bộ giỏ hàng
//      */
//     @Transactional
//     public void clearCart(User user) {
//         Cart cart = getCart(user);
//         cartItemRepository.deleteByCartId(cart.getId());
//     }

//     /**
//      * Lấy tổng số sản phẩm trong giỏ hàng
//      */
//     public int getCartItemCount(User user) {
//         Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
//         if (cart == null) {
//             return 0;
//         }
//         return cartItemRepository.findByCartId(cart.getId())
//                 .stream()
//                 .mapToInt(CartItem::getQuantity)
//                 .sum();
//     }
// }