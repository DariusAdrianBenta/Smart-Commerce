package com.smartcommerce.wishlist.controller;

import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getMyWishlist(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.getMyWishlist(user.getId()));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<Void> addToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        wishlistService.addToWishlist(user.getId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        wishlistService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.noContent().build();
    }
}
