package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "defective_stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectiveStock {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_desc")
    private String itemDesc;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
