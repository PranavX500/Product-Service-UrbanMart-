package com.example.Product_Service.Exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(final String message) {
        super(message);
    }
}
