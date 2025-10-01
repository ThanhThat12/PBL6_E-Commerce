package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.dto.CartDTO;
import com.PBL6.Ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Lấy giỏ hàng của user hiện tại (dựa vào authentication)
    @GetMapping("")
    public ResponseEntity<CartDTO> getCartForCurrentUser(java.security.Principal principal) {
        // principal.getName() trả về username, cần ánh xạ sang userId nếu cần
        CartDTO cart = cartService.getCartByUsername(principal.getName());
        return ResponseEntity.ok(cart);
    }

    // Thêm các endpoint khác nếu cần (add item, update, remove...)
}
