package com.smartcommerce.wishlist.service.impl;

import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.mapper.ProductMapper;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import com.smartcommerce.wishlist.entity.WishlistItem;
import com.smartcommerce.wishlist.repository.WishlistItemRepository;
import com.smartcommerce.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getMyWishlist(Long userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(item -> productMapper.toDTO(item.getProduct()))
                .toList();
    }

    @Override
    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Idempotente: si ya está en favoritos, no se duplica.
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }

        User user = userRepository.getReferenceById(userId);
        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();
        wishlistItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
