package com.starservice.inventory.inventory_app.exception;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handle405(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Method not allowed")
                        .data(null)
                        .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException e) {

        String message = e.getReason() != null ? e.getReason() : e.getMessage();

        return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handle500(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }
}
