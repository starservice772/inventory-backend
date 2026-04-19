package com.starservice.inventory.inventory.dto.purchase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseResponseDTO {

    private String uuid;

    private String companyName;
    private String itemCode;
    private String itemDesc;
    private String gstNo;
    private String invoiceNo;
    private String hsnCode;
    private String rateDp;
    private String quantity;
    private String gstPercentage;
    private String gstValue;
    private String totalDp;
    private String totalPrice;
    private String invoiceDate;

    private String createdDate;
    private String updatedDate;
}
