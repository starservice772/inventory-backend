package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.StockTransferReportDTO;
import com.starservice.inventory.inventory_app.entity.Employee;
import com.starservice.inventory.inventory_app.entity.Item;
import com.starservice.inventory.inventory_app.entity.StockTransferHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.EmployeeRepository;
import com.starservice.inventory.inventory_app.repository.ItemRepository;
import com.starservice.inventory.inventory_app.repository.StockTransferHistoryRepository;
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
public class StockTransferReportService {

    private static final int BATCH_SIZE = 100;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final StockTransferHistoryRepository stockTransferHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public byte[] generateStockTransferReportExcel(Instant fromDate, Instant toDate) throws IOException {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Stock Transfer Report");
            createTitleRow(sheet, "Stock Transfer Transaction Report", reportDate);
            String[] headers = {
                    "DATE", "ENGINEER'S NAME", "ITEM CODE", "ITEM DESCRIPTION", "TYPE", "QUANTITY"
            };
            createHeaderRow(sheet, headers);

            int rowIndex = 2;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("transferDate").ascending());

            List<StockTransferReportDTO> reportData = new ArrayList<>();

            while (true) {
                Page<StockTransferHistory> historyPage = stockTransferHistoryRepository.findByDefaultCompanyAndTransferDateBetweenOrderByTransferDateAsc(
                        company, fromDate, toDate, pageable);

                if (historyPage.isEmpty()) {
                    break;
                }

                List<String> employeeIds = historyPage.getContent().stream()
                        .map(StockTransferHistory::getEmployeeId)
                        .distinct()
                        .collect(Collectors.toList());

                List<String> itemCodes = historyPage.getContent().stream()
                        .map(StockTransferHistory::getItemCode)
                        .distinct()
                        .collect(Collectors.toList());

                Map<String, String> employeeNames = employeeRepository.findByIdInAndCompanyAndDelFlFalse(employeeIds, company)
                        .stream()
                        .collect(Collectors.toMap(Employee::getId, Employee::getName));

                Map<String, String> itemDescriptions = itemRepository.findByItemCodeInAndCompanyAndDelFlFalse(itemCodes, company)
                        .stream()
                        .collect(Collectors.toMap(Item::getItemCode, Item::getItemDescription));


                for (StockTransferHistory history : historyPage.getContent()) {
                    reportData.add(StockTransferReportDTO.builder()
                            .date(history.getTransferDate() != null ? history.getTransferDate().atZone(IST).format(REPORT_DATE_FORMAT) : "")
                            .engineerName(employeeNames.getOrDefault(history.getEmployeeId(), "N/A"))
                            .itemCode(history.getItemCode())
                            .itemDescription(itemDescriptions.getOrDefault(history.getItemCode(), "N/A"))
                            .type(history.getType().name())
                            .quantity(history.getQuantity())
                            .build());
                }

                if (!historyPage.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("transferDate").ascending());
            }

            for (StockTransferReportDTO dto : reportData) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, dto.getDate());
                writeCell(row, 1, dto.getEngineerName());
                writeCell(row, 2, dto.getItemCode());
                writeCell(row, 3, dto.getItemDescription());
                writeCell(row, 4, dto.getType());
                writeCell(row, 5, dto.getQuantity());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate stock transfer report: " + e.getMessage(), e);
        }
    }

    public String getStockTransferReportFileName(Instant fromDate, Instant toDate) {
        return String.format(
                "stock_transfer_report_%s_to_%s.xlsx",
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
