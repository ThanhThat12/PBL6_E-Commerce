package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.AddCartItemDTO;
import com.PBL6.Ecommerce.domain.dto.CartDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateCartItemDTO;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User resolveCurrentUser(Authentication authentication) {
        String principal = authentication.getName();
        Optional<User> userOpt = Optional.empty();
        if (principal != null && principal.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(principal);
                userOpt = userRepository.findById(id);
            } catch (NumberFormatException ignored) {}
        }
        if (userOpt.isEmpty()) userOpt = userRepository.findOneByUsername(principal);
        if (userOpt.isEmpty()) userOpt = userRepository.findOneByEmail(principal);
        return userOpt.orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<CartDTO>> getCart(Authentication authentication) {
        User user = resolveCurrentUser(authentication);
        CartDTO dto = cartService.getCartDtoForUser(user);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Cart retrieved", dto));
    }

    @PostMapping("/items")
    public ResponseEntity<ResponseDTO<CartDTO>> addItem(Authentication authentication, @RequestBody AddCartItemDTO body) {
        User user = resolveCurrentUser(authentication);
        CartDTO dto = cartService.addItem(user, body.getProductId(), body.getQuantity());
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Item added", dto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ResponseDTO<CartDTO>> updateItem(Authentication authentication, @PathVariable Long itemId, @RequestBody UpdateCartItemDTO body) {
        User user = resolveCurrentUser(authentication);
        CartDTO dto = cartService.updateItem(user, itemId, body.getQuantity());
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Item updated", dto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ResponseDTO<CartDTO>> removeItem(Authentication authentication, @PathVariable Long itemId) {
        User user = resolveCurrentUser(authentication);
        CartDTO dto = cartService.removeItem(user, itemId);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Item removed", dto));
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO<CartDTO>> clearCart(Authentication authentication) {
        User user = resolveCurrentUser(authentication);
        CartDTO dto = cartService.clearCart(user);
        return ResponseEntity.ok(new ResponseDTO<>(200, null, "Cart cleared", dto));
    }
}