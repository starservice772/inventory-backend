package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.entity.DefectiveTransferCompanyHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.DefectiveTransferCompanyHistoryRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectiveTransferCompanyReportService {

    private static final int BATCH_SIZE = 100;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter REPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final DefectiveTransferCompanyHistoryRepository defectiveTransferCompanyHistoryRepository;

    @Transactional(readOnly = true)
    public byte[] generateDefectiveTransferCompanyReportExcel(Instant fromDate, Instant toDate) throws IOException {
        Company company = getCompanyFromToken();
        String reportDate = getCurrentIstDateTime();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Defective Transfer to Company Report");
            createTitleRow(sheet, "Defective Stock Transfer to Company Transaction Report", reportDate);
            String[] headers = {
                    "DATE", "ITEM CODE", "ITEM DESCRIPTION", "QUANTITY"
            };
            createHeaderRow(sheet, headers);

            int rowIndex = 2;
            int pageNo = 0;
            Pageable pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("transferDate").ascending());

            while (true) {
                Page<DefectiveTransferCompanyHistory> historyPage = defectiveTransferCompanyHistoryRepository.findByDefaultCompanyAndTransferDateBetweenOrderByTransferDateAsc(
                        company, fromDate, toDate, pageable);

                if (historyPage.isEmpty()) {
                    break;
                }

                for (DefectiveTransferCompanyHistory history : historyPage.getContent()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeCell(row, 0, history.getTransferDate() != null ? history.getTransferDate().atZone(IST).format(REPORT_DATE_FORMAT) : "");
                    writeCell(row, 1, history.getItemCode());
                    writeCell(row, 2, history.getItemDesc());
                    writeCell(row, 3, history.getQuantity());
                }

                if (!historyPage.hasNext()) {
                    break;
                }

                pageNo++;
                pageable = PageRequest.of(pageNo, BATCH_SIZE, Sort.by("transferDate").ascending());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate defective stock transfer to company report: " + e.getMessage(), e);
        }
    }

    public String getDefectiveTransferCompanyReportFileName(Instant fromDate, Instant toDate) {
        return String.format(
                "defective_transfer_company_report_%s_to_%s.xlsx",
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
