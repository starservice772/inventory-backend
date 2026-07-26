package com.starservice.inventory.inventory_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReportDTO {
    private int slNo;
    private Instant date;
    private String itemCode;
    private String itemDescription;
    private String hsn;
    private String quantity;
    private String rateDp;
    private String gst;
    private String totalDp;
    private String totalPrice;
    private String type; // INVOICE/CHALLAN
    private String invoiceNo;
    private String invoiceDate;
    private String gstNo;
}
