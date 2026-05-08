package com.starservice.inventory.inventory_app.dto.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starservice.inventory.inventory_app.enums.Company;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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
    private String createdDate;
    private String updatedDate;
}
