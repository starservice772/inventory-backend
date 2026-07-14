package com.starservice.inventory.inventory_app.dto.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemSearchResponse {

    private String itemCode;

    private String itemDescription;

    private String hsnCode;
}
