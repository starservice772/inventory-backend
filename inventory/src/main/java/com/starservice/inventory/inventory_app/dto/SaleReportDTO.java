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
public class SaleReportDTO {
    private int slNo;
    private Instant date;
    private String engineerName;
    private String itemCode;
    private String itemDescription;
    private String quantity;
    private String rate;
    private String amount;
    private String workOrderNo;
    private String invoiceNo;
    private String invoiceDate;
}
