package com.example.Product_Service.Exception;

public class InvalidCategoryException extends RuntimeException {
    public InvalidCategoryException(final String message) {
        super(message);
    }
}
