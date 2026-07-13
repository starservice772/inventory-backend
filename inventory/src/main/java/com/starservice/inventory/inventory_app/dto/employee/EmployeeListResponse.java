package com.starservice.inventory.inventory_app.dto.employee;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeListResponse {

    private String empId;
    private String empName;
}
