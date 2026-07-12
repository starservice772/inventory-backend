package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.OfficeStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficeStockService {

    @Autowired
    private OfficeStockRepository officeStockRepository;

    public void addStockFromPurchaseItems(List<PurchaseItem> items, Company defaultCompany) {
        for (PurchaseItem item : items) {
            int quantity = parseQuantity(item.getQuantity());
            officeStockRepository.findByItemCodeAndDefaultCompany(item.getItemCode(), defaultCompany)
                    .ifPresentOrElse(
                            existing -> updateExistingStock(existing, item, quantity),
                            () -> createNewStock(item, quantity, defaultCompany)
                    );
        }
    }

    private void updateExistingStock(OfficeStock existing, PurchaseItem item, int quantity) {
        existing.setQuantity(existing.getQuantity() + quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            existing.setItemDesc(item.getItemDesc());
        }
        officeStockRepository.save(existing);
    }

    private void createNewStock(PurchaseItem item, int quantity, Company defaultCompany) {
        OfficeStock stock = OfficeStock.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(item.getItemCode())
                .itemDesc(item.getItemDesc())
                .quantity(quantity)
                .defaultCompany(defaultCompany)
                .build();
        officeStockRepository.save(stock);
    }

    private int parseQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            throw new IllegalArgumentException("Item quantity cannot be empty");
        }
        try {
            return Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid item quantity: " + quantity);
        }
    }
}
