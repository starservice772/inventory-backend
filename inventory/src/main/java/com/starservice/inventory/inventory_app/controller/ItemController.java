package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.item.AddItemRequest;
import com.starservice.inventory.inventory_app.dto.item.ItemResponse;
import com.starservice.inventory.inventory_app.dto.item.UpdateItemRequest;
import com.starservice.inventory.inventory_app.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/item/save")
    public ResponseEntity<ApiResponse<?>> saveItem(@RequestBody AddItemRequest request) {

        try {

            ItemResponse response = itemService.saveItem(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Item saved successfully")
                            .data(response)
                            .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PutMapping("/item/update")
    public ResponseEntity<ApiResponse<?>> updateItem(
            @RequestBody UpdateItemRequest request) {

        try {

            ItemResponse response = itemService.updateItem(request);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Item updated successfully")
                            .data(response)
                            .build());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/item/getById")
    public ResponseEntity<ApiResponse<?>> getById(@RequestParam String id) {

        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Item fetched")
                            .data(itemService.getById(id))
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/item/changeStatus")
    public ResponseEntity<ApiResponse<?>> toggleStatus(@RequestParam String id) {

        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message(itemService.toggleStatus(id))
                            .data(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/item/upload")
    public ResponseEntity<ApiResponse<?>> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message(itemService.uploadItemExcel(file))
                            .data(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/item/getAll/{pageNo}/{pageSize}")
    public ResponseEntity<PageResponse<ItemResponse>> getItems(
            @PathVariable int pageNo,
            @PathVariable int pageSize,
            @RequestParam(required = false) String search) {
        PageResponse<ItemResponse> items = itemService.getItems(pageNo, pageSize, search);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/item/search")
    public ResponseEntity<ApiResponse<?>> searchByItemCode(@RequestParam String itemCode) {

        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Items fetched")
                            .data(itemService.searchByItemCode(itemCode))
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

}