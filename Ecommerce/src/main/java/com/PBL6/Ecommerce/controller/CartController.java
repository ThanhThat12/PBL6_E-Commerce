package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.domain.CartItem;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddToCartRequest;
import com.PBL6.Ecommerce.domain.dto.CartItemResponseDTO;
import com.PBL6.Ecommerce.domain.dto.CartResponseDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateCartQuantityRequest;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.CartService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/cart - Thêm product variant vào giỏ hàng
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<String>> addToCart(@RequestBody AddToCartRequest request) {
        try {
            User user = getCurrentUser();
            cartService.addToCart(user, request.getProductVariantId(), request.getQuantity());
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Đã thêm vào giỏ hàng", "Sản phẩm đã được thêm vào giỏ hàng thành công")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * GET /api/cart - Xem giỏ hàng hiện tại
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<CartResponseDTO>> getCart() {
        try {
            User user = getCurrentUser();
            Cart cart = cartService.getCart(user);
            
            CartResponseDTO response = convertToCartResponseDTO(cart);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy giỏ hàng thành công", response)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * PUT /api/cart/{productVariantId} - Cập nhật số lượng
     */
    @PutMapping("/{productVariantId}")
    public ResponseEntity<ResponseDTO<String>> updateQuantity(
            @PathVariable Long productVariantId,
            @RequestBody UpdateCartQuantityRequest request) {
        try {
            User user = getCurrentUser();
            cartService.updateQuantity(user, productVariantId, request.getQuantity());
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Cập nhật thành công", "Số lượng sản phẩm đã được cập nhật")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * DELETE /api/cart/{productVariantId} - Xóa khỏi giỏ
     */
    @DeleteMapping("/{productVariantId}")
    public ResponseEntity<ResponseDTO<String>> removeFromCart(@PathVariable Long productVariantId) {
        try {
            User user = getCurrentUser();
            cartService.removeFromCart(user, productVariantId);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Xóa thành công", "Sản phẩm đã được xóa khỏi giỏ hàng")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * DELETE /api/cart/clear - Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ResponseDTO<String>> clearCart() {
        try {
            User user = getCurrentUser();
            cartService.clearCart(user);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Xóa thành công", "Giỏ hàng đã được làm trống")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    /**
     * GET /api/cart/count - Lấy tổng số items
     */
    @GetMapping("/count")
    public ResponseEntity<ResponseDTO<Integer>> getCartItemCount() {
        try {
            User user = getCurrentUser();
            int count = cartService.getCartItemCount(user);
            
            return ResponseEntity.ok(
                new ResponseDTO<>(200, null, "Lấy số lượng thành công", count)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO<>(400, e.getMessage(), "Thất bại", null)
            );
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String username = authentication.getName();
        return userRepository.findOneByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    private CartResponseDTO convertToCartResponseDTO(Cart cart) {
        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());

        List<CartItemResponseDTO> itemDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                CartItemResponseDTO itemDTO = new CartItemResponseDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductVariantId(item.getProductVariant().getId());
                itemDTO.setProductName(item.getProductVariant().getProduct().getName());
                itemDTO.setProductSku(item.getProductVariant().getSku());
                itemDTO.setProductImage(item.getProductVariant().getProduct().getMainImage());
                itemDTO.setPrice(item.getProductVariant().getPrice());
                itemDTO.setStock(item.getProductVariant().getStock());
                itemDTO.setQuantity(item.getQuantity());

                BigDecimal subtotal = item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                itemDTO.setSubtotal(subtotal);

                itemDTOs.add(itemDTO);
                totalAmount = totalAmount.add(subtotal);
                totalItems += item.getQuantity();
            }
        }

        response.setItems(itemDTOs);
        response.setTotalItems(totalItems);
        response.setTotalAmount(totalAmount);

        return response;
    }
}

