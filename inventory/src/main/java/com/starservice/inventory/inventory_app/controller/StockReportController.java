package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.service.StockReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockReportController {

    @Autowired
    private StockReportService stockReportService;

    @GetMapping("/report/officeStock/download")
    public ResponseEntity<?> downloadOfficeStockReport() {
        return downloadReport(
                stockReportService.generateOfficeStockReport(),
                stockReportService.getOfficeStockReportFileName()
        );
    }

    @GetMapping("/report/employeeStock/download")
    public ResponseEntity<?> downloadEmployeeStockReport() {
        return downloadReport(
                stockReportService.generateEmployeeStockReport(),
                stockReportService.getEmployeeStockReportFileName()
        );
    }

    @GetMapping("/report/defectiveStock/download")
    public ResponseEntity<?> downloadDefectiveStockReport() {
        return downloadReport(
                stockReportService.generateDefectiveStockReport(),
                stockReportService.getDefectiveStockReportFileName()
        );
    }

    private ResponseEntity<?> downloadReport(byte[] reportBytes, String fileName) {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(reportBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
