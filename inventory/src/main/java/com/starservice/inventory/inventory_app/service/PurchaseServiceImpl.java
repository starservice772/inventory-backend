package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.purchase.PurchaseRequestDTO;
import com.starservice.inventory.inventory_app.dto.purchase.PurchaseResponseDTO;
import com.starservice.inventory.inventory_app.entity.Purchase;
import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import com.starservice.inventory.inventory_app.repository.PurchaseItemRepository;
import com.starservice.inventory.inventory_app.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private final PurchaseItemRepository purchaseItemRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String save(PurchaseRequestDTO request) {

        // Validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Item list cannot be empty");
        }

        try {
            String purchaseId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            // Save Header
            Purchase purchase = Purchase.builder()
                    .uuid(purchaseId)
                    .companyName(request.getCompanyName())
                    .gstNo(request.getGstNo())
                    .invoiceNo(request.getInvoiceNo())
                    .invoiceType(request.getInvoiceType())
                    .gstPercentage(request.getGstPercentage())
                    .invoiceDate(request.getInvoiceDate())
                    .createdDate(now)
                    .updatedDate(now)
                    .build();

            purchaseRepository.save(purchase);

            // Save Items (Batch)
            List<PurchaseItem> items = request.getItems().stream().map(item ->
                    PurchaseItem.builder()
                            .uuid(UUID.randomUUID().toString())
                            .purchaseId(purchaseId)
                            .itemCode(item.getItemCode())
                            .itemDesc(item.getItemDesc())
                            .hsnCode(item.getHsnCode())
                            .rateDp(item.getRateDp())
                            .quantity(item.getQuantity())
                            .gstValue(item.getGstValue())
                            .totalDp(item.getTotalDp())
                            .totalPrice(item.getTotalPrice())
                            .createdDate(now)
                            .updatedDate(now)
                            .build()
            ).toList();

            purchaseItemRepository.saveAll(items);

            return "Purchase created successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to create purchase: " + e.getMessage());
        }
    }
}
