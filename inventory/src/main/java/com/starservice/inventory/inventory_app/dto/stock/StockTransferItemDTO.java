package com.starservice.inventory.inventory_app.dto.stock;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class StockTransferItemDTO {

    private String itemCode;
    private String itemDesc;

    @JsonAlias("quantiy")
    private String quantity;
}
