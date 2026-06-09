package com.example.Product_Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListSuccessEvent {
    private List<ItemQuantityDTO> items;
    private String requestId;
    private String emailId;
    private Long userId;
}
