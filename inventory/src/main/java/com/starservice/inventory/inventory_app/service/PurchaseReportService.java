package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.PurchaseReportDTO;
import com.starservice.inventory.inventory_app.entity.Purchase;
import com.starservice.inventory.inventory_app.entity.PurchaseItem;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.PurchaseItemRepository;
import com.starservice.inventory.inventory_app.repository.PurchaseRepository;
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
public class PurchaseReportService {

    private static final int BATCH_SIZE = 100;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    @Transactional(readOnly = true)
    public byte[] generatePurchaseReportExcel(Instant fromDate, Instant toDate) throws IOException {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();
        List<PurchaseReportDTO> reportData = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Purchase Report");
            createTitleRow(sheet, "Purchase Report", reportDate);
            createHeaderRow(sheet, new String[]{
                    "SL.NO.", "DATE", "ITEM CODE", "ITEM DESCRIPTION", "HSN", "QUANTITY",
                    "RATE DP", "GST", "TOTAL DP", "TOTAL PRICE", "TYPE", "INVOICE NO.",
                    "INVOICE DATE", "GST NO."
            });

            int rowIndex = 2;
            int slNo = 1;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("createdDate").ascending());

            while (true) {
                Page<Purchase> purchasePage = purchaseRepository.findByDefaultCompanyAndCreatedDateBetweenOrderByCreatedDateAsc(
                        company, fromDate, toDate, pageable);

                if (purchasePage.isEmpty()) {
                    break;
                }

                List<String> purchaseIds = purchasePage.getContent().stream()
                        .map(Purchase::getUuid)
                        .collect(Collectors.toList());

                // Fetch all purchase items for the current batch of purchases
                Map<String, List<PurchaseItem>> purchaseItemsMap = purchaseItemRepository.findByPurchaseIdIn(purchaseIds)
                        .stream()
                        .collect(Collectors.groupingBy(PurchaseItem::getPurchaseId));

                for (Purchase purchase : purchasePage.getContent()) {
                    List<PurchaseItem> purchaseItems = purchaseItemsMap.getOrDefault(purchase.getUuid(), new ArrayList<>());
                    for (PurchaseItem item : purchaseItems) {
                        Row row = sheet.createRow(rowIndex++);
                        writeCell(row, 0, slNo++);
                        writeCell(row, 1, item.getCreatedDate() != null ? item.getCreatedDate().atZone(IST).format(REPORT_DATE_TIME_FORMAT) : "");
                        writeCell(row, 2, item.getItemCode());
                        writeCell(row, 3, item.getItemDesc());
                        writeCell(row, 4, item.getHsnCode());
                        writeCell(row, 5, item.getQuantity());
                        writeCell(row, 6, item.getRateDp());
                        writeCell(row, 7, item.getGstValue());
                        writeCell(row, 8, item.getTotalDp());
                        writeCell(row, 9, item.getTotalPrice());
                        writeCell(row, 10, getInvoiceType(purchase.getInvoiceType()));
                        writeCell(row, 11, purchase.getInvoiceNo());
                        writeCell(row, 12, purchase.getInvoiceDate());
                        writeCell(row, 13, purchase.getGstNo());
                    }
                }

                if (!purchasePage.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("createdDate").ascending());
            }

            autoSizeColumns(sheet, 14);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate purchase report: " + e.getMessage(), e);
        }
    }

    public String getPurchaseReportFileName(Instant fromDate, Instant toDate) {
        return String.format(
                "purchase_report_%s_to_%s.xlsx",
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

    private String getCurrentIstFileDate() {
        return Instant.now().atZone(IST).format(FILE_DATE_FORMAT);
    }

    private String getInvoiceType(String invoiceType) {
        return "I".equalsIgnoreCase(invoiceType) ? "Invoice" : "Challan";
    }

    private Company getCompanyFromToken() {
        return (Company) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}
