package com.smartcommerce.cart.controller;

import com.smartcommerce.cart.dto.request.AddToCartRequest;
import com.smartcommerce.cart.dto.request.UpdateCartItemRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.getMyCart(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(user.getId(), request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                cartService.updateItemQuantity(user.getId(), productId, request.getQuantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            Authentication authentication,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.removeItem(user.getId(), productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }
}
