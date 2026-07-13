package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.employee.AddEmployeeRequest;
import com.starservice.inventory.inventory_app.dto.employee.EmployeeListResponse;
import com.starservice.inventory.inventory_app.dto.employee.EmployeeResponse;
import com.starservice.inventory.inventory_app.dto.employee.UpdateEmployeeRequest;
import com.starservice.inventory.inventory_app.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee/save")
    public ResponseEntity<ApiResponse<?>> createEmployee(@RequestBody AddEmployeeRequest request) {

        try{
            EmployeeResponse response = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                            .success(true)
                            .message("Employee created successfully")
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

    @GetMapping("/employee/getById")
    public ResponseEntity<ApiResponse<?>> getById(@RequestParam String id) {

        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Employee fetched")
                    .data(employeeService.getById(id))
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

    @PutMapping("/employee/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody UpdateEmployeeRequest request) {

        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Employee updated")
                    .data(employeeService.updateEmployee(request))
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

    @PostMapping("/employee/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestParam String id) {

        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(employeeService.deleteEmployee(id))
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

    @PostMapping("/employee/changeStatus")
    public ResponseEntity<ApiResponse<?>> toggle(@RequestParam String id) {

        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(employeeService.toggleStatus(id))
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

    @GetMapping("/employee/getAll/{pageNo}/{pageSize}")
    public ResponseEntity<PageResponse<EmployeeResponse>> getEmployees(
            @PathVariable int pageNo,
            @PathVariable int pageSize,
            @RequestParam(required = false) String search) {

        PageResponse<EmployeeResponse> employees = employeeService.getEmployees(pageNo, pageSize, search);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employee/list")
    public ResponseEntity<ApiResponse<?>> getEmployeeList(
            @RequestParam(required = false) String search) {

        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Employees fetched successfully")
                    .data(employeeService.getEmployeeList(search))
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
