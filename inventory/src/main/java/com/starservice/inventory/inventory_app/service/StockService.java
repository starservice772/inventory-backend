package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.stock.ItemStockLocationResponse;
import com.starservice.inventory.inventory_app.dto.stock.ItemStockResponse;
import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.entity.Item;
import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.EmployeeStockRepository;
import com.starservice.inventory.inventory_app.repository.ItemRepository;
import com.starservice.inventory.inventory_app.repository.OfficeStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private static final String OFFICE_LOCATION = "Office";
    private static final String ENGINEER_LOCATION = "Engineer";
    private static final String OFFICE_ENGINEER_NAME = "-";

    @Autowired
    private OfficeStockRepository officeStockRepository;

    @Autowired
    private EmployeeStockRepository employeeStockRepository;

    @Autowired
    private ItemRepository itemRepository;

    public ItemStockResponse getItemStockByItemCode(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            throw new IllegalArgumentException("Item code is required");
        }

        String trimmedItemCode = itemCode.trim();
        Company company = getCompanyFromToken();

        Optional<OfficeStock> officeStock = officeStockRepository
                .findByItemCodeAndDefaultCompany(trimmedItemCode, company);

        List<EmployeeStock> employeeStocks = employeeStockRepository
                .findByItemCodeAndDefaultCompany(trimmedItemCode, company);

        List<ItemStockLocationResponse> locations = new ArrayList<>();

        int officeQuantity = officeStock.map(OfficeStock::getQuantity).orElse(0);
        locations.add(ItemStockLocationResponse.builder()
                .location(OFFICE_LOCATION)
                .quantity(officeQuantity)
                .engineerName(OFFICE_ENGINEER_NAME)
                .build());

        for (EmployeeStock employeeStock : employeeStocks) {
            if (employeeStock.getQuantity() == null || employeeStock.getQuantity() <= 0) {
                continue;
            }

            locations.add(ItemStockLocationResponse.builder()
                    .location(ENGINEER_LOCATION)
                    .quantity(employeeStock.getQuantity())
                    .engineerName(employeeStock.getEmployeeName())
                    .build());
        }

        return ItemStockResponse.builder()
                .itemCode(trimmedItemCode)
                .itemDescription(resolveItemDescription(trimmedItemCode, company, officeStock, employeeStocks))
                .locations(locations)
                .build();
    }

    private String resolveItemDescription(
            String itemCode,
            Company company,
            Optional<OfficeStock> officeStock,
            List<EmployeeStock> employeeStocks) {

        Optional<Item> item = itemRepository.findByItemCodeAndCompanyAndDelFlFalse(itemCode, company);
        if (item.isPresent() && item.get().getItemDescription() != null && !item.get().getItemDescription().isBlank()) {
            return item.get().getItemDescription();
        }

        if (officeStock.isPresent() && officeStock.get().getItemDesc() != null && !officeStock.get().getItemDesc().isBlank()) {
            return officeStock.get().getItemDesc();
        }

        return employeeStocks.stream()
                .map(EmployeeStock::getItemDesc)
                .filter(desc -> desc != null && !desc.isBlank())
                .findFirst()
                .orElse(null);
    }

    private Company getCompanyFromToken() {
        return (Company) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}
