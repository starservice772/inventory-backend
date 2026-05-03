package com.starservice.inventory.inventory_app.dto.employee;

import lombok.Data;

@Data
public class AddEmployeeRequest {
    private String name;
    private String phone;
    private String employeeCode;
    private String gender;
    private String role;
}
