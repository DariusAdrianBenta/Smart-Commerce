package com.smartcommerce.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id){
        super("Producto no encontrado con id: " + id);
    }
    public ProductNotFoundException(String name) {
        super("Producto no encontrado con nombre: " + name);
    }




}
