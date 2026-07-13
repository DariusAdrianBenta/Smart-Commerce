package com.smartcommerce.wishlist.service;

import com.smartcommerce.product.dto.response.ProductResponseDTO;

import java.util.List;

public interface WishlistService {

    List<ProductResponseDTO> getMyWishlist(Long userId);

    void addToWishlist(Long userId, Long productId);

    void removeFromWishlist(Long userId, Long productId);
}
