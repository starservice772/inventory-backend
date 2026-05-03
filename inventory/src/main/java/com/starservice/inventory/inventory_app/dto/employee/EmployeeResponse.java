package com.starservice.inventory.inventory_app.dto.employee;

import com.starservice.inventory.inventory_app.enums.Company;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {

    private String id;
    private String name;
    private String phone;
    private String employeeCode;
    private String gender;
    private Company company;
    private String status;
    private String role;
}
