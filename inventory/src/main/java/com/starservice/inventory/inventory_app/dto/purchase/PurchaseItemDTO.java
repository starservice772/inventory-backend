package com.starservice.inventory.inventory_app.dto.purchase;

import lombok.Data;

@Data
public class PurchaseItemDTO {
    private String itemCode;
    private String itemDesc;
    private String hsnCode;
    private String rateDp;
    private String quantity;
    private String gstValue;
    private String totalDp;
    private String totalPrice;
}
