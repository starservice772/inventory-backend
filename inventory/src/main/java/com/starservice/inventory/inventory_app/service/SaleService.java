package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.sale.SaleRequestDTO;

public interface SaleService {

    String save(SaleRequestDTO request);
}
