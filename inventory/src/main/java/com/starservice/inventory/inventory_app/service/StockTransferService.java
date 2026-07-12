package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.stock.StockTransferItemDTO;
import com.starservice.inventory.inventory_app.dto.stock.StockTransferRequest;
import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.EmployeeStockRepository;
import com.starservice.inventory.inventory_app.repository.OfficeStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    @Autowired
    private OfficeStockRepository officeStockRepository;

    @Autowired
    private EmployeeStockRepository employeeStockRepository;

    @Transactional
    public String transferToEmployee(StockTransferRequest request) {
        validateRequest(request);
        Company company = getCompanyFromToken();

        for (StockTransferItemDTO item : request.getItems()) {
            int quantity = parseQuantity(item.getQuantity());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for item: " + item.getItemCode());
            }

            OfficeStock officeStock = officeStockRepository
                    .findByItemCodeAndDefaultCompany(item.getItemCode(), company)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Item not found in office stock: " + item.getItemCode()));

            if (officeStock.getQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient office stock for item: " + item.getItemCode());
            }

            officeStock.setQuantity(officeStock.getQuantity() - quantity);
            officeStockRepository.save(officeStock);

            employeeStockRepository
                    .findByItemCodeAndEmployeeIdAndDefaultCompany(item.getItemCode(), request.getEmpId(), company)
                    .ifPresentOrElse(
                            employeeStock -> addToEmployeeStock(employeeStock, item, quantity),
                            () -> createEmployeeStock(request, item, quantity, company, officeStock.getItemDesc())
                    );
        }

        return "Stock transferred successfully";
    }

    private void addToEmployeeStock(EmployeeStock employeeStock, StockTransferItemDTO item, int quantity) {
        employeeStock.setQuantity(employeeStock.getQuantity() + quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            employeeStock.setItemDesc(item.getItemDesc());
        }
        employeeStockRepository.save(employeeStock);
    }

    private void createEmployeeStock(
            StockTransferRequest request,
            StockTransferItemDTO item,
            int quantity,
            Company company,
            String officeItemDesc) {

        String itemDesc = item.getItemDesc() != null && !item.getItemDesc().isBlank()
                ? item.getItemDesc()
                : officeItemDesc;

        EmployeeStock employeeStock = EmployeeStock.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(item.getItemCode())
                .itemDesc(itemDesc)
                .quantity(quantity)
                .employeeId(request.getEmpId())
                .employeeName(request.getEmpName())
                .defaultCompany(company)
                .build();

        employeeStockRepository.save(employeeStock);
    }

    private void validateRequest(StockTransferRequest request) {
        if (request.getEmpId() == null || request.getEmpId().isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }
        if (request.getEmpName() == null || request.getEmpName().isBlank()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Item list cannot be empty");
        }

        for (StockTransferItemDTO item : request.getItems()) {
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
