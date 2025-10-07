package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.dto.CartItemDTO;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.Cart;
import com.PBL6.Ecommerce.service.CartService;
import com.PBL6.Ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    @Autowired
    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartItemDTO cartItem, @AuthenticationPrincipal Jwt jwt) {
        System.out.println("addToCart called with productId=" + cartItem.getProductId() + ", quantity=" + cartItem.getQuantity());
        String username = jwt.getSubject();
        System.out.println("Username from JWT: " + username);
        User user = userRepository.findOneByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found in DB");
            return ResponseEntity.badRequest().body("User not found");
        }
        try {
            cartService.addToCart(user, cartItem.getProductId(), cartItem.getQuantity());
            return ResponseEntity.ok("Added to cart successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        User user = userRepository.findOneByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        Cart cart = cartService.getCart(user);
        System.out.println("[getCart] User: " + username + ", Cart Items: " + cart.getItems());
        // Convert CartItem entity to CartItemDTO for JSON serialization
        java.util.List<CartItemDTO> itemDTOs = new java.util.ArrayList<>();
        if (cart != null && cart.getItems() != null) {
            for (var item : cart.getItems()) {
                CartItemDTO dto = new CartItemDTO();
                dto.setId(item.getId());
                dto.setProductId(item.getProduct().getId());
                dto.setProductName(item.getProduct().getName());
                dto.setProductImage(item.getProduct().getImage());
                dto.setProductPrice(item.getProduct().getPrice());
                dto.setQuantity(item.getQuantity());
                itemDTOs.add(dto);
            }
        }
        return ResponseEntity.ok(itemDTOs);
    }
}
