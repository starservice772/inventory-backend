package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.entity.ItemEmployeeStockValue;
import com.starservice.inventory.inventory_app.entity.ItemOfficeStockValue;
import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.ItemEmployeeStockValueRepository;
import com.starservice.inventory.inventory_app.repository.ItemOfficeStockValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemOfficeStockValueService {

    private final ItemOfficeStockValueRepository repository;
    private final ItemEmployeeStockValueRepository employeeRepository;

    public void updateStockValue(List<PurchaseItem> items,
                                 Company company) {

        for (PurchaseItem item : items) {

            int purchaseQty = Integer.parseInt(item.getQuantity());

            BigDecimal purchaseTotal = new BigDecimal(item.getTotalPrice());

            repository.findByItemCodeAndDefaultCompany(
                    item.getItemCode(), company
            ).ifPresentOrElse(

                    existing -> update(existing,
                            purchaseQty,
                            purchaseTotal,
                            item),

                    () -> create(item,
                            purchaseQty,
                            purchaseTotal,
                            company)
            );
        }
    }

    public void transferToEmployee(String empId,
                                   String itemCode,
                                   int quantity,
                                   Company company,
                                   String itemDesc) {

        ItemOfficeStockValue office = repository
                .findByItemCodeAndDefaultCompany(itemCode, company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found in office stock value: " + itemCode));

        if (office.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient office stock value for item: " + itemCode);
        }

        BigDecimal unitValue = new BigDecimal(office.getValue());

        office.setQuantity(office.getQuantity() - quantity);
        repository.save(office);

        employeeRepository
                .findByItemCodeAndEmpIdAndDefaultCompany(itemCode, empId, company)
                .ifPresentOrElse(
                        existing -> addToEmployeeStock(existing, quantity, unitValue, itemDesc),
                        () -> createEmployeeStock(empId, itemCode, quantity, unitValue, company, itemDesc)
                );
    }

    public void returnFromEmployee(String empId,
                                   String itemCode,
                                   int quantity,
                                   Company company,
                                   String itemDesc) {

        ItemEmployeeStockValue employee = employeeRepository
                .findByItemCodeAndEmpIdAndDefaultCompany(itemCode, empId, company)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found in employee stock value: " + itemCode));

        if (employee.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient employee stock value for item: " + itemCode);
        }

        BigDecimal unitValue = new BigDecimal(employee.getValue());

        employee.setQuantity(employee.getQuantity() - quantity);
        employeeRepository.save(employee);

        repository
                .findByItemCodeAndDefaultCompany(itemCode, company)
                .ifPresentOrElse(
                        existing -> addToOfficeStock(existing, quantity, unitValue, itemDesc),
                        () -> createOfficeStockFromReturn(itemCode, quantity, unitValue, company, itemDesc)
                );
    }

    private void update(ItemOfficeStockValue existing,
                        int purchaseQty,
                        BigDecimal purchaseTotal,
                        PurchaseItem item) {

        int oldQty = existing.getQuantity();

        BigDecimal oldValue =
                new BigDecimal(existing.getValue());

        BigDecimal newValue =
                oldValue.multiply(BigDecimal.valueOf(oldQty))
                        .add(purchaseTotal)
                        .divide(
                                BigDecimal.valueOf(oldQty + purchaseQty),
                                2,
                                RoundingMode.HALF_UP
                        );

        existing.setQuantity(oldQty + purchaseQty);
        existing.setValue(newValue.toPlainString());
        existing.setItemDesc(item.getItemDesc());

        repository.save(existing);
    }

    private void create(PurchaseItem item,
                        int purchaseQty,
                        BigDecimal purchaseTotal,
                        Company company) {

        BigDecimal value = purchaseTotal.divide(
                BigDecimal.valueOf(purchaseQty),
                2,
                RoundingMode.HALF_UP
        );

        ItemOfficeStockValue stock =
                ItemOfficeStockValue.builder()
                        .uuid(UUID.randomUUID().toString())
                        .itemCode(item.getItemCode())
                        .itemDesc(item.getItemDesc())
                        .quantity(purchaseQty)
                        .value(value.toPlainString())
                        .defaultCompany(company)
                        .build();

        repository.save(stock);
    }

    private void addToEmployeeStock(ItemEmployeeStockValue existing,
                                    int transferQty,
                                    BigDecimal transferUnitValue,
                                    String itemDesc) {

        int oldQty = existing.getQuantity();
        BigDecimal oldValue = new BigDecimal(existing.getValue());
        BigDecimal transferTotal = transferUnitValue.multiply(BigDecimal.valueOf(transferQty));

        BigDecimal newValue = oldValue.multiply(BigDecimal.valueOf(oldQty))
                .add(transferTotal)
                .divide(
                        BigDecimal.valueOf(oldQty + transferQty),
                        2,
                        RoundingMode.HALF_UP
                );

        existing.setQuantity(oldQty + transferQty);
        existing.setValue(newValue.toPlainString());
        if (itemDesc != null && !itemDesc.isBlank()) {
            existing.setItemDesc(itemDesc);
        }

        employeeRepository.save(existing);
    }

    private void createEmployeeStock(String empId,
                                     String itemCode,
                                     int quantity,
                                     BigDecimal unitValue,
                                     Company company,
                                     String itemDesc) {

        ItemEmployeeStockValue stock = ItemEmployeeStockValue.builder()
                .uuid(UUID.randomUUID().toString())
                .empId(empId)
                .itemCode(itemCode)
                .itemDesc(itemDesc)
                .quantity(quantity)
                .value(unitValue.toPlainString())
                .defaultCompany(company)
                .build();

        employeeRepository.save(stock);
    }

    private void addToOfficeStock(ItemOfficeStockValue existing,
                                  int returnQty,
                                  BigDecimal returnUnitValue,
                                  String itemDesc) {

        int oldQty = existing.getQuantity();
        BigDecimal oldValue = new BigDecimal(existing.getValue());
        BigDecimal returnTotal = returnUnitValue.multiply(BigDecimal.valueOf(returnQty));

        BigDecimal newValue = oldValue.multiply(BigDecimal.valueOf(oldQty))
                .add(returnTotal)
                .divide(
                        BigDecimal.valueOf(oldQty + returnQty),
                        2,
                        RoundingMode.HALF_UP
                );

        existing.setQuantity(oldQty + returnQty);
        existing.setValue(newValue.toPlainString());
        if (itemDesc != null && !itemDesc.isBlank()) {
            existing.setItemDesc(itemDesc);
        }

        repository.save(existing);
    }

    private void createOfficeStockFromReturn(String itemCode,
                                             int quantity,
                                             BigDecimal unitValue,
                                             Company company,
                                             String itemDesc) {

        ItemOfficeStockValue stock = ItemOfficeStockValue.builder()
                .uuid(UUID.randomUUID().toString())
                .itemCode(itemCode)
                .itemDesc(itemDesc)
                .quantity(quantity)
                .value(unitValue.toPlainString())
                .defaultCompany(company)
                .build();

        repository.save(stock);
    }
}
