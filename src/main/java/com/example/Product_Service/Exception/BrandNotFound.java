package com.example.Product_Service.Exception;

public class BrandNotFound extends RuntimeException {
    public BrandNotFound(final String message) {
        super(message);
    }
}
