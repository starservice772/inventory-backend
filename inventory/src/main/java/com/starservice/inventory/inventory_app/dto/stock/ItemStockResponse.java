package com.starservice.inventory.inventory_app.dto.stock;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemStockResponse {

    private String itemCode;
    private String itemDescription;
    private List<ItemStockLocationResponse> locations;
}
