package com.smartcommerce.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long productId) {
        super("Ítem de carrito no encontrado para el producto con id: " + productId);
    }
}
