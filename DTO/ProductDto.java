package com.example.Product_Service.DTO;

import com.example.Product_Service.Model.Categories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
   private Long productId;
    private String productName;
    private Double price;
    private int quantity;
    private String description;
    private String brand;
    private Categories categories;

}
