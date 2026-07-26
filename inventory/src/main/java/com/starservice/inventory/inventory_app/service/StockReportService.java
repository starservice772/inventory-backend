package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.entity.DefectiveStock;
import com.starservice.inventory.inventory_app.entity.EmployeeStock;
import com.starservice.inventory.inventory_app.entity.Item;
import com.starservice.inventory.inventory_app.entity.ItemEmployeeStockValue;
import com.starservice.inventory.inventory_app.entity.ItemOfficeStockValue;
import com.starservice.inventory.inventory_app.entity.OfficeStock;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.DefectiveStockRepository;
import com.starservice.inventory.inventory_app.repository.EmployeeStockRepository;
import com.starservice.inventory.inventory_app.repository.ItemEmployeeStockValueRepository;
import com.starservice.inventory.inventory_app.repository.ItemOfficeStockValueRepository;
import com.starservice.inventory.inventory_app.repository.ItemRepository;
import com.starservice.inventory.inventory_app.repository.OfficeStockRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReportService {

    private static final int BATCH_SIZE = 100;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    private OfficeStockRepository officeStockRepository;

    @Autowired
    private EmployeeStockRepository employeeStockRepository;

    @Autowired
    private DefectiveStockRepository defectiveStockRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemOfficeStockValueRepository itemOfficeStockValueRepository;

    @Autowired
    private ItemEmployeeStockValueRepository itemEmployeeStockValueRepository;

    @Transactional(readOnly = true)
    public byte[] generateOfficeStockReport() {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Office Stock");
            createTitleRow(sheet, "Office Stock Report", reportDate);
            createHeaderRow(sheet, new String[]{
                    "Sl No", "Item Code", "Item Desc", "HSN Code", "Quantity", "Stock Value"
            });

            int rowIndex = 2;
            int slNo = 1;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("itemCode").ascending());

            while (true) {
                Page<OfficeStock> page = officeStockRepository
                        .findByDefaultCompanyAndQuantityGreaterThan(company, 0, pageable);

                if (page.isEmpty()) {
                    break;
                }

                Map<String, String> hsnByItemCode = getHsnByItemCode(
                        page.getContent().stream().map(OfficeStock::getItemCode).toList(),
                        company
                );

                Map<String, String> stockValueByItemCode = getOfficeStockValueByItemCode(
                        page.getContent().stream().map(OfficeStock::getItemCode).toList(),
                        company
                );

                for (OfficeStock stock : page.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeCell(row, 0, slNo++);
                    writeCell(row, 1, stock.getItemCode());
                    writeCell(row, 2, stock.getItemDesc());
                    writeCell(row, 3, hsnByItemCode.get(stock.getItemCode()));
                    writeCell(row, 4, stock.getQuantity());
                    writeCell(row, 5, stockValueByItemCode.get(stock.getItemCode()));
                }

                if (!page.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("itemCode").ascending());
            }

            autoSizeColumns(sheet, 6);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate office stock report: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateEmployeeStockReport() {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employee Stock");
            createTitleRow(sheet, "Employee Stock Report", reportDate);
            createHeaderRow(sheet, new String[]{
                    "Sl No", "Employee Name", "Item Code", "Item Desc", "HSN Code", "Quantity", "Stock Value"
            });

            int rowIndex = 2;
            int slNo = 1;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(
                    pageNo,
                    BATCH_SIZE,
                    Sort.by("employeeName").ascending().and(Sort.by("itemCode").ascending())
            );

            while (true) {
                Page<EmployeeStock> page = employeeStockRepository
                        .findByDefaultCompanyAndQuantityGreaterThan(company, 0, pageable);

                if (page.isEmpty()) {
                    break;
                }

                Map<String, String> hsnByItemCode = getHsnByItemCode(
                        page.getContent().stream().map(EmployeeStock::getItemCode).toList(),
                        company
                );

                Map<String, String> stockValueByEmpAndItem = getEmployeeStockValueByEmpAndItem(
                        page.getContent().stream().map(EmployeeStock::getItemCode).toList(),
                        company
                );

                for (EmployeeStock stock : page.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeCell(row, 0, slNo++);
                    writeCell(row, 1, stock.getEmployeeName());
                    writeCell(row, 2, stock.getItemCode());
                    writeCell(row, 3, stock.getItemDesc());
                    writeCell(row, 4, hsnByItemCode.get(stock.getItemCode()));
                    writeCell(row, 5, stock.getQuantity());
                    writeCell(row, 6, stockValueByEmpAndItem.get(
                            employeeStockValueKey(stock.getEmployeeId(), stock.getItemCode())));
                }

                if (!page.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(
                        pageNo,
                        BATCH_SIZE,
                        Sort.by("employeeName").ascending().and(Sort.by("itemCode").ascending())
                );
            }

            autoSizeColumns(sheet, 7);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate employee stock report: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateDefectiveStockReport() {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Defective Stock");
            createTitleRow(sheet, "Defective Stock Report", reportDate);
            createHeaderRow(sheet, new String[]{
                    "Sl No", "Item Code", "Item Desc", "HSN Code", "Quantity"
            });

            int rowIndex = 2;
            int slNo = 1;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("itemCode").ascending());

            while (true) {
                Page<DefectiveStock> page = defectiveStockRepository
                        .findByDefaultCompanyAndQuantityGreaterThan(company, 0, pageable);

                if (page.isEmpty()) {
                    break;
                }

                Map<String, String> hsnByItemCode = getHsnByItemCode(
                        page.getContent().stream().map(DefectiveStock::getItemCode).toList(),
                        company
                );

                for (DefectiveStock stock : page.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeCell(row, 0, slNo++);
                    writeCell(row, 1, stock.getItemCode());
                    writeCell(row, 2, stock.getItemDesc());
                    writeCell(row, 3, hsnByItemCode.get(stock.getItemCode()));
                    writeCell(row, 4, stock.getQuantity());
                }

                if (!page.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("itemCode").ascending());
            }

            autoSizeColumns(sheet, 5);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate defective stock report: " + e.getMessage(), e);
        }
    }

    public String getOfficeStockReportFileName() {
        return "office_stock_report_" + getCurrentIstFileDate() + ".xlsx";
    }

    public String getEmployeeStockReportFileName() {
        return "employee_stock_report_" + getCurrentIstFileDate() + ".xlsx";
    }

    public String getDefectiveStockReportFileName() {
        return "defective_stock_report_" + getCurrentIstFileDate() + ".xlsx";
    }

    private Map<String, String> getHsnByItemCode(List<String> itemCodes, Company company) {
        if (itemCodes == null || itemCodes.isEmpty()) {
            return Map.of();
        }

        List<String> distinctItemCodes = itemCodes.stream().distinct().toList();
        List<Item> items = itemRepository.findByItemCodeInAndCompanyAndDelFlFalse(distinctItemCodes, company);

        return items.stream()
                .collect(Collectors.toMap(
                        Item::getItemCode,
                        item -> item.getHsnCode() != null ? item.getHsnCode() : "",
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, String> getOfficeStockValueByItemCode(List<String> itemCodes, Company company) {
        if (itemCodes == null || itemCodes.isEmpty()) {
            return Map.of();
        }

        List<String> distinctItemCodes = itemCodes.stream().distinct().toList();
        List<ItemOfficeStockValue> stockValues =
                itemOfficeStockValueRepository.findByItemCodeInAndDefaultCompany(distinctItemCodes, company);

        return stockValues.stream()
                .collect(Collectors.toMap(
                        ItemOfficeStockValue::getItemCode,
                        stockValue -> stockValue.getValue() != null ? stockValue.getValue() : "",
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, String> getEmployeeStockValueByEmpAndItem(List<String> itemCodes, Company company) {
        if (itemCodes == null || itemCodes.isEmpty()) {
            return Map.of();
        }

        List<String> distinctItemCodes = itemCodes.stream().distinct().toList();
        List<ItemEmployeeStockValue> stockValues =
                itemEmployeeStockValueRepository.findByItemCodeInAndDefaultCompany(distinctItemCodes, company);

        return stockValues.stream()
                .collect(Collectors.toMap(
                        stockValue -> employeeStockValueKey(stockValue.getEmpId(), stockValue.getItemCode()),
                        stockValue -> stockValue.getValue() != null ? stockValue.getValue() : "",
                        (existing, replacement) -> existing
                ));
    }

    private String employeeStockValueKey(String empId, String itemCode) {
        return empId + "|" + itemCode;
    }

    private void createTitleRow(Sheet sheet, String title, String reportDate) {
        Row titleRow = sheet.createRow(0);
        writeCell(titleRow, 0, title + " | Date: " + reportDate);
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            writeCell(headerRow, i, headers[i]);
        }
    }

    private void writeCell(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String getCurrentIstDateTime() {
        return Instant.now().atZone(IST).format(REPORT_DATE_TIME_FORMAT) + " IST";
    }

    private String getCurrentIstFileDate() {
        return Instant.now().atZone(IST).format(FILE_DATE_FORMAT);
    }

    private Company getCompanyFromToken() {
        return (Company) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}
