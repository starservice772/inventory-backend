package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.stock.StockTransferItemDTO;
import com.starservice.inventory.inventory_app.dto.stock.StockTransferRequest;
import com.starservice.inventory.inventory_app.entity.DefectiveStock;
import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.entity.StockTransferHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.enums.StockTransferType;
import com.starservice.inventory.inventory_app.repository.DefectiveStockRepository;
import com.starservice.inventory.inventory_app.repository.EmployeeRepository;
import com.starservice.inventory.inventory_app.repository.EmployeeStockRepository;
import com.starservice.inventory.inventory_app.repository.OfficeStockRepository;
import com.starservice.inventory.inventory_app.repository.StockTransferHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    @Autowired
    private OfficeStockRepository officeStockRepository;

    @Autowired
    private EmployeeStockRepository employeeStockRepository;

    @Autowired
    private StockTransferHistoryRepository stockTransferHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DefectiveStockRepository defectiveStockRepository;

    @Transactional
    public String transferStock(StockTransferRequest request) {
        validateRequest(request);
        Company company = getCompanyFromToken();

        employeeRepository.findByIdAndCompanyAndDelFlFalse(request.getEmpId(), company)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.getEmpId()));

        for (StockTransferItemDTO item : request.getItems()) {
            int quantity = parseQuantity(item.getQuantity());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for item: " + item.getItemCode());
            }

            switch (item.getType()) {
                case ISSUE -> issueToEmployee(request, item, quantity, company);
                case RETURN -> returnToOffice(request, item, quantity, company);
                case DEFECTIVE_RETURN -> returnDefectiveToStock(request, item, quantity, company);
            }

            saveTransferHistory(request.getEmpId(), item.getType(), item.getItemCode(), quantity, company);
        }

        return "Stock transfer processed successfully";
    }

    private void issueToEmployee(
            StockTransferRequest request,
            StockTransferItemDTO item,
            int quantity,
            Company company) {

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

    private void returnDefectiveToStock(
            StockTransferRequest request,
            StockTransferItemDTO item,
            int quantity,
            Company company) {

        EmployeeStock employeeStock = employeeStockRepository
                .findByItemCodeAndEmployeeIdAndDefaultCompany(item.getItemCode(), request.getEmpId(), company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found in employee stock: " + item.getItemCode()));

        if (employeeStock.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient employee stock for item: " + item.getItemCode());
        }

        employeeStock.setQuantity(employeeStock.getQuantity() - quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            employeeStock.setItemDesc(item.getItemDesc());
        }
        employeeStockRepository.save(employeeStock);

        defectiveStockRepository
                .findByItemCodeAndDefaultCompany(item.getItemCode(), company)
                .ifPresentOrElse(
                        defectiveStock -> addToDefectiveStock(defectiveStock, item, quantity),
                        () -> createDefectiveStock(item, quantity, company, employeeStock.getItemDesc())
                );
    }

    private void returnToOffice(
            StockTransferRequest request,
            StockTransferItemDTO item,
            int quantity,
            Company company) {

        EmployeeStock employeeStock = employeeStockRepository
                .findByItemCodeAndEmployeeIdAndDefaultCompany(item.getItemCode(), request.getEmpId(), company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found in employee stock: " + item.getItemCode()));

        if (employeeStock.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient employee stock for item: " + item.getItemCode());
        }

        employeeStock.setQuantity(employeeStock.getQuantity() - quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            employeeStock.setItemDesc(item.getItemDesc());
        }
        employeeStockRepository.save(employeeStock);

        officeStockRepository
                .findByItemCodeAndDefaultCompany(item.getItemCode(), company)
                .ifPresentOrElse(
                        officeStock -> addToOfficeStock(officeStock, item, quantity),
                        () -> createOfficeStock(item, quantity, company)
                );
    }

    private void addToEmployeeStock(EmployeeStock employeeStock, StockTransferItemDTO item, int quantity) {
        employeeStock.setQuantity(employeeStock.getQuantity() + quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            employeeStock.setItemDesc(item.getItemDesc());
        }
        employeeStockRepository.save(employeeStock);
    }

    private void addToDefectiveStock(DefectiveStock defectiveStock, StockTransferItemDTO item, int quantity) {
        defectiveStock.setQuantity(defectiveStock.getQuantity() + quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            defectiveStock.setItemDesc(item.getItemDesc());
        }
        defectiveStockRepository.save(defectiveStock);
    }

    private void createDefectiveStock(
            StockTransferItemDTO item,
            int quantity,
            Company company,
            String employeeItemDesc) {

        String itemDesc = item.getItemDesc() != null && !item.getItemDesc().isBlank()
                ? item.getItemDesc()
                : employeeItemDesc;

        DefectiveStock defectiveStock = DefectiveStock.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(item.getItemCode())
                .itemDesc(itemDesc)
                .quantity(quantity)
                .defaultCompany(company)
                .build();

        defectiveStockRepository.save(defectiveStock);
    }

    private void addToOfficeStock(OfficeStock officeStock, StockTransferItemDTO item, int quantity) {
        officeStock.setQuantity(officeStock.getQuantity() + quantity);
        if (item.getItemDesc() != null && !item.getItemDesc().isBlank()) {
            officeStock.setItemDesc(item.getItemDesc());
        }
        officeStockRepository.save(officeStock);
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

    private void createOfficeStock(StockTransferItemDTO item, int quantity, Company company) {
        OfficeStock officeStock = OfficeStock.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(item.getItemCode())
                .itemDesc(item.getItemDesc())
                .quantity(quantity)
                .defaultCompany(company)
                .build();

        officeStockRepository.save(officeStock);
    }

    private void saveTransferHistory(
            String employeeId,
            StockTransferType type,
            String itemCode,
            int quantity,
            Company company) {

        StockTransferHistory history = StockTransferHistory.builder()
                .uuid(UUID.randomUUID().toString())
                .employeeId(employeeId)
                .type(type)
                .transferDate(Instant.now())
                .itemCode(itemCode)
                .quantity(quantity)
                .defaultCompany(company)
                .build();

        stockTransferHistoryRepository.save(history);
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
            if (item.getType() == null) {
                throw new IllegalArgumentException("Transfer type is required for item: " + item.getItemCode());
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
