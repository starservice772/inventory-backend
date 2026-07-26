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
public class DefectiveTransferCompanyReportDTO {
    private String date;
    private String itemCode;
    private String itemDescription;
    private Integer quantity;
}
