package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.defective.DefectiveStockResponse;
import com.starservice.inventory.inventory_app.dto.defective.DefectiveTransferItemDTO;
import com.starservice.inventory.inventory_app.dto.defective.DefectiveTransferToCompanyRequest;
import com.starservice.inventory.inventory_app.entity.DefectiveStock;
import com.starservice.inventory.inventory_app.entity.DefectiveTransferCompanyHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.DefectiveStockRepository;
import com.starservice.inventory.inventory_app.repository.DefectiveTransferCompanyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefectiveTransferService {

    @Autowired
    private DefectiveStockRepository defectiveStockRepository;

    @Autowired
    private DefectiveTransferCompanyHistoryRepository defectiveTransferCompanyHistoryRepository;

    @Transactional
    public String transferDefectiveToCompany(DefectiveTransferToCompanyRequest request) {
        validateRequest(request);
        Company company = getCompanyFromToken();

        for (DefectiveTransferItemDTO item : request.getItems()) {
            int quantity = parseQuantity(item.getQuantity());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for item: " + item.getItemCode());
            }

            DefectiveStock defectiveStock = defectiveStockRepository
                    .findByItemCodeAndDefaultCompany(item.getItemCode(), company)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Item not found in defective stock: " + item.getItemCode()));

            if (defectiveStock.getQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient defective stock for item: " + item.getItemCode());
            }

            defectiveStock.setQuantity(defectiveStock.getQuantity() - quantity);
            if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
                defectiveStock.setItemDesc(item.getItemDesc());
            }
            defectiveStockRepository.save(defectiveStock);

            saveTransferHistory(item, quantity, company);
        }

        return "Defective stock transferred to company successfully";
    }

    public PageResponse<DefectiveStockResponse> getDefectiveStocks(int pageNo, int pageSize, String searchKey) {
        Company company = getCompanyFromToken();

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("itemCode").ascending());
        Page<DefectiveStock> defectiveStockPage;

        if (searchKey == null || searchKey.isBlank()) {
            defectiveStockPage = defectiveStockRepository
                    .findByDefaultCompanyAndQuantityGreaterThan(company, 0, pageable);
        } else {
            defectiveStockPage = defectiveStockRepository
                    .searchDefectiveStocks(company, searchKey.trim(), pageable);
        }

        List<DefectiveStockResponse> defectiveStocks = defectiveStockPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<DefectiveStockResponse>builder()
                .totalPages(defectiveStockPage.getTotalPages())
                .totalRecords(defectiveStockPage.getTotalElements())
                .response(defectiveStocks)
                .build();
    }

    private DefectiveStockResponse mapToResponse(DefectiveStock defectiveStock) {
        return DefectiveStockResponse.builder()
                .uuid(defectiveStock.getUuid())
                .itemCode(defectiveStock.getItemCode())
                .itemDesc(defectiveStock.getItemDesc())
                .quantity(defectiveStock.getQuantity())
                .build();
    }

    private void saveTransferHistory(DefectiveTransferItemDTO item, int quantity, Company company) {
        DefectiveTransferCompanyHistory history = DefectiveTransferCompanyHistory.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(item.getItemCode())
                .itemDesc(item.getItemDesc())
                .quantity(quantity)
                .transferDate(Instant.now())
                .defaultCompany(company)
                .build();

        defectiveTransferCompanyHistoryRepository.save(history);
    }

    private void validateRequest(DefectiveTransferToCompanyRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Item list cannot be empty");
        }

        for (DefectiveTransferItemDTO item : request.getItems()) {
            if (item.getItemCode() == null || item.getItemCode().isBlank()) {
                throw new IllegalArgumentException("Item code is required");
            }
        }
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

    private Company getCompanyFromToken() {
        return (Company) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}
