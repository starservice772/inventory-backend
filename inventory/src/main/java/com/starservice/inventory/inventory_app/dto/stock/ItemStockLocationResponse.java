package com.starservice.inventory.inventory_app.dto.stock;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemStockLocationResponse {

    private String location;
    private Integer quantity;
    private String engineerName;
}
