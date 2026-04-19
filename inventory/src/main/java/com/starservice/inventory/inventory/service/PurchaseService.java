package com.starservice.inventory.inventory.service;

import com.starservice.inventory.inventory.dto.purchase.PurchaseRequestDTO;
import com.starservice.inventory.inventory.dto.purchase.PurchaseResponseDTO;
import com.starservice.inventory.inventory.entity.Purchase;

public interface PurchaseService {
    PurchaseResponseDTO save(PurchaseRequestDTO request);
}
