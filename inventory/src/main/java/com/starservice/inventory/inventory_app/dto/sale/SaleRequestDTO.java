package com.starservice.inventory.inventory_app.dto.sale;

import lombok.Data;

import java.util.List;

@Data
public class SaleRequestDTO {

    private String empId;
    private String empName;
    private String workOrderNo;
    private String invoiceNo;
    private String invoiceDate;
    private List<SaleItemDTO> items;
}
