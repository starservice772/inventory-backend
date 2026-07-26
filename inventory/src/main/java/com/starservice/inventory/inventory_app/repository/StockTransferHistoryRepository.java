package com.starservice.inventory.inventory_app.repository;

import com.starservice.inventory.inventory_app.entity.StockTransferHistory;
import com.starservice.inventory.inventory_app.enums.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface StockTransferHistoryRepository extends JpaRepository<StockTransferHistory, String> {
    Page<StockTransferHistory> findByDefaultCompanyAndTransferDateBetweenOrderByTransferDateAsc(
            Company defaultCompany, Instant startDate, Instant endDate, Pageable pageable);
}
