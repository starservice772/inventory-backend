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
@Table(name = "purchase_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "purchase_id")
    private String purchaseId;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_desc", columnDefinition = "TEXT")
    private String itemDesc;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "rate_dp")
    private String rateDp;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "gst_value")
    private String gstValue;

    @Column(name = "total_dp")
    private String totalDp;

    @Column(name = "total_price")
    private String totalPrice;

    @Column(name = "crtd_date")
    private Instant createdDate;

    @Column(name = "updt_date")
    private Instant updatedDate;
}
