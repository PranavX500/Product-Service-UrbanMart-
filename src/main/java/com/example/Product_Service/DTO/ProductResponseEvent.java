package com.example.Product_Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseEvent {
    private List<ProductDto> products;
    private String requestId;
    private String emailId;
    private Long userId;

    public ProductResponseEvent(final List<ProductDto> productDtos) {
        this.products = productDtos;
    }
}
