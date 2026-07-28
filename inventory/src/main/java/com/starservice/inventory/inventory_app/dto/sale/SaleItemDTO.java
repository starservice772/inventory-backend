package com.starservice.inventory.inventory_app.dto.sale;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SaleItemDTO {

    private String itemCode;
    private String itemDesc;
    private String hsnCode;
    private String rate;

    @JsonAlias("quantiy")
    private String quantity;

//    private String gst;
    private String total;
    private String totalPrice;
}
