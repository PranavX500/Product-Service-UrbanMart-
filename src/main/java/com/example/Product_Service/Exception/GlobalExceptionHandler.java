package com.example.Product_Service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BrandNotFound.class)
    public ResponseEntity<ApiError> handleBrandNotFound(final BrandNotFound ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFound(
            final ProductNotFoundException ex
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiError> handleInvalidCategory(
            final InvalidCategoryException ex
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiError> handleImageUpload(final ImageUploadException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(final Exception ex) {
        return buildErrorResponse(
                "Something went wrong: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            final String message,
            final HttpStatus status
    ) {
        return new ResponseEntity<>(new ApiError(message, status.value()), status);
    }
}
