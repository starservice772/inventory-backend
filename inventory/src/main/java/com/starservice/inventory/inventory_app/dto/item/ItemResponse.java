package com.starservice.inventory.inventory_app.dto.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {

    private String id;

    private String itemCode;

    private String itemDescription;

    private String status;

    private String createdDate;

    private String updatedDate;
}
