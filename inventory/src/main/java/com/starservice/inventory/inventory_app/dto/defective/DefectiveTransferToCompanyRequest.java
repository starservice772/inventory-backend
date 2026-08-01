package com.starservice.inventory.inventory_app.dto.defective;

import lombok.Data;

import java.util.List;

@Data
public class DefectiveTransferToCompanyRequest {
    private String date;

    private List<DefectiveTransferItemDTO> items;
}
