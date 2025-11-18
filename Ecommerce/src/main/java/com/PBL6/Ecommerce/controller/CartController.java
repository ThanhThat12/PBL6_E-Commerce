package com.PBL6.Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PBL6.Ecommerce.domain.dto.AddCartItemDTO;
import com.PBL6.Ecommerce.domain.dto.CartDTO;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.UpdateCartItemDTO;
import com.PBL6.Ecommerce.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<CartDTO>> getCart(Authentication authentication) {
        CartDTO dto = cartService.getCartDtoForUser(authentication);
        return ResponseDTO.success(dto, "Cart retrieved");
    }

    @PostMapping("/items")
    public ResponseEntity<ResponseDTO<CartDTO>> addItem(
            Authentication authentication, 
            @Valid @RequestBody AddCartItemDTO body) {
        // Validate that at least one ID is provided
        if (!body.hasValidId()) {
            return ResponseDTO.badRequest("Either productId or variantId must be provided");
        }
        
        // Use getVariantId() which has fallback logic to productId
        CartDTO dto = cartService.addItem(authentication, body.getVariantId(), body.getQuantity());
        return ResponseDTO.success(dto, "Item added");
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ResponseDTO<CartDTO>> updateItem(
            Authentication authentication, 
            @PathVariable Long itemId, 
            @Valid @RequestBody UpdateCartItemDTO body) {
        CartDTO dto = cartService.updateItem(authentication, itemId, body.getQuantity());
        return ResponseDTO.success(dto, "Item updated");
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ResponseDTO<CartDTO>> removeItem(
            Authentication authentication, 
            @PathVariable Long itemId) {
        CartDTO dto = cartService.removeItem(authentication, itemId);
        return ResponseDTO.success(dto, "Item removed");
    }

    @DeleteMapping
    public ResponseEntity<ResponseDTO<CartDTO>> clearCart(Authentication authentication) {
        CartDTO dto = cartService.clearCart(authentication);
        return ResponseDTO.success(dto, "Cart cleared");
    }
}