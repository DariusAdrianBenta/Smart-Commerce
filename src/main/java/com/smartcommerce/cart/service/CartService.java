package com.smartcommerce.cart.service;

import com.smartcommerce.cart.dto.request.AddToCartRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;

public interface CartService {

    CartResponseDTO getMyCart(Long userId);

    CartResponseDTO addToCart(Long userId, AddToCartRequest request);

    CartResponseDTO updateItemQuantity(Long userId, Long productId, Integer quantity);

    CartResponseDTO removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
