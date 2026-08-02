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
    @Column(name = "uuid", length = 100, nullable = false)
    private String uuid;

    @Column(name = "item_code", length = 255, nullable = false)
    private String itemCode;

    @Column(name = "item_desc", length = 255)
    private String itemDesc;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "crtd_dt", nullable = false)
    private Instant crtdDt;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company", length = 50)
    private Company defaultCompany;

    @Column(name = "transfer_date", length = 50)
    private String transferDate;
}
