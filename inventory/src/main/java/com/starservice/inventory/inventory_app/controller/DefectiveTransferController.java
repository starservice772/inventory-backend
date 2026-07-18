package com.starservice.inventory.inventory_app.controller;

import com.starservice.inventory.inventory_app.dto.common.ApiResponse;
import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.defective.DefectiveStockResponse;
import com.starservice.inventory.inventory_app.dto.defective.DefectiveTransferToCompanyRequest;
import com.starservice.inventory.inventory_app.service.DefectiveTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefectiveTransferController {

    @Autowired
    private DefectiveTransferService defectiveTransferService;

    @GetMapping("/defective/getAll/{pageNo}/{pageSize}")
    public ResponseEntity<PageResponse<DefectiveStockResponse>> getDefectiveStocks(
            @PathVariable int pageNo,
            @PathVariable int pageSize,
            @RequestParam(required = false) String searchKey) {

        PageResponse<DefectiveStockResponse> defectiveStocks =
                defectiveTransferService.getDefectiveStocks(pageNo, pageSize, searchKey);

        return ResponseEntity.ok(defectiveStocks);
    }

    @PostMapping("/defective/transfer/toCompany")
    public ResponseEntity<ApiResponse<?>> transferDefectiveToCompany(
            @RequestBody DefectiveTransferToCompanyRequest request) {

        try {
            String message = defectiveTransferService.transferDefectiveToCompany(request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(message)
                    .data(null)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
