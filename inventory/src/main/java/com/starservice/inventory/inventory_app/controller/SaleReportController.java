package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.service.SaleReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/report/sale")
@RequiredArgsConstructor
public class SaleReportController {

    private final SaleReportService saleReportService;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> generateSaleReport(
            @RequestParam("fromDate") String fromDateStr,
            @RequestParam("toDate") String toDateStr) throws IOException {

        Instant fromDate;
        Instant toDate;

        try {
            fromDate = LocalDate.parse(fromDateStr)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            toDate = LocalDate.parse(toDateStr)
                    .atStartOfDay(ZoneId.systemDefault())
                    .plusDays(1) // Include the entire toDate
                    .minusNanos(1) // To get to the end of the day
                    .toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.");
        }

        byte[] excelBytes = saleReportService.generateSaleReportExcel(fromDate, toDate);

        ByteArrayResource resource = new ByteArrayResource(excelBytes);

        String filename = saleReportService.getSaleReportFileName(fromDate, toDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}
