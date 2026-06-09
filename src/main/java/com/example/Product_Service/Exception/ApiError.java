package com.example.Product_Service.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String message;
    private int status;
    private LocalDateTime timestamp;

    public ApiError(final String errorMessage, final int errorStatus) {
        this.message = errorMessage;
        this.status = errorStatus;
        this.timestamp = LocalDateTime.now();
    }
}
