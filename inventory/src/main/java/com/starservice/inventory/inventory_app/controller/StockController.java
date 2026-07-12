package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.dto.stock.StockTransferRequest;
import com.starservice.inventory.inventory_app.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockController {

    @Autowired
    private StockTransferService stockTransferService;

    @PostMapping("/stock/transfer")
    public ResponseEntity<ApiResponse<?>> transferToEmployee(@RequestBody StockTransferRequest request) {

        try {
            String message = stockTransferService.transferToEmployee(request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(message)
                    .data(null)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
