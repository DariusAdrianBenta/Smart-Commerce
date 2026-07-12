package com.smartcommerce.cart.service.impl;

import com.smartcommerce.cart.dto.request.AddToCartRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.cart.mapper.CartMapper;
import com.smartcommerce.cart.repository.CartItemRepository;
import com.smartcommerce.cart.repository.CartRepository;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.exception.CartItemNotFoundException;
import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = getProductOrThrow(request.getProductId());
        validateProductIsPurchasable(product);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        int currentQuantity = (item != null) ? item.getQuantity() : 0;
        int newQuantity = currentQuantity + request.getQuantity();
        validateStock(product, newQuantity);

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }

        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        validateStock(item.getProduct(), quantity);
        item.setQuantity(quantity);

        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        // orphanRemoval = true en Cart.items: al quitarlo de la colección se elimina de la BD.
        cart.getItems().remove(item);

        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cart -> cart.getItems().clear());
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // El usuario está autenticado, por lo que basta una referencia perezosa.
                    User user = userRepository.getReferenceById(userId);
                    Cart cart = Cart.builder().user(user).build();
                    return cartRepository.save(cart);
                });
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateProductIsPurchasable(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("El producto no está disponible para la compra");
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new IllegalStateException(
                    "Stock insuficiente. Disponible: " + product.getStock());
        }
    }
}
