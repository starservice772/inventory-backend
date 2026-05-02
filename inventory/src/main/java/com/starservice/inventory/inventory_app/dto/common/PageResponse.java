package com.starservice.inventory.inventory_app.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {

    private int totalPages;
    private long totalRecords;
    private List<T> response;
}
