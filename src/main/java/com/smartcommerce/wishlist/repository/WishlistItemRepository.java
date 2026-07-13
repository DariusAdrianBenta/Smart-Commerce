package com.smartcommerce.wishlist.repository;

import com.smartcommerce.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
