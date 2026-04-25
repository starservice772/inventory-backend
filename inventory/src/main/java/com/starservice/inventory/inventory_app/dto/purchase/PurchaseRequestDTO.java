package com.starservice.inventory.inventory_app.dto.purchase;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequestDTO {

    // Header fields
    private String companyName;
    private String gstNo;
    private String invoiceNo;
    private String invoiceDate;
    private String invoiceType;
    private String gstPercentage;

    // Item list
    private List<PurchaseItemDTO> items;
}
