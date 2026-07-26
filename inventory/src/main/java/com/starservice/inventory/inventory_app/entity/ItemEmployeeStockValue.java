package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_employee_stock_value")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEmployeeStockValue {

    @Id
    private String uuid;

    @Column(name = "emp_id", nullable = false)
    private String empId;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_desc")
    private String itemDesc;

    @Column(nullable = false)
    private Integer quantity;

    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
