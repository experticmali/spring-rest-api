package com.example.api.exception;

public class ProductNameAlreadyExistsException extends RuntimeException {
    public ProductNameAlreadyExistsException(String name) {
        super("Un produit avec le nom '" + name + "' existe déjà");
    }
}
