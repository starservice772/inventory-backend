package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.purchase.PurchaseRequestDTO;
import com.starservice.inventory.inventory_app.dto.purchase.PurchaseResponseDTO;

public interface PurchaseService {
    String save(PurchaseRequestDTO request);
}
