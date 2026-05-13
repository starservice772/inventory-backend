package com.starservice.inventory.inventory_app.service;

import com.starservice.inventory.inventory_app.dto.common.PageResponse;
import com.starservice.inventory.inventory_app.dto.item.AddItemRequest;
import com.starservice.inventory.inventory_app.dto.item.ItemResponse;
import com.starservice.inventory.inventory_app.dto.item.ItemSearchResponse;
import com.starservice.inventory.inventory_app.dto.item.UpdateItemRequest;
import com.starservice.inventory.inventory_app.entity.Employee;
import com.starservice.inventory.inventory_app.entity.Item;
import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public ItemResponse saveItem(AddItemRequest request) {

        Company company = getCompanyFromToken();

        if (request.getItemCode() == null || request.getItemCode().isBlank()) {
            throw new RuntimeException("Item code is required");
        }

        Optional<Item> existing = itemRepository.findByItemCodeAndCompanyAndDelFlFalse(request.getItemCode(), company);

        if (existing.isPresent()) {
            throw new RuntimeException("Item code already exists");
        }

        Item item = Item.builder()
                .itemCode(request.getItemCode())
                .itemDescription(request.getItemDescription())
                .company(company)
                .activeFl(true)
                .delFl(false)
                .crtdDt(Instant.now())
                .updtDt(Instant.now())
                .build();

        itemRepository.save(item);

        return mapToResponse(item);
    }

    public ItemResponse updateItem(UpdateItemRequest request) {

        Company company = getCompanyFromToken();

        Item item = itemRepository.findByIdAndCompanyAndDelFlFalse(request.getId(), company)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setItemDescription(request.getItemDescription());
        item.setUpdtDt(Instant.now());

        itemRepository.save(item);

        return mapToResponse(item);
    }

    private ItemResponse mapToResponse(Item item) {

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd-MM-yyyy hh:mm a")
                .withZone(ZoneId.of("Asia/Kolkata"));

        return ItemResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .status(Boolean.TRUE.equals(item.getActiveFl()) ? "ACTIVE" : "INACTIVE")
                .createdDate(item.getCrtdDt() != null ? formatter.format(item.getCrtdDt()) : null)
                .updatedDate(item.getUpdtDt() != null ? formatter.format(item.getUpdtDt()) : null)
                .build();
    }

    private Company getCompanyFromToken() {
        return (Company) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }

    public ItemResponse getById(String id) {

        Company company = getCompanyFromToken();
        Item item = itemRepository.findByIdAndCompanyAndDelFlFalse(id, company).orElseThrow(() -> new RuntimeException("Item not found"));
        return mapToResponse(item);
    }

    public String toggleStatus(String id) {

        Company company = getCompanyFromToken();

        Item item = itemRepository.findByIdAndCompanyAndDelFlFalse(id, company).orElseThrow(() -> new RuntimeException("Item not found"));

        item.setActiveFl(!Boolean.TRUE.equals(item.getActiveFl()));
        item.setUpdtDt(Instant.now());
        itemRepository.save(item);

        return item.getActiveFl() ? "Item activated" : "Item deactivated";
    }

    public String uploadItemExcel(MultipartFile file) {

        try {

            Company company = getCompanyFromToken();

            Workbook workbook = WorkbookFactory.create(file.getInputStream());

            Sheet sheet = workbook.getSheetAt(0);

            List<Item> items = new ArrayList<>();

            // Fetch all existing item codes once
            Set<String> existingCodes = new HashSet<>(itemRepository.findAllItemCodesByCompany(company));

            // Prevent duplicate inside same excel
            Set<String> excelCodes = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Cell itemCodeCell = row.getCell(0);
                Cell itemDescriptionCell = row.getCell(1);

                String itemCode = itemCodeCell != null
                        ? itemCodeCell.toString().trim()
                        : null;

                String itemDescription = itemDescriptionCell != null
                        ? itemDescriptionCell.toString().trim()
                        : null;

                if (itemCode == null || itemCode.isBlank()) {
                    continue;
                }

                // duplicate inside excel
                if (excelCodes.contains(itemCode.toUpperCase())) {
                    continue;
                }

                excelCodes.add(itemCode.toUpperCase());

                // already present in DB
                if (existingCodes.contains(itemCode)) {
                    continue;
                }

                Item item = Item.builder()
                        .itemCode(itemCode)
                        .itemDescription(itemDescription)
                        .company(company)
                        .activeFl(true)
                        .delFl(false)
                        .crtdDt(Instant.now())
                        .updtDt(Instant.now())
                        .build();

                items.add(item);

                existingCodes.add(itemCode);
            }

            itemRepository.saveAll(items);

            workbook.close();

            return items.size() + " items uploaded successfully";

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Failed to upload excel");
        }
    }

    public PageResponse<ItemResponse> getItems(int pageNo, int pageSize, String search) {
        Company company = getCompanyFromToken();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("crtdDt").descending());
        Page<Item> itemPage;
        if (search == null || search.isBlank()) {
            itemPage = itemRepository.findByCompanyAndDelFlFalse(company, pageable);
        } else {
            itemPage = itemRepository.searchItems(company, search, pageable);
        }

        List<ItemResponse> items = itemPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<ItemResponse>builder()
                .totalPages(itemPage.getTotalPages())
                .totalRecords(itemPage.getTotalElements())
                .response(items)
                .build();
    }

    public List<ItemSearchResponse> searchByItemCode(String itemCode) {

        Company company = getCompanyFromToken();

        List<Item> items = itemRepository.searchByItemCode(company, itemCode);

        return items.stream()
                .map(item -> ItemSearchResponse.builder()
                        .itemCode(item.getItemCode())
                        .itemDescription(item.getItemDescription())
                        .build())
                .toList();
    }

    public String deleteItem(String id) {

        Company company = getCompanyFromToken();
        Item item = itemRepository.findByIdAndCompanyAndDelFlFalse(id, company)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setDelFl(true);
        item.setUpdtDt(Instant.now());
        itemRepository.save(item);

        return "Item deleted successfully";
    }
}