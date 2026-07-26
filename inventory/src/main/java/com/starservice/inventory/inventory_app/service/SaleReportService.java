package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.entity.Sale;
import com.starservice.inventory.inventory_app.entity.SaleItem;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.SaleItemRepository;
import com.starservice.inventory.inventory_app.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleReportService {

    private static final int BATCH_SIZE = 100;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    @Transactional(readOnly = true)
    public byte[] generateSaleReportExcel(Instant fromDate, Instant toDate) throws IOException {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sale Report");
            createTitleRow(sheet, "Sale Report", reportDate);
            String[] headers = {
                    "SL.NO.", "DATE", "ENGINEER'S NAME", "ITEM CODE", "ITEM DESCRIPTION",
                    "QUANTITY", "RATE", "AMOUNT", "WORK ORDER NO.", "INVOICE NO.", "INVOICE DATE"
            };
            createHeaderRow(sheet, headers);

            int rowIndex = 2;
            int slNo = 1;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("createdDate").ascending());

            while (true) {
                Page<Sale> salePage = saleRepository.findByDefaultCompanyAndCreatedDateBetweenOrderByCreatedDateAsc(
                        company, fromDate, toDate, pageable);

                if (salePage.isEmpty()) {
                    break;
                }

                List<String> saleIds = salePage.getContent().stream()
                        .map(Sale::getUuid)
                        .collect(Collectors.toList());

                Map<String, List<SaleItem>> saleItemsMap = saleItemRepository.findBySaleIdIn(saleIds)
                        .stream()
                        .collect(Collectors.groupingBy(SaleItem::getSaleId));

                for (Sale sale : salePage.getContent()) {
                    List<SaleItem> saleItems = saleItemsMap.getOrDefault(sale.getUuid(), new ArrayList<>());
                    for (SaleItem item : saleItems) {
                        Row row = sheet.createRow(rowIndex++);
                        writeCell(row, 0, slNo++);
                        writeCell(row, 1, item.getCreatedDate() != null ? item.getCreatedDate().atZone(IST).format(REPORT_DATE_FORMAT) : "");
                        writeCell(row, 2, sale.getEmpName());
                        writeCell(row, 3, item.getItemCode());
                        writeCell(row, 4, item.getItemDesc());
                        writeCell(row, 5, item.getQuantity());
                        writeCell(row, 6, item.getRate());
                        writeCell(row, 7, item.getTotal()); // Assuming 'total' in SaleItem corresponds to 'AMOUNT'
                        writeCell(row, 8, sale.getWorkOrderNo());
                        writeCell(row, 9, sale.getInvoiceNo());
                        writeCell(row, 10, sale.getInvoiceDate());
                    }
                }

                if (!salePage.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("createdDate").ascending());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate sale report: " + e.getMessage(), e);
        }
    }

    public String getSaleReportFileName(Instant fromDate, Instant toDate) {
        return String.format(
                "sales_report_%s_to_%s.xlsx",
                fromDate.atZone(IST).format(FILE_DATE_FORMAT),
                toDate.atZone(IST).format(FILE_DATE_FORMAT)
        );
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


    private Company getCompanyFromToken() {
        return (Company) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}
