package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStock {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_desc")
    private String itemDesc;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
