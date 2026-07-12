package com.starservice.inventory.inventory_app.dto.stock;

import lombok.Data;

import java.util.List;

@Data
public class StockTransferRequest {

    private String empId;
    private String empName;
    private List<StockTransferItemDTO> items;
}
