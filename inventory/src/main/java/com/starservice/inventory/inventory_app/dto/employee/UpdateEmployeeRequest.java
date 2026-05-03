package com.starservice.inventory.inventory_app.dto.employee;

import lombok.Data;

@Data
public class UpdateEmployeeRequest {
    private String id;
    private String name;
    private String phone;
    private String gender;
    private String role;
}
