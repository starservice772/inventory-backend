package com.starservice.inventory.inventory_app.dto.stock;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.starservice.inventory.inventory_app.enums.StockTransferType;
import lombok.Data;

@Data
public class StockTransferItemDTO {

    private String itemCode;
    private String itemDesc;

    @JsonAlias("quantiy")
    private String quantity;

    private StockTransferType type;
}
