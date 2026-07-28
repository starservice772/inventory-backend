package com.starservice.inventory.inventory_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sale_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "sale_id", nullable = false)
    private String saleId;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_desc", columnDefinition = "TEXT")
    private String itemDesc;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "rate")
    private String rate;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "total")
    private String total;

    @Column(name = "total_price")
    private String totalPrice;

    @Column(name = "crtd_date")
    private Instant createdDate;

    @Column(name = "updt_date")
    private Instant updatedDate;
}
