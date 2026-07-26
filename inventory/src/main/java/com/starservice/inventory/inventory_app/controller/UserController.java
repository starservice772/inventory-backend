package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.users.AddUserRequest;
import com.starservice.inventory.inventory_app.dto.users.ForgotPasswordRequest;
import com.starservice.inventory.inventory_app.dto.users.UpdateUserRequest;
import com.starservice.inventory.inventory_app.dto.users.UserResponse;
import com.starservice.inventory.inventory_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users/save")
    public ResponseEntity<ApiResponse<?>> addUser(@RequestBody AddUserRequest request) {

        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                            .success(true)
                            .message("User created successfully")
                            .data(response)
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

    @GetMapping("/users/getAll/{pageNo}/{pageSize}")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @PathVariable int pageNo,
            @PathVariable int pageSize,
            @RequestParam(required = false) String search) {

        PageResponse<UserResponse> users = userService.getUsers(pageNo, pageSize, search);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/getById")
    public ResponseEntity<UserResponse> getUserById(@RequestParam String id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/changeStatus")
    public ResponseEntity<ApiResponse<?>> toggleStatus(@RequestParam String id) {
        UserResponse response = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.builder()
                        .data(response)
                        .build());
    }

    @PostMapping("/users/delete")
    public ResponseEntity<ApiResponse<?>> deleteUser(@RequestParam String id) {
        UserResponse response = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.builder()
                        .data(response)
                        .build());
    }

    @PostMapping("/users/update")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UpdateUserRequest request) {

        try {
            UserResponse response = userService.updateUser(request);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                            .success(true)
                            .message("User updated successfully")
                            .data(response)
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

    @PostMapping("/users/forgotPassword")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        try {
            userService.forgotPassword(request);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Password updated successfully")
                            .build()
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

}