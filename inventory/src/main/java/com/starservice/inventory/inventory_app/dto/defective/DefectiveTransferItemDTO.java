package com.starservice.inventory.inventory_app.dto.defective;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DefectiveTransferItemDTO {

    private String itemCode;
    private String itemDesc;

    @JsonAlias("quantiy")
    private String quantity;
}
