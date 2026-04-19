package com.starservice.inventory.inventory.service;

import com.starservice.inventory.inventory.dto.purchase.PurchaseRequestDTO;
import com.starservice.inventory.inventory.dto.purchase.PurchaseResponseDTO;
import com.starservice.inventory.inventory.entity.Purchase;
import com.starservice.inventory.inventory.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Override
    public PurchaseResponseDTO save(PurchaseRequestDTO request) {

        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }

        if (request.getItemCode() == null || request.getItemCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Item code is required");
        }

        if (request.getQuantity() == null || request.getQuantity().trim().isEmpty()) {
            throw new IllegalArgumentException("Quantity is required");
        }

        if (request.getTotalPrice() == null || request.getTotalPrice().trim().isEmpty()) {
            throw new IllegalArgumentException("Total price is required");
        }

        try {
            Double.parseDouble(request.getQuantity());
        } catch (Exception e) {
            throw new IllegalArgumentException("Quantity must be a valid number");
        }

        try {
            Double.parseDouble(request.getTotalPrice());
        } catch (Exception e) {
            throw new IllegalArgumentException("Total price must be a valid number");
        }

        // Map DTO → Entity
        Purchase purchase = new Purchase();

        purchase.setUuid(UUID.randomUUID().toString());
        purchase.setCompanyName(request.getCompanyName());
        purchase.setItemCode(request.getItemCode());
        purchase.setItemDesc(request.getItemDesc());
        purchase.setGstNo(request.getGstNo());
        purchase.setInvoiceNo(request.getInvoiceNo());
        purchase.setHsnCode(request.getHsnCode());
        purchase.setRateDp(request.getRateDp());
        purchase.setQuantity(request.getQuantity());
        purchase.setGstPercentage(request.getGstPercentage());
        purchase.setGstValue(request.getGstValue());
        purchase.setTotalDp(request.getTotalDp());
        purchase.setTotalPrice(request.getTotalPrice());

        // Invoice date as String (as per your design)
        purchase.setInvoiceDate(request.getInvoiceDate());

        // System timestamps
        purchase.setCreatedDate(Instant.now());
        purchase.setUpdatedDate(Instant.now());

        // Save
        Purchase saved = purchaseRepository.save(purchase);

        // Map Entity → Response DTO
        return PurchaseResponseDTO.builder()
                .uuid(saved.getUuid())
                .companyName(saved.getCompanyName())
                .itemCode(saved.getItemCode())
                .itemDesc(saved.getItemDesc())
                .gstNo(saved.getGstNo())
                .invoiceNo(saved.getInvoiceNo())
                .hsnCode(saved.getHsnCode())
                .rateDp(saved.getRateDp())
                .quantity(saved.getQuantity())
                .gstPercentage(saved.getGstPercentage())
                .gstValue(saved.getGstValue())
                .totalDp(saved.getTotalDp())
                .totalPrice(saved.getTotalPrice())
                .invoiceDate(saved.getInvoiceDate())
                .createdDate(String.valueOf(saved.getCreatedDate()))
                .updatedDate(String.valueOf(saved.getUpdatedDate()))
                .build();
    }

}
