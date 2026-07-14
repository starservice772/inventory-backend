package com.starservice.inventory.inventory_app.dto.defective;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefectiveStockResponse {

    private String uuid;
    private String itemCode;
    private String itemDesc;
    private Integer quantity;
}
