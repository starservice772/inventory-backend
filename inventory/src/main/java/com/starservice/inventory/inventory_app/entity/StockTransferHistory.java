package com.starservice.inventory.inventory_app.entity;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.enums.StockTransferType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "stock_transfer_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferHistory {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "emp_id", nullable = false)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StockTransferType type;

    @Column(name = "transfer_date", nullable = false)
    private Instant transferDate;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_company")
    private Company defaultCompany;
}
