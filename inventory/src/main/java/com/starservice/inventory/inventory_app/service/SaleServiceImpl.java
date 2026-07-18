package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.sale.SaleItemDTO;
import com.starservice.inventory.inventory_app.dto.sale.SaleRequestDTO;
import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.entity.Sale;
import com.starservice.inventory.inventory_app.entity.SaleItem;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.EmployeeRepository;
import com.starservice.inventory.inventory_app.repository.EmployeeStockRepository;
import com.starservice.inventory.inventory_app.repository.SaleItemRepository;
import com.starservice.inventory.inventory_app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private EmployeeStockRepository employeeStockRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public String save(SaleRequestDTO request) {
        validateRequest(request);

        Company company = getCompanyFromToken();

        employeeRepository.findByIdAndCompanyAndDelFlFalse(request.getEmpId(), company)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.getEmpId()));

        Map<String, Integer> quantityByItemCode = aggregateQuantitiesByItemCode(request.getItems());

        for (Map.Entry<String, Integer> entry : quantityByItemCode.entrySet()) {
            String itemCode = entry.getKey();
            int quantity = entry.getValue();

            EmployeeStock employeeStock = employeeStockRepository
                    .findByItemCodeAndEmployeeIdAndDefaultCompany(itemCode, request.getEmpId(), company)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Item not found in employee stock: " + itemCode));

            if (employeeStock.getQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient employee stock for item: " + itemCode);
            }
        }

        String saleId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Sale sale = Sale.builder()
                .uuid(saleId)
                .empId(request.getEmpId())
                .empName(request.getEmpName())
                .workOrderNo(request.getWorkOrderNo())
                .invoiceNo(request.getInvoiceNo())
                .invoiceDate(request.getInvoiceDate())
                .defaultCompany(company)
                .createdDate(now)
                .updatedDate(now)
                .build();

        saleRepository.save(sale);

        List<SaleItem> saleItems = request.getItems().stream()
                .map(item -> SaleItem.builder()
                        .uuid(UUID.randomUUID().toString())
                        .saleId(saleId)
                        .itemCode(item.getItemCode())
                        .itemDesc(item.getItemDesc())
                        .hsnCode(item.getHsnCode())
                        .rate(item.getRate())
                        .quantity(item.getQuantity())
                        .gstValue(item.getGst())
                        .total(item.getTotal())
                        .totalPrice(item.getTotalPrice())
                        .createdDate(now)
                        .updatedDate(now)
                        .build())
                .toList();

        saleItemRepository.saveAll(saleItems);

        for (Map.Entry<String, Integer> entry : quantityByItemCode.entrySet()) {
            deductEmployeeStock(request.getEmpId(), entry.getKey(), entry.getValue(), company);
        }

        return "Sale created successfully";
    }

    private Map<String, Integer> aggregateQuantitiesByItemCode(List<SaleItemDTO> items) {
        Map<String, Integer> quantityByItemCode = new HashMap<>();

        for (SaleItemDTO item : items) {
            int quantity = parseQuantity(item.getQuantity());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for item: " + item.getItemCode());
            }

            quantityByItemCode.merge(item.getItemCode(), quantity, Integer::sum);
        }

        return quantityByItemCode;
    }

    private void deductEmployeeStock(String empId, String itemCode, int quantity, Company company) {
        EmployeeStock employeeStock = employeeStockRepository
                .findByItemCodeAndEmployeeIdAndDefaultCompany(itemCode, empId, company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found in employee stock: " + itemCode));

        employeeStock.setQuantity(employeeStock.getQuantity() - quantity);
        employeeStockRepository.save(employeeStock);
    }

    private void validateRequest(SaleRequestDTO request) {
        if (request.getEmpId() == null || request.getEmpId().isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }
        if (request.getEmpName() == null || request.getEmpName().isBlank()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Item list cannot be empty");
        }

        for (SaleItemDTO item : request.getItems()) {
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
