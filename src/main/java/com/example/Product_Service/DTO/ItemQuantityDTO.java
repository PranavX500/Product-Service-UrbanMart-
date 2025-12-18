package com.example.Product_Service.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemQuantityDTO {
    private Long id;
    private Integer quantity;
}