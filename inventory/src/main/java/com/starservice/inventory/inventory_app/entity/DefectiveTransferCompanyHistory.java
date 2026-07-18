package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "defective_transfer_company_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectiveTransferCompanyHistory {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_desc")
    private String itemDesc;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "transfer_date", nullable = false)
    private Instant transferDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
