package com.smartcommerce.exception;

public class CannotDeleteDefaultAddressException extends RuntimeException {
    public CannotDeleteDefaultAddressException() {
        super("No puedes eliminar la dirección predeterminada. Asigna otra dirección como predeterminada primero.");
    }
}
