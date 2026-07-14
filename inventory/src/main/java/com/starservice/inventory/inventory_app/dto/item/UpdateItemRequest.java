package com.starservice.inventory.inventory_app.dto.item;

import lombok.Data;

@Data
public class UpdateItemRequest {

    private String id;
    private String itemCode;
    private String itemDescription;
    private String hsnCode;
}
