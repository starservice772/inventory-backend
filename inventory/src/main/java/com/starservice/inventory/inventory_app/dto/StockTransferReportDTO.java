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
public class StockTransferReportDTO {
    private String date;
    private String engineerName;
    private String itemCode;
    private String itemDescription;
    private String type; // StockTransferType
    private Integer quantity;
}
